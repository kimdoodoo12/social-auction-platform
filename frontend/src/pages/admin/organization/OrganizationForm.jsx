import { useState } from 'react'
import { Button, Card, Field } from '../../../components/admin/ui'
import './organization.css'

// 09 기관 등록 / 10 기관 수정 공통 폼. 필드는 OrganizationInfoRequest와 같다.
// 파일 업로드 API가 없어 이미지/협약서/사업자등록 정보는 문자열(경로/번호)로 입력받는다.

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

// onSubmit(request) → Promise<boolean>. true면 onSuccess 호출.
export default function OrganizationForm({ initial, submitLabel, onSubmit, onSuccess, onCancel }) {
  const [form, setForm] = useState(() => toFormState(initial))
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState(null)

  const bind = (key) => ({
    value: form[key],
    onChange: (e) => setForm((f) => ({ ...f, [key]: e.target.value })),
  })

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!form.name.trim()) {
      setError('기관명을 입력하세요.')
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
    <form className="org-form" onSubmit={handleSubmit}>
      <Card title="기본 정보">
        <div className="admin-form-grid">
          <Field label="기관명" required className="span-2">
            <input className="admin-input" placeholder="기관명을 입력하세요" {...bind('name')} />
          </Field>
          <Field label="기관 소개" className="span-2">
            <textarea className="admin-textarea" placeholder="기관 소개를 입력하세요" {...bind('description')} />
          </Field>
          <Field label="주소" className="span-2">
            <input className="admin-input" placeholder="주소를 입력하세요" {...bind('address')} />
          </Field>
          <Field label="사업자등록번호">
            <input className="admin-input" placeholder="000-00-00000" {...bind('businessRegistration')} />
          </Field>
          <Field label="기관 이미지 경로" hint="이미지 업로드 API가 없어 경로를 직접 입력합니다.">
            <input className="admin-input" placeholder="/uploads/organizations/..." {...bind('organizationImage')} />
          </Field>
          <Field label="담당자">
            <input className="admin-input" placeholder="담당자 이름" {...bind('manager')} />
          </Field>
          <Field label="담당자 연락처">
            <input className="admin-input" placeholder="02-000-0000" {...bind('managerPhone')} />
          </Field>
        </div>
      </Card>

      <Card title="협약 정보">
        <div className="admin-form-grid">
          <Field label="협약 상태">
            <select className="admin-select" {...bind('agreementStatus')}>
              <option value="">선택 안 함</option>
              <option value="true">협약</option>
              <option value="false">미협약</option>
            </select>
          </Field>
          <Field label="협약일">
            <input type="date" className="admin-input" {...bind('agreementDate')} />
          </Field>
          <Field label="협약 내용" className="span-2">
            <textarea className="admin-textarea" placeholder="협약 기간, 지원 비율 등" {...bind('agreementInfo')} />
          </Field>
          <Field label="협약서 파일 경로" className="span-2" hint="파일 업로드 API가 없어 경로를 직접 입력합니다.">
            <input className="admin-input" placeholder="/uploads/agreements/..." {...bind('agreementFile')} />
          </Field>
        </div>
      </Card>

      <div className="admin-actions admin-actions--end">
        {error && <span className="org-form__error">{error}</span>}
        <Button onClick={onCancel} disabled={submitting}>
          취소
        </Button>
        <Button variant="primary" type="submit" disabled={submitting}>
          {submitting ? '저장 중...' : submitLabel}
        </Button>
      </div>
    </form>
  )
}
