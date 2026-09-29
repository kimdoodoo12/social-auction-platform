import { useMatches } from 'react-router-dom'
import AdminPage from '../../components/admin/AdminPage'
import { MockNotice } from '../../components/admin/ui'

// 아직 구현되지 않은 화면 자리. 제목은 route의 handle.title에서 읽는다.
export default function Placeholder() {
  const matches = useMatches()
  const title = matches.at(-1)?.handle?.title ?? '준비 중'
  return (
    <AdminPage title={title}>
      <MockNotice>{title} 화면은 아직 구현되지 않았습니다.</MockNotice>
    </AdminPage>
  )
}
