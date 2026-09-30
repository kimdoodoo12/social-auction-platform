import { useState } from 'react'
import ImageBox from '../../../components/admin/ImageBox'
import { Button, Card, Field, FormActionBar, StepList } from '../../../components/admin/ui'
import './organization.css'

// 09 기관 등록 / 10 기관 수정 공통 폼 (Figma 74:325). 필드는 OrganizationInfoRequest와 같다.
// Figma 항목 중 DTO에 없는 기관 유형·직함·이메일 입력칸은 숨겼다(사용자 결정).
// 파일 업로드 API가 없어 로고·협약서는 경로 문자열로 입력받는다. 비고 → agreementInfo.

const AFTER_STEPS = [
  '기관 등록 → 선택한 협약 상태로 저장',
  '협약서 확인 후 「협약 중」으로 변경',
  '협약 중 기관만 상품 등록 대상이 됩니다',
]

// OrganizationInfoResponse → 폼 상태 (null은 빈 문자열로)
function toFormState(info) {
  return {
    name: info?.name ?? '',
    description: info?.description ?? '',
    address: info?.address ?? '',
    manager: info?.manager ?? '',
    managerPhone: info?.managerPhone ?? '',
    // LocalDateTime "2026-01-10T10:00:00" → date input "2026-01-10"
    agreementDate: info?.agreementDate ? String(info.agreementDate).slice(0, 10) : '',
    // 기존 시간 정보를 보존하기 위해 원본을 보관한다
    agreementDateOriginal: info?.agreementDate ?? null,
    agreementStatus: info?.agreementStatus === true ? 'true' : info?.agreementStatus === false ? 'false' : '',
    agreementInfo: info?.agreementInfo ?? '',
    agreementFile: info?.agreementFile ?? '',
    organizationImage: info?.organizationImage ?? '',
    businessRegistration: info?.businessRegistration ?? '',
  }
}

const emptyToNull = (value) => {
  const v = value.trim()
  return v === '' ? null : v
}

// 폼 상태 → OrganizationInfoRequest. 수정 API는 모든 필드를 덮어쓰므로 빈 값도 null로 보낸다.
function toRequest(form) {
  let agreementDate = null
  if (form.agreementDate) {
    const original = form.agreementDateOriginal ? String(form.agreementDateOriginal) : ''
    agreementDate = original.startsWith(form.agreementDate) ? original : `${form.agreementDate}T00:00:00`
  }
  return {
    name: form.name.trim(),
    description: emptyToNull(form.description),
    address: emptyToNull(form.address),
    manager: emptyToNull(form.manager),
    managerPhone: emptyToNull(form.managerPhone),
    agreementDate,
    agreementStatus: form.agreementStatus === '' ? null : form.agreementStatus === 'true',
    agreementInfo: emptyToNull(form.agreementInfo),
    agreementFile: emptyToNull(form.agreementFile),
    organizationImage: emptyToNull(form.organizationImage),
    businessRegistration: emptyToNull(form.businessRegistration),
  }
}

// Figma에서 * 표시된 필수 항목
const REQUIRED = [
  ['name', '기관명'],
  ['description', '기관 소개'],
  ['address', '주소'],
  ['manager', '담당자명'],
  ['managerPhone', '연락처'],
  ['agreementDate', '협약일'],
  ['agreementStatus', '협약 상태'],
]

