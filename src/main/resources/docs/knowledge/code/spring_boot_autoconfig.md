---
title: Spring Boot 自动配置原理
version: 1.2
tags: [spring-boot, auto-configuration]
author: AI Code Helper
create_time: 2023-10-05
---

# Spring Boot 自动配置原理

Spring Boot 的核心特性之一是自动配置（Auto-Configuration）。

## 1. @EnableAutoConfiguration
该注解通常由 `@SpringBootApplication` 包含。它利用 `SpringFactoriesLoader` 机制加载 `META-INF/spring.factories` 中的配置类。

## 2. 条件注解
自动配置类通常配合 `@Conditional` 注解使用：
- `@ConditionalOnClass`: 类路径下存在指定类时生效。
- `@ConditionalOnMissingBean`: 容器中不存在指定 Bean 时生效。
- `@ConditionalOnProperty`: 指定属性存在或为特定值时生效。

## 3. 示例
`HttpEncodingAutoConfiguration` 只有在 `CharacterEncodingFilter` 类存在时才会生效。
