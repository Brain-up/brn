/* eslint-disable */
const { colors } = require('tailwindcss/defaultTheme');

module.exports = {
  theme: {
    extend: {
      colors: {
        blue: {
          ...colors.blue,
          '100': '#DAF0FC',
          '300': '#EDF8FE',
          '500': '#81D5F9',
          '700': '#5E6EED',
          '900': '#001274',
        },
        pink: {
          ...colors.pink,
          '200': '#FBEDFE',
          '500': '#FF6373',
        },

        yellow: {
          ...colors.yellow,
          '500': '#F68820',
        },

        purple: {
          ...colors.purple,
          '700': '#2A165B',
        },

        indigo: {
          ...colors.indigo,
          '200': '#EDF0FE',
        },
      },
      borderRadius: {
        large: '1.25rem',
      },
    },
  },
  variants: {},
  plugins: [],
};
