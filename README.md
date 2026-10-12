# 🏗️ Saga Pattern — Распределённые транзакции в микросервисной архитектуре

## 📋 Описание задачи

### Проблема
В микросервисной архитектуре данные распределены между несколькими независимыми сервисами, каждый из которых имеет свою базу данных. Классические **ACID-транзакции** (с двумя фазными коммитами — 2PC) неприменимы, так как:
- Сервисы используют разные БД (или даже разные типы БД)
- 2PC блокирует ресурсы на время транзакции
- Невозможно гарантировать атомарность в распределённой системе без единой точки управления

### Решение — Saga Pattern

**Saga** — это паттерн управления распределёнными транзакциями, который разбивает глобальную бизнес-транзакцию на последовательность локальных транзакций. Каждая локальная транзакция публикует событие, которое триггерит следующую локальную транзакцию. Если какая-то из транзакций завершается неудачей, сага запускает **компенсирующие (откатные) транзакции**, чтобы отменить изменения предыдущих шагов.

### Что реализовано в проекте

В данном проекте реализована Saga **создания и оплаты заказа** с использованием **Orchestration-based Saga** (оркестрируемая сага):

Роль **оркестратора** выполняет класс `OrderOrchestrator` в сервисе `order-service`. Он слушает события от всех сервисов и на основе их результатов принимает решения — отправлять следующую команду или запускать компенсирующие действия.

```
Клиент → Order Service (создаёт заказ)
   ↓
[OrderCreatedEvent] → Оркестратор → команда Stock Service (резервировать товар)
   ↓
[ProductReservedEvent] → Оркестратор → команда Payment Service (списать оплату)
   ↓
[ProductPaidEvent] → Оркестратор → команда Order Service (подтвердить заказ ✅)
```

**Компенсирующие действия (при сбое):**
```
Payment Failed ❌
   ↓
[PaymentFailedEvent] → Stock Service (отменяет резерв товара)
   ↓
[CancelReservationSucceededEvent] → Order Service (отклоняет заказ)
```

---

## 🧩 Архитектура проекта

### 📦 Микросервисы

| Сервис | Порт | База данных | Описание |
|--------|------|-------------|----------|
| **order-service** | `8080` | PostgreSQL (`order_db:5433`) | Принимает запросы на создание заказа. Содержит **оркестратор** Saga. Ведёт историю статусов заказа. |
| **stock-service** | `8081` | PostgreSQL (`stock_db:5434`) | Управляет остатками товаров. Резервирует товар и отменяет резервирование. |
| **payment-service** | `8082` | — (мок) | Симулирует вызов внешнего платёжного шлюза. В текущей реализации **всегда выбрасывает исключение**, демонстрируя **сценарий отказа и компенсации**. |

### 📬 Топики Kafka (6 топиков)

Оркестратор (`OrderOrchestrator`) слушает **все 3 топика-события** и пишет в **3 топика-команды**:

**События** (сигнализируют об успехе или неудаче локальной транзакции):

| Топик | Событие | От кого приходит | Кто читает |
|-------|---------|-----------------|------------|
| `order-events-topic` | `OrderCreatedEvent` | OrderServiceImpl (при создании заказа) | Оркестратор |
| `stock-event-topic` | `ProductReservedEvent` / `CancelReservationSucceededEvent` | Stock Service | Оркестратор |
| `payment-event-topic` | `ProductPaidEvent` / `PaymentFailedEvent` | Payment Service | Оркестратор |

**Команды** (указания оркестратора сервисам выполнить действие):

| Топик | Команда | Кто пишет | Кто выполняет |
|-------|---------|-----------|---------------|
| `stock-command-topic` | `ProductReserveCommand` / `CancelReservationCommand` | Оркестратор | Stock Service |
| `payment-command-topic` | `PaymentPayCommand` | Оркестратор | Payment Service |
| `order-command-topic` | `OrderApprovedCommand` / `RejectOrderCommand` | Оркестратор | Order Service (через `OrderCommandHandler`) |

