/** 匹配度 0.0 ~ 1.0 → 百分比（0-100 取整，规范 8.4） */
export function formatPercent(score: number): number {
  return Math.round(score * 100)
}

/** 匹配度颜色：>80 绿 / 60-80 橙 / <60 灰（规范 8.4） */
export function scoreColor(score: number): string {
  const p = formatPercent(score)
  if (p > 80) return 'text-success'
  if (p >= 60) return 'text-orange-500'
  return 'text-muted'
}
