import { useNavigate, useParams } from 'react-router-dom'
import AdminPage from '../../../components/admin/AdminPage'
import DataTable from '../../../components/admin/DataTable'
import { AsyncBoundary, Badge, Button, Card, DetailList } from '../../../components/admin/ui'
import { fetchOrganizationDetail } from '../../../api/organization'
import { useAsync } from '../../../hooks/useAsync'
import { formatDate, formatNumber, formatPrice } from '../../../utils/format'
import { AgreementBadge } from './agreement'
import './organization.css'

// [ADMIN] 08 기관 상세
// auction_status 값: 대기 / 진행 / 완료 / 판매 중지 (backend sample.sql, ProductService 기준)
const auctionTone = (status) => {
  if (status === '진행') return 'primary'
  if (status === '대기') return 'outline'
  if (status === '완료' || status === '판매 중지') return 'dark'
  return 'muted'
}

export default function OrganizationDetailPage() {
  const { organizationId } = useParams()
  const navigate = useNavigate()
  const { loading, error, data } = useAsync(() => fetchOrganizationDetail(organizationId), [organizationId])

  const info = data?.info
  const products = data?.products ?? []

  const productColumns = [
    { key: 'name', header: '상품명', className: 'strong' },
    { key: 'startPrice', header: '시작가', align: 'right', render: (r) => formatPrice(r.startPrice) },
    { key: 'currentPrice', header: '현재가', align: 'right', render: (r) => formatPrice(r.currentPrice) },
    {
      key: 'auctionStatus',
      header: '경매 상태',
      render: (r) => (r.auctionStatus ? <Badge tone={auctionTone(r.auctionStatus)}>{r.auctionStatus}</Badge> : '-'),
    },
    { key: 'createdAt', header: '등록일', align: 'right', render: (r) => formatDate(r.createdAt) },
  ]

  return (
    <AdminPage
      title="기관 상세"
      back="/admin/organizations"
      actions={
        info && (
          <Button variant="primary" onClick={() => navigate(`/admin/organizations/${organizationId}/edit`)}>
            기관 수정
          </Button>
        )
      }
    >
      <AsyncBoundary loading={loading && !data} error={error}>
        {info && (
          <>
            <Card title={info.name}>
              <div className="org-detail__head">
                <AgreementBadge status={info.agreementStatus} />
                {info.description && <p className="org-detail__desc">{info.description}</p>}
              </div>
            </Card>

            <div className="org-detail__grid">
              <Card title="기본 정보">
                <DetailList
                  items={[
                    { label: '기관명', value: info.name },
                    { label: '주소', value: info.address },
                    { label: '사업자등록번호', value: info.businessRegistration },
                    { label: '담당자', value: info.manager },
                    { label: '담당자 연락처', value: info.managerPhone },
                    { label: '기관 이미지', value: info.organizationImage },
                  ]}
                />
              </Card>
              <Card title="협약 정보">
                <DetailList
                  items={[
                    { label: '협약 상태', value: <AgreementBadge status={info.agreementStatus} /> },
                    { label: '협약일', value: formatDate(info.agreementDate) },
                    { label: '협약 내용', value: info.agreementInfo },
                    { label: '협약서 파일', value: info.agreementFile },
                  ]}
                />
              </Card>
            </div>

            <Card title={`등록 상품 ${formatNumber(products.length)}개`} noBody>
              <DataTable
                rowKey="productId"
                rows={products}
                columns={productColumns}
                emptyText="등록된 상품이 없습니다."
                onRowClick={(row) => navigate(`/admin/products/${row.productId}`)}
              />
            </Card>
          </>
        )}
      </AsyncBoundary>
    </AdminPage>
  )
}
