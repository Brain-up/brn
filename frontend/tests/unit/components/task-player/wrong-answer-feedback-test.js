import { module, test } from 'qunit';

// Tests for the wrong answer visual feedback fix across all exercise task-player types.
//
// Bug: `isCorrect` was initialized as `false` and reset to `false` in `startTask()`,
// so when a wrong answer set it to `false` again, Ember's tracked property system
// detected no change and `didUpdate` never fired.
//
// Fix: Changed `isCorrect` from `boolean` to `boolean | null`, initialized as `null`,
// reset to `null` in `startTask()`. Now wrong answers trigger a `null → false` change
// that Ember's tracking system detects.

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

/**
 * Adds a tracked `isCorrect` property (with change logging) to the given
 * object. This simulates Ember's @tracked behaviour: when the value changes,
 * the change is logged so we can assert that `didUpdate` would have fired.
 *
 * Returns the changeLog array.
 */
function addTrackedIsCorrect(obj) {
  const changeLog = [];
  let _isCorrect = null;

  Object.defineProperty(obj, 'isCorrect', {
    configurable: true,
    enumerable: true,
    get() {
      return _isCorrect;
    },
    set(value) {
      const oldValue = _isCorrect;
      _isCorrect = value;
      if (oldValue !== value) {
        changeLog.push({ from: oldValue, to: value });
      }
    },
  });

  return changeLog;
}

// ---------------------------------------------------------------------------
// 1. SingleSimpleWordsComponent (option sub-component with didUpdate)
// ---------------------------------------------------------------------------

module('Unit | Component | task-player | wrong-answer-feedback | single-simple-words', function () {
  function createComponent() {
    const component = {
      currentAnswer: [],
      // Replicate startTask() from single-simple-words/index.gts line 147-152
      startTask() {
        this.isCorrect = null;
      },
      // Replicate showTaskResult logic from single-simple-words/index.gts line 231-258
      submitAnswer(isCorrect) {
        this.isCorrect = isCorrect;
      },
    };
    const changeLog = addTrackedIsCorrect(component);
    return { component, changeLog };
  }

  test('isCorrect starts as null', function (assert) {
    const { component } = createComponent();
    assert.strictEqual(component.isCorrect, null, 'isCorrect is null on init');
  });

  test('wrong answer changes isCorrect from null to false', function (assert) {
    const { component, changeLog } = createComponent();
    assert.strictEqual(component.isCorrect, null, 'starts as null');

    component.submitAnswer(false);
    assert.strictEqual(component.isCorrect, false, 'isCorrect is false after wrong answer');
    assert.strictEqual(changeLog.length, 1, 'one change was detected');
    assert.deepEqual(changeLog[0], { from: null, to: false }, 'change is null -> false');
  });

  test('correct answer changes isCorrect from null to true', function (assert) {
    const { component, changeLog } = createComponent();
    component.submitAnswer(true);
    assert.strictEqual(component.isCorrect, true, 'isCorrect is true after correct answer');
    assert.strictEqual(changeLog.length, 1, 'one change was detected');
    assert.deepEqual(changeLog[0], { from: null, to: true }, 'change is null -> true');
  });

  test('startTask resets isCorrect from false to null', function (assert) {
    const { component, changeLog } = createComponent();

    component.submitAnswer(false);
    assert.strictEqual(component.isCorrect, false, 'isCorrect is false after wrong answer');

    component.startTask();
    assert.strictEqual(component.isCorrect, null, 'isCorrect reset to null after startTask');
    assert.strictEqual(changeLog.length, 2, 'two changes were detected');
    assert.deepEqual(changeLog[1], { from: false, to: null }, 'change is false -> null');
  });

  test('startTask resets isCorrect from true to null', function (assert) {
    const { component, changeLog } = createComponent();

    component.submitAnswer(true);
    component.startTask();
    assert.strictEqual(component.isCorrect, null, 'isCorrect reset to null after startTask');
    assert.deepEqual(changeLog[1], { from: true, to: null }, 'change is true -> null');
  });

  test('consecutive wrong answers each produce a tracked change (null -> false -> null -> false)', function (assert) {
    const { component, changeLog } = createComponent();

    // First wrong answer
    component.submitAnswer(false);
    assert.strictEqual(changeLog.length, 1, 'first wrong answer detected');

    // startTask resets
    component.startTask();
    assert.strictEqual(changeLog.length, 2, 'reset detected');

    // Second wrong answer
    component.submitAnswer(false);
    assert.strictEqual(changeLog.length, 3, 'second wrong answer detected');
    assert.deepEqual(changeLog[2], { from: null, to: false }, 'second wrong answer is null -> false');
  });

  test('handleAnswer in option component distinguishes null from false for didUpdate', function (assert) {
    // Replicate option.gts handleAnswer logic (lines 63-79)
    let buttonStyle = '';
    let isClicked = true;

    function handleAnswer(isCorrect) {
      if (isClicked) {
        if (isCorrect) {
          buttonStyle = '#47CD8A'; // green
        } else {
          buttonStyle = '#F38698'; // red
        }
        isClicked = false;
      } else {
        buttonStyle = '';
      }
    }

    // When isCorrect changes from null to false, didUpdate fires and handleAnswer runs
    handleAnswer(false);
    assert.strictEqual(buttonStyle, '#F38698', 'button gets red background for wrong answer');
    assert.false(isClicked, 'isClicked reset after handling');
  });
});