// onSubmit(request) → Promise<boolean>. true면 onSuccess 호출.
export default function OrganizationForm({ initial, mode = 'create', onSubmit, onSuccess, onCancel }) {
  const [form, setForm] = useState(() => toFormState(initial))
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState(null)
  const isEdit = mode === 'edit'

  const bind = (key) => ({
    value: form[key],
    onChange: (e) => setForm((f) => ({ ...f, [key]: e.target.value })),
  })

  const handleSubmit = async (e) => {
    e.preventDefault()
    const missing = REQUIRED.find(([key]) => !String(form[key]).trim())
    if (missing) {
      setError(`${missing[1]}을(를) 입력하세요.`)
      return
    }
    setSubmitting(true)
    setError(null)
    try {
      const ok = await onSubmit(toRequest(form))
      if (ok === true) {
        onSuccess()
        return
      }
      setError('저장에 실패했습니다.')
    } catch (err) {
      setError(err.message || '저장에 실패했습니다.')
    }
    setSubmitting(false)
  }

  return (
    <form className="admin-stack" onSubmit={handleSubmit} noValidate>
      <div className="admin-split org-split">
        <div className="admin-stack">
          <Card title="기관 정보" subtitle="장애인 및 사회적 약자 관련 시설·단체만 등록합니다.">
            <div className="admin-form-grid">
              <Field label="기관명" required className="span-2">
                <input className="admin-input" placeholder="예) 온기공방" {...bind('name')} />
              </Field>
              <Field label="기관 소개" required className="span-2">
                <textarea
                  className="admin-textarea org-textarea"
                  placeholder="어떤 분들이 일하는 곳인지, 어떤 훈련 과정을 운영하는지 적어 주세요. 사용자 상세 페이지에 그대로 노출됩니다."
                  {...bind('description')}
                />
              </Field>
              <Field label="사업자등록번호">
                <input className="admin-input" placeholder="000-00-00000" {...bind('businessRegistration')} />
              </Field>
              <Field label="주소" required className="span-2">
                <input className="admin-input" placeholder="도로명 주소를 입력하세요" {...bind('address')} />
              </Field>
            </div>
          </Card>

          <Card title="담당자 정보">
            <div className="admin-form-grid">
              <Field label="담당자명" required>
                <input className="admin-input" placeholder="예) 이정아" {...bind('manager')} />
              </Field>
              <Field label="연락처" required>
                <input className="admin-input" placeholder="02-000-0000" {...bind('managerPhone')} />
              </Field>
            </div>
          </Card>

          <Card title="협약 정보">
            <div className="admin-form-grid">
              <Field label="협약일" required>
                <input className="admin-input" type="date" {...bind('agreementDate')} />
              </Field>
              <Field label="협약 상태" required>
                <select className="admin-select" {...bind('agreementStatus')}>
                  <option value="">선택하세요</option>
                  <option value="true">협약 중</option>
                  <option value="false">협약 종료</option>
                </select>
              </Field>
              <Field label="비고" className="span-2">
                <textarea className="admin-textarea org-textarea--sm" placeholder="정산 조건, 배송 주체 등 내부 메모" {...bind('agreementInfo')} />
              </Field>
            </div>
          </Card>
        </div>

        <div className="admin-stack">
          <Card title="기관 로고" subtitle="사용자 상품 상세 페이지에 노출됩니다.">
            <div className="admin-stack">
              <div className="org-logo-box">
                <ImageBox path={form.organizationImage.trim()} label="로고 (선택)" />
              </div>
              <Field label="로고 이미지 경로" hint="파일 업로드 API가 없어 경로를 입력합니다.">
                <input className="admin-input" placeholder="/images/파일명.png" {...bind('organizationImage')} />
              </Field>
            </div>
          </Card>

          <Card title="협약서 첨부">
            <Field hint="PDF · 내부 보관용이며 사용자에게 노출되지 않습니다.">
              <input className="admin-input org-file-input" placeholder="협약서 파일 경로를 입력하세요" {...bind('agreementFile')} />
            </Field>
          </Card>

          {!isEdit && (
            <Card title="등록 후 흐름">
              <StepList steps={AFTER_STEPS} />
            </Card>
          )}
        </div>
      </div>

      <FormActionBar error={error} note="* 표시는 필수 입력 항목입니다.">
        <Button size="lg" onClick={onCancel} disabled={submitting}>
          취소
        </Button>
        <Button type="submit" variant="primary" size="lg" disabled={submitting}>
          {submitting ? '저장 중...' : isEdit ? '수정 저장' : '등록하기'}
        </Button>
      </FormActionBar>
    </form>
  )
}
