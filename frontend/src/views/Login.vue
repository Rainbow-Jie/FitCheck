<template>
  <div class="login-page">
    <!-- 左侧大图区 -->
    <div class="login-hero">
      <div class="hero-grid"></div>
      <div class="hero-dot" v-for="i in 5" :key="i"></div>
      <div class="hero-content">
        <div class="hero-logo-wrap">💪</div>
        <div class="hero-brand">FitCheck</div>
        <div class="hero-tagline">坚持，是最好的健身方法</div>
        <div class="hero-title-main">打卡每一天</div>
        <div class="hero-title-outline">成就更好的自己</div>
        <p class="hero-desc">记录每一次努力，见证每一步成长。<br>你的健身旅程，从这里开始。</p>
        <div class="hero-features">
          <div class="hero-feat-item" v-for="feat in features" :key="feat.icon">
            <span class="feat-emoji">{{ feat.icon }}</span>
            <div>
              <div class="feat-title">{{ feat.title }}</div>
              <div class="feat-sub">{{ feat.sub }}</div>
            </div>
          </div>
        </div>
        <div class="hero-stats">
          <div class="hero-av-stack">
            <div class="hero-av" v-for="av in avatars" :key="av.letter" :style="{ background: av.color }">{{ av.letter }}</div>
          </div>
          <span class="hero-stats-text"><strong>2,847+</strong> 位运动者正在坚持</span>
        </div>
      </div>
    </div>

    <!-- 右侧表单区 -->
    <div class="login-form-side">
      <div class="form-deco top"></div>
      <div class="form-deco bottom"></div>
      <div class="form-inner">
        <h2 class="welcome-title">{{ isLogin ? '欢迎回来 👋' : '加入我们 🎉' }}</h2>
        <p class="welcome-sub">{{ isLogin ? '继续你的健身旅程' : '开启你的打卡之旅' }}</p>

        <div class="form-tabs">
          <div class="form-tab" :class="{ active: isLogin }" @click="isLogin = true">登录</div>
          <div class="form-tab" :class="{ active: !isLogin }" @click="isLogin = false">注册</div>
        </div>

        <Transition name="slide" mode="out-in">
          <div v-if="isLogin" key="login">
            <div class="field">
              <label>用户名</label>
              <div class="input-wrap">
                <span class="iico">👤</span>
                <input v-model="loginForm.username" type="text" placeholder="请输入用户名" />
              </div>
            </div>
            <div class="field">
              <label>手机号后四位</label>
              <div class="input-wrap">
                <span class="iico">📱</span>
                <input v-model="loginForm.phoneLast4" type="text" maxlength="4" placeholder="请输入手机号后四位" />
              </div>
              <p class="field-hint">用于区分同名账号</p>
            </div>
            <div class="field">
              <label>密码</label>
              <div class="input-wrap">
                <span class="iico">🔒</span>
                <input v-model="loginForm.password" :type="showPwd ? 'text' : 'password'" placeholder="请输入密码" autocomplete="current-password" />
                <span class="eye-btn" @click="showPwd = !showPwd">{{ showPwd ? '🙈' : '👁️' }}</span>
              </div>
            </div>
            <button class="submit-btn" :disabled="loading" @click="handleLogin">
              <span v-if="!loading">立即登录</span>
              <span v-else class="dot-loading"><i></i><i></i><i></i></span>
            </button>
            <p class="switch-hint">没有账号？<a @click.prevent="isLogin = false">立即注册</a></p>
          </div>

          <div v-else key="register">
            <div class="field">
              <label>用户名</label>
              <div class="input-wrap">
                <span class="iico">👤</span>
                <input v-model="registerForm.username" type="text" placeholder="支持中文/字母/数字，2-20个字符" />
              </div>
            </div>
            <div class="field">
              <label>手机号</label>
              <div class="input-wrap" :class="{ error: phoneError }">
                <span class="iico">📱</span>
                <input v-model="registerForm.phone" type="tel" maxlength="11" placeholder="请输入11位手机号" />
              </div>
              <p v-if="phoneError" class="error-tip">{{ phoneError }}</p>
            </div>
            <div class="field">
              <label>密码</label>
              <div class="input-wrap">
                <span class="iico">🔒</span>
                <input v-model="registerForm.password" type="password" placeholder="6-20位密码" />
              </div>
            </div>
            <div class="field">
              <label>确认密码</label>
              <div class="input-wrap" :class="{ error: pwdMismatch }">
                <span class="iico">✅</span>
                <input v-model="registerForm.confirmPassword" type="password" placeholder="再次输入密码" />
              </div>
              <p v-if="pwdMismatch" class="error-tip">两次密码不一致</p>
            </div>
            <button class="submit-btn" :disabled="loading" @click="handleRegister">
              <span v-if="!loading">立即注册</span>
              <span v-else class="dot-loading"><i></i><i></i><i></i></span>
            </button>
            <p class="switch-hint">已有账号？<a @click.prevent="isLogin = true">立即登录</a></p>
          </div>
        </Transition>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast } from 'vant'