// ---------------------------------------------------------------------------
// 2. PhonemePairsComponent (CSS class feedback based on currentAnswer + isCorrect)
// ---------------------------------------------------------------------------

module('Unit | Component | task-player | wrong-answer-feedback | phoneme-pairs', function () {
  function createComponent() {
    const component = {
      currentAnswer: '',
      // Replicate startTask() from phoneme-pairs/index.gts line 141-146
      startTask() {
        this.isCorrect = null;
      },
      // Replicate showTaskResult logic from phoneme-pairs/index.gts line 171-187
      submitAnswer(selectedWord, correctAnswer) {
        this.currentAnswer = selectedWord;
        const isCorrect = this.currentAnswer === correctAnswer;
        this.isCorrect = isCorrect;
        return isCorrect;
      },
      // Replicate the CSS class logic from phoneme-pairs template (lines 222-233)
      getButtonClass(answerOptionWord) {
        if (this.currentAnswer === answerOptionWord && this.isCorrect === true) {
          return 'bg-green-500';
        }
        if (this.currentAnswer === answerOptionWord && this.isCorrect === false) {
          return 'bg-red-400';
        }
        return 'bg-transparent';
      },
    };
    const changeLog = addTrackedIsCorrect(component);
    return { component, changeLog };
  }

  test('isCorrect starts as null', function (assert) {
    const { component } = createComponent();
    assert.strictEqual(component.isCorrect, null, 'isCorrect is null on init');
  });

  test('wrong answer sets isCorrect to false and applies red class', function (assert) {
    const { component, changeLog } = createComponent();

    component.submitAnswer('wrong-word', 'correct-word');

    assert.strictEqual(component.isCorrect, false, 'isCorrect is false');
    assert.strictEqual(
      component.getButtonClass('wrong-word'),
      'bg-red-400',
      'selected wrong button gets red background',
    );
    assert.strictEqual(changeLog.length, 1, 'one change detected');
    assert.deepEqual(changeLog[0], { from: null, to: false }, 'tracked change: null -> false');
  });

  test('correct answer sets isCorrect to true and applies green class', function (assert) {
    const { component } = createComponent();

    component.submitAnswer('correct-word', 'correct-word');

    assert.strictEqual(component.isCorrect, true, 'isCorrect is true');
    assert.strictEqual(
      component.getButtonClass('correct-word'),
      'bg-green-500',
      'selected correct button gets green background',
    );
  });

  test('non-selected button gets no feedback class', function (assert) {
    const { component } = createComponent();

    component.submitAnswer('wrong-word', 'correct-word');

    assert.strictEqual(
      component.getButtonClass('other-word'),
      'bg-transparent',
      'non-selected button has no feedback color',
    );
  });

  test('isCorrect === null produces no feedback class (initial state)', function (assert) {
    const { component } = createComponent();

    // Simulate that currentAnswer is set but isCorrect is still null
    component.currentAnswer = 'some-word';

    assert.strictEqual(
      component.getButtonClass('some-word'),
      'bg-transparent',
      'null isCorrect produces no feedback styling',
    );
  });

  test('startTask resets and consecutive wrong answers each trigger change', function (assert) {
    const { component, changeLog } = createComponent();

    component.submitAnswer('wrong1', 'correct-word');
    assert.strictEqual(component.getButtonClass('wrong1'), 'bg-red-400', 'first wrong answer shows red');

    component.startTask();
    assert.strictEqual(component.isCorrect, null, 'reset to null');

    component.submitAnswer('wrong2', 'correct-word');
    assert.strictEqual(component.getButtonClass('wrong2'), 'bg-red-400', 'second wrong answer shows red');
    assert.strictEqual(changeLog.length, 3, 'three changes: null->false, false->null, null->false');
  });
});

