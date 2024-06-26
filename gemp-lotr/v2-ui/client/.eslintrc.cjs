module.exports = {
  env: {
    browser: true,
    es2021: true,
  },
  extends: [
    'airbnb',
    'airbnb-typescript',
    'airbnb/hooks',
    'plugin:react/recommended',
    'plugin:@typescript-eslint/recommended',
    'plugin:prettier/recommended',
  ],
  overrides: [],
  parser: '@typescript-eslint/parser',
  parserOptions: {
    ecmaVersion: 'latest',
    sourceType: 'module',
    project: './tsconfig.json',
  },
  plugins: ['react', '@typescript-eslint', 'prettier'],
  rules: {
    'react/require-default-props': 0, // since we are using typescript no need to redefine props again
    'react/react-in-jsx-scope': 0,
    'react/jsx-props-no-spreading': 0,
    'prettier/prettier': ['error', { endOfLine: 'auto' }],
    'import/prefer-default-export': 0,
    'no-plusplus': 0,
    'no-param-reassign': { props: false}
  },
};
