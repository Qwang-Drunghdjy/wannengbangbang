import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'
import SimpleLayout from '@/layouts/SimpleLayout.vue'
import TabLayout from '@/layouts/TabLayout.vue'

/**
 * 路由（规范 2 节，模型 A）：
 * - 一级 tab：/（首页）、/messages（消息）、/profile（我的）→ TabLayout（带底部导航）
 * - 二级页：/publish、/item/:id、/match-result → SimpleLayout（带返回按钮）
 * - 登录/注册：/login、/register → SimpleLayout
 * - meta.requiresAuth：未登录访问跳 /login（带回跳）
 */
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: TabLayout,
      children: [
        { path: '', name: 'home', component: () => import('@/views/HomeView.vue') },
        { path: 'messages', name: 'messages', component: () => import('@/views/MessagesView.vue') },
        {
          path: 'profile',
          name: 'profile',
          component: () => import('@/views/ProfileView.vue'),
          meta: { requiresAuth: true },
        },
      ],
    },
    {
      path: '/login',
      component: SimpleLayout,
      meta: { title: '登录' },
      children: [{ path: '', name: 'login', component: () => import('@/views/LoginView.vue') }],
    },
    {
      path: '/register',
      component: SimpleLayout,
      meta: { title: '注册' },
      children: [
        { path: '', name: 'register', component: () => import('@/views/RegisterView.vue') },
      ],
    },
    {
      path: '/publish',
      component: SimpleLayout,
      meta: { title: '发布信息', requiresAuth: true },
      children: [{ path: '', name: 'publish', component: () => import('@/views/PublishView.vue') }],
    },
    {
      path: '/item/:id',
      component: SimpleLayout,
      meta: { title: '物品详情' },
      children: [
        { path: '', name: 'item-detail', component: () => import('@/views/ItemDetailView.vue') },
      ],
    },
    {
      path: '/all-messages',
      component: SimpleLayout,
      meta: { title: '全部消息' },
      children: [
        {
          path: '',
          name: 'all-messages',
          component: () => import('@/views/AllMessagesView.vue'),
        },
      ],
    },
    {
      path: '/match-result',
      component: SimpleLayout,
      meta: { title: '匹配结果' },
      children: [
        { path: '', name: 'match-result', component: () => import('@/views/MatchResultView.vue') },
      ],
    },
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
})

// 路由守卫：需登录页未登录 → /login（带回跳，规范 6.2）
router.beforeEach((to) => {
  if (to.matched.some((r) => r.meta.requiresAuth) && !getToken()) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  return true
})

export default router
