import { DefinePlugin } from 'webpack';

export default {
  output: {
    // ... other configurations
    hashFunction: 'sha256',
  },
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
