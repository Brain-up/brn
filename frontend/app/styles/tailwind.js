/* eslint-disable */
const {
  colors,
  borderRadius,
  justifyContent,
} = require('tailwindcss/defaultTheme');

module.exports = {
  theme: {
    extend: {
      colors: {
        PROGRESS: {
          BAD: '#F38698',
          GOOD: '#FBD051',
          GREAT: '#47CD8A',
        },
        blue: {
          ...colors.blue,
          100: '#DAF0FC',
          300: '#EDF8FE',
          500: '#81D5F9',
          700: '#5E6EED',
          900: '#001274',
          light: '#428DFC',
          dark: '#5A6BFF',
          1100: '#428DFC',
        },
        pink: {
          ...colors.pink,
          200: '#FBEDFE',
          500: '#FF6373',
          secondary: '#F38698',
        },

        yellow: {
          ...colors.yellow,
          500: '#F68820',
          secondary: '#FBD051',
        },

        purple: {
          ...colors.purple,
          700: '#2A165B',
          primary: '#503DAD',
          left: '#7979f2',
          right: '#a179f2',
        },

        indigo: {
          ...colors.indigo,
          200: '#EDF0FE',
          500: '#63BBEB',
        },
        green: {
          ...colors.green,
          secondary: '#47CD8A',
        },
      },
      borderRadius: {
        ...borderRadius,
        large: '1.25rem',
      },
      justifyContent: {
        ...justifyContent,
        evenly: 'space-evenly',
      },
      flex: {
        6: '0 1 16.666%',
      },
      height: {
        '200px': '200px',
        '48px': '48px',
      },
      width: {
        '48px': '48px',
      },
    },
    fontFamily: {
      openSans: ['Open Sans'],
    },
  },
  variants: {
    backgroundColor: ['responsive', 'hover', 'focus', 'disabled', 'active'],
    textColor: ['responsive', 'hover', 'focus', 'disabled', 'active'],
    gradientColorStops: ['responsive', 'hover', 'focus', 'disabled', 'active'],
    cursor: ['disabled'],
    boxShadow: ['disabled'],
    opacity: ['disabled'],
  },
  plugins: [],
};
