// 登录 token 的 localStorage 存储键
const TOKEN_KEY = 'cloud_gallery_token'

// 读取 token
export function getToken(): string {
  return localStorage.getItem(TOKEN_KEY) ?? ''
}

// 保存 token
export function setToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token)
}

// 清除 token
export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
}
