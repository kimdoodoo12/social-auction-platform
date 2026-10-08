import { resolveImageUrl } from '../../../api/product'
import { useEffect, useRef, useState } from 'react'
import { fetchCategoryOptions, fetchOrganizationOptions, PRODUCT_LIMITS } from '../../../api/product'
import ImageBox from '../../../components/admin/ImageBox'
import { Button, Callout, Card, Field, FormActionBar, StepList } from '../../../components/admin/ui'
import { useAsync } from '../../../hooks/useAsync'

// 05 상품 등록 / 06 상품 수정 공용 폼 (Figma 52:200). 요청 본문은 백엔드 ProductDto 형태로 만든다.
// initial: { productId, name, organizationId, categoryId, startPrice, description, background, images: [{ imageId, image, sortOrder }] }
// Figma 항목 중 백엔드 DTO에 없는 공개 여부·최소 입찰 단위·간단한 설명 입력칸은 숨겼다(사용자 결정).

const MAX_INT = 2147483647

const AFTER_STEPS = ['등록 완료 → 상태 「경매 대기」', '사용자 최초 입찰 → 경매 자동 시작', '24시간 경과 → 최고가 입찰자 자동 낙찰']

// 백엔드 ProductService.saveProduct / updateProduct 검증과 같은 규칙
function validate(values) {
  const name = values.name.trim()
  if (!name) return '상품명을 입력해주세요.'
  if (name.length > PRODUCT_LIMITS.name) return `상품명은 ${PRODUCT_LIMITS.name}자 이하로 입력해주세요.`
  if (!values.organizationId || !values.categoryId) return '제작 기관과 카테고리를 선택해주세요.'
  const price = Number(values.startPrice)
  if (values.startPrice === '' || !Number.isInteger(price) || price < 0 || price > MAX_INT) {
    return '시작 가격은 0 이상의 금액을 입력해주세요.'
  }
  if (values.description.length > PRODUCT_LIMITS.text) return `상품 설명은 ${PRODUCT_LIMITS.text}자 이하로 입력해주세요.`
  if (values.background.length > PRODUCT_LIMITS.text) return `제작 배경은 ${PRODUCT_LIMITS.text}자 이하로 입력해주세요.`
  if (!values.images.some((img) => img.sortOrder === 1 && (img.file || img.image))) {
    return '1번 대표 이미지를 선택해주세요.'
  }
  return null
}

function toProductDto(values) {
  return {
    productId: values.productId,
    name: values.name.trim(),
    organizationId: Number(values.organizationId),
    categoryId: Number(values.categoryId),
    startPrice: Number(values.startPrice),
    description: values.description || null,
    background: values.background || null,
    images: values.images.filter((img) => img.file)
      .map(({ imageId, file, sortOrder }) => ({ imageId, file, sortOrder })),
  }
}

// 등록과 수정에서 같은 위치의 사진을 선택하고 미리보기로 표시한다.
function UploadSlots({ images, onChange, disabled }) {
  const inputs = useRef([])
  const previews = useRef(new Map())
  const [error, setError] = useState(null)
  useEffect(() => {
    const urls = previews.current
    return () => { urls.forEach((url) => URL.revokeObjectURL(url)); urls.clear() }
  }, [])

  const selectFile = (sortOrder, event) => {
    const file = event.target.files?.[0]
    event.target.value = ''
    if (!file) return
    if (!file.type.startsWith('image/') || file.size === 0) {
      setError('비어 있지 않은 이미지 파일을 선택해주세요.')
      return
    }
    setError(null)
    const oldUrl = previews.current.get(sortOrder)
    if (oldUrl) URL.revokeObjectURL(oldUrl)
    const preview = URL.createObjectURL(file)
    previews.current.set(sortOrder, preview)
    onChange([...images.filter((img) => img.sortOrder !== sortOrder), { ...images.find((img) => img.sortOrder === sortOrder), file, sortOrder, preview }]
      .sort((a, b) => a.sortOrder - b.sortOrder))
  }
  const removeFile = (sortOrder) => {
    const url = previews.current.get(sortOrder)
    if (url) URL.revokeObjectURL(url)
    previews.current.delete(sortOrder)
    onChange(images.flatMap((img) => {
      if (img.sortOrder !== sortOrder) return [img]
      return img.imageId != null ? [{ ...img, file: undefined, preview: undefined }] : []
    }))
  }
  const slot = (sortOrder) => {
    const image = images.find((img) => img.sortOrder === sortOrder)
    const label = sortOrder === 1 ? '대표 이미지 (필수)' : `${sortOrder}번 이미지`
    return (
      <div key={sortOrder} className="admin-stack">
        <input type="file" accept="image/*" hidden disabled={disabled}
          aria-label={`${label} 파일 선택`}
          ref={(element) => { inputs.current[sortOrder] = element }}
          onChange={(event) => selectFile(sortOrder, event)} />
        <button type="button" disabled={disabled} aria-label={`${label} 선택 또는 교체`}
          className={`product-slot${sortOrder === 1 ? ' product-slot--main' : ''}`}
          onClick={() => inputs.current[sortOrder]?.click()}>
          <ImageBox path={resolveImageUrl(image?.preview ?? image?.image)} alt={label} label={label} />
        </button>
        {image?.file && <small style={{ overflowWrap: 'anywhere' }}>{image.file.name}</small>}
        {image?.file && (image.imageId != null || sortOrder !== 1) && (
          <button type="button" disabled={disabled} onClick={() => removeFile(sortOrder)}
            aria-label={`${sortOrder}번 이미지 선택 취소`}>선택 취소</button>
        )}
      </div>
    )
  }
  return <div className="product-slots">
    {slot(1)}
    <div className="product-slots__row">
      {Array.from({ length: PRODUCT_LIMITS.images - 1 }, (_, i) => slot(i + 2))}
    </div>
    <small>칸을 눌러 사진을 선택하세요. 대표 사진은 필수이며, 다시 누르면 교체할 수 있습니다.</small>
    {error && <p role="alert" className="product-form-error">{error}</p>}
  </div>
}

