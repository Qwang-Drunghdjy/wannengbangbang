import dayjs from 'dayjs'
import relativeTimePlugin from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTimePlugin)
dayjs.locale('zh-cn')

/** 相对时间（如 "2小时前"）；空值或非法值返回 "—" */
export function relativeTime(value?: string | null): string {
  if (!value) return '—'
  const d = dayjs(value)
  if (!d.isValid()) return '—'
  return d.fromNow()
}
