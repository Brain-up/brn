import { defineConfig } from 'vite';
import { extensions, classicEmberSupport, ember } from '@embroider/vite';
import { babel } from '@rollup/plugin-babel';
import tailwindcss from 'tailwindcss';
import postcssImport from 'postcss-import';
import { resolve } from 'path';
import { copyFileSync, mkdirSync, existsSync } from 'fs';

// Copy static assets (ffmpeg, loader.js) to public/assets for serving
const copyAssetsPlugin = () => ({
  name: 'copy-static-assets',
  buildStart() {
    const destDir = resolve('public/assets');
    if (!existsSync(destDir)) {
      mkdirSync(destDir, { recursive: true });
    }
    // Copy @ffmpeg/core assets
    const ffmpegCoreDist = resolve('node_modules/@ffmpeg/core/dist');
    for (const file of ['ffmpeg-core.js', 'ffmpeg-core.wasm', 'ffmpeg-core.worker.js']) {
      const src = resolve(ffmpegCoreDist, file);
      if (existsSync(src)) {
        copyFileSync(src, resolve(destDir, file));
      }
    }
    // Copy loader.js (AMD module loader for v1 addon compatibility)
    const loaderSrc = resolve('node_modules/loader.js/dist/loader/loader.js');
    if (existsSync(loaderSrc)) {
      copyFileSync(loaderSrc, resolve(destDir, 'loader.js'));
    }
  },
});

export default defineConfig(({ mode: _mode }) => ({
  resolve: {
    alias: {
      // v1 addons use `import require from 'require'` (AMD require from loader.js).
      // Map to a shim that re-exports the global AMD require as an ES module.
      'require': resolve('lib/require-shim.js'),
    },
  },
  plugins: [
    classicEmberSupport(),
    ember(),
    babel({
      babelHelpers: 'runtime',
      extensions,
    }),
    copyAssetsPlugin(),
  ],
  css: {
    postcss: {
      plugins: [
        postcssImport({ path: ['node_modules'] }),
        tailwindcss('./app/styles/tailwind.js'),
      ],
    },
  },
  build: {
    rollupOptions: {
      output: {
        // Exclude ffmpeg assets from fingerprinting
        assetFileNames(assetInfo) {
          if (assetInfo.name && assetInfo.name.startsWith('ffmpeg-core')) {
            return 'assets/[name][extname]';
          }
          return 'assets/[name]-[hash][extname]';
        },
      },
    },
  },
}));
