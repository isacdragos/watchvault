import fs from 'node:fs'
import path from 'node:path'
import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

const certificatePath = path.resolve(
  'watchvault-backend-springboot',
  'src',
  'main',
  'resources',
  'ssl',
  'watchvault-local.p12',
)

const hasCertificate = fs.existsSync(certificatePath)

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const backendProxyTarget =
    env.BACKEND_PROXY_TARGET || env.VITE_API_BASE_URL || 'https://localhost:8443'

  return {
    plugins: [react()],
    server: hasCertificate
      ? {
          host: '0.0.0.0',
          port: 5173,
          https: {
            pfx: fs.readFileSync(certificatePath),
            passphrase: 'watchvault',
          },
          proxy: {
            '/api': {
              target: backendProxyTarget,
              changeOrigin: true,
              secure: false,
            },
          },
        }
      : {
          host: '0.0.0.0',
          port: 5173,
          proxy: {
            '/api': {
              target: backendProxyTarget,
              changeOrigin: true,
              secure: false,
            },
          },
        },
  }
})
