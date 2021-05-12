import { DefinePlugin } from 'webpack';

export default {
  plugins: [
    new DefinePlugin({
      process: {
        env: {
          CACHE_OFF: Date.now().toString(),
        },
      },
    }),
  ],
};