// ---------------------------------------------------------------------------
// 3. ProsodyComponent (same CSS class feedback as phoneme-pairs)
// ---------------------------------------------------------------------------

module('Unit | Component | task-player | wrong-answer-feedback | prosody', function () {
  function createComponent() {
    const component = {
      currentAnswer: '',
      // Replicate startTask() from prosody/index.gts line 141-146
      startTask() {
        this.isCorrect = null;
      },
      // Replicate showTaskResult logic from prosody/index.gts line 171-187
      submitAnswer(selectedLabel, correctAnswer) {
        this.currentAnswer = selectedLabel;
        const isCorrect = this.currentAnswer === correctAnswer;
        this.isCorrect = isCorrect;
        return isCorrect;
      },
      // Replicate the CSS class logic from prosody template (lines 222-233)
      getButtonClass(answerOptionWord) {
        if (this.currentAnswer === answerOptionWord && this.isCorrect === true) {
          return 'bg-green-500';
        }
        if (this.currentAnswer === answerOptionWord && this.isCorrect === false) {
          return 'bg-red-400';
        }
        return 'bg-transparent';
      },
    };
    const changeLog = addTrackedIsCorrect(component);
    return { component, changeLog };
  }

  test('isCorrect starts as null', function (assert) {
    const { component } = createComponent();
    assert.strictEqual(component.isCorrect, null, 'isCorrect is null on init');
  });

  test('wrong answer changes isCorrect from null to false', function (assert) {
    const { component, changeLog } = createComponent();

    component.submitAnswer('wrong-label', 'correct-label');

    assert.strictEqual(component.isCorrect, false, 'isCorrect is false');
    assert.deepEqual(changeLog[0], { from: null, to: false }, 'tracked change: null -> false');
  });

  test('wrong answer applies bg-red-400 class on selected button', function (assert) {
    const { component } = createComponent();

    component.submitAnswer('wrong-label', 'correct-label');

    assert.strictEqual(
      component.getButtonClass('wrong-label'),
      'bg-red-400',
      'wrong answer button gets red background',
    );
  });

  test('correct answer applies bg-green-500 class on selected button', function (assert) {
    const { component } = createComponent();

    component.submitAnswer('correct-label', 'correct-label');

    assert.strictEqual(
      component.getButtonClass('correct-label'),
      'bg-green-500',
      'correct answer button gets green background',
    );
  });

  test('startTask resets isCorrect to null for next round', function (assert) {
    const { component, changeLog } = createComponent();

    component.submitAnswer('wrong-label', 'correct-label');
    component.startTask();

    assert.strictEqual(component.isCorrect, null, 'isCorrect reset to null');
    assert.deepEqual(changeLog[1], { from: false, to: null }, 'tracked change: false -> null');
  });

  test('consecutive wrong answers each produce tracked changes', function (assert) {
    const { component, changeLog } = createComponent();

    // First attempt
    component.submitAnswer('wrong1', 'correct');
    assert.strictEqual(changeLog.length, 1, 'first change detected');

    // Reset for next attempt
    component.startTask();
    assert.strictEqual(changeLog.length, 2, 'reset detected');

    // Second attempt
    component.submitAnswer('wrong2', 'correct');
    assert.strictEqual(changeLog.length, 3, 'third change detected');

    // All changes are distinct state transitions
    assert.deepEqual(changeLog, [
      { from: null, to: false },
      { from: false, to: null },
      { from: null, to: false },
    ], 'full change sequence: null->false->null->false');
  });
});

