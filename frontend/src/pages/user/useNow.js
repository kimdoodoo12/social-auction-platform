import { useEffect, useState } from 'react'

// 남은 시간 표시용 시계. interval(ms)마다 현재 시각을 갱신한다.
export function useNow(interval = 1000) {
  const [now, setNow] = useState(() => Date.now())
  useEffect(() => {
    const timer = setInterval(() => setNow(Date.now()), interval)
    return () => clearInterval(timer)
  }, [interval])
  return now
}
