---
title: Vue.js 3 响应式原理
version: 1.1
tags: [vue, frontend, reactive]
author: AI Code Helper
create_time: 2023-10-15
---

# Vue.js 3 响应式原理

Vue 3 使用 Proxy 替代了 Vue 2 的 Object.defineProperty 来实现响应式。

## 1. Proxy vs Object.defineProperty
- **Proxy**: 可以拦截对象的所有操作（包括属性添加、删除），性能更好，支持数组索引修改。
- **Object.defineProperty**: 需要遍历对象属性，无法检测属性添加/删除。

## 2. Composition API
Vue 3 引入了 Composition API (`setup`, `ref`, `reactive`)，更好地复用逻辑。

### 示例
```javascript
import { ref } from 'vue';

export default {
  setup() {
    const count = ref(0);
    const increment = () => count.value++;
    return { count, increment };
  }
}
```
