<template>
  <div class="app-root">
    <router-view />
  </div>
</template>

<script setup>
// App root
</script>

<style>
/* Global Resets */
body {
  margin: 0;
  padding: 0;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background-color: #f8f9fa; /* Default bg */
}

/* Ensure full height */
#app, .app-root {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}

/* Scrollbar Styles */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background-color: rgba(128, 128, 128, 0.3);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background-color: rgba(128, 128, 128, 0.5);
}
</style>
