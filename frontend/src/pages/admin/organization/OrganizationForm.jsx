import { useState } from 'react'
import { organizationFilePath, originalFileName } from '../../../api/organization'
import ImageBox from '../../../components/admin/ImageBox'
import { Button, Card, Field, FormActionBar, StepList } from '../../../components/admin/ui'
import './organization.css'

// 09 기관 등록 / 10 기관 수정 공통 폼 (Figma 74:325). 필드는 OrganizationInfoRequest와 같다.
// Figma 항목 중 DTO에 없는 기관 유형·직함·이메일 입력칸은 숨겼다(사용자 결정). 비고 → agreementInfo.
// 로고(organizationImageFile)·협약서(agreementFile)는 multipart 파일로 함께 보낸다.

const MB = 1024 * 1024
// Figma 안내 문구 기준 제한 (백엔드 multipart 최대 10MB)
const LOGO_LIMIT = { size: 2 * MB, accept: 'image/png,image/jpeg' }
const AGREEMENT_LIMIT = { size: 10 * MB, accept: 'application/pdf' }

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
    // 이미 저장된 파일명 (수정 화면 표시용)
    agreementFileName: info?.agreementFileName ?? null,
    organizationImageFileName: info?.organizationImageFileName ?? null,
    businessRegistration: info?.businessRegistration ?? '',
  }
}

const emptyToNull = (value) => {
  const v = value.trim()
  return v === '' ? null : v
}

// 폼 상태 → OrganizationInfoRequest의 문자열 필드. 빈 값(null)은 FormData에 넣지 않는다.
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
    // 기존 파일명도 함께 보낸다(백엔드가 새 파일이 없을 때 유지하는 데 쓸 수 있도록)
    agreementFileName: form.agreementFileName,
    organizationImageFileName: form.organizationImageFileName,
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

// 파일 선택 검사: 형식과 크기
function checkFile(file, limit, label) {
  const types = limit.accept.split(',')
  if (!types.includes(file.type)) return `${label} 형식이 올바르지 않습니다.`
  if (file.size > limit.size) return `${label}은(는) ${limit.size / MB}MB 이하만 올릴 수 있습니다.`
  return null
}

// onSubmit(fields, files) → Promise<boolean>. true면 onSuccess 호출.
export default function OrganizationForm({ initial, mode = 'create', onSubmit, onSuccess, onCancel }) {
  const [form, setForm] = useState(() => toFormState(initial))
  const [logoFile, setLogoFile] = useState(null)
  const [logoPreview, setLogoPreview] = useState(null) // 새로 고른 로고의 미리보기 object URL
  const [agreementFile, setAgreementFile] = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState(null)
  const isEdit = mode === 'edit'

  const pickLogo = (e) => {
    const file = e.target.files?.[0]
    e.target.value = ''
    if (!file) return
    const message = checkFile(file, LOGO_LIMIT, '로고 이미지')
    if (message) {
      setError(message)
      return
    }
    setError(null)
    if (logoPreview) URL.revokeObjectURL(logoPreview)
    setLogoFile(file)
    setLogoPreview(URL.createObjectURL(file))
  }

  const clearLogo = () => {
    if (logoPreview) URL.revokeObjectURL(logoPreview)
    setLogoFile(null)
    setLogoPreview(null)
  }

  const pickAgreement = (e) => {
    const file = e.target.files?.[0]
    e.target.value = ''
    if (!file) return
    const message = checkFile(file, AGREEMENT_LIMIT, '협약서')
    if (message) {
      setError(message)
      return
    }
    setError(null)
    setAgreementFile(file)
  }

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
      const ok = await onSubmit(toRequest(form), { organizationImageFile: logoFile, agreementFile })
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
            <div className="admin-stack org-file-stack">
              <label className="org-logo-box">
                <input type="file" accept={LOGO_LIMIT.accept} className="org-file-hidden" onChange={pickLogo} />
                {logoPreview ? (
                  <img className="admin-image" src={logoPreview} alt="새 로고 미리보기" />
                ) : (
                  <ImageBox path={organizationFilePath(form.organizationImageFileName)} label="로고 업로드 (선택)" />
                )}
              </label>
              <div className="org-file-meta">
                <span>JPG · PNG / 2MB 이하 / 정사각 권장</span>
                {logoFile && (
                  <button type="button" className="admin-link-btn admin-link-btn--muted" onClick={clearLogo}>
                    선택 취소
                  </button>
                )}
              </div>
            </div>
          </Card>

          <Card title="협약서 첨부">
            <div className="admin-stack org-file-stack">
              <label className="org-file-row">
                <input type="file" accept={AGREEMENT_LIMIT.accept} className="org-file-hidden" onChange={pickAgreement} />
                <span className={agreementFile || form.agreementFileName ? 'org-file-name' : 'org-file-name empty'}>
                  {agreementFile?.name ?? originalFileName(form.agreementFileName) ?? '협약서 파일을 첨부하세요'}
                </span>
                <span className="org-file-pick">파일 선택</span>
              </label>
              <span className="admin-field__hint">PDF · 10MB 이하 / 내부 보관용이며 사용자에게 노출되지 않습니다.</span>
            </div>
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
