import { useState } from 'react'
import AdminPage from '../../../components/admin/AdminPage'
import DataTable from '../../../components/admin/DataTable'
import { Button, Card, ConfirmModal, LinkButton, Summary } from '../../../components/admin/ui'
import { createCategory, deleteCategory, fetchCategories, updateCategory } from '../../../api/category'
import { useAsync } from '../../../hooks/useAsync'
import { formatNumber } from '../../../utils/format'
import './category.css'

// [ADMIN] 15 카테고리 관리
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

  const handleCreate = async (e) => {
    e.preventDefault()
    const name = newName.trim()
    if (!name) {
      setActionError('카테고리명을 입력하세요.')
      return
    }
    if (await run(() => createCategory(name), '카테고리를 추가하지 못했습니다.')) setNewName('')
  }

  const handleUpdate = async () => {
    const name = editing.name.trim()
    if (!name) {
      setActionError('카테고리명을 입력하세요.')
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
      align: 'right',
      width: 160,
      render: (r) =>
        isEditing(r) ? (
          <div className="admin-actions admin-actions--end">
            <LinkButton tone="primary" disabled={busy} onClick={handleUpdate}>
              저장
            </LinkButton>
            <span className="admin-actions__sep">·</span>
            <LinkButton tone="muted" onClick={() => setEditing(null)}>
              취소
            </LinkButton>
          </div>
        ) : (
          <div className="admin-actions admin-actions--end">
            <LinkButton disabled={busy} onClick={() => setEditing({ categoryId: r.categoryId, name: r.name ?? '' })}>
              수정
            </LinkButton>
            <span className="admin-actions__sep">·</span>
            {/* 상품이 연결된 카테고리는 FK 제약으로 백엔드 삭제가 실패(500)하므로 막는다 */}
            <LinkButton
              tone="muted"
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

  return (
    <AdminPage title="카테고리 관리">
      <Card title="카테고리 추가">
        <form className="category-add" onSubmit={handleCreate}>
          <input
            className="admin-input"
            placeholder="새 카테고리명을 입력하세요"
            value={newName}
            onChange={(e) => setNewName(e.target.value)}
          />
          <Button variant="primary" type="submit" disabled={busy}>
            + 추가
          </Button>
        </form>
        {actionError && <p className="category-error">{actionError}</p>}
      </Card>

      <Summary label={`전체 ${formatNumber(categories.length)}개`} />

      <Card noBody>
        <DataTable
          rowKey="categoryId"
          rows={categories}
          columns={columns}
          loading={loading && !data}
          error={error}
          emptyText="등록된 카테고리가 없습니다."
        />
      </Card>

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
