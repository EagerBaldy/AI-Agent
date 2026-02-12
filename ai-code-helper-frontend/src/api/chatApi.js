import axios from 'axios'

// 配置axios基础URL
const API_BASE_URL = '/api'

/**
 * 使用 SSE 方式调用聊天接口
 * @param {number} memoryId 聊天室ID
 * @param {string} message 用户消息
 * @param {string} type 聊天类型 ('code' | 'travel')
 * @param {Function} onMessage 接收消息的回调函数
 * @param {Function} onError 错误处理回调函数
 * @param {Function} onClose 连接关闭回调函数
 * @returns {EventSource} 返回 EventSource 对象，用于手动关闭连接
 */
export function chatWithSSE(memoryId, message, type, onMessage, onError, onClose) {
    // 构建URL参数
    const params = new URLSearchParams({
        memoryId: memoryId,
        message: message
    })
    
    // 根据类型选择不同的接口
    let endpoint = '/ai/chat'
    if (type === 'travel') {
        endpoint = '/ai/travel/chat'
    } else if (type === 'essay') {
        endpoint = '/ai/essay/chat'
    } else if (type === 'material') {
        endpoint = '/ai/material/chat'
    } else if (type === 'medical') {
        endpoint = '/ai/medical/chat'
    } else if (type === 'college') {
        endpoint = '/ai/college/chat'
    }
    
    // 创建 EventSource 连接
    const eventSource = new EventSource(`${API_BASE_URL}${endpoint}?${params}`, {
        withCredentials: true
    })
    
    // 处理接收到的消息
    eventSource.onmessage = function(event) {
        try {
            const data = event.data
            if (data && data.trim() !== '') {
                onMessage(data)
            }
        } catch (error) {
            console.error('解析消息失败:', error)
            onError && onError(error)
        }
    }
    
    // 处理错误
    eventSource.onerror = function(error) {
        console.log('SSE 连接状态:', eventSource.readyState)
        // 只有在连接状态不是正常关闭时才报错
        if (eventSource.readyState !== EventSource.CLOSED) {
            console.error('SSE 连接错误:', error)
            onError && onError(error)
        } else {
            console.log('SSE 连接正常结束')
        }
        
        // 确保连接关闭
        if (eventSource.readyState !== EventSource.CLOSED) {
            eventSource.close()
        }
    }
    
    // 处理连接关闭
    eventSource.onclose = function() {
        console.log('SSE 连接已关闭')
        onClose && onClose()
    }
    
    return eventSource
}

/**
 * 检查后端服务是否可用
 * @returns {Promise<boolean>} 返回服务是否可用
 */
export async function checkServiceHealth() {
    try {
        const response = await axios.get(`${API_BASE_URL}/health`, {
            timeout: 5000
        })
        return response.status === 200
    } catch (error) {
        console.error('服务健康检查失败:', error)
        return false
    }
}

/**
 * 获取历史聊天记录
 * @param {number} memoryId 会话ID
 * @param {string} assistantType 助手类型
 * @returns {Promise<Array>} 消息列表
 */
