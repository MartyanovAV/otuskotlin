import { defineConfig } from 'orval';

export default defineConfig({
  fitbridge: {
    input: '../fit-bridge-be/training-service/specs/specs/specs-training-v2.yaml',
    output: {
      mode: 'tags-split',
      target: 'src/shared/api/generated',
      schemas: 'src/shared/api/generated/models',
      client: 'vue-query',
      override: {
        mutator: {
          path: 'src/shared/api/axios-instance.ts',
          name: 'customInstance',
        },
      },
    },
  },
});
