import { fileURLToPath, URL } from "url";

import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";

const target = "http://127.0.0.1:8050";

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
    },
  },
  transpileDependencies: true,
  build: {
    emptyOutDir: true,
    outDir: "../src/main/resources/static",
    rollupOptions: {
      treeshake:  false 
    },
    commonjsOptions: {
      ignoreTryCatch: false
    }
  },
  server: {
    port: 8052,
    proxy: {
      "^/api": {
        target,
        changeOrigin: true,
      },
    },
  },
});
