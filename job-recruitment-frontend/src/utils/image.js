export const getImageUrl = (url) => {
  if (!url) return ''
  if (/^https?:\/\//i.test(url)) return url
  if (url.startsWith('/uploads/')) {
    return `${import.meta.env.VITE_API_ORIGIN || 'http://localhost:8080'}${url}`
  }
  if (url.startsWith('/')) return url
  return `/${url}`
}
