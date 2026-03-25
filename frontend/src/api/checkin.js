import request from '@/utils/request'

export const checkinApi = {
  add: () => request.post('/check-in/add'),
  calendar: () => request.post('/check-in/calendar'),
  today: () => request.post('/check-in/today')
}
