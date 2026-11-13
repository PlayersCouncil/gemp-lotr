// @ts-check
const eslint = require('@eslint/js');
const { defineConfig } = require('eslint/config');
const tseslint = require('typescript-eslint');
const angular = require('angular-eslint');

module.exports = defineConfig([
  {
    files: ['**/*.ts'],
    extends: [
      eslint.configs.recommended,
      tseslint.configs.strictTypeChecked,
      tseslint.configs.stylisticTypeChecked,
      angular.configs.tsRecommended,
    ],
    languageOptions: { parserOptions: { projectService: true, tsconfigRootDir: __dirname } },
    processor: angular.processInlineTemplates,
    rules: {
      // `() => doSomething()` is the normal short form for a callback
      '@typescript-eslint/no-confusing-void-expression': ['error', { ignoreArrowShorthand: true }],
      '@typescript-eslint/restrict-template-expressions': ['error', { allowNumber: true }],
      // Angular classes can be empty: the decorator is what counts
      '@typescript-eslint/no-extraneous-class': ['error', { allowWithDecorator: true }],
      '@angular-eslint/prefer-signals': 'error',
      '@angular-eslint/prefer-output-readonly': 'error',
      '@angular-eslint/directive-selector': [
        'error',
        {
          type: 'attribute',
          prefix: 'gd',
          style: 'camelCase',
        },
      ],
      '@angular-eslint/component-selector': [
        'error',
        {
          type: 'element',
          // gd-*: the deck builder's parts; gemp-app: the shell; deckbuilder-app: the deck builder's root
          prefix: ['gd', 'gemp', 'deckbuilder'],
          style: 'kebab-case',
        },
      ],
    },
  },
  // The specs are checked by the compiler; the type-aware rules cost more than they find there
  {
    files: ['**/*.spec.ts', 'src/testing/**'],
    extends: [tseslint.configs.disableTypeChecked],
    rules: { '@typescript-eslint/no-non-null-assertion': 'off' },
  },
  {
    files: ['**/*.html'],
    extends: [angular.configs.templateRecommended, angular.configs.templateAccessibility],
    rules: {
      // In a native <dialog>, autofocus is how the HTML standard picks the control focused when it opens
      '@angular-eslint/template/no-autofocus': 'off',
      // <button [gdCard]>: the card component fills the button
      '@angular-eslint/template/elements-content': ['error', { allowList: ['gdCard'] }],
    },
  },
]);
