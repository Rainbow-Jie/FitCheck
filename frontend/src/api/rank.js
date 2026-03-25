import request from '@/utils/request'

export const rankApi = {
  list: (type) => request.post('/rank/list', { type })
}
