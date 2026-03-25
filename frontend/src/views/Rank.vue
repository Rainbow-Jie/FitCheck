<template>
  <div class="rank-page">
    <!-- 头部 -->
    <div class="rank-header">
      <div class="rh-deco d1"></div>
      <div class="rh-deco d2"></div>
      <h1 class="rh-title">🏆 排行榜</h1>
      <!-- Tab -->
      <div class="rank-tabs">
        <div
          v-for="t in tabs"
          :key="t.value"
          class="rank-tab"
          :class="{ active: activeTab === t.value }"
          @click="changeTab(t.value)"
        >{{ t.label }}</div>
      </div>
    </div>

    <div class="rank-body">
      <!-- 加载中 -->
      <div v-if="loading" class="loading-wrap">
        <van-loading color="#F97316" size="28px" />
      </div>

      <template v-else>
        <!-- 前三名奖台 -->
        <div class="podium-wrap" v-if="rankList.length >= 3">
          <!-- 2nd -->
          <div class="podium-item second">
            <img :src="getAvatar(rankList[1])" class="podium-avatar silver" />
            <div class="podium-name">{{ rankList[1]?.username }}</div>
            <div class="podium-score">{{ rankList[1]?.checkInCount || 0 }}次</div>
            <div class="podium-stand s2">🥈</div>
          </div>
          <!-- 1st -->
          <div class="podium-item first">
            <div class="crown">👑</div>
            <img :src="getAvatar(rankList[0])" class="podium-avatar gold" />
            <div class="podium-name">{{ rankList[0]?.username }}</div>
            <div class="podium-score">{{ rankList[0]?.checkInCount || 0 }}次</div>
            <div class="podium-stand s1">🥇</div>
          </div>
          <!-- 3rd -->
          <div class="podium-item third">
            <img :src="getAvatar(rankList[2])" class="podium-avatar bronze" />
            <div class="podium-name">{{ rankList[2]?.username }}</div>
            <div class="podium-score">{{ rankList[2]?.checkInCount || 0 }}次</div>
            <div class="podium-stand s3">🥉</div>
          </div>
        </div>

        <!-- 列表 -->
        <div class="rank-list">
          <div
            v-for="(item, index) in rankList"
            :key="item.userId"
            class="rank-row"
            :class="{ 'is-top3': index < 3 }"
          >
            <div class="rank-pos" :class="`pos-${Math.min(index+1,4)}`">{{ index + 1 }}</div>
            <img :src="getAvatar(item)" class="rank-avatar" />
            <div class="rank-info">
              <div
                class="rank-name clickable"
                @click.stop="showUserPopup(item, $event)"
              >{{ item.username }}</div>
            </div>
            <div class="rank-score">{{ item.checkInCount || 0 }}<span>次</span></div>
          </div>
          <div v-if="rankList.length === 0" class="empty">暂无数据</div>
        </div>
      </template>
    </div>

    <!-- 用户信息气泡 -->
    <Transition name="popup-fade">
      <div
        v-if="popup.visible"
        class="user-popup"
        :style="{ top: popup.y + 'px', left: popup.x + 'px' }"
        @click.stop
      >
        <div class="popup-arrow"></div>
        <div class="popup-name">{{ popup.username }}</div>
        <div class="popup-row">
          <span class="popup-label">性别</span>
          <span class="popup-val">{{ genderText(popup.gender) }}</span>
        </div>
        <div class="popup-row">
          <span class="popup-label">年龄</span>
          <span class="popup-val">{{ ageText(popup.birthday) }}</span>
        </div>
      </div>
    </Transition>

    <BottomNav />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import BottomNav from '@/components/BottomNav.vue'
import { rankApi } from '@/api/rank'

const tabs = [
  { label: '总榜', value: 'total' },
  { label: '月榜', value: 'month' },
  { label: '周榜', value: 'week' },
  { label: '日榜', value: 'day' }
]
const activeTab = ref('total')
const rankList = ref([])
const loading = ref(false)

// 悬浮气泡
const popup = ref({ visible: false, x: 0, y: 0, username: '', gender: null, birthday: null })
let popupTimer = null