---

## 🔄 Жизненный цикл Saga (пошагово)

### ✅ Успешный сценарий

```
1. Клиент → POST /api/v1/create-order
       │
       ▼
2. Order Service (OrderServiceImpl)
   • Создаёт OrderEntity (status = CREATED)
   • Публикует OrderCreatedEvent → order-events-topic
       │
       ▼
3. Оркестратор (OrderOrchestrator) получает OrderCreatedEvent
   • addHistory(CREATED)
   • Отправляет ProductReserveCommand → stock-command-topic
       │
       ▼
4. Stock Service получает ProductReserveCommand
   • reserveProduct() — уменьшает количество товара на складе
   • Публикует ProductReservedEvent → stock-event-topic
       │
       ▼
5. Оркестратор получает ProductReservedEvent
   • addHistory(RESERVED)
   • Отправляет PaymentPayCommand → payment-command-topic
       │
       ▼
6. Payment Service получает PaymentPayCommand
   • pay() → успешно
   • Публикует ProductPaidEvent → payment-event-topic
       │
       ▼
7. Оркестратор получает ProductPaidEvent
   • addHistory(PAID)
   • Отправляет OrderApprovedCommand → order-command-topic
       │
       ▼
8. OrderService (OrderCommandHandler) получает OrderApprovedCommand
   • approveOrder() — статус → APPROVED ✅
```

### ❌ Сценарий сбоя (компенсация)

```
Payment Pay Command → Payment Service
   │
   ▼
ExternalMockPaymentServiceImpl.pay()
   │
   ▼  ❌ Runtime Exception ("Недостаточно средств")
   │
   ▼
Payment Service публикует PaymentFailedEvent
   │
   ▼
Оркестратор получает PaymentFailedEvent
   │
   ▼  Создаёт CancelReservationCommand
   │
   ▼
Stock Service получает CancelReservationCommand
   │
   ▼  cancelReservation() — возвращает товар на склад
   │
   ▼  Публикует CancelReservationSucceededEvent
   │
   ▼
Оркестратор получает CancelReservationSucceededEvent
   │
   ▼  Создаёт RejectOrderCommand
   │
   ▼
OrderService получает RejectOrderCommand
   │
   ▼  Статус → REJECTED ❌
```

---

## 🛠️ Статусы заказа (OrderStatus)

| Статус | Описание |
|--------|----------|
| `CREATED` | Заказ создан, ожидание резервирования товара |
| `RESERVED` | Товар зарезервирован на складе |
| `PAID` | Оплата успешно проведена |
| `APPROVED` | ✅ Заказ подтверждён (финальный успешный статус) |
| `REJECTED` | ❌ Заказ отклонён (откат по компенсирующей транзакции) |

---

## 🚀 Быстрый старт

### 1️⃣ Запуск инфраструктуры (Docker)

```bash
docker compose up -d
```

Будут запущены:
- **Kafka** (брокер) на порту `9092`
- **Kafka UI** на порту `8085`
- **PostgreSQL (order_db)** на порту `5433`
- **PostgreSQL (stock_db)** на порту `5434`

### 2️⃣ Запуск микросервисов

```bash
# Терминал 1: Order Service (порт 8080)
cd order-service
./mvnw spring-boot:run

# Терминал 2: Stock Service (порт 8081)
cd stock-service
./mvnw spring-boot:run

# Терминал 3: Payment Service (порт 8082)
cd payment-service
./mvnw spring-boot:run
```

### 3️⃣ Отправка тестового запроса

```bash
curl -X POST http://localhost:8080/api/v1/create-order \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "00000000-0000-0000-0000-000000000001",
    "productQuantity": 2
  }'
```

> **⚠️ Примечание:** Для работы потребуется предварительно добавить товар с соответствующим `productId` в базу `stock_db` (через скрипт или вручную).

