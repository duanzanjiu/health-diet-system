import vue from '@vitejs/plugin-vue'
import { defineConfig } from 'vite'
import {resolve} from 'path'
// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  server:{
    proxy:{
      '/api':{
        target:'http://127.0.0.1:5500',
        changeOrigin:true,
      },
      '/uploads':{
        target:'http://127.0.0.1:5500',
        changeOrigin:true,
      }
    }
  }
})
