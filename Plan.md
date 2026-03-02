API endpoints

BASE_URL = "/api/v1"


Auth Service (Port 8081)

- POST /api/v1/auth/register – Create user credentials and link to Account Service.
- POST /api/v1/auth/login – Returns a JWT required for all other service request headers.
- POST /api/v1/auth/logout – Invalidate the token (if using a blacklist/Redis).

Account Service (Port 8082 - MySQL/Postgres)

- GET /api/v1/accounts/{id} – Retrieve profile, shipping, and billing info.
- PUT /api/v1/accounts/{id} – Update user details.
- GET /api/v1/accounts/me – Helper for the frontend to get the current logged-in user profile.

Item Service (Port 8083 - MongoDB)
- GET /api/v1/items – List all items; should support pagination (e.g., ?page=0&size=10).
- GET /api/v1/items/{id} – Get metadata (pictures, description, etc.).
- PATCH /api/v1/items/{id}/inventory – Critical for Atomicity. Used by Order Service via OpenFeign to "lock" or "deduct" stock.

Order Service (Port 8084 - Cassandra)
- POST /api/v1/orders – Replaces /orders/create. This starts the transaction.
- GET /api/v1/orders/{id} – Lookup current state.
- GET /api/v1/orders/user/{userId} – Get order history for a specific customer.
- PATCH /api/v1/orders/{id}/status – Internal endpoint to update state (e.g., from PENDING to PAID) via Kafka events.

- each order should have state: PAID, CANCELLED, REFUNDED, etc.


Payment Service (Port 8085 - MySQL/Postgres)

- POST /api/v1/payments – Receives orderId and payment details.
- GET /api/v1/payments/{id} – Lookup transaction status.
- POST /api/v1/payments/{id}/refund – Replaces /cancel to match real-world banking terminology.


Spin up all db instances, play around with MongoDB and Cassandra

Develop:
Item Service
Account Service
Order Service
Payment Service
Auth Service
Don't forget CORS

An Account has orders, payment and profile, and is linked to auth service.
When you fetch all orders of an account, you query order service: give me all orders of that account id


for auth, don't forget that user can't get other account by id

Think about how services can communicate with each other