export async function getChatHistory(memoryId, assistantType) {
    try {
        // 如果是 Agent 模式，调用 Agent 的历史记录接口
        // UI 中 currentMode 为 'code', 'travel' 等
        // assistantType 传入的就是 currentMode
        // 任何使用 Agent 模式的助手类型都需要走这个逻辑
        const agentModes = ['code', 'travel', 'essay', 'material', 'medical', 'college'];
        if (agentModes.includes(assistantType)) {
            console.log('Fetching agent history for sessionId:', memoryId, 'type:', assistantType);
            const response = await axios.get(`${API_BASE_URL}/agent/history`, {
                params: {
                    sessionId: memoryId
                },
                withCredentials: true // 确保发送 cookie 以获取当前用户的历史记录
            })
            console.log('Agent history response:', response.data);
            
            // 适配数据格式并合并连续的 Agent 消息
            let stepCount = 0;
            const mappedMessages = response.data.map(item => {
                let content = item.content || '';
                let detailsContent = '';

                if (item.thought || (item.action && item.action !== 'None')) {
                    // 这是一个步骤
                    stepCount++;
                    detailsContent += `#### 第 ${stepCount} 步\n`;
                    if (item.thought) {
                        detailsContent += `> **思考**: ${item.thought}\n`;
                    }
                    if (item.action && item.action !== 'None') {
                        detailsContent += `> 🛠️ **调用工具**: \`${item.action}\` (参数: \`${item.actionInput}\`)\n`;
                    }
                    if (item.observation) {
                         detailsContent += `> 👁️ **观察**: \n> ${item.observation.replace(/\n/g, '\n> ')}\n\n`;
                    }
                }
                
                // 将 detailsContent 放在 content 字段中，稍后统一处理
                if (detailsContent) {
                    return {
                       id: item.id,
                       role: item.role,
                       createTime: item.createTime,
                       isStep: true,
                       stepContent: detailsContent,
                       finalContent: content
                    };
                }
                
                // 如果是最终答案（没有步骤），重置 stepCount (或者不重置，取决于是否希望跨会话连续)
                // 通常一个会话中 stepCount 应该累加吗？
                // 这里的 response.data 是历史记录列表。通常是一次完整对话的记录。
                // 但是 getChatHistory 获取的是 *整个* 会话的所有消息。
                // 如果用户发了多条消息，agent 会有多次执行。
                // 我们应该检测 user 消息来重置 stepCount？
                // 简单起见，我们检测到 user 消息时重置 stepCount。
                
                if (item.role === 'user') {
                    stepCount = 0;
                }
                
                // 过滤掉完全空的消息
                if (!content.trim()) {
                    return null;
                }
                
                return {
                    id: item.id,
                    content: content.trim(),
                    role: item.role,
                    createTime: item.createTime,
                    isStep: false
                }
            }).filter(item => item !== null);

            // 合并连续的 Agent 消息
            const mergedMessages = [];
            let currentDetailsBlock = '';
            let currentFinalAnswer = '';
            let firstAgentMsg = null;

            for (let i = 0; i < mappedMessages.length; i++) {
                const current = mappedMessages[i];
                
                if (current.role === 'agent') {
                    if (!firstAgentMsg) firstAgentMsg = current;
                    
                    if (current.isStep) {
                        currentDetailsBlock += current.stepContent;
                    }
                    if (current.finalContent) {
                        currentFinalAnswer += (currentFinalAnswer ? '\n\n' : '') + current.finalContent;
                    }
                    
                    // 检查下一条是否还是 agent
                    const next = i + 1 < mappedMessages.length ? mappedMessages[i+1] : null;
                    if (!next || next.role !== 'agent') {
                        // Agent 连续消息结束，构建最终消息
                        let finalMessageContent = '';
                        if (currentDetailsBlock) {
                            const isOpen = !currentFinalAnswer ? ' open' : '';
                            finalMessageContent += `<details${isOpen}><summary>观察思考过程</summary>\n\n${currentDetailsBlock}\n</details>\n\n`;
                        }
                        finalMessageContent += currentFinalAnswer;
                        
                        mergedMessages.push({
                            id: firstAgentMsg.id,
                            role: 'agent',
                            content: finalMessageContent.trim(),
                            createTime: firstAgentMsg.createTime
                        });
                        
                        // 重置临时变量
                        currentDetailsBlock = '';
                        currentFinalAnswer = '';
                        firstAgentMsg = null;
                    }
                } else {
                    // 非 agent 消息 (user)，直接添加
                    mergedMessages.push(current);
                }
            }
            
            return mergedMessages;
        }

        const response = await axios.get(`${API_BASE_URL}/chat/history`, {
            params: {
                memoryId,
                assistantType
            },
            withCredentials: true
        })
        return response.data
    } catch (error) {
        console.error('获取历史记录失败:', error)
        throw error // 抛出错误供调用方处理
    }
}

/**
 * 保存消息到历史记录
 * @param {number} memoryId 会话ID
 * @param {string} message 消息内容
 * @param {boolean} isUser 是否为用户消息
 * @param {string} assistantType 助手类型
 */
export async function saveMessage(memoryId, message, isUser, assistantType) {
    try {
        await axios.post(`${API_BASE_URL}/chat/history`, {
            memoryId,
            message,
            isUser,
            assistantType
        }, {
            withCredentials: true
        })
    } catch (error) {
        console.error('保存消息失败:', error)
    }
} 