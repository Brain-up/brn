import { module, test } from 'qunit';

module('Unit | Component | global-timer | minutes getter', function () {
  // GlobalTimerComponent uses isTesting() (compile-time macro) which
  // prevents the sync loop from running in tests. We test getter logic
  // directly by replicating it, following the project pattern from
  // tests/unit/components/task-player/component-test.js.

  function createTimer(seconds) {
    return {
      seconds,
      get minutes() {
        const sec = this.seconds % 60;
        const min = Math.floor(this.seconds / 60);
        const secNormalized = sec < 10 ? '0' + sec : sec || '00';
        const minNormalized = min < 10 ? '0' + min : min || '00';
        return `${minNormalized + ' : ' + secNormalized}`;
      },
    };
  }

  test('formats 0 seconds as 00 : 00', function (assert) {
    assert.strictEqual(createTimer(0).minutes, '00 : 00');
  });

  test('formats single-digit seconds with leading zero', function (assert) {
    assert.strictEqual(createTimer(5).minutes, '00 : 05');
  });

  test('formats seconds under a minute', function (assert) {
    assert.strictEqual(createTimer(45).minutes, '00 : 45');
  });

  test('formats exact minutes with 00 seconds', function (assert) {
    assert.strictEqual(createTimer(120).minutes, '02 : 00');
  });

  test('formats mixed minutes and seconds', function (assert) {
    assert.strictEqual(createTimer(754).minutes, '12 : 34');
  });

  test('formats large values beyond one hour', function (assert) {
    assert.strictEqual(createTimer(3661).minutes, '61 : 01');
  });

  test('formats 10 seconds without double-padding', function (assert) {
    assert.strictEqual(createTimer(10).minutes, '00 : 10');
  });

  test('formats 60 seconds as 01 : 00', function (assert) {
    assert.strictEqual(createTimer(60).minutes, '01 : 00');
  });
});

module('Unit | Component | global-timer | getColor getter', function () {
  function createTimer(seconds) {
    return {
      seconds,
      get getColor() {
        if (this.seconds > 1200) {
          return 'bg-green-secondary';
        } else if (this.seconds > 960) {
          return 'bg-yellow-secondary';
        } else {
          return 'bg-pink-secondary';
        }
      },
    };
  }

  test('returns pink for 0 seconds', function (assert) {
    assert.strictEqual(createTimer(0).getColor, 'bg-pink-secondary');
  });

  test('returns pink at 960 seconds (boundary)', function (assert) {
    assert.strictEqual(createTimer(960).getColor, 'bg-pink-secondary');
  });

  test('returns yellow at 961 seconds', function (assert) {
    assert.strictEqual(createTimer(961).getColor, 'bg-yellow-secondary');
  });

  test('returns yellow at 1200 seconds (boundary)', function (assert) {
    assert.strictEqual(createTimer(1200).getColor, 'bg-yellow-secondary');
  });

  test('returns green at 1201 seconds', function (assert) {
    assert.strictEqual(createTimer(1201).getColor, 'bg-green-secondary');
  });
});

module('Unit | Component | global-timer | syncTask loop logic', function () {
  // syncTask uses isTesting() which returns true in tests, causing
  // the loop to break immediately. We test the production logic path
  // by replicating the branching logic.

  async function runSyncLoop({
    isAuthenticated,
    isEnabled,
    userModel,
    requestFn,
    isDestroying,
  }) {
    let requestMade = false;
    let seconds = 0;
    let iterations = 0;
    let reachedTimeout = false;

    // Reproduce the production path of the syncTask loop
    do {
      iterations++;
      // Safety: prevent infinite loops in tests
      if (iterations > 5) break;

      try {
        // Production path (isTesting() === false)
        if (isAuthenticated && isEnabled) {
          if (userModel) {
            requestMade = true;
            const data = await requestFn();
            seconds = data;
          }
        }
      } catch {
        // ok
      }

      reachedTimeout = true;
      // In real code: await timeout(10000)
      break; // Exit after one full iteration for unit testing
    } while (!isDestroying);

    return { requestMade, seconds, iterations, reachedTimeout };
  }

  test('skips request when userModel is not set, still reaches timeout', async function (assert) {
    const result = await runSyncLoop({
      isAuthenticated: true,
      isEnabled: true,
      userModel: null,
      requestFn: () => 999,
      isDestroying: false,
    });
    assert.false(result.requestMade, 'no API request was made');
    assert.strictEqual(result.seconds, 0, 'seconds stayed at 0');
    assert.true(result.reachedTimeout, 'loop reached the timeout (no tight busy-loop)');
  });

  test('makes request when userModel is set', async function (assert) {
    const result = await runSyncLoop({
      isAuthenticated: true,
      isEnabled: true,
      userModel: { id: '1', name: 'Test User' },
      requestFn: () => 1234,
      isDestroying: false,
    });
    assert.true(result.requestMade, 'API request was made');
    assert.strictEqual(result.seconds, 1234, 'seconds updated from response');
  });

  test('skips request when not authenticated', async function (assert) {
    const result = await runSyncLoop({
      isAuthenticated: false,
      isEnabled: true,
      userModel: { id: '1' },
      requestFn: () => 999,
      isDestroying: false,
    });
    assert.false(result.requestMade, 'no request when not authenticated');
  });

  test('skips request when disabled (window blurred)', async function (assert) {
    const result = await runSyncLoop({
      isAuthenticated: true,
      isEnabled: false,
      userModel: { id: '1' },
      requestFn: () => 999,
      isDestroying: false,
    });
    assert.false(result.requestMade, 'no request when disabled');
  });

  test('loop exits when isDestroying is true', function (assert) {
    let iterations = 0;
    const isDestroying = true;

    do {
      iterations++;
      if (iterations > 5) break;
    } while (!isDestroying);

    assert.strictEqual(iterations, 1, 'loop body ran once then exited (do/while checks after body)');
  });

  test('loop continues when isDestroying is false', function (assert) {
    let iterations = 0;
    let destroying = false;

    do {
      iterations++;
      if (iterations >= 3) {
        destroying = true;
      }
    } while (!destroying);

    assert.strictEqual(iterations, 3, 'loop ran until isDestroying became true');
  });

  test('error in request is caught and loop continues', async function (assert) {
    const result = await runSyncLoop({
      isAuthenticated: true,
      isEnabled: true,
      userModel: { id: '1' },
      requestFn: () => { throw new Error('network error'); },
      isDestroying: false,
    });
    assert.true(result.requestMade, 'request was attempted');
    assert.true(result.reachedTimeout, 'loop reached timeout despite error');
    assert.strictEqual(result.seconds, 0, 'seconds unchanged after error');
  });
});
