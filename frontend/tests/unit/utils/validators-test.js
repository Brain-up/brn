import { module, test } from 'qunit';
import { isNotEmptyString, isBornYearValid } from 'brn/utils/validators';

module('Unit | Utility | validators', function () {
  module('isNotEmptyString', function () {
    test('returns true for a non-empty string', function (assert) {
      assert.true(isNotEmptyString('hello'));
    });

    test('returns true for a string with spaces around content', function (assert) {
      assert.true(isNotEmptyString('  hello  '));
    });

    test('returns false for an empty string', function (assert) {
      assert.false(isNotEmptyString(''));
    });

    test('returns false for a whitespace-only string', function (assert) {
      assert.false(isNotEmptyString('   '));
    });

    test('returns false for a tab-only string', function (assert) {
      assert.false(isNotEmptyString('\t'));
    });

    test('returns false for null', function (assert) {
      assert.false(isNotEmptyString(null));
    });

    test('returns false for undefined', function (assert) {
      assert.false(isNotEmptyString(undefined));
    });

    test('returns false for a number', function (assert) {
      assert.false(isNotEmptyString(42));
    });

    test('returns false for a boolean', function (assert) {
      assert.false(isNotEmptyString(true));
    });

    test('returns false for an object', function (assert) {
      assert.false(isNotEmptyString({}));
    });

    test('returns false for an array', function (assert) {
      assert.false(isNotEmptyString([]));
    });

    test('returns true for a single character', function (assert) {
      assert.true(isNotEmptyString('a'));
    });
  });

  module('isBornYearValid', function () {
    test('returns true for a valid recent year', function (assert) {
      assert.true(isBornYearValid('1990'));
    });

    test('returns true for the current year', function (assert) {
      const currentYear = new Date().getFullYear().toString();
      assert.true(isBornYearValid(currentYear));
    });

    test('returns true for exactly 100 years ago', function (assert) {
      const hundredYearsAgo = (new Date().getFullYear() - 100).toString();
      assert.true(isBornYearValid(hundredYearsAgo));
    });

    test('returns false for more than 100 years ago', function (assert) {
      const tooOld = (new Date().getFullYear() - 101).toString();
      assert.false(isBornYearValid(tooOld));
    });

    test('returns false for a future year', function (assert) {
      const futureYear = (new Date().getFullYear() + 1).toString();
      assert.false(isBornYearValid(futureYear));
    });

    test('returns false for a 3-digit year string', function (assert) {
      assert.false(isBornYearValid('999'));
    });

    test('returns false for a 5-digit year string', function (assert) {
      assert.false(isBornYearValid('19900'));
    });

    test('returns false for an empty string', function (assert) {
      assert.false(isBornYearValid(''));
    });

    test('returns false for a non-numeric 4-character string', function (assert) {
      assert.false(isBornYearValid('abcd'));
    });

    test('returns true for year 2000', function (assert) {
      assert.true(isBornYearValid('2000'));
    });
  });
});
