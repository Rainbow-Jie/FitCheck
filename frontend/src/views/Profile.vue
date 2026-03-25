<template>
  <div class="profile-page">
    <!-- 头部 -->
    <div class="profile-header">
      <svg class="wave-svg" viewBox="0 0 375 60" preserveAspectRatio="none">
        <path d="M0,20 C120,60 250,0 375,30 L375,60 L0,60 Z" fill="rgba(255,255,255,0.08)"/>
      </svg>
      <div class="header-bg-orb"></div>
      <img :src="avatarUrl" class="profile-avatar" @click="triggerAvatarUpload" />
      <div class="avatar-edit-hint">点击更换头像</div>
      <div class="profile-name">{{ user.username }}</div>
      <!-- 手机号脱敏展示 -->
      <div class="profile-phone" v-if="user.phone">📱 {{ maskedPhone }}</div>
      <input ref="fileInput" type="file" accept="image/*" style="display:none" @change="handleAvatarChange" />

      <!-- 统计 -->
      <div class="stats-row">
        <div class="stat-item">
          <div class="stat-num">{{ stats.totalDays || 0 }}</div>
          <div class="stat-lbl">累计打卡</div>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <div class="stat-num">{{ stats.rank || '-' }}</div>
          <div class="stat-lbl">当前排名</div>
        </div>
      </div>
    </div>

    <!-- 个人信息卡片 -->
    <div class="info-section">
      <div class="info-card">
        <div class="info-item">
          <span class="info-icon">👤</span>
          <span class="info-lbl">性别</span>
          <span class="info-val">{{ genderText }}</span>
        </div>
        <div class="info-item">
          <span class="info-icon">🎂</span>
          <span class="info-lbl">生日</span>
          <span class="info-val">{{ user.birthday || '未设置' }}</span>
        </div>
      </div>
    </div>

    <!-- 菜单 -->
    <div class="menu-section">
      <div class="menu-card">
        <div class="menu-item" @click="openEditDialog">
          <div class="menu-icon-wrap" style="background:#fff4ed">✏️</div>
          <span class="menu-label">编辑资料</span>
          <span class="menu-arrow">›</span>
        </div>
        <div class="menu-item" @click="showPwdDialog = true">
          <div class="menu-icon-wrap" style="background:#fff7ed">🔑</div>
          <span class="menu-label">修改密码</span>
          <span class="menu-arrow">›</span>
        </div>
        <div class="menu-item" @click="handleLogout">
          <div class="menu-icon-wrap" style="background:#fef2f2">🚪</div>
          <span class="menu-label logout">退出登录</span>
          <span class="menu-arrow" style="color:#f87171">›</span>
        </div>
      </div>
    </div>

    <!-- 编辑资料弹窗 -->
    <Transition name="fade">
      <div v-if="showEditDialog" class="overlay" @click.self="showEditDialog = false">
        <div class="dialog-card">
          <div class="dialog-title">✏️ 编辑资料</div>

          <!-- 用户名 -->
          <div class="dialog-field">
            <label>用户名</label>
            <div class="input-wrap" :class="{ error: editUsernameError && editForm.username }">
              <span class="iico">👤</span>
              <input v-model="editForm.username" class="dialog-input" placeholder="支持中文/字母/数字，2-20字符" />
            </div>
            <p v-if="editUsernameError && editForm.username" class="error-tip">{{ editUsernameError }}</p>
          </div>

          <!-- 手机号 -->
          <div class="dialog-field">
            <label>手机号</label>
            <div class="input-wrap" :class="{ error: editPhoneError }">
              <span class="iico">📱</span>
              <input v-model="editForm.phone" type="tel" maxlength="11" class="dialog-input" placeholder="11位手机号" />
            </div>
            <p v-if="editPhoneError" class="error-tip">{{ editPhoneError }}</p>
          </div>

          <!-- 性别 -->
          <div class="dialog-field">
            <label>性别</label>
            <div class="gender-row">
              <div class="gender-btn" :class="{ active: editForm.gender === 0 }" @click="editForm.gender = 0">🤷 未知</div>
              <div class="gender-btn" :class="{ active: editForm.gender === 1 }" @click="editForm.gender = 1">👦 男</div>
              <div class="gender-btn" :class="{ active: editForm.gender === 2 }" @click="editForm.gender = 2">👧 女</div>
            </div>
          </div>

          <!-- 生日 -->
          <div class="dialog-field">
            <label>生日</label>
            <div class="input-wrap">
              <span class="iico">🎂</span>
              <input v-model="editForm.birthday" type="date" class="dialog-input" />
            </div>
          </div>

          <div class="dialog-actions">
            <div class="update-quota" :class="{ warn: remainingUpdates <= 1 }">
              本月剩余修改次数：<strong>{{ remainingUpdates }}</strong> / 3
            </div>
            <div class="action-btns">
              <button class="btn-cancel" @click="showEditDialog = false">取消</button>
              <button class="btn-confirm" :disabled="!!editUsernameError || !!editPhoneError || remainingUpdates <= 0" @click="handleEditProfile">
                {{ remainingUpdates <= 0 ? '本月已达上限' : '保存' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </Transition>

    <!-- 修改密码弹窗 -->
    <Transition name="fade">
      <div v-if="showPwdDialog" class="overlay" @click.self="showPwdDialog = false">
        <div class="dialog-card">
          <div class="dialog-title">🔑 修改密码</div>
          <div class="dialog-field">
            <label>旧密码</label>
            <input v-model="pwdForm.oldPassword" type="password" class="dialog-input plain" placeholder="输入旧密码" />
          </div>
          <div class="dialog-field">
            <label>新密码</label>
            <input v-model="pwdForm.newPassword" type="password" class="dialog-input plain" placeholder="输入新密码" />
          </div>
          <div class="dialog-field">
            <label>确认新密码</label>
            <input v-model="pwdForm.confirmPassword" type="password" class="dialog-input plain" placeholder="再次输入新密码" />
          </div>
          <div class="dialog-actions">
            <button class="btn-cancel" @click="showPwdDialog = false">取消</button>
            <button class="btn-confirm" @click="handleChangePwd">确认</button>
          </div>
        </div>
      </div>
    </Transition>

    <BottomNav />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import BottomNav from '@/components/BottomNav.vue'
import { authApi } from '@/api/auth'
import { userApi } from '@/api/user'

const router = useRouter()
const fileInput = ref(null)
const showEditDialog = ref(false)
const showPwdDialog = ref(false)

const user = reactive({ id: null, username: '', avatar: '', phone: '', gender: 0, birthday: '', profileUpdateCount: 0, profileUpdateMonth: null })
const stats = reactive({ totalDays: 0, rank: '-' })
const editForm = reactive({ username: '', phone: '', gender: 0, birthday: '' })
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const avatarUrl = computed(() => {
  const av = user.avatar
  if (!av) return `https://api.dicebear.com/7.x/avataaars/svg?seed=${user.username}`
  if (av.startsWith('http') || av.startsWith('data:')) return av
  // /avatar/xxx.jpg → /fitcheck-api/avatar/xxx.jpg（后端 context-path）
  const apiPath = av.startsWith('/fitcheck-api') ? av : '/fitcheck-api' + av
  const base = import.meta.env.DEV ? 'http://localhost:20001' : ''
  return base + apiPath
})

const maskedPhone = computed(() => {
  if (!user.phone || user.phone.length < 7) return user.phone
  return user.phone.slice(0, 3) + '****' + user.phone.slice(-4)
})

const genderText = computed(() => ['未设置', '男', '女'][user.gender] || '未设置')

const editPhoneError = computed(() => {
  if (!editForm.phone) return ''
  if (!/^1[3-9]\d{9}$/.test(editForm.phone)) return '手机号格式不正确'
  return ''
})

const editUsernameError = computed(() => {
  const v = editForm.username.trim()
  if (!v) return '用户名不能为空'
  if (v.length < 2 || v.length > 20) return '用户名长度 2-20 个字符'
  return ''
})

// 计算本月剩余修改次数
const remainingUpdates = computed(() => {
  const nowMonth = parseInt(
    new Date().toISOString().slice(0, 7).replace('-', '')  // 'YYYYMM'
  )
  const savedMonth = user.profileUpdateMonth || 0
  const count = savedMonth === nowMonth ? (user.profileUpdateCount || 0) : 0
  return Math.max(0, 3 - count)
})

function triggerAvatarUpload() { fileInput.value?.click() }

function openEditDialog() {
  editForm.username = user.username
  editForm.phone = user.phone || ''
  editForm.gender = user.gender || 0
  editForm.birthday = user.birthday || ''
  showEditDialog.value = true
}

async function handleAvatarChange(e) {
  const file = e.target.files[0]
  if (!file) return
  const fd = new FormData()
  fd.append('file', file)
  try {
    const res = await userApi.uploadAvatar(fd)
    console.log('[avatar] 上传返回:', JSON.stringify(res))
    // 后端返回 Result<String>，经过 axios 拦截器后 res = {code,data,message}
    // data 直接是 "/avatar/xxx.jpg" 字符串
    const path = res.data?.url || res.data
    console.log('[avatar] 解析 url:', path)
    if (path) {
      // 开发环境拼接后端地址，生产环境用相对路径（Nginx 统一处理）
      const base = import.meta.env.DEV ? 'http://localhost:20001' : ''
      user.avatar = base + path
      saveLocal()
    }
    showToast('头像更新成功')
  } catch(err) {
    console.error('[avatar] 上传失败:', err)
    showToast('头像上传失败：' + (err.message || '未知错误'))
  }
}

function saveLocal() {
  localStorage.setItem('user', JSON.stringify({
    id: user.id, username: user.username, avatar: user.avatar,
    phone: user.phone, gender: user.gender, birthday: user.birthday
  }))
}

async function handleEditProfile() {
  if (editUsernameError.value) { showToast(editUsernameError.value); return }
  if (editPhoneError.value) { showToast(editPhoneError.value); return }
  try {
    const payload = {
      username: editForm.username,
      phone: editForm.phone,
      gender: editForm.gender,
      birthday: editForm.birthday || null
    }
    const res = await userApi.updateInfo(payload)
    // 用接口返回的最新数据更新本地
    const updated = res.data || {}
    user.username = updated.username || editForm.username
    user.phone = updated.phone || editForm.phone
    user.gender = updated.gender ?? editForm.gender
    user.birthday = updated.birthday || editForm.birthday
    user.profileUpdateCount = updated.profileUpdateCount ?? user.profileUpdateCount
    user.profileUpdateMonth = updated.profileUpdateMonth ?? user.profileUpdateMonth
    saveLocal()
    showEditDialog.value = false
    showToast('修改成功 ✅')
  } catch(e) {
    // 错误 toast 已由 request 拦截器统一弹出，此处只需阻止后续逻辑
  }
}

async function handleChangePwd() {
  if (!pwdForm.oldPassword || !pwdForm.newPassword || !pwdForm.confirmPassword) {
    showToast('请填写完整'); return
  }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) { showToast('两次密码不一致'); return }
  try {
    await authApi.changePassword(pwdForm)
    showPwdDialog.value = false
    pwdForm.oldPassword = ''; pwdForm.newPassword = ''; pwdForm.confirmPassword = ''
    showToast('密码修改成功 🎉')
  } catch(e) {}
}

function handleLogout() {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  showToast('已退出登录')
  router.push('/login')
}

onMounted(async () => {
  const stored = localStorage.getItem('user')
  if (stored) Object.assign(user, JSON.parse(stored))
  try {
    // 从服务端拉最新用户信息（含phone/gender/birthday）
    const [infoRes, statsRes] = await Promise.allSettled([
      userApi.info(),
      userApi.stats()
    ])
    if (infoRes.status === 'fulfilled' && infoRes.value.data) {
      const d = infoRes.value.data
      user.username = d.username || user.username
      user.phone = d.phone || user.phone
      user.gender = d.gender ?? user.gender
      user.birthday = d.birthday || user.birthday
      user.avatar = d.avatar || user.avatar
      user.profileUpdateCount = d.profileUpdateCount ?? user.profileUpdateCount
      user.profileUpdateMonth = d.profileUpdateMonth ?? user.profileUpdateMonth
      saveLocal()
    }
    if (statsRes.status === 'fulfilled') {
      Object.assign(stats, statsRes.value.data || {})
    }
  } catch(e) {}
})
</script>

<style scoped>
.profile-page { min-height: 100vh; background: #f5f6fa; padding-bottom: 70px; }

/* 头部 */
.profile-header {
  background: linear-gradient(135deg, #C2410C 0%, #EA580C 45%, #F97316 80%, #FB923C 100%);
  padding: 36px 18px 28px;
  text-align: center;
  color: #fff;
  position: relative;
  overflow: hidden;
}
.header-bg-orb {
  position: absolute; top: -80px; right: -60px;
  width: 240px; height: 240px; border-radius: 50%;
  background: rgba(255,255,255,.07);
}
.wave-svg { position: absolute; bottom: 0; left: 0; right: 0; width: 100%; height: 60px; }
.profile-avatar {
  width: 82px; height: 82px; border-radius: 50%;
  border: 4px solid rgba(255,255,255,.5);
  object-fit: cover; cursor: pointer;
  transition: transform .2s;
  position: relative; z-index: 1;
}
.profile-avatar:hover { transform: scale(1.04); }
.avatar-edit-hint { font-size: 11px; opacity: .6; margin-top: 4px; }
.profile-name { font-size: 20px; font-weight: 800; margin: 8px 0 2px; }
.profile-phone { font-size: 12px; opacity: .7; margin-bottom: 14px; }
.stats-row {
  display: flex; justify-content: center; align-items: center;
  gap: 0; background: rgba(255,255,255,.12);
  border-radius: 14px; padding: 14px 0;
  backdrop-filter: blur(6px);
  position: relative; z-index: 1;
}
.stat-item { flex: 1; text-align: center; }
.stat-num { font-size: 22px; font-weight: 800; }
.stat-lbl { font-size: 12px; opacity: .72; margin-top: 2px; }
.stat-divider { width: 1px; height: 36px; background: rgba(255,255,255,.25); }

/* 信息卡片 */
.info-section { padding: 14px 16px 0; }
.info-card {
  background: #fff; border-radius: 16px;
  overflow: hidden; box-shadow: 0 4px 14px rgba(0,0,0,.06);
}
.info-item {
  display: flex; align-items: center; padding: 13px 16px;
  border-bottom: 1px solid #f5f5f5; font-size: 14px;
}
.info-item:last-child { border-bottom: none; }
.info-icon { font-size: 16px; width: 24px; flex-shrink: 0; }
.info-lbl { color: #999; flex: 1; margin-left: 8px; }
.info-val { color: #333; font-weight: 500; }

/* 菜单 */
.menu-section { padding: 14px 16px; }
.menu-card {
  background: #fff; border-radius: 18px;
  overflow: hidden; box-shadow: 0 4px 18px rgba(0,0,0,.07);
}
.menu-item {
  display: flex; align-items: center; padding: 16px 18px;
  border-bottom: 1px solid #f5f5f5; cursor: pointer;
  transition: background .15s;
}
.menu-item:last-child { border-bottom: none; }
.menu-item:active { background: #fafafa; }
.menu-icon-wrap {
  width: 36px; height: 36px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  font-size: 18px; margin-right: 14px; flex-shrink: 0;
}
.menu-label { flex: 1; font-size: 15px; color: #333; font-weight: 500; }
.menu-label.logout { color: #f87171; }
.menu-arrow { font-size: 18px; color: #ccc; }

/* 弹窗 */
.overlay {
  position: fixed; inset: 0;
  background: rgba(0,0,0,.5);
  display: flex; align-items: center; justify-content: center;
  z-index: 999;
}
.dialog-card {
  background: #fff; border-radius: 20px;
  padding: 24px 20px; width: 320px; max-height: 90vh; overflow-y: auto;
  animation: popIn .3s ease;
}
@keyframes popIn {
  from { transform: scale(.9); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}
.dialog-title { font-size: 18px; font-weight: 800; color: #1a1a2e; margin-bottom: 18px; text-align: center; }
.dialog-field { margin-bottom: 14px; }
.dialog-field label { display: block; font-size: 13px; font-weight: 600; color: #555; margin-bottom: 6px; }

/* 输入框容器（带图标） */
.input-wrap {
  display: flex; align-items: center;
  border: 1.5px solid #ffe4cc; border-radius: 10px;
  background: #fafafa;
  transition: border-color .2s;
}
.input-wrap:focus-within { border-color: #F97316; background: #fff; box-shadow: 0 0 0 3px rgba(249,115,22,.08); }
.input-wrap.error { border-color: #f87171; }
.iico { padding: 0 10px; font-size: 15px; flex-shrink: 0; }
.dialog-input {
  flex: 1; padding: 11px 12px 11px 0;
  border: none; background: transparent;
  font-size: 14px; outline: none; color: #333;
}
.dialog-input.plain {
  width: 100%; padding: 11px 14px;
  border: 1.5px solid #ffe4cc; border-radius: 10px;
  font-size: 14px; outline: none; transition: all .25s;
  background: #fafafa;
}
.dialog-input.plain:focus { border-color: #F97316; box-shadow: 0 0 0 3px rgba(249,115,22,.08); background: #fff; }
.error-tip { font-size: 11px; color: #f87171; margin-top: 4px; }

/* 性别选择 */
.gender-row { display: flex; gap: 8px; }
.gender-btn {
  flex: 1; padding: 9px 4px; text-align: center;
  border-radius: 10px; font-size: 13px; font-weight: 500;
  background: #f5f5f5; color: #888; cursor: pointer;
  transition: all .2s; border: 2px solid transparent;
}
.gender-btn.active {
  background: #fff4ed; color: #F97316;
  border-color: #F97316; font-weight: 700;
}

.dialog-actions { display: flex; flex-direction: column; gap: 10px; margin-top: 20px; }
.action-btns { display: flex; gap: 10px; }
.update-quota {
  text-align: center; font-size: 12px; color: #aaa;
  background: #f9fafb; border-radius: 8px; padding: 6px 10px;
}
.update-quota strong { color: #F97316; font-size: 14px; }
.update-quota.warn strong { color: #ef4444; }
.update-quota.warn { background: #fff5f5; color: #ef4444; }
.btn-cancel {
  flex: 1; padding: 12px;
  background: #f5f5f5; border: none; border-radius: 10px;
  font-size: 15px; color: #666; cursor: pointer; font-weight: 500;
}
.btn-confirm {
  flex: 1; padding: 12px;
  background: linear-gradient(135deg, #C2410C, #F97316);
  border: none; border-radius: 10px;
  font-size: 15px; color: #fff; cursor: pointer; font-weight: 700;
  box-shadow: 0 4px 12px rgba(249,115,22,.35);
}
.btn-confirm:disabled { opacity: .5; cursor: not-allowed; }

.fade-enter-active, .fade-leave-active { transition: opacity .2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
