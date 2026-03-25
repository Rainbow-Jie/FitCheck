<template>
  <div class="home-page">
    <!-- 加载遮罩 -->
    <Transition name="loading-fade">
      <div v-if="pageLoading" class="page-loading">
        <div class="loading-spinner">
          <div class="spinner-ring"></div>
          <div class="spinner-ring r2"></div>
          <div class="spinner-ring r3"></div>
        </div>
        <div class="loading-text">加载中...</div>
      </div>
    </Transition>
    <!-- 头部 -->
    <div class="home-header">
      <div class="header-bg-circle c1"></div>
      <div class="header-bg-circle c2"></div>
      <!-- 浮动装饰点 -->
      <div class="float-dot fd1"></div>
      <div class="float-dot fd2"></div>
      <div class="float-dot fd3"></div>
      <div class="header-top">
        <div>
          <div class="greeting">{{ greetingText }}，{{ user.username }} 👋</div>
          <div class="time-badge">
            <span class="time-icon">🕐</span>
            <span class="time-main">{{ timeHM }}</span>
            <span class="time-sec">:{{ timeSec }}</span>
          </div>
          <div class="date-text">{{ dateText }}</div>
        </div>
        <div class="avatar-wrap">
          <img :src="avatarUrl" class="header-avatar" />
          <div class="avatar-ring"></div>
        </div>
      </div>
      <!-- 状态条 -->
      <div class="header-status-bar">
        <div class="hsb-item">
          <span class="hsb-num">{{ user.totalDays || 0 }}</span>
          <span class="hsb-lbl">累计打卡</span>
        </div>
        <div class="hsb-divider"></div>
        <div class="hsb-item">
          <span class="hsb-num">{{ user.rank || '-' }}</span>
          <span class="hsb-lbl">排名</span>
        </div>
      </div>
    </div>

    <div class="home-body">
      <!-- 打卡卡片 -->
      <div class="checkin-card">
        <div class="card-top-bar"></div>
        <!-- 已打卡状态 -->
        <!-- 已打卡状态 -->
        <div v-if="user.hasCheckedIn" class="done-banner">
          <div class="done-icon-wrap">
            <span class="done-icon">✅</span>
            <div class="done-ripple r1"></div>
            <div class="done-ripple r2"></div>
          </div>
          <div class="done-texts">
            <div class="done-title">今日已完成打卡！</div>
            <div class="done-sub">✨ 继续保持，明天见！</div>
          </div>
          <div class="done-badge">已打卡</div>
        </div>

        <!-- 未打卡状态 -->
        <template v-else>
          <div class="todo-banner">
            <div class="todo-icon-wrap">
              <span class="todo-icon">💪</span>
              <div class="pulse-ring"></div>
            </div>
            <div class="todo-texts">
              <div class="todo-title">今天还没打卡哦～</div>
              <div class="todo-sub">点击下方按钮，完成今日打卡！</div>
            </div>
          </div>
          <button
            class="checkin-btn"
            :disabled="checkingIn"
            @click="handleCheckIn"
          >
            <span v-if="checkingIn">⏳ 打卡中...</span>
            <span v-else>🔥 立即打卡</span>
          </button>
        </template>

        <!-- 激励语 -->
        <div class="motto-bar">
          <span class="motto-icon">✨</span>
          <span class="motto-text">{{ currentMotto }}</span>
        </div>
      </div>

      <!-- 装饰卡片 - 运动小贴士 -->
      <div class="tips-card">
        <div class="tips-header">
          <span class="tips-title">💡 今日运动小贴士</span>
          <span class="tips-refresh" @click="rotateTip">换一条</span>
        </div>
        <div class="tips-content">{{ currentTip }}</div>
      </div>

      <!-- 日历卡片 -->
      <div class="calendar-card">
        <div class="cal-header">
          <button class="nav-btn" @click="prevMonth">‹</button>
          <span class="cal-title">📅 {{ calendarTitle }}</span>
          <button class="nav-btn" @click="nextMonth">›</button>
        </div>
        <div class="cal-grid">
          <div v-for="wd in weekdays" :key="wd" class="cal-weekday"
            :class="{ 'wd-weekend': wd==='日'||wd==='六' }">{{ wd }}</div>
          <div
            v-for="(day, idx) in calendarDays"
            :key="idx"
            class="cal-day"
            :class="{
              empty: !day,
              checked: day && day.checked,
              today: day && day.isToday,
              weekend: day && (idx % 7 === 0 || idx % 7 === 6)
            }"
          >
            <span v-if="day">{{ day.day }}</span>
            <span v-if="day && day.checked" class="check-dot"></span>
          </div>
        </div>
        <div class="cal-legend">
          <span class="legend-item"><i class="legend-dot checked-dot"></i>已打卡</span>
          <span class="legend-item"><i class="legend-dot today-dot"></i>今天</span>
        </div>
      </div>

      <!-- 成就徽章区 -->
      <div class="badge-card">
        <div class="badge-title">🏅 成就</div>
        <div class="badge-row">
          <div class="badge-item" :class="{ lit: user.totalDays >= 1 }">
            <span class="badge-icon">🌱</span>
            <span class="badge-name">初出茅庐</span>
            <span class="badge-req">打卡1天</span>
          </div>
          <div class="badge-item" :class="{ lit: user.totalDays >= 7 }">
            <span class="badge-icon">🔥</span>
            <span class="badge-name">燃烧意志</span>
            <span class="badge-req">打卡7天</span>
          </div>
          <div class="badge-item" :class="{ lit: user.totalDays >= 30 }">
            <span class="badge-icon">💎</span>
            <span class="badge-name">钻石意志</span>
            <span class="badge-req">打卡30天</span>
          </div>
          <div class="badge-item" :class="{ lit: user.totalDays >= 90 }">
            <span class="badge-icon">🏆</span>
            <span class="badge-name">自律达人</span>
            <span class="badge-req">打卡90天</span>
          </div>
          <div class="badge-item" :class="{ lit: user.totalDays >= 180 }">
            <span class="badge-icon">👑</span>
            <span class="badge-name">半年勇士</span>
            <span class="badge-req">打卡180天</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 打卡成功弹窗 -->
    <Transition name="fade">
      <div v-if="showSuccess" class="overlay" @click.self="showSuccess = false">
        <div class="success-card">
          <div class="confetti-wrap">
            <span v-for="i in 12" :key="i" class="confetti" :style="confettiStyle(i)"></span>
          </div>
          <div class="success-icon">🎉</div>
          <div class="success-title">打卡成功！</div>
          <div class="success-msg">{{ successMsg }}</div>
          <button class="success-close" @click="showSuccess = false">太棒了，继续！</button>
        </div>
      </div>
    </Transition>

    <BottomNav />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, onActivated } from 'vue'
