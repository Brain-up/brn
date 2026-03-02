import { defineConfig } from 'vite';
import { extensions, classicEmberSupport, ember } from '@embroider/vite';
import { babel } from '@rollup/plugin-babel';
import tailwindcss from 'tailwindcss';
import postcssImport from 'postcss-import';
import { resolve } from 'path';
import { copyFileSync, mkdirSync, existsSync } from 'fs';

// Copy @ffmpeg/core assets to public/assets for static serving
const ffmpegCopyPlugin = () => ({
  name: 'copy-ffmpeg-assets',
  buildStart() {
    const destDir = resolve('public/assets');
    if (!existsSync(destDir)) {
      mkdirSync(destDir, { recursive: true });
    }
    const ffmpegCoreDist = resolve('node_modules/@ffmpeg/core/dist');
    for (const file of ['ffmpeg-core.js', 'ffmpeg-core.wasm', 'ffmpeg-core.worker.js']) {
      const src = resolve(ffmpegCoreDist, file);
      if (existsSync(src)) {
        copyFileSync(src, resolve(destDir, file));
      }
    }
  },
});

export default defineConfig(({ mode: _mode }) => ({
  plugins: [
    classicEmberSupport(),
    ember(),
    babel({
      babelHelpers: 'runtime',
      extensions,
    }),
    ffmpegCopyPlugin(),
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
