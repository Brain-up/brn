/* eslint-disable */
const { colors } = require('tailwindcss/defaultTheme')

module.exports = {
  theme: {
    extend: {
      colors: {
        blue: {
          ...colors.blue,
          '500': '#81D5F9',
          '700': '#5E6EED',
          '900': '#001274',
        },
      },
    },
  },
  variants: {},
  plugins: [],
};
