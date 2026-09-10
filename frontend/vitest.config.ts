import {defineConfig} from 'vitest/config';

export default defineConfig({
    test: {
        include: ['src/**/*.test.ts'],
        environment: 'node',
        coverage: {
            provider: 'v8',
            reporter: ['text', 'html'],
            include: ['src/lib/api.ts'],
            thresholds: {
                lines: 95,
                functions: 95,
                branches: 90,
                statements: 95
            }
        }
    }
});