function showUserPopup(item, event) {
  const rect = event.target.getBoundingClientRect()
  const scrollY = window.scrollY || document.documentElement.scrollTop
  const scrollX = window.scrollX || document.documentElement.scrollLeft
  // 气泡显示在点击元素右侧，若超出屏幕则改为左侧
  let x = rect.right + scrollX + 8
  const popupWidth = 150
  if (x + popupWidth > window.innerWidth) {
    x = rect.left + scrollX - popupWidth - 8
  }
  const y = rect.top + scrollY - 10

  // 再次点击同一用户：关闭
  if (popup.value.visible && popup.value.username === item.username) {
    closePopup()
    return
  }
  // 清除旧计时器
  if (popupTimer) clearTimeout(popupTimer)

  popup.value = { visible: true, x, y, username: item.username, gender: item.gender, birthday: item.birthday }

  // 3 秒后自动关闭
  popupTimer = setTimeout(() => {
    popup.value.visible = false
    popupTimer = null
  }, 3000)
}

function genderText(gender) {
  if (gender === 1) return '♂ 男'
  if (gender === 2) return '♀ 女'
  return '保密'
}

function ageText(birthday) {
  if (!birthday) return '未填写'
  const birth = new Date(birthday)
  const today = new Date()
  let age = today.getFullYear() - birth.getFullYear()
  const m = today.getMonth() - birth.getMonth()
  if (m < 0 || (m === 0 && today.getDate() < birth.getDate())) age--
  return age >= 0 ? `${age} 岁` : '未填写'
}

function closePopup() {
  popup.value.visible = false
  if (popupTimer) { clearTimeout(popupTimer); popupTimer = null }
}

function getAvatar(item) {
  const av = item?.avatar
  if (!av) return `https://api.dicebear.com/7.x/avataaars/svg?seed=${item?.username}`
  if (av.startsWith('http') || av.startsWith('data:')) return av
  const apiPath = av.startsWith('/fitcheck-api') ? av : '/fitcheck-api' + av
  const base = import.meta.env.DEV ? 'http://localhost:20001' : ''
  return base + apiPath
}

async function loadRank(type) {
  loading.value = true
  try {
    const res = await rankApi.list(type)
    rankList.value = res.data || []
  } catch(e) {
    rankList.value = []
  } finally {
    loading.value = false
  }
}

function changeTab(type) {
  activeTab.value = type
  loadRank(type)
}

onMounted(() => {
  loadRank('total')
  document.addEventListener('click', closePopup)
})
onUnmounted(() => {
  document.removeEventListener('click', closePopup)
  if (popupTimer) clearTimeout(popupTimer)
})
</script>

