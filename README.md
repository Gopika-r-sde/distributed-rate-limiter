# Distributed Rate Limiter

A distributed rate limiting service built with Spring Boot and Redis that enforces
per-client request limits consistently across multiple application instances.

> 🚧 Status: In Progress

## Features (planned)
- Three algorithms: Fixed Window, Token Bucket, Sliding Window Log
- Redis for shared counters, with atomic Lua scripts to prevent race conditions
- MySQL for client configurations and request logs
- React dashboard to manage limits and view usage
- Load testing to compare algorithms on accuracy and latency

## Tech Stack
Java, Spring Boot, Redis, MySQL, React

## Getting Started
Setup instructions coming soon.