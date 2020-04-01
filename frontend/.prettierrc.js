/*eslint-env node*/

module.exports = {
  'tabWidth': 2,
  'useTabs': false,
  'semi': true,
  'singleQuote': true,
  'arrowParens': 'always',
  'endOfLine': 'lf',
  'trailingComma': 'all',
  'overrides': [{
    'files': '*.hbs',
    'options': {
      'semi': true,
      'singleQuote': false,
      'parser': 'glimmer',
    }
  }],
};
