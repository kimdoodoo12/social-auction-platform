// 목록 표
// columns: [{ key, header, align: 'left'|'right'|'center', width, className, render: (row, index) => node }]
// render가 없으면 row[key]를 그대로 표시한다.
export default function DataTable({ columns, rows, rowKey, onRowClick, loading, error, emptyText = '데이터가 없습니다.' }) {
  const getKey = (row, index) => (typeof rowKey === 'function' ? rowKey(row) : (row[rowKey] ?? index))

  let body
  if (loading || error || !rows || rows.length === 0) {
    const message = loading ? '불러오는 중...' : error ? error.message || '요청에 실패했습니다.' : emptyText
    body = (
      <tr>
        <td className="admin-table__empty" colSpan={columns.length}>
          {message}
        </td>
      </tr>
    )
  } else {
    body = rows.map((row, index) => (
      <tr
        key={getKey(row, index)}
        className={onRowClick ? 'clickable' : undefined}
        onClick={onRowClick ? () => onRowClick(row) : undefined}
      >
        {columns.map((col) => (
          <td key={col.key} className={[col.align ? `align-${col.align}` : '', col.className ?? ''].join(' ').trim() || undefined}>
            {col.render ? col.render(row, index) : (row[col.key] ?? '-')}
          </td>
        ))}
      </tr>
    ))
  }

  return (
    <div className="admin-table-wrap">
      <table className="admin-table">
        <thead>
          <tr>
            {columns.map((col) => (
              <th key={col.key} className={col.align ? `align-${col.align}` : undefined} style={col.width ? { width: col.width } : undefined}>
                {col.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>{body}</tbody>
      </table>
    </div>
  )
}
