import { useNavigate, useParams } from 'react-router-dom'
import {
  agreementDownloadUrl,
  fetchOrganizationDetail,
  organizationFilePath,
  originalFileName,
} from '../../../api/organization'
import AdminPage from '../../../components/admin/AdminPage'
import DataTable from '../../../components/admin/DataTable'
import ImageBox from '../../../components/admin/ImageBox'
import { AsyncBoundary, BackLink, Button, Card } from '../../../components/admin/ui'
import { useAsync } from '../../../hooks/useAsync'
import { formatDate, formatNumber, formatPrice } from '../../../utils/format'
import { AgreementBadge } from './agreement'
import './organization.css'

// [ADMIN] 08 기관 상세 (Figma 52:367) — GET /ieum/admin/organization/detail/{id}
// Figma는 기관 정보를 등록 폼과 같은 모양(읽기 전용)으로 보여준다.
// 사업자등록번호·협약서·비고는 Figma에 없지만 백엔드 값이라 카드 아래에 추가했다(사용자 결정).
// 누적 낙찰은 데이터가 없어 숨겼고, 협약 해지는 API가 없어 비활성이다.

const STATUS_LABEL = { 대기: '대기', 진행: '진행 중', 완료: '종료', '판매 중지': '판매 중지' }

function ReadField({ label, value, multiline, className = '' }) {
  return (
    <div className={`admin-field ${className}`.trim()}>
      <span className="admin-field__label">{label}</span>
      <div className={`org-read ${multiline ? 'org-read--multi' : ''}`.trim()}>{value || '-'}</div>
    </div>
  )
}

export default function OrganizationDetailPage() {
  const { organizationId } = useParams()
  const navigate = useNavigate()
  const { loading, error, data } = useAsync(() => fetchOrganizationDetail(organizationId), [organizationId])

  const info = data?.info
  const products = data?.products ?? []

  return (
    <AdminPage title={info ? `기관 상세 · ${info.name}` : '기관 상세'}>
      <BackLink to="/admin/organizations">기관 관리로 돌아가기</BackLink>

      <AsyncBoundary loading={loading && !data} error={error}>
        {info && (
          <>
            <section className="admin-card admin-hero">
              <ImageBox path={organizationFilePath(info.organizationImageFileName)} alt={info.name} className="admin-hero__thumb org-logo" label="" />
              <div className="admin-hero__body">
                <div className="admin-hero__meta">{info.organizationId}</div>
                <div className="org-hero-title">
                  <span className="admin-hero__title">{info.name}</span>
                  <AgreementBadge status={info.agreementStatus} />
                </div>
                <div className="admin-hero__sub">
                  협약일 {formatDate(info.agreementDate)} · 등록 상품 {formatNumber(products.length)}개
                </div>
              </div>
              <div className="admin-actions">
                <Button size="lg" onClick={() => navigate(`/admin/organizations/${organizationId}/edit`)}>
                  정보 수정
                </Button>
                <Button size="lg" pending>
                  협약 해지
                </Button>
              </div>
            </section>

            <div className="org-detail">
              <Card title="기관 정보" subtitle="등록 화면에서도 동일한 폼을 사용합니다.">
                <div className="admin-form-grid">
                  <ReadField label="기관명" value={info.name} className="span-2" />
                  <ReadField label="기관 소개" value={info.description} multiline className="span-2" />
                  <ReadField label="주소" value={info.address} className="span-2" />
                  <ReadField label="담당자" value={info.manager} />
                  <ReadField label="연락처" value={info.managerPhone} />
                  <ReadField label="협약일" value={formatDate(info.agreementDate)} />
                  <ReadField
                    label="협약 상태"
                    value={info.agreementStatus === true ? '협약 중' : info.agreementStatus === false ? '협약 종료' : '-'}
                  />
                  <ReadField label="사업자등록번호" value={info.businessRegistration} />
                  <ReadField
                    label="협약서"
                    value={
                      info.agreementFileName && (
                        <a className="org-download" href={agreementDownloadUrl(info.organizationId)}>
                          {originalFileName(info.agreementFileName)}
                        </a>
                      )
                    }
                  />
                  <ReadField label="비고" value={info.agreementInfo} multiline className="span-2" />
                </div>
              </Card>

              <Card title="등록 상품" subtitle={`총 ${formatNumber(products.length)}개 · 최신순`} noBody>
                <DataTable
                  rowKey="productId"
                  rows={products}
                  emptyText="등록된 상품이 없습니다."
                  onRowClick={(r) => navigate(`/admin/products/${r.productId}`, { state: { status: r.auctionStatus } })}
                  columns={[
                    { key: 'productId', header: '상품번호', width: 70 },
                    { key: 'name', header: '상품명', className: 'strong' },
                    { key: 'startPrice', header: '시작가', align: 'right', render: (r) => formatPrice(r.startPrice) },
                    {
                      key: 'currentPrice',
                      header: '현재가',
                      align: 'right',
                      render: (r) => (r.currentPrice == null ? '—' : formatPrice(r.currentPrice)),
                    },
                    { key: 'auctionStatus', header: '상태', render: (r) => STATUS_LABEL[r.auctionStatus] ?? r.auctionStatus ?? '-' },
                    { key: 'createdAt', header: '등록일', align: 'right', render: (r) => formatDate(r.createdAt) },
                  ]}
                />
              </Card>
            </div>
          </>
        )}
      </AsyncBoundary>
    </AdminPage>
  )
}