export default function ProductForm({ initial, mode = 'create', onSubmit, onCancel }) {
  const [values, setValues] = useState(initial)
  const [error, setError] = useState(null)
  const [submitting, setSubmitting] = useState(false)

  const categories = useAsync(fetchCategoryOptions, [])
  const organizations = useAsync(fetchOrganizationOptions, [])

  const setField = (key) => (e) => setValues((prev) => ({ ...prev, [key]: e.target.value }))

  const handleSubmit = async (e) => {
    e.preventDefault()
    const message = validate(values)
    if (message) {
      setError(message)
      return
    }
    setError(null)
    setSubmitting(true)
    try {
      await onSubmit(toProductDto(values))
    } catch (err) {
      setError(err.message || '저장에 실패했습니다.')
      setSubmitting(false)
    }
  }

  const optionError = categories.error || organizations.error
  const isEdit = mode === 'edit'

  return (
    <form onSubmit={handleSubmit} noValidate className="admin-stack">
      <div className="admin-split product-split">
        <div className="admin-stack">
          <Card title="기본 정보" subtitle="협약 기관에서 제공받아 검수 완료한 상품만 등록합니다.">
            <div className="admin-form-grid">
              <Field label="상품명" required className="span-2">
                <input
                  className="admin-input"
                  value={values.name}
                  maxLength={PRODUCT_LIMITS.name}
                  placeholder="예) 손뜨개 울 머플러 (차콜)"
                  onChange={setField('name')}
                />
              </Field>
              <Field label="제작 기관" required>
                <select className="admin-select" value={values.organizationId} onChange={setField('organizationId')}>
                  <option value="">{organizations.loading ? '불러오는 중...' : '기관을 선택하세요'}</option>
                  {(organizations.data ?? []).map((o) => (
                    <option key={o.id} value={o.id}>
                      {o.name}
                    </option>
                  ))}
                </select>
              </Field>
              <Field label="카테고리" required>
                <select className="admin-select" value={values.categoryId} onChange={setField('categoryId')}>
                  <option value="">{categories.loading ? '불러오는 중...' : '카테고리를 선택하세요'}</option>
                  {(categories.data ?? []).map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.name}
                    </option>
                  ))}
                </select>
              </Field>
            </div>
            {optionError && <p className="product-form-error">선택지를 불러오지 못했습니다: {optionError.message}</p>}
          </Card>

          <Card title="가격 정보">
            <div className="admin-stack">
              <div className="admin-form-grid">
                <Field label="시작 가격" required hint="단위: 원">
                  <input
                    className="admin-input"
                    type="number"
                    min={0}
                    step={1}
                    value={values.startPrice}
                    placeholder="0"
                    onChange={setField('startPrice')}
                  />
                </Field>
              </div>
              <Callout title="경매 시작 시간은 설정하지 않습니다.">
                등록 시 상품은 자동으로 「경매 대기」 상태가 됩니다. 사용자의 최초 입찰이 발생하는 순간 경매가 시작되고, 그
                시점부터 24시간 뒤 자동 종료됩니다.
              </Callout>
            </div>
          </Card>

          <Card title="상품 설명">
            <div className="admin-stack">
              <Field label="상품 설명" hint={`${values.description.length} / ${PRODUCT_LIMITS.text}자`}>
                <textarea
                  className="admin-textarea"
                  value={values.description}
                  maxLength={PRODUCT_LIMITS.text}
                  placeholder="소재 · 크기 · 색상 · 세탁 방법 등 구매에 필요한 정보"
                  onChange={setField('description')}
                />
              </Field>
              <Field label="상품 제작 배경" hint={`${values.background.length} / ${PRODUCT_LIMITS.text}자`}>
                <textarea
                  className="admin-textarea"
                  value={values.background}
                  maxLength={PRODUCT_LIMITS.text}
                  placeholder="어떤 분들이, 어떤 과정으로 만들었는지 — 사회적 가치를 전달하는 영역입니다."
                  onChange={setField('background')}
                />
              </Field>
            </div>
          </Card>
        </div>

        <div className="admin-stack">
          <Card title="상품 이미지" subtitle={`대표 이미지 1장 · 추가 이미지 최대 ${PRODUCT_LIMITS.images - 1}장`}>
            <UploadSlots images={values.images} disabled={submitting}
              onChange={(images) => setValues((prev) => ({ ...prev, images }))} />
          </Card>
          {!isEdit && (
            <Card title="등록 후 흐름">
              <StepList steps={AFTER_STEPS} />
            </Card>
          )}
        </div>
      </div>

      <FormActionBar
        error={error}
        note={
          isEdit
            ? '* 표시는 필수 입력 항목입니다.'
            : '* 표시는 필수 입력 항목입니다. 등록 즉시 사용자 화면에 노출되며, 「경매 대기」 상태로 첫 입찰을 기다립니다.'
        }
      >
        <Button size="lg" onClick={onCancel} disabled={submitting}>
          취소
        </Button>
        {!isEdit && (
          <Button size="lg" pending>
            임시 저장
          </Button>
        )}
        <Button type="submit" variant="primary" size="lg" disabled={submitting}>
          {submitting ? '저장 중...' : isEdit ? '수정 저장' : '등록하기'}
        </Button>
      </FormActionBar>
    </form>
  )
}
