# 💵 Exchange rate proxy service

A RESTful proxy service connecting to exchangerate.host APIs.

## 🚀 Features

- Fetches and returns data from downstream APIs.
- Handle resilient communication with third-party APIs using Resilience4j, including circuit breaker, rate limiter,
  timeout, and retry.

## 🛠 Tech Stack

- Kotlin 1.9.25 (with Coroutine)
- Gradle
- Spring
    - Spring Boot 3.4.4
    - Spring Webflux
- Resilience4j
- JUnit5, MockK, kotlinx-coroutines-test