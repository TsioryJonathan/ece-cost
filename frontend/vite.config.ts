/// <reference types="vitest/config" />
import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, '.', '');
  return {
    plugins: [react()],
    server: {
      proxy: { '/api': env.API_TARGET ?? 'http://localhost:8080' },
    },
    test: {
      environment: 'jsdom',
      setupFiles: ['./src/test/setup.ts'],
    },
  };
});
