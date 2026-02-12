import axios from 'axios'

const API_BASE_URL = '/api'

export async function createSession(assistantType, name) {
    try {
        const response = await axios.post(`${API_BASE_URL}/session/create`, {
            assistantType,
            name
        })
        return response.data
    } catch (error) {
        console.error('创建会话失败:', error)
        return null
    }
}

export async function listSessions(assistantType) {
    try {
        const response = await axios.get(`${API_BASE_URL}/session/list`, {
            params: { assistantType }
        })
        return response.data
    } catch (error) {
        console.error('获取会话列表失败:', error)
        return []
    }
}

export async function renameSession(id, name) {
    try {
        await axios.post(`${API_BASE_URL}/session/rename`, {
            id,
            name
        })
        return true
    } catch (error) {
        console.error('重命名会话失败:', error)
        return false
    }
}

export async function deleteSession(id) {
    try {
        await axios.post(`${API_BASE_URL}/session/delete`, { id })
        return true
    } catch (error) {
        console.error('删除会话失败:', error)
        return false
    }
}