<style scoped>
.rank-page { min-height: 100vh; background: #f5f6fa; padding-bottom: 70px; position: relative; }

/* 头部 */
.rank-header {
  background: linear-gradient(135deg, #C2410C 0%, #EA580C 45%, #F97316 80%, #FB923C 100%);
  padding: 24px 18px 20px;
  color: #fff;
  position: relative;
  overflow: hidden;
}
.rh-deco {
  position: absolute; border-radius: 50%;
  background: rgba(255,255,255,.08);
}
.d1 { width:200px;height:200px;top:-80px;right:-50px; }
.d2 { width:120px;height:120px;bottom:-50px;left:-30px; }
.rh-title {
  font-size: 22px; font-weight: 800;
  text-align: center; margin-bottom: 18px;
  position: relative; z-index: 1;
}
.rank-tabs {
  display: flex; background: rgba(255,255,255,.15);
  border-radius: 12px; padding: 4px;
  position: relative; z-index: 1;
  backdrop-filter: blur(4px);
}
.rank-tab {
  flex: 1; text-align: center; padding: 9px;
  border-radius: 9px; font-size: 14px; font-weight: 500;
  color: rgba(255,255,255,.7); cursor: pointer; transition: all .25s;
}
.rank-tab.active {
  background: #fff; color: #F97316; font-weight: 700;
  box-shadow: 0 2px 10px rgba(0,0,0,.1);
}

.rank-body { padding: 18px 16px 0; }
.loading-wrap { display: flex; justify-content: center; padding: 60px 0; }

/* 奖台 */
.podium-wrap {
  display: flex; justify-content: center; align-items: flex-end;
  gap: 8px; margin-bottom: 20px; padding: 0 8px;
}
.podium-item { flex: 1; max-width: 110px; text-align: center; }
.crown { font-size: 22px; margin-bottom: 4px; }
.podium-avatar {
  width: 56px; height: 56px; border-radius: 50%;
  margin: 0 auto 6px; object-fit: cover;
  border: 3px solid transparent;
  box-shadow: 0 4px 14px rgba(0,0,0,.15);
}
.podium-avatar.gold { border-color: #FFD700; box-shadow: 0 0 14px rgba(255,215,0,.5); }
.podium-avatar.silver { border-color: #C0C0C0; }
.podium-avatar.bronze { border-color: #CD7F32; }
.podium-name { font-size: 12px; font-weight: 600; color: #333; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.podium-score { font-size: 11px; color: #888; margin-bottom: 6px; }
.podium-stand {
  border-radius: 8px 8px 0 0;
  display: flex; align-items: center; justify-content: center;
  font-size: 18px; color: #fff; font-weight: 700;
}
.s1 { height: 72px; background: linear-gradient(180deg, #FFD700, #FFA500); }
.s2 { height: 54px; background: linear-gradient(180deg, #C0C0C0, #A9A9A9); }
.s3 { height: 40px; background: linear-gradient(180deg, #CD7F32, #B8860B); }

/* 列表 */
.rank-list {
  background: #fff; border-radius: 18px;
  overflow: hidden; box-shadow: 0 4px 18px rgba(0,0,0,.06);
}
.rank-row {
  display: flex; align-items: center;
  padding: 14px 16px; border-bottom: 1px solid #f5f5f5;
  transition: background .15s;
}
.rank-row:last-child { border-bottom: none; }
.rank-row.is-top3 { background: linear-gradient(90deg, #fff8f4, #fff); }
.rank-pos {
  width: 28px; height: 28px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 700;
  background: #fff4ed; color: #F97316;
  margin-right: 12px; flex-shrink: 0;
}
.pos-1 { background: linear-gradient(135deg, #FFD700, #FFA500); color: #fff; }
.pos-2 { background: linear-gradient(135deg, #C0C0C0, #A9A9A9); color: #fff; }
.pos-3 { background: linear-gradient(135deg, #CD7F32, #B8860B); color: #fff; }
.rank-avatar {
  width: 42px; height: 42px; border-radius: 50%;
  margin-right: 12px; object-fit: cover;
  border: 2px solid #ffedd5;
}
.rank-info { flex: 1; }
.rank-name { font-size: 15px; font-weight: 600; color: #333; }

.rank-score {
  font-size: 20px; font-weight: 800;
  background: linear-gradient(135deg, #EA580C, #FB923C);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
}
.rank-score span { font-size: 13px; }
.empty { padding: 50px; text-align: center; color: #bbb; font-size: 15px; }

/* 可点击的用户名 */
.rank-name.clickable {
  cursor: pointer;
  display: inline-block;
  transition: color .2s;
}
.rank-name.clickable:active { color: #F97316; }

/* 悬浮气泡 */
.user-popup {
  position: absolute;
  z-index: 999;
  background: #fff;
  border-radius: 12px;
  padding: 12px 14px;
  min-width: 140px;
  box-shadow: 0 8px 28px rgba(0,0,0,.15);
  border: 1px solid #ffedd5;
}
.popup-arrow {
  position: absolute;
  left: -7px; top: 16px;
  width: 0; height: 0;
  border-top: 7px solid transparent;
  border-bottom: 7px solid transparent;
  border-right: 7px solid #fff;
  filter: drop-shadow(-2px 0 2px rgba(0,0,0,.08));
}
.popup-name {
  font-size: 13px; font-weight: 700; color: #333;
  margin-bottom: 8px;
  padding-bottom: 6px;
  border-bottom: 1px solid #f0f0f0;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.popup-row {
  display: flex; justify-content: space-between; align-items: center;
  margin-top: 6px;
}
.popup-label {
  font-size: 11px; color: #aaa; font-weight: 500;
}
.popup-val {
  font-size: 13px; font-weight: 600;
  color: #F97316;
}

.popup-fade-enter-active { transition: opacity .15s, transform .15s; }
.popup-fade-leave-active { transition: opacity .12s, transform .12s; }
.popup-fade-enter-from { opacity: 0; transform: translateX(-6px); }
.popup-fade-leave-to { opacity: 0; transform: translateX(-6px); }
</style>
