import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { authApi } from './authApi'

export default function ForbiddenPage() {
  const navigate = useNavigate()
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  async function handleAdminLogin() {
    if (busy) return

    setBusy(true)
    setError('')

    try {
      if (await authApi.logout() !== true) {
        throw new Error('Logout failed')
      }
      navigate('/admin/login', { replace: true })
    } catch {
      setError('로그아웃하지 못했습니다. 다시 시도해 주세요.')
    } finally {
      setBusy(false)
    }
  }

  return (
    <section className="ua-card ua-forbidden" aria-labelledby="forbidden-title">
      <p className="ua-forbidden-code">403 · 접근 권한 없음</p>
      <h1 id="forbidden-title">관리자가 아닙니다</h1>
      <p className="ua-forbidden-description">
        이 페이지는 관리자만 이용할 수 있습니다.<br />
        관리자 계정으로 로그인해 주세요.
      </p>

      {error && <p className="ua-error" role="alert">{error}</p>}

      <button
        type="button"
        className="ua-primary ua-forbidden-link"
        onClick={handleAdminLogin}
        disabled={busy}
      >
        {busy ? '로그아웃 중…' : '관리자 계정으로 로그인'}
      </button>
    </section>
  )
}