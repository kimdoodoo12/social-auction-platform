import { useState } from 'react'
import { fetchCategoryOptions, fetchOrganizationOptions, PRODUCT_LIMITS } from '../../../api/product'
import { Button, Card, Field } from '../../../components/admin/ui'
import { useAsync } from '../../../hooks/useAsync'
import { ProductImage } from './ProductParts'

// 05 상품 등록 / 06 상품 수정 공용 폼. 요청 본문은 백엔드 ProductDto 형태로 만든다.
// initial: { productId, name, organizationId, categoryId, startPrice, description, background, images: [{ imageId, image }] }

const MAX_INT = 2147483647

// 백엔드 ProductService.saveProduct / updateProduct 검증과 같은 규칙
function validate(values) {
  const name = values.name.trim()
  if (!name) return '상품명을 입력해주세요.'
  if (name.length > PRODUCT_LIMITS.name) return `상품명은 ${PRODUCT_LIMITS.name}자 이하로 입력해주세요.`
  if (!values.organizationId || !values.categoryId) return '기관과 카테고리를 설정해주세요.'
  const price = Number(values.startPrice)
  if (values.startPrice === '' || !Number.isInteger(price) || price < 0 || price > MAX_INT) {
    return '시작가는 0 이상의 금액을 입력해주세요.'
  }
  if (values.description.length > PRODUCT_LIMITS.text) return `상품 설명은 ${PRODUCT_LIMITS.text}자 이하로 입력해주세요.`
  if (values.background.length > PRODUCT_LIMITS.text) return `상품 배경은 ${PRODUCT_LIMITS.text}자 이하로 입력해주세요.`
  // 기존 이미지(imageId 있음)는 삭제 API가 없어 경로를 비울 수 없다
  if (values.images.some((img) => img.imageId != null && !img.image.trim())) return '유효한 이미지 경로가 아닙니다.'
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
    // 새 이미지 중 빈 칸은 보내지 않는다
    images: values.images
      .filter((img) => img.imageId != null || img.image.trim())
      .map((img) => ({ imageId: img.imageId ?? null, image: img.image.trim() })),
  }
}

export default function ProductForm({ initial, submitLabel, onSubmit, onCancel, startPriceHint }) {
  const [values, setValues] = useState(initial)
  const [error, setError] = useState(null)
  const [submitting, setSubmitting] = useState(false)

  const categories = useAsync(fetchCategoryOptions, [])
  const organizations = useAsync(fetchOrganizationOptions, [])

  const setField = (key) => (e) => setValues((prev) => ({ ...prev, [key]: e.target.value }))

  const setImage = (index, image) =>
    setValues((prev) => ({ ...prev, images: prev.images.map((img, i) => (i === index ? { ...img, image } : img)) }))
  const addImage = () => setValues((prev) => ({ ...prev, images: [...prev.images, { imageId: null, image: '' }] }))
  const removeImage = (index) => setValues((prev) => ({ ...prev, images: prev.images.filter((_, i) => i !== index) }))

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

  return (
    <form onSubmit={handleSubmit} noValidate style={{ display: 'contents' }}>
      <Card title="기본 정보">
        <div className="admin-form-grid">
          <Field label="상품명" required hint={`${values.name.length} / ${PRODUCT_LIMITS.name}자`} className="span-2">
            <input
              className="admin-input"
              value={values.name}
              maxLength={PRODUCT_LIMITS.name}
              placeholder="상품명을 입력하세요"
              onChange={setField('name')}
            />
          </Field>
          <Field label="제작기관" required>
            <select className="admin-select" value={values.organizationId} onChange={setField('organizationId')}>
              <option value="">{organizations.loading ? '불러오는 중...' : '기관 선택'}</option>
              {(organizations.data ?? []).map((o) => (
                <option key={o.id} value={o.id}>
                  {o.name}
                </option>
              ))}
            </select>
          </Field>
          <Field label="카테고리" required>
            <select className="admin-select" value={values.categoryId} onChange={setField('categoryId')}>
              <option value="">{categories.loading ? '불러오는 중...' : '카테고리 선택'}</option>
              {(categories.data ?? []).map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
          </Field>
          <Field label="시작가 (원)" required hint={startPriceHint}>
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
        {optionError && <p className="product-form-error">선택지를 불러오지 못했습니다: {optionError.message}</p>}
      </Card>

      <Card title="상품 설명">
        <div className="admin-form-grid">
          <Field label="상품 설명" hint={`${values.description.length} / ${PRODUCT_LIMITS.text}자`} className="span-2">
            <textarea
              className="admin-textarea"
              value={values.description}
              maxLength={PRODUCT_LIMITS.text}
              placeholder="상품 설명을 입력하세요"
              onChange={setField('description')}
            />
          </Field>
          <Field label="제작 배경" hint={`${values.background.length} / ${PRODUCT_LIMITS.text}자`} className="span-2">
            <textarea
              className="admin-textarea"
              value={values.background}
              maxLength={PRODUCT_LIMITS.text}
              placeholder="상품의 제작 배경을 입력하세요"
              onChange={setField('background')}
            />
          </Field>
        </div>
      </Card>

      <Card
        title={`상품 이미지 (${values.images.length} / ${PRODUCT_LIMITS.images})`}
        actions={
          <Button size="sm" onClick={addImage} disabled={values.images.length >= PRODUCT_LIMITS.images}>
            + 이미지 경로 추가
          </Button>
        }
      >
        <div className="product-image-rows">
          {values.images.length === 0 && <span className="admin-field__hint">등록된 이미지가 없습니다.</span>}
          {values.images.map((img, index) => (
            <div className="product-image-row" key={img.imageId ?? `new-${index}`}>
              <ProductImage path={img.image.trim()} />
              <input
                className="admin-input"
                value={img.image}
                placeholder="/uploads/products/example.jpg"
                onChange={(e) => setImage(index, e.target.value)}
              />
              {img.imageId != null ? (
                <span className="product-image-row__tag">기존</span>
              ) : (
                <Button size="sm" onClick={() => removeImage(index)}>
                  삭제
                </Button>
              )}
            </div>
          ))}
          <span className="admin-field__hint">
            파일 업로드 API가 없어 이미지 경로를 직접 입력합니다. 기존 이미지는 경로 변경만 가능하고 삭제할 수 없습니다. 최대{' '}
            {PRODUCT_LIMITS.images}장.
          </span>
        </div>
      </Card>

      <div className="product-form-footer">
        <span className="product-form-error">{error}</span>
        <div className="admin-actions">
          <Button onClick={onCancel} disabled={submitting}>
            취소
          </Button>
          <Button type="submit" variant="primary" disabled={submitting}>
            {submitting ? '저장 중...' : submitLabel}
          </Button>
        </div>
      </div>
    </form>
  )
}