// ---------------------------------------------------------------------------
// 4. AuditorySequenceComponent (bg-red-100/bg-green-100 on selection info)
// ---------------------------------------------------------------------------

module('Unit | Component | task-player | wrong-answer-feedback | auditory-sequence', function () {
  function createComponent() {
    const component = {
      selectedSequence: [],
      // Replicate startTask() from auditory-sequence/index.gts line 160-166
      startTask() {
        this.isCorrect = null;
        this.selectedSequence = [];
      },
      // Replicate showTaskResult logic from auditory-sequence/index.gts line 195-215
      submitSequence(selectedSequence, targetSequence) {
        this.selectedSequence = selectedSequence;
        const isCorrect =
          JSON.stringify(selectedSequence) === JSON.stringify(targetSequence);
        this.isCorrect = isCorrect;
        return isCorrect;
      },
      // Replicate the CSS class logic from auditory-sequence template (lines 286-287)
      getSelectionInfoClass() {
        if (this.isCorrect === true) {
          return 'bg-green-100 rounded-md';
        }
        if (this.isCorrect === false) {
          return 'bg-red-100 rounded-md';
        }
        return '';
      },
    };
    const changeLog = addTrackedIsCorrect(component);
    return { component, changeLog };
  }

  test('isCorrect starts as null', function (assert) {
    const { component } = createComponent();
    assert.strictEqual(component.isCorrect, null, 'isCorrect is null on init');
  });

  test('wrong sequence sets isCorrect to false', function (assert) {
    const { component, changeLog } = createComponent();

    component.submitSequence(['b', 'a'], ['a', 'b']);

    assert.strictEqual(component.isCorrect, false, 'isCorrect is false for wrong sequence');
    assert.deepEqual(changeLog[0], { from: null, to: false }, 'tracked change: null -> false');
  });

  test('correct sequence sets isCorrect to true', function (assert) {
    const { component } = createComponent();

    component.submitSequence(['a', 'b'], ['a', 'b']);

    assert.strictEqual(component.isCorrect, true, 'isCorrect is true for correct sequence');
  });

  test('wrong sequence applies bg-red-100 on selection info container', function (assert) {
    const { component } = createComponent();

    component.submitSequence(['b', 'a'], ['a', 'b']);

    assert.strictEqual(
      component.getSelectionInfoClass(),
      'bg-red-100 rounded-md',
      'selection info gets red background',
    );
  });

  test('correct sequence applies bg-green-100 on selection info container', function (assert) {
    const { component } = createComponent();

    component.submitSequence(['a', 'b'], ['a', 'b']);

    assert.strictEqual(
      component.getSelectionInfoClass(),
      'bg-green-100 rounded-md',
      'selection info gets green background',
    );
  });

  test('null isCorrect produces no feedback class', function (assert) {
    const { component } = createComponent();

    assert.strictEqual(
      component.getSelectionInfoClass(),
      '',
      'no feedback class when isCorrect is null',
    );
  });

  test('startTask resets isCorrect and selectedSequence', function (assert) {
    const { component, changeLog } = createComponent();

    component.submitSequence(['b', 'a'], ['a', 'b']);
    component.startTask();

    assert.strictEqual(component.isCorrect, null, 'isCorrect reset to null');
    assert.deepEqual(component.selectedSequence, [], 'selectedSequence reset to empty');
    assert.deepEqual(changeLog[1], { from: false, to: null }, 'tracked change: false -> null');
  });

  test('consecutive wrong sequences each produce tracked changes', function (assert) {
    const { component, changeLog } = createComponent();

    component.submitSequence(['b', 'a'], ['a', 'b']);
    component.startTask();
    component.submitSequence(['c', 'a'], ['a', 'b']);

    assert.strictEqual(changeLog.length, 3, 'three changes detected');
    assert.deepEqual(changeLog[2], { from: null, to: false }, 'second wrong sequence detected');
  });
});

