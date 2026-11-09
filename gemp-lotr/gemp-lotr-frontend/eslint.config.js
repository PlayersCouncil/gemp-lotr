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
      tseslint.configs.recommended,
      tseslint.configs.stylistic,
      angular.configs.tsRecommended,
    ],
    processor: angular.processInlineTemplates,
    rules: {
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
