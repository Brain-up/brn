/* eslint-disable */
const defaultTheme = require('tailwindcss/defaultTheme');

/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./app/**/*.{gts,gjs,hbs,ts,js}'],
  safelist: [
    // Dynamic PROGRESS color classes used via template interpolation:
    // bg-PROGRESS-{{@data.progress}} where progress is BAD | GOOD | GREAT
    'bg-PROGRESS-BAD',
    'bg-PROGRESS-GOOD',
    'bg-PROGRESS-GREAT',
    // Dynamic classes used in global-timer component
    'bg-green-secondary',
    'bg-yellow-secondary',
    'bg-pink-secondary',
  ],
  theme: {
    extend: {
      fontSize: {
        xss: '.5rem',
      },
      colors: {
        PROGRESS: {
          BAD: '#F38698',
          GOOD: '#FBD051',
          GREAT: '#47CD8A',
        },
        blue: {
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
          200: '#FBEDFE',
          500: '#FF6373',
          secondary: '#F38698',
        },
        yellow: {
          500: '#F68820',
          secondary: '#FBD051',
        },
        purple: {
          700: '#2A165B',
          primary: '#503DAD',
          left: '#7979f2',
          right: '#a179f2',
        },
        indigo: {
          200: '#EDF0FE',
          500: '#63BBEB',
        },
        green: {
          secondary: '#47CD8A',
        },
      },
      borderRadius: {
        large: '1.25rem',
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
  plugins: [],
};