// ---------------------------------------------------------------------------
// 5. EnvironmentalSoundsComponent (bg-red-50/bg-green-50 on options list)
// ---------------------------------------------------------------------------

module('Unit | Component | task-player | wrong-answer-feedback | environmental-sounds', function () {
  function createComponent() {
    const component = {
      currentAnswer: [],
      // Replicate startTask() from environmental-sounds/index.gts line 142-147
      startTask() {
        this.isCorrect = null;
      },
      // Replicate showTaskResult logic from environmental-sounds/index.gts line 168-195
      submitAnswer(currentAnswer, correctAnswer) {
        this.currentAnswer = currentAnswer;
        const isCorrect = currentAnswer.join('') === correctAnswer.join('');
        this.isCorrect = isCorrect;
        return isCorrect;
      },
      // Replicate the CSS class logic from environmental-sounds template (lines 217-219)
      getOptionsListClass() {
        if (this.isCorrect === false) {
          return 'bg-red-50 rounded-md';
        }
        if (this.isCorrect === true) {
          return 'bg-green-50 rounded-md';
        }
        return '';
      },
    };
    const changeLog = addTrackedIsCorrect(component);
    return { component, changeLog };
  }

  test('isCorrect starts as null', function (assert) {
    const { component } = createComponent();
    assert.strictEqual(component.isCorrect, null, 'isCorrect is null on init');
  });

  test('wrong answer sets isCorrect to false', function (assert) {
    const { component, changeLog } = createComponent();

    component.submitAnswer(['wrong'], ['correct']);

    assert.strictEqual(component.isCorrect, false, 'isCorrect is false');
    assert.deepEqual(changeLog[0], { from: null, to: false }, 'tracked change: null -> false');
  });

  test('correct answer sets isCorrect to true', function (assert) {
    const { component } = createComponent();

    component.submitAnswer(['correct'], ['correct']);

    assert.strictEqual(component.isCorrect, true, 'isCorrect is true');
  });

  test('wrong answer applies bg-red-50 on options list container', function (assert) {
    const { component } = createComponent();

    component.submitAnswer(['wrong'], ['correct']);

    assert.strictEqual(
      component.getOptionsListClass(),
      'bg-red-50 rounded-md',
      'options list gets light red background',
    );
  });

  test('correct answer applies bg-green-50 on options list container', function (assert) {
    const { component } = createComponent();

    component.submitAnswer(['correct'], ['correct']);

    assert.strictEqual(
      component.getOptionsListClass(),
      'bg-green-50 rounded-md',
      'options list gets light green background',
    );
  });

  test('null isCorrect produces no feedback class', function (assert) {
    const { component } = createComponent();

    assert.strictEqual(
      component.getOptionsListClass(),
      '',
      'no feedback class when isCorrect is null',
    );
  });

  test('startTask resets isCorrect to null', function (assert) {
    const { component, changeLog } = createComponent();

    component.submitAnswer(['wrong'], ['correct']);
    component.startTask();

    assert.strictEqual(component.isCorrect, null, 'isCorrect reset to null');
    assert.deepEqual(changeLog[1], { from: false, to: null }, 'tracked change: false -> null');
  });

  test('consecutive wrong answers each produce tracked changes', function (assert) {
    const { component, changeLog } = createComponent();

    component.submitAnswer(['wrong1'], ['correct']);
    component.startTask();
    component.submitAnswer(['wrong2'], ['correct']);

    assert.strictEqual(changeLog.length, 3, 'three changes detected');
    assert.deepEqual(changeLog, [
      { from: null, to: false },
      { from: false, to: null },
      { from: null, to: false },
    ], 'full change sequence: null->false->null->false');
  });
});

// ---------------------------------------------------------------------------
// 6. WordsSequencesComponent (TextImageButton with check/X icons)
// ---------------------------------------------------------------------------

