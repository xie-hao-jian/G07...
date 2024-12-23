export default {
  extends: [
    'stylelint-config-standard',
    'stylelint-config-recommended-vue'
  ],
  rules: {
    'at-rule-no-unknown': [
      true,
      {
        ignoreAtRules: [
          'tailwind',
          'apply',
          'variants',
          'responsive',
          'screen',
          'layer'
        ],
      },
    ],
    'no-descending-specificity': null,
    'selector-pseudo-class-no-unknown': [
      true,
      {
        ignorePseudoClasses: ['deep']
      }
    ],
    'property-no-unknown': [
      true,
      {
        ignoreProperties: ['line-clamp']
      }
    ]
  },
  overrides: [
    {
      files: ['*.vue', '**/*.vue'],
      rules: {
        'selector-pseudo-class-no-unknown': [
          true,
          {
            ignorePseudoClasses: ['deep', 'global']
          }
        ]
      }
    }
  ]
}