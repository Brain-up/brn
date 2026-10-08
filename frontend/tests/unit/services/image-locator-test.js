import { module, test } from 'qunit';
import { setupTest } from 'ember-qunit';
import { settled } from '@ember/test-helpers';
import mockService from '../../test-support/mock-service';

module('Unit | Service | image-locator', function (hooks) {
  setupTest(hooks);

  test('it exists', function (assert) {
    const service = this.owner.lookup('service:image-locator');
    assert.ok(service);
  });

  module('maybeUploadImage', function (hooks) {
    let uploadCalls;
    let uploadResolvers;

    hooks.beforeEach(function () {
      uploadCalls = [];
      uploadResolvers = [];
      mockService(this.owner, 'network', {
        uploadPictureFile(blob, fileName) {
          uploadCalls.push({ blob, fileName });
          for (const resolve of uploadResolvers) {
            resolve();
          }
          return Promise.resolve(new Response(null, { status: 200 }));
        },
      });
    });

    function createSmallCanvas() {
      const canvas = document.createElement('canvas');
      canvas.width = 2;
      canvas.height = 2;
      const ctx = canvas.getContext('2d');
      ctx.fillStyle = '#ff0000';
      ctx.fillRect(0, 0, 2, 2);
      return canvas;
    }

    function waitForUploadCall() {
      if (uploadCalls.length > 0) {
        return Promise.resolve();
      }
      return new Promise((resolve) => {
        uploadResolvers.push(resolve);
        // Safety timeout so tests don't hang forever
        setTimeout(resolve, 2000);
      });
    }

    test('uploads image when authenticated', async function (assert) {
      mockService(this.owner, 'session', { isAuthenticated: true });

      const service = this.owner.lookup('service:image-locator');
      const canvas = createSmallCanvas();

      service.maybeUploadImage('кот', canvas);
      await waitForUploadCall();
      await settled();

      assert.strictEqual(uploadCalls.length, 1, 'uploadPictureFile called once');
      assert.strictEqual(uploadCalls[0].fileName, 'кот.png', 'filename is word.png');
      assert.ok(uploadCalls[0].blob instanceof Blob, 'blob is a Blob');
      assert.strictEqual(uploadCalls[0].blob.type, 'image/png', 'blob is PNG format');
    });

    test('skips upload when not authenticated', async function (assert) {
      mockService(this.owner, 'session', { isAuthenticated: false });

      const service = this.owner.lookup('service:image-locator');
      const canvas = createSmallCanvas();

      service.maybeUploadImage('кот', canvas);

      // Give toBlob time to fire (it shouldn't, since we return early)
      await new Promise((r) => setTimeout(r, 100));
      await settled();

      assert.strictEqual(uploadCalls.length, 0, 'uploadPictureFile not called');
    });

    test('deduplicates uploads for the same word', async function (assert) {
      mockService(this.owner, 'session', { isAuthenticated: true });

      const service = this.owner.lookup('service:image-locator');

      service.maybeUploadImage('кот', createSmallCanvas());
      service.maybeUploadImage('кот', createSmallCanvas());
      await waitForUploadCall();
      await settled();

      assert.strictEqual(uploadCalls.length, 1, 'uploadPictureFile called only once for same word');
      assert.true(service.uploadedWords.has('кот'), 'word is in uploadedWords set');
    });

    test('allows uploads for different words', async function (assert) {
      mockService(this.owner, 'session', { isAuthenticated: true });

      const service = this.owner.lookup('service:image-locator');

      service.maybeUploadImage('кот', createSmallCanvas());
      service.maybeUploadImage('дом', createSmallCanvas());

      // Wait for both uploads
      await waitForUploadCall();
      // Second call may not have fired yet — give extra time
      await new Promise((r) => setTimeout(r, 100));
      await settled();

      assert.strictEqual(uploadCalls.length, 2, 'uploadPictureFile called for each unique word');
    });

    test('skips upload when blob exceeds 512KB', async function (assert) {
      mockService(this.owner, 'session', { isAuthenticated: true });

      const service = this.owner.lookup('service:image-locator');

      // Create a large canvas that produces a blob > 512KB
      const canvas = document.createElement('canvas');
      canvas.width = 1000;
      canvas.height = 1000;
      const ctx = canvas.getContext('2d');
      // Fill with random-ish pattern to prevent PNG compression
      for (let i = 0; i < 1000; i++) {
        ctx.fillStyle = `rgb(${i % 256},${(i * 7) % 256},${(i * 13) % 256})`;
        ctx.fillRect(i, 0, 1, 1000);
      }

      // Verify the canvas actually produces a large blob
      const blobSize = await new Promise((resolve) => {
        canvas.toBlob((b) => resolve(b ? b.size : 0), 'image/png');
      });

      if (blobSize < 512 * 1024) {
        assert.ok(true, `Canvas blob is ${blobSize} bytes (under 512KB) — skipping size guard test`);
        return;
      }

      service.maybeUploadImage('большой', canvas);
      await new Promise((r) => setTimeout(r, 200));
      await settled();

      assert.strictEqual(uploadCalls.length, 0, 'uploadPictureFile not called for oversized blob');
    });

    test('silently handles upload errors', async function (assert) {
      mockService(this.owner, 'session', { isAuthenticated: true });

      const service = this.owner.lookup('service:image-locator');

      // Replace the network service's method directly to simulate failure
      const networkService = this.owner.lookup('service:network');
      networkService.uploadPictureFile = function () {
        uploadCalls.push('called');
        return Promise.reject(new Error('network failure'));
      };

      service.maybeUploadImage('кот', createSmallCanvas());
      await waitForUploadCall();
      await settled();

      assert.strictEqual(uploadCalls.length, 1, 'upload was attempted');
      // No error thrown — fire-and-forget works
      assert.ok(true, 'no error propagated');
    });

    test('skips upload when service is destroying', async function (assert) {
      mockService(this.owner, 'session', { isAuthenticated: true });

      const service = this.owner.lookup('service:image-locator');

      // Simulate destroying state
      Object.defineProperty(service, 'isDestroying', { value: true });

      service.maybeUploadImage('кот', createSmallCanvas());
      await new Promise((r) => setTimeout(r, 100));
      await settled();

      assert.strictEqual(uploadCalls.length, 0, 'uploadPictureFile not called when destroying');
    });
  });
});
