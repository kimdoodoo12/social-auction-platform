// 회원 상태(MemberEntity.role) 값. schema.sql 주석 기준: 정상, 정지, 잠금, 탈퇴
export const MEMBER_ROLES = ['정상', '정지', '잠금', '탈퇴']

export const SUSPENDED_ROLE = '정지'

export function memberRoleTone(role) {
  if (role === '정상') return 'soft'
  if (role === '정지') return 'dark'
  return 'muted'
}
