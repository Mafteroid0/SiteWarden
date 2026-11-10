import { defineConfig } from 'vite'

export default defineConfig({
  server: {
    host: '127.0.0.1',
    port: 5173,
    strictPort: true,
    proxy: {
      '/api/config': {
        target: 'http://127.0.0.1:8080',
        rewrite: path => path.replace(/^\/api\/config/, '')
      },
      '/api/results': {
        target: 'http://127.0.0.1:8081',
        rewrite: path => path.replace(/^\/api\/results/, '')
      }
    }
  }
})
