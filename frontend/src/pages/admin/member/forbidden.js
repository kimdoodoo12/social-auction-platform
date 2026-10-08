export const FORBIDDEN_PATH = '/forbidden'

export function redirectToForbidden() {
  if (window.location.pathname !== FORBIDDEN_PATH) {
    window.location.replace(FORBIDDEN_PATH)
  }
}
