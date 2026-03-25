<template>
  <div class="register-page">
    <div class="register-header">
      <van-icon name="arrow-left" class="back-btn" @click="goBack" />
      <h1>注册账号</h1>
      <p>加入FitCheck，开始健身之旅</p>
    </div>
    
    <div class="register-form">
      <van-form @submit="handleRegister">
        <van-cell-group inset>
          <van-field
            v-model="formData.username"
            name="username"
            label="用户名"
            placeholder="4-20位字母或数字"
            :rules="[
              { required: true, message: '请输入用户名' },
              { pattern: /^[a-zA-Z0-9]{4,20}$/, message: '用户名需4-20位字母或数字' }
            ]"
          />
          <van-field
            v-model="formData.password"
            type="password"
            name="password"
            label="密码"
            placeholder="6-20位密码"
            :rules="[
              { required: true, message: '请输入密码' },
              { pattern: /^.{6,20}$/, message: '密码需6-20位' }
            ]"
          />
          <van-field
            v-model="formData.confirmPassword"
            type="password"
            name="confirmPassword"
            label="确认密码"
            placeholder="再次输入密码"
            :rules="[
              { required: true, message: '请确认密码' },
              { validator: validateConfirm, message: '两次密码不一致' }
            ]"
          />
        </van-cell-group>
        
        <div class="form-actions">
          <van-button round block type="primary" native-type="submit" :loading="loading" class="register-btn">
            注册
          </van-button>
          <div class="login-link">
            已有账号？<span @click="goToLogin">立即登录</span>
          </div>
        </div>
      </van-form>
    </div>
    
    <div class="register-tips">
      <div class="tip-item">
        <van-icon name="shield-o" />
        <span>密码加密存储</span>
      </div>
      <div class="tip-item">
        <van-icon name="lock-o" />
        <span>数据安全保护</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showSuccessToast } from 'vant'
import { authApi } from '@/api/auth'

const router = useRouter()
const loading = ref(false)
const formData = ref({
  username: '',
  password: '',
  confirmPassword: ''
})

const validateConfirm = (value) => {
  return value === formData.value.password
}

const handleRegister = async () => {
  if (formData.value.password !== formData.value.confirmPassword) {
    showToast('两次密码不一致')
    return
  }
  
  loading.value = true
  try {
    await authApi.register({
      username: formData.value.username,
      password: formData.value.password,
      confirmPassword: formData.value.confirmPassword
    })
    showSuccessToast({ message: '注册成功', forbidClick: true })
    setTimeout(() => {
      router.replace('/login')
    }, 500)
  } catch (error) {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style lang="scss" scoped>
.register-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #FF6B35 0%, #FF8F5C 50%, #FFB088 100%);
  display: flex;
  flex-direction: column;
}

.register-header {
  padding: 40px 20px 24px;
  color: #fff;
  position: relative;
  
  .back-btn {
    font-size: 24px;
    margin-bottom: 16px;
  }
  
  h1 {
    font-size: 28px;
    font-weight: 700;
    margin-bottom: 8px;
  }
  
  p {
    font-size: 14px;
    opacity: 0.9;
  }
}

.register-form {
  flex: 1;
  background: #fff;
  border-radius: 32px 32px 0 0;
  padding: 32px 16px;
}

.form-actions {
  padding: 24px 16px 0;
  
  .register-btn {
    background: linear-gradient(135deg, #FF6B35 0%, #FF8F5C 100%);
    border: none;
    font-size: 16px;
    height: 48px;
  }
}

.login-link {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: #666;
  
  span {
    color: #FF6B35;
    font-weight: 600;
  }
}

.register-tips {
  display: flex;
  justify-content: center;
  gap: 24px;
  padding: 16px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 12px;
  
  .tip-item {
    display: flex;
    align-items: center;
    gap: 4px;
    
    .van-icon {
      font-size: 16px;
    }
  }
}

:deep(.van-cell-group--inset) {
  margin: 0;
}
</style>
