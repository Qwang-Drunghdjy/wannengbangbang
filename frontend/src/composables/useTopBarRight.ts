import { shallowRef, type Component } from 'vue'

/**
 * 布局 TopBar 右侧 slot 的注入通道。
 *
 * 详情页等二级页由 `SimpleLayout`（内部含 `TopBar.vue`）渲染，路由视图组件够不到
 * TopBar 的 `#right` slot。本模块用一个模块级 `shallowRef` 保存"要渲染进右侧"的组件：
 * - `SimpleLayout` 在 TopBar 的 `#right` 里渲染 `<component :is="topBarRight" />`；
 * - 视图组件在 `onMounted` 里设置、`onUnmounted` 里清空（避免跳转后残留）。
 */

/** 当前要渲染进 TopBar 右侧 slot 的组件；为 `null` 时右侧不显示内容 */
export const topBarRight = shallowRef<Component | null>(null)

/** 获取 TopBar 右侧注入容器（供 SimpleLayout / 视图组件读写 `topBarRight`） */
export function useTopBarRight() {
  return { topBarRight }
}
