import request from '@/utils/request.js'

/**
 * 验证token是否有效
 */
export const validateToken = () => {
  return request.get('/user/validateToken')
}