<template>
  <nav class="bottom-nav">
    <router-link
      v-for="item in navItems"
      :key="item.to"
      :to="item.to"
      class="nav-item"
      :class="{ active: $route.path === item.to }"
    >
      <span class="nav-icon">{{ item.icon }}</span>
      <span class="nav-text">{{ item.label }}</span>
    </router-link>
  </nav>
</template>

<script setup>
const navItems = [
  { to: '/home', icon: '🏠', label: '首页' },
  { to: '/rank', icon: '🏆', label: '排行' },
  { to: '/profile', icon: '👤', label: '我的' }
]
</script>

<style scoped>
.bottom-nav {
  position: fixed;
  bottom: 0; left: 0; right: 0;
  height: 60px;
  background: rgba(255,255,255,0.92);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  display: flex;
  border-top: 1px solid rgba(0,0,0,0.06);
  box-shadow: 0 -4px 20px rgba(0,0,0,0.06);
  z-index: 100;
  padding-bottom: env(safe-area-inset-bottom);
}
.nav-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-decoration: none;
  color: #aaa;
  gap: 2px;
  transition: all .25s;
  position: relative;
}
.nav-item.active { color: #F97316; }
.nav-item.active::after {
  content: '';
  position: absolute;
  bottom: 6px;
  width: 4px; height: 4px;
  border-radius: 50%;
  background: #F97316;
}
.nav-item:active { transform: scale(.92); }
.nav-icon { font-size: 22px; line-height: 1; }
.nav-text { font-size: 11px; font-weight: 500; }
</style>
