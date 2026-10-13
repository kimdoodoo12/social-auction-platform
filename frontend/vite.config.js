import react, { reactCompilerPreset } from '@vitejs/plugin-react'
import babel from '@rolldown/plugin-babel'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  server: {
    port: 5173,
    strictPort: true,
    proxy: Object.fromEntries(['/ieum', '/api', '/images', '/organization'].map(path => [path, { target: 'http://localhost:8080', changeOrigin: true }])),
  },
  plugins: [
    react(),
    babel({ presets: [reactCompilerPreset()] })
  ],
})
