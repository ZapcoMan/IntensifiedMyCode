import request from '@/utils/request.js'

// 获取用户菜单
export const getMenuByRole = (role) => {
  return request.get('/menu/getByRole', {
    params: {
      role: role
    }
  })
}

// 获取所有菜单
export const getAllMenu = () => {
  return request.get('/menu/getAll')
}

// 添加菜单
export const addMenu = (data) => {
  return request.post('/menu/add', data)
}

// 更新菜单
export const updateMenu = (data) => {
  return request.put('/menu/update', data)
}

// 删除菜单
export const deleteMenu = (id) => {
  return request.delete(`/menu/delete/${id}`)
}