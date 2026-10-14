import react, { reactCompilerPreset } from '@vitejs/plugin-react'
import babel from '@rolldown/plugin-babel'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  server: {
    port: 5173,
    strictPort: true,
    // 백엔드로 넘길 경로. '^/bid/'는 입찰 API(BidController)만 잡도록 정규식으로 쓴다.
    proxy: Object.fromEntries(['/ieum', '/api', '/images', '/organization', '^/bid/'].map(path => [path, { target: 'http://localhost:8080', changeOrigin: true }])),
  },
  plugins: [
    react(),
    babel({ presets: [reactCompilerPreset()] })
  ],
})
