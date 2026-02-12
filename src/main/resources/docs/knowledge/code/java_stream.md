---
title: Java Stream API 指南
version: 1.0
tags: [java, stream, functional-programming]
author: AI Code Helper
create_time: 2023-10-01
---

# Java Stream API 指南

Java Stream API 是 Java 8 引入的重要特性，支持对集合进行函数式操作。

## 1. 核心概念
Stream 是一个来自数据源的元素队列并支持聚合操作。
- **Source**: 集合、数组、I/O channel 等。
- **Intermediate Operations**: filter, map, sorted 等，返回 Stream。
- **Terminal Operations**: forEach, collect, reduce 等，返回结果或 void。

## 2. 常用操作
### 2.1 过滤 (Filter)
```java
List<String> filtered = list.stream()
    .filter(s -> s.startsWith("a"))
    .collect(Collectors.toList());
```

### 2.2 映射 (Map)
```java
List<Integer> lengths = list.stream()
    .map(String::length)
    .collect(Collectors.toList());
```

## 3. 最佳实践
- 尽量使用并行流处理大数据集 (`parallelStream()`)。
- 注意 Stream 的惰性求值特性。
