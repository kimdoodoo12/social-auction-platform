import { api, toPage } from '../../api/client'

const root = '/ieum/user/product'
export const catalogApi = {
  categories: () => api.get(`${root}/categories`),
  section: (type) => api.get(`${root}/${type}`),
  list: async (params) => toPage(await api.get(`${root}/list`, params)),
}
