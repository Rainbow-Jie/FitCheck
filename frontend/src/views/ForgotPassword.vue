<template>
  <div class="forgot-page">
    <div class="forgot-header">
      <van-icon name="arrow-left" class="back-btn" @click="goBack" />
      <h1>忘记密码</h1>
      <p>验证身份后重置密码</p>
    </div>
    
    <div class="forgot-form">
      <!-- Step 1: 输入用户名 -->
      <div v-if="step === 1" class="step-content">
        <div class="step-header">
          <div class="step-number">1</div>
          <span>输入用户名</span>
        </div>
        
        <van-form @submit="handleSendCode">
          <div class="form-group">
            <label>用户名</label>
            <div class="input-wrapper">
              <van-icon name="user-o" class="input-icon" />
              <input 
                v-model="formData.username" 
                type="text" 
                placeholder="请输入您的用户名"
                class="custom-input"
              />
            </div>
          </div>
          
          <button type="submit" class="submit-btn" :disabled="loading">
            <span v-if="!loading">发送验证码</span>
            <van-loading v-else type="spinner" size="20px" color="#fff" />
          </button>
        </van-form>
      </div>
      
      <!-- Step 2: 输入验证码和新密码 -->
      <div v-if="step === 2" class="step-content">
        <div class="step-header">
          <div class="step-number">2</div>
          <span>验证并重置</span>
        </div>
        
        <div class="code-tip">
          <van-icon name="info-o" />
          <span>验证码已发送，请查收（开发环境显示在控制台）</span>
        </div>
        
        <van-form @submit="handleResetPassword">
          <div class="form-group">
            <label>验证码</label>
            <div class="input-wrapper code-input">
              <van-icon name="shield-o" class="input-icon" />
              <input 
                v-model="formData.code" 
                type="text" 
                placeholder="请输入6位验证码"
                maxlength="6"
                class="custom-input"
              />
              <button 
                type="button" 
                class="resend-btn" 
                :disabled="countdown > 0"
                @click="resendCode"
              >
                {{ countdown > 0 ? `${countdown}s` : '重新获取' }}
              </button>
            </div>
          </div>
          
          <div class="form-group">
            <label>新密码</label>
            <div class="input-wrapper">
              <van-icon name="lock" class="input-icon" />
              <input 
                v-model="formData.newPassword" 
                :type="showPassword ? 'text' : 'password'"
                placeholder="6-20位密码"
                class="custom-input"
              />
              <van-icon 
                :name="showPassword ? 'eye-o' : 'closed-eye'" 
                class="eye-icon"
                @click="showPassword = !showPassword"
              />
            </div>
          </div>
          
          <div class="form-group">
            <label>确认密码</label>
            <div class="input-wrapper">
              <van-icon name="lock" class="input-icon" />
              <input 
                v-model="formData.confirmPassword" 
                :type="showPassword ? 'text' : 'password'"
                placeholder="再次输入新密码"
                class="custom-input"
              />
            </div>
          </div>
          
          <button type="submit" class="submit-btn" :disabled="loading">
            <span v-if="!loading">重置密码</span>
            <van-loading v-else type="spinner" size="20px" color="#fff" />
          </button>
        </van-form>
      </div>
      
      <!-- Step 3: 成功 -->
      <div v-if="step === 3" class="step-content success-content">
        <div class="success-icon">
          <van-icon name="checked" />
        </div>
        <h2>密码重置成功</h2>
        <p>请使用新密码登录</p>
        <button class="submit-btn" @click="goToLogin">返回登录</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showSuccessToast } from 'vant'
import { authApi } from '@/api/auth'

