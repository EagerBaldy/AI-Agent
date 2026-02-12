<template>
  <div class="home-container">
    <div class="user-status" v-if="userStore.isLoggedIn">
        <span class="user-name">欢迎, {{ userStore.currentUser?.userAccount }}</span>
        <button class="logout-btn" @click="handleLogout">退出登录</button>
    </div>
    <h1 class="home-title">AI 全能 Agent</h1>
    <p class="home-subtitle">您的智能工作与生活伙伴</p>
    
    <div class="cards-wrapper">
      <div class="card code-card" @click="switchMode('code')">
        <div class="card-icon">💻</div>
        <h2>编程助手</h2>
        <p>解答技术难题，提供代码示例</p>
        <button class="start-btn" :style="{ background: themes.code.primary }" @click.stop="switchMode('code')">快来创造</button>
        <div class="card-bg">
          <div class="bg-tags">
            <span>代码</span><span>算法</span><span>调试</span>
            <span>架构</span><span>部署</span><span>优化</span>
            <span>性能</span><span>安全</span><span>数据库</span>
            <span>接口</span><span>敏捷</span><span>重构</span>
          </div>
        </div>
      </div>
      
      <div class="card travel-card" @click="switchMode('travel')">
        <div class="card-icon">✈️</div>
        <h2>旅游助手</h2>
        <p>规划完美行程，探索世界之美</p>
        <button class="start-btn" :style="{ background: themes.travel.primary }" @click.stop="switchMode('travel')">说走就走</button>
        <div class="card-bg">
          <div class="bg-tags">
            <span>攻略</span><span>景点</span><span>美食</span>
            <span>住宿</span><span>交通</span><span>签证</span>
            <span>摄影</span><span>民俗</span><span>路线</span>
            <span>预算</span><span>探险</span><span>休闲</span>
          </div>
        </div>
      </div>

      <div class="card essay-card" @click="switchMode('essay')">
        <div class="card-icon">📝</div>
        <h2>作文老师</h2>
        <p>精批细改，提升写作水平</p>
        <button class="start-btn" :style="{ background: themes.essay.primary }" @click.stop="switchMode('essay')">开始辅导</button>
        <div class="card-bg">
          <div class="bg-tags">
            <span>审题</span><span>立意</span><span>结构</span>
            <span>文采</span><span>逻辑</span><span>范文</span>
            <span>修辞</span><span>论证</span><span>情感</span>
            <span>细节</span><span>开头</span><span>结尾</span>
          </div>
        </div>
      </div>

      <div class="card material-card" @click="switchMode('material')">
        <div class="card-icon">📚</div>
        <h2>素材百科</h2>
        <p>海量素材，丰富文章内涵</p>
        <button class="start-btn" :style="{ background: themes.material.primary }" @click.stop="switchMode('material')">增加知识</button>
        <div class="card-bg">
          <div class="bg-tags">
            <span>名言</span><span>典故</span><span>事实</span>
            <span>论据</span><span>人物</span><span>时事</span>
            <span>历史</span><span>科技</span><span>艺术</span>
            <span>哲理</span><span>寓言</span><span>金句</span>
          </div>
        </div>
      </div>

      <div class="card medical-card" @click="switchMode('medical')">
        <div class="card-icon">🏥</div>
        <h2>医疗助手</h2>
        <p>健康咨询，症状初筛</p>
        <button class="start-btn" :style="{ background: themes.medical.primary }" @click.stop="switchMode('medical')">健康咨询</button>
        <div class="card-bg">
          <div class="bg-tags">
            <span>问诊</span><span>症状</span><span>养生</span>
            <span>预防</span><span>饮食</span><span>睡眠</span>
            <span>运动</span><span>心理</span><span>体检</span>
            <span>急救</span><span>康复</span><span>营养</span>
          </div>
        </div>
      </div>

      <div class="card college-card" @click="switchMode('college')">
        <div class="card-icon">🎓</div>
        <h2>升学规划</h2>
        <p>专业解读，院校对比</p>
        <button class="start-btn" :style="{ background: themes.college.primary }" @click.stop="switchMode('college')">规划未来</button>
        <div class="card-bg">
          <div class="bg-tags">
            <span>选校</span><span>专业</span><span>排名</span>
            <span>就业</span><span>考研</span><span>留学</span>
            <span>奖学金</span><span>导师</span><span>简历</span>
            <span>面试</span><span>实习</span><span>证书</span>
          </div>
        </div>
      </div>
    </div>

    <AppFooter />
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { themes } from '../utils/themes'
import AppFooter from '../components/layout/AppFooter.vue'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const handleLogout = async () => {
  await userStore.logout()
  router.push('/login')
}

const switchMode = (mode) => {
  router.push(`/workspace/${mode}`)
}
</script>

<style scoped>
.home-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  background: var(--bg-body);
  padding: 20px;
  overflow-y: auto;
  position: relative;
}

