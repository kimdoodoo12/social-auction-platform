import { useCallback, useEffect, useState } from 'react'

// 비동기 요청 상태(loading/error/data)를 관리한다.
// deps(원시값 배열)가 바뀌면 다시 요청하고, reload()로 수동 재요청할 수 있다.
// 이전 data는 새 요청이 끝날 때까지 유지된다(목록 페이지 이동 시 깜빡임 방지).
export function useAsync(fn, deps = []) {
  const [tick, setTick] = useState(0)
  const key = JSON.stringify([...deps, tick])
  const [result, setResult] = useState({ key: null, data: null, error: null })

  useEffect(() => {
    let cancelled = false
    fn()
      .then((data) => {
        if (!cancelled) setResult({ key, data, error: null })
      })
      .catch((error) => {
        if (!cancelled) setResult((prev) => ({ key, data: prev.data, error }))
      })
    return () => {
      cancelled = true
    }
    // key가 deps와 tick을 모두 담고 있다
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [key])

  const reload = useCallback(() => setTick((t) => t + 1), [])
  return {
    loading: result.key !== key,
    error: result.key === key ? result.error : null,
    data: result.data,
    reload,
  }
}