const router = useRouter()
const step = ref(1)
const loading = ref(false)
const countdown = ref(0)
const showPassword = ref(false)
const formData = ref({
  username: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

let countdownTimer = null

const goBack = () => {
  if (step.value === 1) {
    router.back()
  } else if (step.value === 2) {
    step.value = 1
  }
}

const handleSendCode = async () => {
  if (!formData.value.username) {
    showToast('请输入用户名')
    return
  }
  
  loading.value = true
  try {
    await authApi.sendCode(formData.value.username)
    showSuccessToast({ message: '验证码已发送', forbidClick: true })
    step.value = 2
    startCountdown()
  } catch (error) {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
}

const resendCode = async () => {
  if (countdown.value > 0) return
  
  loading.value = true
  try {
    await authApi.sendCode(formData.value.username)
    showSuccessToast({ message: '验证码已重新发送', forbidClick: true })
    startCountdown()
  } catch (error) {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
}

const startCountdown = () => {
  countdown.value = 60
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(countdownTimer)
    }
  }, 1000)
}

const handleResetPassword = async () => {
  if (!formData.value.code) {
    showToast('请输入验证码')
    return
  }
  if (formData.value.code.length !== 6) {
    showToast('验证码为6位数字')
    return
  }
  if (!formData.value.newPassword) {
    showToast('请输入新密码')
    return
  }
  if (formData.value.newPassword.length < 6 || formData.value.newPassword.length > 20) {
    showToast('密码长度需6-20位')
    return
  }
  if (formData.value.newPassword !== formData.value.confirmPassword) {
    showToast('两次密码不一致')
    return
  }
  
  loading.value = true
  try {
    await authApi.resetPassword({
      username: formData.value.username,
      code: formData.value.code,
      newPassword: formData.value.newPassword
    })
    step.value = 3
    clearInterval(countdownTimer)
  } catch (error) {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
}

const goToLogin = () => {
  router.replace('/login')
}

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})
</script>

<style lang="scss" scoped>
.forgot-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #FF9A56 0%, #FF7B54 100%);
}

.forgot-header {
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

.forgot-form {
  flex: 1;
  background: #fff;
  border-radius: 32px 32px 0 0;
  padding: 32px 24px;
  min-height: calc(100vh - 200px);
}

.step-content {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.step-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  
  .step-number {
    width: 32px;
    height: 32px;
    background: linear-gradient(135deg, #FF9A56 0%, #FF7B54 100%);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-weight: 600;
    font-size: 14px;
  }
  
  span {
    font-size: 16px;
    font-weight: 600;
    color: #333;
  }
}

.code-tip {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 12px;
  background: #fff5f0;
  border-radius: 8px;
  margin-bottom: 24px;
  font-size: 13px;
  color: #FF7B54;
  
  .van-icon {
    font-size: 16px;
    margin-top: 2px;
  }
}

.form-group {
  margin-bottom: 20px;
  
  label {
    display: block;
    font-size: 14px;
    font-weight: 500;
    color: #333;
    margin-bottom: 8px;
  }
}

.input-wrapper {
  display: flex;
  align-items: center;
  border: 1px solid #e5e5e5;
  border-radius: 12px;
  padding: 0 16px;
  transition: all 0.3s;
  
  &:focus-within {
    border-color: #FF7B54;
    box-shadow: 0 0 0 3px rgba(255,123,84,0.1);
  }
  
  &.code-input {
    padding-right: 8px;
  }
  
  .input-icon {
    color: #999;
    font-size: 20px;
  }
  
  .custom-input {
    flex: 1;
    border: none;
    outline: none;
    padding: 14px 12px;
    font-size: 15px;
    background: transparent;
    
    &::placeholder { color: #bbb; }
  }
  
  .eye-icon {
    color: #999;
    cursor: pointer;
    padding: 4px;
  }
  
  .resend-btn {
    background: transparent;
    border: none;
    color: #FF7B54;
    font-size: 13px;
    padding: 8px 12px;
    cursor: pointer;
    
    &:disabled {
      color: #ccc;
    }
  }
}

.submit-btn {
  width: 100%;
  padding: 16px;
  background: linear-gradient(135deg, #FF9A56 0%, #FF7B54 100%);
  border: none;
  border-radius: 12px;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  margin-top: 24px;
  
  &:hover { transform: translateY(-2px); box-shadow: 0 8px 20px rgba(255,123,84,0.3); }
  &:disabled { opacity: 0.7; transform: none; }
}

.success-content {
  text-align: center;
  padding-top: 40px;
  
  .success-icon {
    width: 80px;
    height: 80px;
    background: linear-gradient(135deg, #2ec4b6 0%, #3dd5c8 100%);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 24px;
    
    .van-icon {
      font-size: 40px;
      color: #fff;
    }
  }
  
  h2 {
    font-size: 22px;
    font-weight: 700;
    color: #333;
    margin-bottom: 8px;
  }
  
  p {
    font-size: 14px;
    color: #999;
    margin-bottom: 32px;
  }
}
</style>
