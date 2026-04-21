import axios from "axios";
import {ElMessage} from "element-plus";
import router from '@/router/index.js'

let baseURL = 'http://127.0.0.1:9991';
const request = axios.create({
    baseURL: baseURL,
    timeout: 30000  // 后台接口超时时间
})

// ✅ 标记是否正在刷新token，防止并发请求多次刷新
let isRefreshing = false
// ✅ 存储待重试的请求队列
let refreshSubscribers = []

/**
 * 将待重试的请求添加到队列中
 */
function subscribeTokenRefresh(cb) {
    refreshSubscribers.push(cb)
}

/**
 * Token刷新后，执行所有待重试的请求
 */
function onRefreshed(newAccessToken) {
    refreshSubscribers.forEach(cb => cb(newAccessToken))
    refreshSubscribers = []
}

// request 拦截器
// 可以自请求发送前对请求做一些处理
request.interceptors.request.use(config => {
    config.headers['Content-Type'] = 'application/json;charset=utf-8';
    // ✅ 从单独的token键中获取AccessToken
    let token = localStorage.getItem('token')
    if (token) {
        config.headers['token'] = token
    }
    return config
}, error => {
    return Promise.reject(error)
});

// response 拦截器
// 可以在接口响应后统一处理结果
request.interceptors.response.use(
    response => {
        let res = response.data;
        // 兼容服务端返回的字符串数据
        if (typeof res === 'string') {
            res = res ? JSON.parse(res) : res
        }
        if (res.code === 20005) {
            ElMessage.error(res.msg)
            router.push('/login')
        } else {
            return res
        }
    },
    async error => {
        // ✅ 处理401错误（Token过期）
        if (error.response && error.response.status === 401) {
            const originalRequest = error.config
            
            // 如果已经在刷新Token，则将请求加入队列等待
            if (isRefreshing) {
                return new Promise((resolve) => {
                    subscribeTokenRefresh((newAccessToken) => {
                        originalRequest.headers['token'] = newAccessToken
                        resolve(request(originalRequest))
                    })
                })
            }
            
            isRefreshing = true
            
            try {
                // 尝试使用RefreshToken刷新AccessToken
                const refreshToken = localStorage.getItem('refreshToken')
                const userInfo = JSON.parse(localStorage.getItem('code_user') || '{}')
                
                if (!refreshToken || !userInfo.id) {
                    // 没有RefreshToken或用户信息，直接跳转登录
                    throw new Error('No refresh token')
                }
                
                // ✅ 根据用户角色调用不同的刷新接口
                const refreshUrl = userInfo.role === 'SUPER_ADMIN' 
                    ? `${baseURL}/admin/refreshToken` 
                    : `${baseURL}/user/refreshToken`
                
                // 调用刷新接口
                const refreshResponse = await axios.post(refreshUrl, {
                    id: userInfo.id,
                    role: userInfo.role,
                    refreshToken: refreshToken
                })
                
                if (refreshResponse.data.code === 20000) {
                    // 刷新成功，保存新AccessToken
                    const newAccessToken = refreshResponse.data.data
                    localStorage.setItem('token', newAccessToken)
                    
                    // 通知所有等待的请求
                    onRefreshed(newAccessToken)
                    
                    // 重试原请求
                    originalRequest.headers['token'] = newAccessToken
                    return request(originalRequest)
                } else {
                    // 刷新失败，跳转登录
                    throw new Error('Refresh failed')
                }
            } catch (refreshError) {
                // 刷新失败，清除本地存储并跳转登录
                console.error('Token刷新失败:', refreshError)
                localStorage.removeItem('token')
                localStorage.removeItem('refreshToken')
                localStorage.removeItem('code_user')
                ElMessage.error('登录已过期，请重新登录')
                router.push('/login')
                return Promise.reject(refreshError)
            } finally {
                isRefreshing = false
            }
        }
        
        // 其他错误处理
        if (error.response && error.response.status === 404) {
            ElMessage.error('未找到请求接口')
        } else if (error.response && error.response.status === 500) {
            ElMessage.error('系统异常，请查看后端控制台报错')
        } else {
            console.error(error.message)
        }
        return Promise.reject(error)
    }
)

// 导出文件上传基础URL
export const fileUploadUrl = `${baseURL}/files/upload`;

export default request