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

Payment flow:
1. Make sure we have enough stock
2. If payment is successful, order status is set to PAID
3. Then the order should go to kafka again, we deduce the stock, and ready for shipping
4. If payment fails, the order status is still PENDING. We should prompt the user to try paying again.
5. After payment, if the user wants to cancel order and ask for a refund, he can do that as long as order status is not shipped.
6. we should return the money, and restore the stock

auth service + api gateway

for auth, don't forget that user can't get other account by id

Think about how services can communicate with each other

Some lessons learned:

1. develop entity -> repository -> service -> controller
2. entity id should not be primitive, because primitive can't be null. use objects such as Long
3. you should not directly send entity to kafka or to frontend; send dto instead
4. dto need to have no arg constructor, all arg constructor and Data, and Builder(if it contains many fields)
5. add @RequiredArgsConstructor annotation for constructor injection, and field must final
6. auth and profile both point to the same user id, but should be two tables. auth only cares about authentication, and is the only place to store password. profile stores user profile and preferences
7. you might need to manually set dto id to null when creating a new use profile
8. kafka listens to a topic, grabs the message and hands over to service; service does its job and hands back another message to kafka. Node kafka logic should be in service code. All we need is a function
9. you can check jwt at jwt.io
10. spring security sits at the very front, it handles authentication; global filter sits behind, it handles routes
11. for multi-tenant, you can have global filter set headers. for it to have headers, you need to add user id and email to jwt at auth service