import { authApi } from '@/api/auth'

const router = useRouter()
const route = useRoute()

const isLogin = ref(true)
const loading = ref(false)
const showPwd = ref(false)

const loginForm = reactive({ username: '', phoneLast4: '', password: '' })
const registerForm = reactive({ username: '', phone: '', password: '', confirmPassword: '' })

const pwdMismatch = computed(() =>
  registerForm.confirmPassword && registerForm.password !== registerForm.confirmPassword
)

const phoneError = computed(() => {
  if (!registerForm.phone) return ''
  if (!/^1[3-9]\d{9}$/.test(registerForm.phone)) return '手机号格式不正确，请输入11位手机号'
  return ''
})

const features = [
  { icon: '🔥', title: '每日打卡', sub: '坚持不懈，习惯成自然' },
  { icon: '📊', title: '数据统计', sub: '量化进步，看见成长' },
  { icon: '🏆', title: '排行榜', sub: '与好友竞争，共同进步' }
]
const avatars = [
  { letter: 'Z', color: '#EC4899' },
  { letter: 'A', color: '#3B82F6' },
  { letter: 'M', color: '#10B981' },
  { letter: '+', color: 'rgba(255,255,255,0.25)' }
]

async function handleLogin() {
  if (!loginForm.username || !loginForm.phoneLast4 || !loginForm.password) {
    showToast('请填写完整信息'); return
  }
  if (!/^\d{4}$/.test(loginForm.phoneLast4)) {
    showToast('手机号后四位须为4位数字'); return
  }
  loading.value = true
  try {
    const res = await authApi.login(loginForm)
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('user', JSON.stringify({
      id: res.data.userId,
      username: res.data.username,
      avatar: res.data.avatar || ''
    }))
    showToast('登录成功 🎉')
    const redirect = route.query.redirect || '/home'
    router.push(redirect)
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  if (!registerForm.username || !registerForm.phone || !registerForm.password || !registerForm.confirmPassword) {
    showToast('请填写完整信息'); return
  }
  if (phoneError.value) { showToast(phoneError.value); return }
  if (pwdMismatch.value) { showToast('两次密码不一致'); return }
  loading.value = true
  try {
    await authApi.register(registerForm)
    showToast('注册成功，请登录 🎉')
    isLogin.value = true
    loginForm.username = registerForm.username
    registerForm.username = ''
    registerForm.phone = ''
    registerForm.password = ''
    registerForm.confirmPassword = ''
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
}

/* ===== 左侧大图 ===== */
.login-hero {
  flex: 1;
  min-height: 100vh;
  background: linear-gradient(135deg, #C2410C 0%, #EA580C 40%, #F97316 75%, #FB923C 100%);
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 44px;
}
.hero-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255,255,255,0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255,255,255,0.04) 1px, transparent 1px);
  background-size: 40px 40px;
}
.login-hero::before {
  content: '';
  position: absolute;
  top: -100px; left: -100px;
  width: 380px; height: 380px;
  background: radial-gradient(circle, rgba(255,255,255,0.12) 0%, transparent 70%);
  border-radius: 50%;
  animation: orbFloat 7s ease-in-out infinite;
}
.login-hero::after {
  content: '';
  position: absolute;
  bottom: -60px; right: -60px;
  width: 280px; height: 280px;
  background: radial-gradient(circle, rgba(255,255,255,0.08) 0%, transparent 70%);
  border-radius: 50%;
  animation: orbFloat 9s ease-in-out infinite reverse;
}
@keyframes orbFloat {
  0%, 100% { transform: translate(0,0) scale(1); }
  50% { transform: translate(16px,-16px) scale(1.04); }
}
.hero-dot {
  position: absolute;
  border-radius: 50%;
  background: rgba(255,255,255,0.18);
  animation: dotFloat linear infinite;
}
.hero-dot:nth-child(2) { width:8px;height:8px;top:14%;left:9%;animation-duration:4s; }
.hero-dot:nth-child(3) { width:5px;height:5px;top:28%;left:82%;animation-duration:6s;animation-delay:-2s; }
.hero-dot:nth-child(4) { width:10px;height:10px;top:62%;left:13%;animation-duration:5s;animation-delay:-1s; }
.hero-dot:nth-child(5) { width:6px;height:6px;top:78%;left:76%;animation-duration:7s;animation-delay:-3s; }
.hero-dot:nth-child(6) { width:4px;height:4px;top:44%;left:52%;animation-duration:3s;animation-delay:-1.5s; }
@keyframes dotFloat {
  0%,100% { transform:translateY(0); opacity:.6; }
  50% { transform:translateY(-16px); opacity:1; }
}
.hero-content {
  position: relative;
  z-index: 2;
  color: #fff;
  text-align: center;
  max-width: 380px;
}
.hero-logo-wrap {
  width: 76px; height: 76px;
  background: rgba(255,255,255,0.15);
  border: 1.5px solid rgba(255,255,255,0.3);
  border-radius: 22px;
  display: flex; align-items: center; justify-content: center;
  font-size: 38px;
  margin: 0 auto 18px;
  backdrop-filter: blur(8px);
}
.hero-brand {
  font-size: 32px; font-weight: 800; letter-spacing: 2px; margin-bottom: 6px;
}
.hero-tagline {
  font-size: 14px; opacity: .72; margin-bottom: 28px;
}
.hero-title-main {
  font-size: 26px; font-weight: 800; line-height: 1.3; margin-bottom: 4px;
}
.hero-title-outline {
  font-size: 24px; font-weight: 800; line-height: 1.3; margin-bottom: 20px;
  -webkit-text-stroke: 2px rgba(255,255,255,.75);
  color: transparent;
}
.hero-desc {
  font-size: 13px; opacity: .68; line-height: 1.9; margin-bottom: 28px;
}
.hero-features {
  display: flex; flex-direction: column; gap: 10px; margin-bottom: 28px;
}
.hero-feat-item {
  display: flex; align-items: center; gap: 12px;
  background: rgba(255,255,255,0.1);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255,255,255,0.15);
  border-radius: 12px;
  padding: 11px 15px;
  text-align: left;
}
.feat-emoji { font-size: 20px; flex-shrink: 0; }
.feat-title { font-size: 13px; font-weight: 600; }
.feat-sub { font-size: 12px; opacity: .62; margin-top: 1px; }
.hero-stats {
  display: flex; align-items: center; justify-content: center; gap: 10px;
}
.hero-av-stack { display: flex; }
.hero-av {
  width: 30px; height: 30px; border-radius: 50%;
  border: 2px solid rgba(255,255,255,.8);
  display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: 700; color: #fff;
  margin-left: -7px;
}
.hero-av:first-child { margin-left: 0; }
.hero-stats-text { font-size: 13px; opacity: .78; }
.hero-stats-text strong { opacity: 1; font-weight: 700; }

