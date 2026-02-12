import axios from 'axios'

const API_BASE_URL = '/api'

export async function login(userAccount, userPassword) {
    try {
        const response = await axios.post(`${API_BASE_URL}/user/login`, {
            userAccount,
            userPassword
        })
        return response.data
    } catch (error) {
        throw new Error(error.response?.data?.message || '登录失败')
    }
}

export async function register(userAccount, userPassword) {
    try {
        const response = await axios.post(`${API_BASE_URL}/user/register`, {
            userAccount,
            userPassword
        })
        return response.data
    } catch (error) {
        throw new Error(error.response?.data?.message || '注册失败')
    }
}

export async function getCurrentUser() {
    try {
        const response = await axios.get(`${API_BASE_URL}/user/current`)
        return response.data
    } catch (error) {
        return null
    }
}

export async function logout() {
    try {
        await axios.post(`${API_BASE_URL}/user/logout`)
        return true
    } catch (error) {
        return false
    }
}
