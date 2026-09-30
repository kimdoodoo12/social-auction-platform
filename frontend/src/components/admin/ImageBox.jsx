import { useState } from 'react'
import { BASE_URL } from '../../api/client'

// 백엔드가 저장한 이미지 경로("/images/a.png")를 백엔드 주소 기준으로 보여준다.
// 경로가 없거나 불러오지 못하면 같은 크기의 "No image" 자리를 보여준다(Figma 틀 유지).
function resolveAssetUrl(path) {
  if (!path) return null
  try {
    return new URL(path, BASE_URL).href
  } catch {
    return null
  }
}

export default function ImageBox({ path, alt = '', className = '', label = 'No image' }) {
  const [failedPath, setFailedPath] = useState(null)
  const src = resolveAssetUrl(path)
  if (!src || failedPath === path) {
    return (
      <span className={`admin-image admin-image--empty ${className}`.trim()} role="img" aria-label="이미지 없음">
        {label}
      </span>
    )
  }
  return <img className={`admin-image ${className}`.trim()} src={src} alt={alt} onError={() => setFailedPath(path)} />
}