import { showToast } from 'vant'
import BottomNav from '@/components/BottomNav.vue'
import { checkinApi } from '@/api/checkin'
import { userApi } from '@/api/user'

const user = reactive({
  id: null, username: '', avatar: '',
  hasCheckedIn: false, totalDays: 0, rank: '-'
})

const timeHM = ref('')
const timeSec = ref('')
const dateText = ref('')
const checkingIn = ref(false)
const showSuccess = ref(false)
const successMsg = ref('')
const pageLoading = ref(true)
const checkedDates = ref(new Set())
const calendarYear = ref(new Date().getFullYear())
const calendarMonth = ref(new Date().getMonth() + 1)
const weekdays = ['日', '一', '二', '三', '四', '五', '六']
const calendarDays = ref([])
const tipIndex = ref(0)
const mottoIndex = ref(0)

const avatarUrl = computed(() =>
  user.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${user.username}`
)
const calendarTitle = computed(() => `${calendarYear.value}年${calendarMonth.value}月`)

const greetingText = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 12) return '早上好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const TIPS = [
  '运动前做5分钟热身，能有效预防受伤 💆',
  '每天8杯水，代谢更通畅，效果更明显 💧',
  '力量训练后48小时肌肉才能完全恢复 💪',
  '有氧运动坚持20分钟以上才开始燃脂 🔥',
  '深蹲是"运动之王"，全身75%肌群参与 🏋️',
  '睡前做拉伸，缓解肌肉紧张，提升睡眠 🌙',
  '高蛋白饮食配合训练，增肌效果翻倍 🥩',
  '核心训练不等于腹肌，稳定性同样重要 🎯',
]
const MOTTOS = [
  '你比昨天更强了一点点！',
  '坚持的人，终将得到回报。',
  '今天的汗水，是明天的肌肉。',
  '没有捷径，只有每一步都算数。',
  '你的身体记得每一次努力。',
]

const currentTip = computed(() => TIPS[tipIndex.value % TIPS.length])
const currentMotto = computed(() => MOTTOS[mottoIndex.value % MOTTOS.length])

function rotateTip() { tipIndex.value++ }

function confettiStyle(i) {
  const colors = ['#F97316','#FB923C','#FCD34D','#34D399','#60A5FA','#F472B6']
  return {
    left: `${(i * 8.3)}%`,
    background: colors[i % colors.length],
    animationDelay: `${(i * 0.08).toFixed(2)}s`,
    animationDuration: `${0.8 + (i % 3) * 0.2}s`
  }
}

let timer = null
function updateTime() {
  const now = new Date()
  const hh = String(now.getHours()).padStart(2, '0')
  const mm = String(now.getMinutes()).padStart(2, '0')
  const ss = String(now.getSeconds()).padStart(2, '0')
  timeHM.value = `${hh}:${mm}`
  timeSec.value = ss
  const weekMap = ['周日','周一','周二','周三','周四','周五','周六']
  dateText.value = `${now.getMonth()+1}月${now.getDate()}日 · ${weekMap[now.getDay()]}`
  mottoIndex.value = Math.floor(Date.now() / 60000) % MOTTOS.length
}

function calcCalendar() {
  const year = calendarYear.value
  const month = calendarMonth.value
  const firstDay = new Date(year, month - 1, 1).getDay()
  const daysInMonth = new Date(year, month, 0).getDate()
  const today = new Date()
  const days = []
  for (let i = 0; i < firstDay; i++) days.push(null)
  for (let d = 1; d <= daysInMonth; d++) {
    const ds = `${year}-${String(month).padStart(2,'0')}-${String(d).padStart(2,'0')}`
    days.push({
      day: d, date: ds,
      checked: checkedDates.value.has(ds),
      isToday: year === today.getFullYear() && month === today.getMonth()+1 && d === today.getDate()
    })
  }
  calendarDays.value = days
}

function prevMonth() {
  if (calendarMonth.value === 1) { calendarMonth.value = 12; calendarYear.value-- }
  else calendarMonth.value--
  calcCalendar()
}
function nextMonth() {
  if (calendarMonth.value === 12) { calendarMonth.value = 1; calendarYear.value++ }
  else calendarMonth.value++
  calcCalendar()
}

async function loadData() {
  const stored = localStorage.getItem('user')
  if (stored) Object.assign(user, JSON.parse(stored))
  pageLoading.value = true
  try {
    const [todayRes, statsRes, calRes] = await Promise.allSettled([
      checkinApi.today(),
      userApi.stats(),
      checkinApi.calendar()
    ])
    if (todayRes.status === 'fulfilled') {
      user.hasCheckedIn = todayRes.value.data?.checkedIn === true
    }
    if (statsRes.status === 'fulfilled') {
      user.totalDays = statsRes.value.data?.totalDays || 0
      user.rank = statsRes.value.data?.rank || '-'
    }
    if (calRes.status === 'fulfilled') {
      const days = calRes.value.data || []
      checkedDates.value = new Set(
        days.filter(d => d.checked).map(d => d.date)
      )
    }
  } catch (e) {}
  finally {
    pageLoading.value = false
  }
  calcCalendar()
}

async function handleCheckIn() {
  checkingIn.value = true
  try {
    await checkinApi.add()
    user.hasCheckedIn = true
    user.totalDays = (user.totalDays || 0) + 1
    const today = new Date()
    const ds = `${today.getFullYear()}-${String(today.getMonth()+1).padStart(2,'0')}-${String(today.getDate()).padStart(2,'0')}`
    checkedDates.value.add(ds)
    calcCalendar()
    successMsg.value = MOTTOS[Math.floor(Math.random() * MOTTOS.length)]
    showSuccess.value = true
  } catch(e) {} finally {
    checkingIn.value = false
  }
}

onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 1000)
  calcCalendar()
  loadData()
})
// ✅ 修复问题2：切换 tab 回来时重新加载数据，保证打卡状态正确
onActivated(() => {
  loadData()
})
onUnmounted(() => clearInterval(timer))
</script>

<style scoped>
.home-page { min-height: 100vh; background: #f5f6fa; padding-bottom: 80px; }

/* ===== 头部 ===== */
.home-header {
  background: linear-gradient(135deg, #C2410C 0%, #EA580C 40%, #F97316 75%, #FB923C 100%);
  padding: 18px 18px 28px;
  color: #fff;
  position: relative;
  overflow: hidden;
}
.header-bg-circle {
  position: absolute; border-radius: 50%;
  background: rgba(255,255,255,0.07);
  pointer-events: none;
}
.c1 { width:200px;height:200px;top:-60px;right:-40px; }
.c2 { width:120px;height:120px;bottom:-50px;left:-30px; }

/* 浮动装饰点 */
.float-dot {
  position: absolute; border-radius: 50%;
  background: rgba(255,255,255,.15);
  pointer-events: none;
  animation: floatY 4s ease-in-out infinite;
}
.fd1 { width:10px;height:10px; top:30px;left:60px; animation-delay:0s; }
.fd2 { width:7px;height:7px; top:70px;right:90px; animation-delay:1.5s; }
.fd3 { width:12px;height:12px; bottom:20px;right:50px; animation-delay:.8s; }
@keyframes floatY {
  0%,100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

.header-top {
  display: flex; justify-content: space-between; align-items: flex-start;
  position: relative; z-index: 1; margin-bottom: 14px;
}
.greeting { font-size: 18px; font-weight: 700; margin-bottom: 8px; }

/* 时间徽章 */
.time-badge {
  display: inline-flex; align-items: baseline; gap: 2px;
  background: rgba(255,255,255,.18);
  border-radius: 20px; padding: 5px 12px;
  backdrop-filter: blur(6px);
  margin-bottom: 4px;
}
.time-icon { font-size: 13px; margin-right: 4px; }
.time-main { font-size: 24px; font-weight: 800; letter-spacing: 1px; }
.time-sec { font-size: 14px; font-weight: 500; opacity: .75; }
.date-text { font-size: 12px; opacity: .7; padding-left: 4px; }

/* 头像区 */
.avatar-wrap { position: relative; flex-shrink: 0; }
.header-avatar {
  width: 52px; height: 52px; border-radius: 50%;
  border: 3px solid rgba(255,255,255,.5);
  object-fit: cover; position: relative; z-index: 1;
}
.avatar-ring {
  position: absolute; inset: -5px; border-radius: 50%;
  border: 2px dashed rgba(255,255,255,.35);
  animation: spinRing 12s linear infinite;
}
@keyframes spinRing { to { transform: rotate(360deg); } }

/* 头部状态条 */
.header-status-bar {
  display: flex; justify-content: space-around; align-items: center;
  background: rgba(255,255,255,.15); border-radius: 14px; padding: 10px 0;
  position: relative; z-index: 1;
  backdrop-filter: blur(4px);
}
.hsb-item { text-align: center; flex: 1; }
.hsb-num { display: block; font-size: 20px; font-weight: 800; }
.hsb-lbl { display: block; font-size: 11px; opacity: .75; margin-top: 1px; }
.hsb-divider { width: 1px; height: 28px; background: rgba(255,255,255,.25); }

/* ===== 主体 ===== */
.home-body { padding: 16px 16px 0; }

/* 打卡卡片 */
.checkin-card {
  background: #fff; border-radius: 20px;
  padding: 0 18px 18px; margin-bottom: 14px;
  box-shadow: 0 8px 28px rgba(249,115,22,.12);
  position: relative; overflow: hidden;
}
.card-top-bar {
  height: 4px; margin: 0 -18px 18px;
  background: linear-gradient(90deg, #C2410C, #F97316, #FB923C);
}

/* 已打卡 Banner */
.done-banner {
  display: flex; align-items: center; gap: 14px;
  background: linear-gradient(135deg, #f0fdf4, #dcfce7);
  border-radius: 16px; padding: 16px 18px;
  border: 1.5px solid #bbf7d0;
  margin-bottom: 0;
}
.done-icon-wrap {
  position: relative; width: 48px; height: 48px; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
}
.done-icon { font-size: 26px; position: relative; z-index: 1; }
.done-ripple {
  position: absolute; inset: 0; border-radius: 50%;
  border: 2px solid rgba(16,185,129,.4);
  animation: doneRipple 2s ease-out infinite;
}
.done-ripple.r2 { animation-delay: 1s; }
@keyframes doneRipple {
  0% { transform: scale(.8); opacity: 1; }
  100% { transform: scale(1.8); opacity: 0; }
}
.done-texts { flex: 1; }
.done-title { font-size: 15px; font-weight: 700; color: #065f46; }
.done-sub { font-size: 12px; color: #16a34a; margin-top: 3px; }
.done-badge {
  flex-shrink: 0;
  background: linear-gradient(135deg, #10b981, #059669);
  color: #fff; font-size: 12px; font-weight: 700;
  padding: 5px 12px; border-radius: 20px;
  box-shadow: 0 2px 8px rgba(16,185,129,.3);
  letter-spacing: .5px;
}

/* 未打卡 Banner */
.todo-banner {
  display: flex; align-items: center; gap: 14px;
  background: linear-gradient(135deg, #fff4ed, #ffedd5);
  border-radius: 14px; padding: 14px 16px; margin-bottom: 14px;
}
.todo-icon-wrap {
  position: relative; width: 52px; height: 52px; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
}
.todo-icon { font-size: 28px; position: relative; z-index: 1; }
.pulse-ring {
  position: absolute; inset: 0; border-radius: 50%;
  border: 2px solid rgba(249,115,22,.5);
  animation: pulseRing 1.8s ease-in-out infinite;
}
@keyframes pulseRing {
  0% { transform: scale(.8); opacity: 1; }
  100% { transform: scale(1.8); opacity: 0; }
}
.todo-title { font-size: 15px; font-weight: 700; color: #92400e; }
.todo-sub { font-size: 13px; color: #b45309; margin-top: 3px; }

/* 打卡按钮 */
.checkin-btn {
  width: 100%; padding: 14px;
  background: linear-gradient(135deg, #C2410C, #F97316, #FB923C);
  border: none; border-radius: 14px;
  color: #fff; font-size: 16px; font-weight: 700;
  cursor: pointer; transition: all .3s;
  box-shadow: 0 5px 18px rgba(249,115,22,.4);
  letter-spacing: .5px;
}
.checkin-btn:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(249,115,22,.5); }
.checkin-btn:active:not(:disabled) { transform: scale(.98); }
.checkin-btn.done {
  background: linear-gradient(135deg, #d1fae5, #a7f3d0);
  color: #065f46; box-shadow: none;
  cursor: default;
}
.checkin-btn:disabled:not(.done) { opacity: .65; cursor: not-allowed; }

/* 格言条 */
.motto-bar {
  display: flex; align-items: center; gap: 6px;
  margin-top: 12px; padding: 8px 12px;
  background: #fafafa; border-radius: 10px;
  font-size: 13px; color: #666;
}
.motto-icon { font-size: 14px; }
.motto-text { flex: 1; }

/* 小贴士卡片 */
.tips-card {
  background: #fff; border-radius: 18px;
  padding: 14px 16px; margin-bottom: 14px;
  box-shadow: 0 4px 16px rgba(0,0,0,.06);
  border-left: 4px solid #F97316;
}
.tips-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.tips-title { font-size: 14px; font-weight: 700; color: #333; }
.tips-refresh { font-size: 12px; color: #F97316; cursor: pointer; padding: 2px 8px; background: #fff4ed; border-radius: 10px; }
.tips-content { font-size: 13px; color: #666; line-height: 1.7; }

/* ===== 日历 ===== */
.calendar-card {
  background: #fff; border-radius: 20px;
  padding: 16px 20px 14px;
  margin: 0 auto 14px;   /* 左右居中，底部 14px */
  box-shadow: 0 4px 18px rgba(0,0,0,.06);
  max-width: 420px;
}
.cal-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;
}
.cal-title { font-size: 14px; font-weight: 700; color: #333; }
.nav-btn {
  width: 26px; height: 26px; border-radius: 50%;
  background: #fff4ed; border: none; cursor: pointer;
  color: #F97316; font-size: 16px;
  display: flex; align-items: center; justify-content: center;
  transition: background .2s;
}
.nav-btn:active { background: #ffedd5; }
/* 日历网格随卡片宽度自适应，不限制 max-width */
.cal-grid {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 4px;
}
.cal-weekday {
  text-align: center;
  font-size: 11px; color: #bbb; font-weight: 600;
  padding: 6px 0;
}
.wd-weekend { color: #f9a8d4; }
.cal-day {
  aspect-ratio: 1;
  min-height: 34px;
  max-height: 42px;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  font-size: 12px; border-radius: 10px; color: #555;
  position: relative;
  transition: all .15s;
}
.cal-day.empty { background: transparent; }
.cal-day.checked {
  background: linear-gradient(135deg, #C2410C, #F97316);
  color: #fff; font-weight: 700;
  box-shadow: 0 2px 6px rgba(249,115,22,.35);
}
.cal-day.today {
  border: 2px solid #F97316;
  background: #fff4ed; color: #EA580C; font-weight: 700;
}
.cal-day.today.checked {
  border: 2px solid #C2410C;
  background: linear-gradient(135deg, #C2410C, #F97316);
  color: #fff;
}
.cal-day.weekend:not(.checked) { color: #f9a8d4; }
.check-dot {
  position: absolute; bottom: 2px;
  width: 3px; height: 3px; border-radius: 50%;
  background: rgba(255,255,255,.7);
}

/* 日历图例 */
.cal-legend {
  display: flex; gap: 14px; justify-content: flex-end;
  padding-top: 8px; border-top: 1px solid #f5f5f5; margin-top: 6px;
}
.legend-item { display: flex; align-items: center; gap: 4px; font-size: 11px; color: #aaa; }
.legend-dot { display: inline-block; width: 10px; height: 10px; border-radius: 4px; }
.checked-dot { background: linear-gradient(135deg, #C2410C, #F97316); }
.today-dot { background: #fff4ed; border: 1.5px solid #F97316; }

/* ===== 成就徽章 ===== */
.badge-card {
  background: #fff; border-radius: 20px;
  padding: 16px; margin-bottom: 14px;
  box-shadow: 0 4px 16px rgba(0,0,0,.06);
}
.badge-title { font-size: 14px; font-weight: 700; color: #333; margin-bottom: 12px; }
.badge-row { display: grid; grid-template-columns: repeat(5, 1fr); gap: 8px; }
.badge-item {
  display: flex; flex-direction: column; align-items: center;
  padding: 10px 4px; border-radius: 14px;
  background: #f9f9f9; opacity: .4;
  transition: all .3s;
}
.badge-item.lit { opacity: 1; background: linear-gradient(135deg, #fff4ed, #ffedd5); }
.badge-icon { font-size: 22px; margin-bottom: 4px; }
.badge-name { font-size: 11px; font-weight: 700; color: #333; text-align: center; }
.badge-req { font-size: 10px; color: #aaa; margin-top: 2px; text-align: center; }

/* ===== 弹窗 ===== */
.overlay {
  position: fixed; inset: 0;
  background: rgba(0,0,0,.55);
  display: flex; align-items: center; justify-content: center;
  z-index: 999;
}
.success-card {
  background: #fff; border-radius: 24px; padding: 32px 28px;
  margin: 20px; text-align: center; max-width: 300px; width: 100%;
  animation: slideUp .35s ease;
  position: relative; overflow: hidden;
}
@keyframes slideUp {
  from { transform: translateY(40px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

/* 彩带 */
.confetti-wrap { position: absolute; top: 0; left: 0; right: 0; height: 40px; pointer-events: none; }
.confetti {
  position: absolute; top: -10px;
  width: 6px; height: 6px; border-radius: 2px;
  animation: confettiFall linear forwards;
}
@keyframes confettiFall {
  0% { transform: translateY(0) rotate(0deg); opacity: 1; }
  100% { transform: translateY(60px) rotate(180deg); opacity: 0; }
}

.success-icon { font-size: 52px; margin-bottom: 10px; }
.success-title { font-size: 22px; font-weight: 800; color: #1a1a2e; margin-bottom: 6px; }
.success-msg { font-size: 14px; color: #666; line-height: 1.7; margin-bottom: 22px; }
.success-close {
  width: 100%; padding: 13px;
  background: linear-gradient(135deg, #C2410C, #F97316);
  border: none; border-radius: 12px; color: #fff;
  font-size: 15px; font-weight: 700; cursor: pointer;
  box-shadow: 0 4px 14px rgba(249,115,22,.35);
}

.fade-enter-active, .fade-leave-active { transition: opacity .2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

/* ===== 页面加载遮罩 ===== */
.page-loading {
  position: fixed; inset: 0; z-index: 1000;
  background: rgba(245, 246, 250, 0.92);
  backdrop-filter: blur(6px);
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  gap: 20px;
}
.loading-spinner {
  position: relative; width: 60px; height: 60px;
}
.spinner-ring {
  position: absolute; inset: 0; border-radius: 50%;
  border: 3px solid transparent;
  border-top-color: #F97316;
  animation: spin 1s linear infinite;
}
.spinner-ring.r2 {
  inset: 8px;
  border-top-color: #FB923C;
  animation-duration: 0.75s;
  animation-direction: reverse;
}
.spinner-ring.r3 {
  inset: 16px;
  border-top-color: #FCD34D;
  animation-duration: 0.5s;
}
@keyframes spin { to { transform: rotate(360deg); } }
.loading-text {
  font-size: 14px; color: #F97316; font-weight: 600;
  letter-spacing: 1px;
  animation: breathe 1.4s ease-in-out infinite;
}
@keyframes breathe {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 1; }
}
.loading-fade-enter-active { transition: opacity 0.25s ease; }
.loading-fade-leave-active { transition: opacity 0.4s ease; }
.loading-fade-enter-from, .loading-fade-leave-to { opacity: 0; }
</style>
