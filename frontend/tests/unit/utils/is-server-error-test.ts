import { module, test } from 'qunit';
import isServerError from 'brn/utils/is-server-error';

module('Unit | Utils | is-server-error', function () {
  test('returns false for null/undefined', function (assert) {
    assert.false(isServerError(null));
    assert.false(isServerError(undefined));
  });

  test('returns false for non-object values', function (assert) {
    assert.false(isServerError('some string error'));
    assert.false(isServerError(42));
    assert.false(isServerError(true));
  });

  test('returns true for WarpDrive FetchError with status 500', function (assert) {
    const error = { isRequestError: true, status: 500, code: 500 };
    assert.true(isServerError(error));
  });

  test('returns true for WarpDrive FetchError with status 503', function (assert) {
    const error = { isRequestError: true, status: 503, code: 503 };
    assert.true(isServerError(error));
  });

  test('returns true for WarpDrive FetchError with status 0 (network error)', function (assert) {
    const error = { isRequestError: true, status: 0, code: 0 };
    assert.true(isServerError(error));
  });

  test('returns false for WarpDrive FetchError with status 401', function (assert) {
    const error = { isRequestError: true, status: 401, code: 401 };
    assert.false(isServerError(error));
  });

  test('returns false for WarpDrive FetchError with status 404', function (assert) {
    const error = { isRequestError: true, status: 404, code: 404 };
    assert.false(isServerError(error));
  });

  test('returns true for error with status 500 in errors array', function (assert) {
    const error = { errors: [{ status: 500 }] };
    assert.true(isServerError(error));
  });

  test('returns true for error with string status "502" in errors array', function (assert) {
    const error = { errors: [{ status: '502' }] };
    assert.true(isServerError(error));
  });

  test('returns false for error with status 400 in errors array', function (assert) {
    const error = { errors: [{ status: 400 }] };
    assert.false(isServerError(error));
  });

  test('returns true for TypeError with fetch message', function (assert) {
    const error = new TypeError('Failed to fetch');
    assert.true(isServerError(error));
  });

  test('returns false for TypeError without fetch message', function (assert) {
    const error = new TypeError('Cannot read property of undefined');
    assert.false(isServerError(error));
  });

  test('returns false for AbortError (user-initiated navigation)', function (assert) {
    const error = { name: 'AbortError', message: 'The operation was aborted' };
    assert.false(isServerError(error));
  });

  test('returns false for generic Error', function (assert) {
    const error = new Error('Something went wrong');
    assert.false(isServerError(error));
  });

  test('returns true for direct status 500 on error object', function (assert) {
    const error = { status: 500 };
    assert.true(isServerError(error));
  });

  test('returns false for direct status 422 on error object', function (assert) {
    const error = { status: 422 };
    assert.false(isServerError(error));
  });
});