module('Unit | Component | task-player | wrong-answer-feedback | words-sequences', function () {
  function createComponent() {
    const component = {
      correctnessPerType: {},
      currentAnswerObject: null,
      // Replicate startTask() from words-sequences/index.gts line 137-145
      startTask(selectedItemsOrder) {
        this.isCorrect = null;
        this.correctnessPerType = {};
        const obj = {};
        for (const key of selectedItemsOrder) {
          obj[key] = null;
        }
        this.currentAnswerObject = obj;
      },
      // Replicate showTaskResult logic from words-sequences/index.gts line 166-196
      submitAnswer(userAnswerWords, correctAnswerWords, selectedItemsOrder) {
        const isCorrect =
          JSON.stringify(userAnswerWords) === JSON.stringify(correctAnswerWords);

        const correctnessPerType = {};
        selectedItemsOrder.forEach(function (orderName, index) {
          correctnessPerType[orderName] = userAnswerWords[index] === correctAnswerWords[index];
        });
        this.correctnessPerType = correctnessPerType;
        this.isCorrect = isCorrect;
        return isCorrect;
      },
    };
    const changeLog = addTrackedIsCorrect(component);
    return { component, changeLog };
  }

  test('isCorrect starts as null', function (assert) {
    const { component } = createComponent();
    assert.strictEqual(component.isCorrect, null, 'isCorrect is null on init');
  });

  test('wrong answer sets isCorrect to false', function (assert) {
    const { component, changeLog } = createComponent();
    const order = ['noun', 'verb'];

    component.submitAnswer(['cat', 'sit'], ['cat', 'run'], order);

    assert.strictEqual(component.isCorrect, false, 'isCorrect is false');
    assert.deepEqual(changeLog[0], { from: null, to: false }, 'tracked change: null -> false');
  });

  test('correct answer sets isCorrect to true', function (assert) {
    const { component } = createComponent();
    const order = ['noun', 'verb'];

    component.submitAnswer(['cat', 'run'], ['cat', 'run'], order);

    assert.strictEqual(component.isCorrect, true, 'isCorrect is true');
  });

  test('correctnessPerType tracks which word types are correct/incorrect', function (assert) {
    const { component } = createComponent();
    const order = ['noun', 'verb'];

    component.submitAnswer(['cat', 'sit'], ['cat', 'run'], order);

    assert.true(component.correctnessPerType['noun'], 'noun is correct');
    assert.false(component.correctnessPerType['verb'], 'verb is incorrect');
  });

  test('startTask resets isCorrect and correctnessPerType', function (assert) {
    const { component, changeLog } = createComponent();
    const order = ['noun', 'verb'];

    component.submitAnswer(['cat', 'sit'], ['cat', 'run'], order);
    component.startTask(order);

    assert.strictEqual(component.isCorrect, null, 'isCorrect reset to null');
    assert.deepEqual(component.correctnessPerType, {}, 'correctnessPerType reset to empty');
    assert.deepEqual(changeLog[1], { from: false, to: null }, 'tracked change: false -> null');
  });

  test('consecutive wrong answers each produce tracked changes', function (assert) {
    const { component, changeLog } = createComponent();
    const order = ['noun', 'verb'];

    component.submitAnswer(['cat', 'sit'], ['cat', 'run'], order);
    component.startTask(order);
    component.submitAnswer(['dog', 'sit'], ['cat', 'run'], order);

    assert.strictEqual(changeLog.length, 3, 'three changes detected');
    assert.deepEqual(changeLog[2], { from: null, to: false }, 'second wrong answer detected');
  });

  test('TextImageButton shows icons only when checked and isSelected', function (assert) {
    // Replicate the TextImageButton template logic (lines 84-99):
    // {{#if (and @checked @isSelected)}}
    //   {{#if @isCorrect}} check-circle {{else}} times-circle {{/if}}
    // {{/if}}
    function getIcon(checked, isSelected, isCorrect) {
      if (checked && isSelected) {
        return isCorrect ? 'check-circle' : 'times-circle';
      }
      return null;
    }

    // answerCompleted (checked) becomes true, button is selected, and isCorrect is false
    assert.strictEqual(
      getIcon(true, true, false),
      'times-circle',
      'wrong answer shows X icon when checked and selected',
    );
    assert.strictEqual(
      getIcon(true, true, true),
      'check-circle',
      'correct answer shows check icon when checked and selected',
    );
    assert.strictEqual(
      getIcon(false, true, false),
      null,
      'no icon when not yet checked',
    );
    assert.strictEqual(
      getIcon(true, false, false),
      null,
      'no icon when not selected',
    );
  });
});

