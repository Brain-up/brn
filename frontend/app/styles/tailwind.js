/* eslint-disable */
const { colors } = require('tailwindcss/defaultTheme');

module.exports = {
  theme: {
    extend: {
      colors: {
        blue: {
          ...colors.blue,
          '300': '#EDF8FE',
          '500': '#81D5F9',
          '700': '#5E6EED',
          '900': '#001274',
        },
        pink: {
          ...colors.pink,
          '500': '#FF6373',
        },

        yellow: {
          ...colors.yellow,
          '500': '#F68820',
        },
      },
    },
  },
  variants: {},
  plugins: [],
};
