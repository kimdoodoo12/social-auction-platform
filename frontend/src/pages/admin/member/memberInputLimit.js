export const MEMBER_MAX_BYTES = 15
const encoder = new TextEncoder()

export const exceedsMemberByteLimit = (value) => encoder.encode(value).length > MEMBER_MAX_BYTES

export function limitMemberInput(event) {
  // 한글 조합 중에는 값을 변경하지 않고 조합 완료 후 제한한다.
  if (event.nativeEvent.isComposing) return
  const input = event.currentTarget
  let value = ''
  let bytes = 0
  for (const character of input.value) {
    bytes += encoder.encode(character).length
    if (bytes > MEMBER_MAX_BYTES) break
    value += character
  }
  if (input.value !== value) input.value = value
}