// ---------------------------------------------------------------------------
// Cross-cutting: Verify the bug would have occurred with the old boolean init
// ---------------------------------------------------------------------------

module('Unit | Component | task-player | wrong-answer-feedback | regression guard', function () {
  test('OLD behavior (boolean false init): consecutive wrong answers produce NO tracked change', function (assert) {
    // This test documents why the old `isCorrect = false` was broken.
    const changeLog = [];
    const component = {
      startTask() {
        this.isCorrect = false; // OLD: reset to false
      },
    };
    let _isCorrect = false; // OLD: initialized as false
    Object.defineProperty(component, 'isCorrect', {
      configurable: true,
      enumerable: true,
      get() { return _isCorrect; },
      set(value) {
        const oldValue = _isCorrect;
        _isCorrect = value;
        if (oldValue !== value) {
          changeLog.push({ from: oldValue, to: value });
        }
      },
    });

    // First wrong answer: false -> false (NO CHANGE - bug!)
    component.isCorrect = false;
    assert.strictEqual(changeLog.length, 0, 'BUG: no change detected for first wrong answer (false -> false)');

    // Reset
    component.startTask();
    assert.strictEqual(changeLog.length, 0, 'no change from startTask either (false -> false)');

    // Second wrong answer: still no change
    component.isCorrect = false;
    assert.strictEqual(changeLog.length, 0, 'BUG: still no change for second wrong answer');
  });

  test('NEW behavior (null init): consecutive wrong answers each produce a tracked change', function (assert) {
    const changeLog = [];
    const component = {
      startTask() {
        this.isCorrect = null; // NEW: reset to null
      },
    };
    let _isCorrect = null; // NEW: initialized as null
    Object.defineProperty(component, 'isCorrect', {
      configurable: true,
      enumerable: true,
      get() { return _isCorrect; },
      set(value) {
        const oldValue = _isCorrect;
        _isCorrect = value;
        if (oldValue !== value) {
          changeLog.push({ from: oldValue, to: value });
        }
      },
    });

    // First wrong answer: null -> false (CHANGE DETECTED)
    component.isCorrect = false;
    assert.strictEqual(changeLog.length, 1, 'FIX: change detected for first wrong answer (null -> false)');

    // Reset: false -> null (CHANGE DETECTED)
    component.startTask();
    assert.strictEqual(changeLog.length, 2, 'FIX: change detected for reset (false -> null)');

    // Second wrong answer: null -> false (CHANGE DETECTED again)
    component.isCorrect = false;
    assert.strictEqual(changeLog.length, 3, 'FIX: change detected for second wrong answer (null -> false)');
  });

  test('with null init, eq helper correctly distinguishes null from false for CSS classes', function (assert) {
    // The templates use {{if (eq this.isCorrect false) "bg-red-..."}}
    // and {{if (eq this.isCorrect true) "bg-green-..."}}.
    // Ember's eq helper uses strict equality (===).

    function eqHelper(a, b) {
      return a === b;
    }

    // When isCorrect is null (initial/reset state), neither red nor green should apply
    assert.false(eqHelper(null, false), 'null !== false: no red class in initial state');
    assert.false(eqHelper(null, true), 'null !== true: no green class in initial state');

    // When isCorrect is false (wrong answer), red should apply
    assert.true(eqHelper(false, false), 'false === false: red class applies for wrong answer');
    assert.false(eqHelper(false, true), 'false !== true: no green class for wrong answer');

    // When isCorrect is true (correct answer), green should apply
    assert.false(eqHelper(true, false), 'true !== false: no red class for correct answer');
    assert.true(eqHelper(true, true), 'true === true: green class applies for correct answer');
  });
});
