import logo from '../assets/ieum_logo.png'
import './brandLogo.css'

// 이음옥션 로고 (assets/ieum_logo.png). 원본 여백이 넓어 CSS로 가운데를 확대해 보여준다.
export default function BrandLogo({ size = 30, className = '' }) {
  return (
    <span className={`brand-logo ${className}`} style={{ width: size, height: size }} aria-hidden="true">
      <img src={logo} alt="" />
    </span>
  )
}