---

## 📁 Структура проекта

```
saga/
├── core/                               # Общий модуль (DTO, исключения)
│   ├── src/main/java/org/example/core/dto/
│   │   ├── commands/                   # Команды для сервисов
│   │   │   ├── ProductReserveCommand.java
│   │   │   ├── PaymentPayCommand.java
│   │   │   ├── OrderApprovedCommand.java
│   │   │   ├── RejectOrderCommand.java
│   │   │   └── CancelReservationCommand.java
│   │   ├── events/                     # События Saga
│   │   │   ├── OrderCreatedEvent.java
│   │   │   ├── ProductReservedEvent.java
│   │   │   ├── ProductPaidEvent.java
│   │   │   ├── PaymentFailedEvent.java
│   │   │   └── CancelReservationSucceededEvent.java
│   │   └── exception/
│   │       └── InsufficientProductQuantityException.java
│
├── order-service/                      # Сервис заказов (оркестратор)
│   ├── controller/OrderController.java
│   ├── service/
│   │   ├── OrderServiceImpl.java       # Бизнес-логика заказов
│   │   └── OrderHistoryServiceImpl.java # История статусов
│   ├── service/handler/
│   │   └── OrderCommandHandler.java    # Обработчик команд
│   ├── saga/
│   │   └── OrderOrchestrator.java      # Оркестратор Saga
│   ├── config/KafkaConfig.java         # Конфигурация топиков
│   ├── domain/
│   │   ├── entity/OrderEntity.java
│   │   ├── entity/OrderHistoryEntity.java
│   │   ├── repository/
│   │   └── status/OrderStatus.java
│   └── dto/RequestCreateOrderDto.java
│
├── stock-service/                      # Складской сервис
│   ├── service/
│   │   ├── ProductService.java         # Интерфейс
│   │   └── impl/ProductServiceImpl.java # Резервирование/отмена
│   ├── service/handler/
│   │   └── StockCommandsHandler.java   # Обработчик команд склада
│   ├── domain/entity/ProductEntity.java
│   └── dto/ProductDto.java
│
├── payment-service/                    # Платёжный сервис
│   ├── service/
│   │   ├── PaymentService.java         # Интерфейс
│   │   └── impl/ExternalMockPaymentServiceImpl.java
│   └── service/handler/
│       └── PaymentCommandsHandler.java # Обработчик команд оплаты
│
├── docker-compose.yml                  # Инфраструктура (Kafka, Postgres)
└── pom.xml                             # Родительский POM
```

---

## 🔧 Технологический стек

| Технология | Версия | Назначение |
|------------|--------|------------|
| Java | 21 | Язык программирования |
| Spring Boot | 3.x | Фреймворк микросервисов |
| Apache Kafka | 3.9.1 | Брокер сообщений (асинхронная коммуникация) |
| PostgreSQL | 17 | Базы данных сервисов |
| Docker / Docker Compose | — | Контейнеризация инфраструктуры |
| Maven | — | Сборка проекта |
| Kafka UI | v0.7.2 | Веб-интерфейс для мониторинга Kafka |

---

## 📚 Ключевые концепции (Saga Pattern)

1. **Асинхронная коммуникация** — сервисы общаются через Kafka, а не синхронными HTTP-вызовами
2. **Гарантированная доставка** — Kafka сохраняет сообщения надёжно
3. **Компенсирующие транзакции** — каждый шаг имеет обратное действие
4. **Оркестрация (Orchestration)** — в проекте используется именно **Orchestration-based Saga**: центральный оркестратор (`OrderOrchestrator`) управляет ходом саги: слушает события, принимает решения и отправляет команды сервисам. Это упрощает логику сервисов — им не нужно знать о других участниках саги.
5. **Idempotency** — обработка сообщений должна быть идемпотентной (в проекте не реализовано, но рекомендуется)
