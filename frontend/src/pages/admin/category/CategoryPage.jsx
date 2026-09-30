import { useState } from 'react'
import AdminPage from '../../../components/admin/AdminPage'
import DataTable from '../../../components/admin/DataTable'
import { Button, Card, ConfirmModal, Field, LinkButton, Summary } from '../../../components/admin/ui'
import { createCategory, deleteCategory, fetchCategories, updateCategory } from '../../../api/category'
import { useAsync } from '../../../hooks/useAsync'
import { formatNumber } from '../../../utils/format'
import './category.css'

// [ADMIN] 15 카테고리 관리 (Figma 311:458) — 왼쪽 목록(행 안에서 바로 수정) + 오른쪽 등록 카드
const NAME_MAX = 20

export default function CategoryPage() {
  const { loading, error, data, reload } = useAsync(fetchCategories, [])
  // 목록 쿼리가 products 기준 LEFT JOIN이라 카테고리 없는 상품 묶음(categoryId null)이 섞일 수 있어 제외한다
  const categories = (data ?? []).filter((c) => c.categoryId !== null && c.categoryId !== undefined)

  const [newName, setNewName] = useState('')
  const [editing, setEditing] = useState(null) // { categoryId, name }
  const [deleting, setDeleting] = useState(null) // CategoryResponse
  const [busy, setBusy] = useState(false)
  const [actionError, setActionError] = useState(null)

  // 변경 요청 공통 처리: 실패 메시지를 보여주고, 성공하면 목록을 다시 불러온다
  const run = async (action, failMessage) => {
    setBusy(true)
    setActionError(null)
    try {
      const result = await action()
      if (result === false) {
        setActionError(failMessage)
        return false
      }
      reload()
      return true
    } catch (err) {
      setActionError(err.message || failMessage)
      return false
    } finally {
      setBusy(false)
    }
  }

  // 백엔드는 길이·중복을 검사하지 않아 Figma 안내 문구대로 화면에서 막는다
  const validateName = (name, exceptId) => {
    if (!name) return '카테고리명을 입력하세요.'
    if (name.length > NAME_MAX) return `카테고리명은 ${NAME_MAX}자 이하로 입력하세요.`
    if (categories.some((c) => c.name === name && c.categoryId !== exceptId)) return '이미 있는 카테고리명입니다.'
    return null
  }

  const handleCreate = async (e) => {
    e.preventDefault()
    const name = newName.trim()
    const message = validateName(name)
    if (message) {
      setActionError(message)
      return
    }
    if (await run(() => createCategory(name), '카테고리를 추가하지 못했습니다.')) setNewName('')
  }

  const handleUpdate = async () => {
    const name = editing.name.trim()
    const message = validateName(name, editing.categoryId)
    if (message) {
      setActionError(message)
      return
    }
    if (await run(() => updateCategory(editing.categoryId, name), '카테고리를 수정하지 못했습니다.')) setEditing(null)
  }

  const handleDelete = async () => {
    const target = deleting
    setDeleting(null)
    // 삭제 API는 본문 없이 끝나므로 예외가 없으면 성공으로 본다
    await run(() => deleteCategory(target.categoryId).then(() => true), '카테고리를 삭제하지 못했습니다.')
  }

  const isEditing = (row) => editing?.categoryId === row.categoryId

  const columns = [
    { key: 'categoryId', header: 'ID', width: 80 },
    {
      key: 'name',
      header: '카테고리명',
      className: 'strong',
      render: (r) =>
        isEditing(r) ? (
          <input
            className="admin-input category-edit-input"
            value={editing.name}
            autoFocus
            onChange={(e) => setEditing((prev) => ({ ...prev, name: e.target.value }))}
            onKeyDown={(e) => {
              if (e.key === 'Enter') handleUpdate()
              if (e.key === 'Escape') setEditing(null)
            }}
          />
        ) : (
          r.name
        ),
    },
    { key: 'productCount', header: '등록 상품', align: 'right', render: (r) => `${formatNumber(r.productCount)}개` },
    {
      key: 'actions',
      header: '관리',
      width: 120,
      render: (r) =>
        isEditing(r) ? (
          <div className="admin-actions">
            <LinkButton tone="primary" disabled={busy} onClick={handleUpdate}>
              저장
            </LinkButton>
            <span className="admin-actions__sep">·</span>
            <LinkButton tone="muted" onClick={() => setEditing(null)}>
              취소
            </LinkButton>
          </div>
        ) : (
          <div className="admin-actions">
            <LinkButton disabled={busy} onClick={() => setEditing({ categoryId: r.categoryId, name: r.name ?? '' })}>
              수정
            </LinkButton>
            <span className="admin-actions__sep">·</span>
            {/* 상품이 연결된 카테고리는 FK 제약으로 백엔드 삭제가 실패(500)하므로 막는다 */}
            <LinkButton
              tone={r.productCount > 0 ? 'muted' : 'primary'}
              disabled={busy || r.productCount > 0}
              title={r.productCount > 0 ? '등록된 상품이 있는 카테고리는 삭제할 수 없습니다.' : undefined}
              onClick={() => setDeleting(r)}
            >
              삭제
            </LinkButton>
          </div>
        ),
    },
  ]

  const totalProducts = categories.reduce((sum, c) => sum + Number(c.productCount ?? 0), 0)

  return (
    <AdminPage title="카테고리 관리">
      <Summary label={`전체 ${formatNumber(categories.length)}개`}>
        <span>· 등록 상품 총 {formatNumber(totalProducts)}개</span>
      </Summary>

      <div className="category-layout">
        <Card noBody>
          <DataTable
            rowKey="categoryId"
            rows={categories}
            columns={columns}
            rowClassName={(r) => (isEditing(r) ? 'category-row--editing' : '')}
            loading={loading && !data}
            error={error}
            emptyText="등록된 카테고리가 없습니다."
          />
        </Card>

        <section className="admin-card category-side">
          <form className="admin-stack" onSubmit={handleCreate}>
            <h2 className="category-side__title">카테고리 등록</h2>
            <Field label="카테고리명" hint={`최대 ${NAME_MAX}자 · 이미 있는 이름은 등록할 수 없어요`}>
              <input
                className="admin-input"
                placeholder="예: 도자기·공예"
                maxLength={NAME_MAX}
                value={newName}
                onChange={(e) => setNewName(e.target.value)}
              />
            </Field>
            <Button variant="primary" size="lg" type="submit" className="admin-btn--block" disabled={busy}>
              + 카테고리 등록
            </Button>
            {actionError && <p className="category-error">{actionError}</p>}
          </form>
          <div className="category-guide">
            <strong>안내</strong>
            <ul>
              <li>ID는 등록 시 자동으로 부여됩니다.</li>
              <li>이름은 목록의 [수정]을 눌러 바로 변경할 수 있어요.</li>
              <li>등록된 상품이 있는 카테고리는 삭제할 수 없어요. 상품을 다른 카테고리로 옮긴 뒤 삭제해 주세요.</li>
            </ul>
          </div>
        </section>
      </div>

      <ConfirmModal
        open={Boolean(deleting)}
        title="카테고리 삭제"
        message={
          deleting
            ? `'${deleting.name}' 카테고리를 삭제하시겠습니까?`
            : ''
        }
        confirmText="삭제"
        danger
        onConfirm={handleDelete}
        onCancel={() => setDeleting(null)}
      />
    </AdminPage>
  )
}