/* ===== 右侧表单 ===== */
.login-form-side {
  width: 420px;
  min-height: 100vh;
  background: #fff;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 48px 40px;
  position: relative;
  overflow: hidden;
}
.form-deco {
  position: absolute;
  border-radius: 50%;
}
.form-deco.top {
  top: -60px; right: -60px;
  width: 180px; height: 180px;
  background: radial-gradient(circle, rgba(249,115,22,.09), transparent 70%);
}
.form-deco.bottom {
  bottom: -40px; left: -40px;
  width: 150px; height: 150px;
  background: radial-gradient(circle, rgba(249,115,22,.06), transparent 70%);
}
.form-inner { position: relative; z-index: 1; }
.welcome-title {
  font-size: 26px; font-weight: 800; color: #1a1a2e; margin-bottom: 6px;
}
.welcome-sub {
  font-size: 14px; color: #999; margin-bottom: 28px;
}
.form-tabs {
  display: flex;
  background: #fff4ed;
  border-radius: 12px;
  padding: 4px;
  margin-bottom: 26px;
}
.form-tab {
  flex: 1; text-align: center;
  padding: 11px; border-radius: 10px;
  font-size: 15px; font-weight: 500;
  color: #888; cursor: pointer;
  transition: all .3s;
}
.form-tab.active {
  background: #F97316; color: #fff;
  font-weight: 600;
  box-shadow: 0 4px 14px rgba(249,115,22,.35);
}
.field { margin-bottom: 18px; }
.field label {
  display: block;
  font-size: 13px; font-weight: 600;
  color: #555; margin-bottom: 8px;
}
.input-wrap {
  position: relative; display: flex; align-items: center;
  border: 1.5px solid #ffe4cc; border-radius: 12px;
  background: #fafafa;
  transition: border-color .25s, box-shadow .25s;
}
.input-wrap:focus-within {
  border-color: #F97316;
  background: #fff;
  box-shadow: 0 0 0 4px rgba(249,115,22,.1);
}
.input-wrap.error { border-color: #f87171; }
.iico {
  padding: 0 10px 0 13px; font-size: 16px;
  pointer-events: none; flex-shrink: 0;
}
.input-wrap input {
  flex: 1; padding: 13px 8px 13px 0;
  border: none; outline: none; background: transparent;
  font-size: 15px; color: #333;
}
.input-wrap input::placeholder { color: #d4b8a0; }
/* 隐藏浏览器原生密码小眼睛（IE/Edge/Chrome） */
.input-wrap input::-ms-reveal,
.input-wrap input::-ms-clear { display: none; }
.input-wrap input::-webkit-credentials-auto-fill-button { display: none !important; }
.eye-btn {
  padding: 0 13px; font-size: 16px; cursor: pointer;
}
.error-tip {
  font-size: 12px; color: #f87171; margin-top: 5px;
}
.field-hint {
  font-size: 11px; color: #bbb; margin-top: 4px;
}
.submit-btn {
  width: 100%; padding: 15px;
  background: linear-gradient(135deg, #C2410C, #F97316, #FB923C);
  border: none; border-radius: 12px;
  color: #fff; font-size: 16px; font-weight: 700;
  cursor: pointer; margin-top: 6px;
  transition: all .3s;
  box-shadow: 0 6px 20px rgba(249,115,22,.4);
  letter-spacing: 1px;
}
.submit-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 8px 24px rgba(249,115,22,.5);
}
.submit-btn:active:not(:disabled) { transform: scale(.98); }
.submit-btn:disabled { opacity: .65; cursor: not-allowed; }
.dot-loading {
  display: inline-flex; gap: 5px; align-items: center;
}
.dot-loading i {
  display: block; width: 7px; height: 7px;
  background: #fff; border-radius: 50%;
  animation: dotBounce .8s ease-in-out infinite;
}
.dot-loading i:nth-child(2) { animation-delay: .16s; }
.dot-loading i:nth-child(3) { animation-delay: .32s; }
@keyframes dotBounce {
  0%,80%,100% { transform: scale(.8); opacity:.5; }
  40% { transform: scale(1.1); opacity:1; }
}
.switch-hint {
  margin-top: 18px; text-align: center; font-size: 14px; color: #aaa;
}
.switch-hint a {
  color: #F97316; font-weight: 600; cursor: pointer;
}
.switch-hint a:hover { text-decoration: underline; }

/* Transition */
.slide-enter-active, .slide-leave-active { transition: all .25s ease; }
.slide-enter-from { opacity: 0; transform: translateX(20px); }
.slide-leave-to  { opacity: 0; transform: translateX(-20px); }

/* 移动端 */
@media (max-width: 860px) {
  .login-hero { display: none; }
  .login-form-side { width: 100%; padding: 40px 28px; }
}
</style>