.user-status {
  position: absolute;
  top: 20px;
  right: 30px;
  display: flex;
  align-items: center;
  gap: 15px;
  z-index: 10;
}

@media (max-width: 768px) {
  .user-status {
    position: static;
    width: 100%;
    justify-content: flex-end;
    margin-bottom: 20px;
    padding-right: 10px;
  }
  
  .home-title {
    font-size: 2.5rem;
  }
}

.user-name {
  color: var(--text-primary);
  font-weight: 500;
}

.logout-btn {
  padding: 6px 16px;
  background: transparent;
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
  border-radius: 20px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.2s;
}

.logout-btn:hover {
  border-color: #ff6b6b;
  color: #ff6b6b;
  background: rgba(255, 107, 107, 0.05);
}

.home-title {
  font-size: 3.5rem;
  font-weight: 800;
  background: linear-gradient(45deg, var(--primary-color, #2193b0), #6dd5ed);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 10px;
  letter-spacing: -1px;
}

.home-subtitle {
  font-size: 1.2rem;
  color: var(--text-secondary);
  margin-bottom: 60px;
}

.cards-wrapper {
  display: flex;
  gap: 40px;
  flex-wrap: wrap;
  justify-content: center;
}

.card {
  width: 300px;
  height: 360px;
  background: var(--card-bg, white);
  border-radius: 24px;
  padding: 40px 30px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  box-shadow: 0 10px 30px var(--shadow-color, rgba(0,0,0,0.05));
  border: 1px solid var(--border-color, rgba(0,0,0,0.05));
}

.card h2 {
  font-size: 1.5rem;
  color: var(--text-primary);
  margin-bottom: 12px;
  z-index: 2;
}

.card p {
  color: var(--text-secondary);
  line-height: 1.6;
  z-index: 2;
}

.card-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  transition: opacity 0.3s;
  z-index: 1;
}

.code-card:hover .card-bg { background: linear-gradient(135deg, rgba(33, 147, 176, 0.1), rgba(109, 213, 237, 0.1)); opacity: 1; }
.travel-card:hover .card-bg { background: linear-gradient(135deg, rgba(255, 154, 158, 0.1), rgba(254, 207, 239, 0.1)); opacity: 1; }
.essay-card:hover .card-bg { background: linear-gradient(135deg, rgba(255, 165, 0, 0.1), rgba(255, 215, 0, 0.1)); opacity: 1; }
.material-card:hover .card-bg { background: linear-gradient(135deg, rgba(144, 238, 144, 0.1), rgba(34, 139, 34, 0.1)); opacity: 1; }
.medical-card:hover .card-bg { background: linear-gradient(135deg, rgba(32, 191, 107, 0.1), rgba(15, 185, 177, 0.1)); opacity: 1; }
.college-card:hover .card-bg { background: linear-gradient(135deg, rgba(75, 123, 236, 0.1), rgba(56, 103, 214, 0.1)); opacity: 1; }

.start-btn {
  margin-top: auto;
  padding: 12px 32px;
  color: white;
  border: none;
  border-radius: 25px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  z-index: 2;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
  letter-spacing: 0.5px;
}

.start-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(0,0,0,0.2);
}

.bg-tags {
  width: 100%;
  height: 100%;
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  align-content: center;
  gap: 15px;
  padding: 60px 20px 20px 20px; /* 增加顶部内边距，避开标题 */
  box-sizing: border-box;
}

.bg-tags span {
  font-size: 1rem;
  font-weight: 800;
  text-transform: uppercase;
  color: currentColor;
  opacity: 0.12;
  transform: rotate(-10deg);
  user-select: none;
  transition: all 0.4s ease;
  white-space: nowrap;
}

/* 随机化一些大小和透明度，制造层次感 */
.bg-tags span:nth-child(2n) {
  font-size: 1.2rem;
  opacity: 0.15;
}

.bg-tags span:nth-child(3n) {
  font-size: 0.9rem;
  opacity: 0.1;
  transform: rotate(-5deg);
}

.bg-tags span:nth-child(5n) {
  font-size: 1.3rem;
  opacity: 0.18;
  transform: rotate(-15deg);
}

.card:hover .bg-tags span {
  transform: rotate(0deg) scale(1.1);
  opacity: 0.25;
}

/* 保持 hover 状态下的一致性，或者可以有轻微差异 */
.card:hover .bg-tags span:nth-child(2n) {
  transform: rotate(0deg) scale(1.15);
  opacity: 0.3;
}

.code-card .bg-tags { color: #2193b0; }
.travel-card .bg-tags { color: #ff9a9e; }
.essay-card .bg-tags { color: #ff9a00; }
.material-card .bg-tags { color: #228b22; }
.medical-card .bg-tags { color: #20bf6b; }
.college-card .bg-tags { color: #3867d6; }
</style>
