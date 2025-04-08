# 🚀 Kontakt API - Technical Documentation

---

## 1. Project Overview

**Kontakt API** is a robust CRM API built using **Spring Boot**. Although the original requirements focused solely on integrating with HubSpot, this project was extended to serve as a corporate-grade solution. It features its own JWT-based authentication (with **Spring Security 6**) before interacting with the HubSpot API. The architecture follows a simplified version of Clean Architecture, emphasizing the **Dependency Inversion Principle** and the **Single Responsibility Principle**.

### Key Features:
- **Robust Architecture:**  
  Scalable design that supports future growth.
- **JWT Authentication:**  
  Secure authentication using Spring Security 6 to protect API access.
- **Resilience:**  
  Fault tolerance implemented using **Resilience4j** (Rate Limiter and Circuit Breaker). A custom annotation `@Resilient` is used to centralize this logic.
- **Caching:**  
  **Redis** is employed to cache HubSpot tokens with a configurable TTL, improving performance and reducing redundant calls.
- **Reusable Webhook Validation:**  
  A custom annotation for HMAC validation ensures uniform validation for all incoming HubSpot (or other) webhooks.
- **API Documentation:**  
  Documented using **Swagger (OpenAPI 3.1)**, which enables interactive testing via Swagger UI.
- **Testing:**  
  Unit tests are written with **JUnit 5** and **Mockito** to guarantee reliable functionality.
- **Deployment:**  
  Deployed on **Google Cloud**, with the API on Compute Engine, Redis on Memorystore, and PostgreSQL on NeonDB.
- **Development Tools:**
    - A shell script (`generate_keys.sh`) is provided to generate RSA keys for JWT.
    - A Docker Compose file is included for local testing with PostgreSQL and Redis.

---

## 2. Execution Instructions

### Prerequisites
- **Java 21+**
- **Maven** for building the project.
- **Docker** for local testing (to run PostgreSQL and Redis).
- **Environment Variables/Profiles:**
    - For local development, configure your `application-local.properties` with your HubSpot Client ID, Client Secret, and Redirect URI.
    - For production, adjust the properties accordingly.
- **SSL Setup (Production):**  
  Ensure that in production all URLs (e.g. `default.access.origin`) use HTTPS.

### Steps for Running the Project

1. **Set Environment Variables**  
   For example, if using a `.env` file:
   ```bash
   export $(grep -v '^#' .env | xargs)
   ```

2. **Build the Project**  
   With Maven:
   ```bash
   mvn clean install
   ```

3. **Run the Application**  
   For local development (with tests skipped):
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=local -DskipTests
   ```
   For production:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=prod -DskipTests
   ```

4. **Docker Compose (Local Testing)**  
   Use the provided Docker Compose file:
   ```yaml
   services:
     postgres:
       image: 'postgres:latest'
       environment:
         - POSTGRES_DB=kontakt-db
         - POSTGRES_PASSWORD=postgres
         - POSTGRES_USER=postgres
       ports:
         - "5432:5432"
     redis:
       image: 'redis:latest'
       ports:
         - "6379:6379"
   ```
   Then run:
   ```bash
   docker-compose up
   ```

5. **Access the API and Documentation**
    - **API Endpoints:**  
      Local: `http://localhost:8080/api/...`  
      Production: `https://kontaktapplication.ddns.net/api/...`
    - **Swagger UI:**  
      Local: `http://localhost:8080/swagger-ui/index.html`  
      Production: `https://kontaktapplication.ddns.net/swagger-ui/index.html`
    - **Actuator Endpoints:**  
      Available at: `http://localhost:8080/actuator/`

6. **Generate RSA Keys**  
   Run the script:
   ```bash
   ./generate_keys.sh
   ```
   This script (found at the project root) generates the RSA keys required for JWT.

---

## 3. Architectural and Library Decisions

### Spring Boot & Spring Security 6 🔐
- **Motivation:**  
  Rapid setup with auto-configuration and production readiness. Spring Security 6 secures API endpoints with JWT authentication.

### Resilience4j ⚙️
- **Motivation:**  
  A lightweight and modular library for implementing fault tolerance.
- **Usage:**
    - **Rate Limiter:**  
      Protects API endpoints by limiting calls per time window to prevent overload.
    - **Circuit Breaker:**  
      Protects the system from repeated failures by breaking the circuit when too many errors occur.

### Custom Resilience Annotation (@Resilient) 🔄
- **Motivation:**  
  To encapsulate and centralize the resilience logic, reducing code duplication.
- **Default Fallback:**  
  A default fallback message is built into the aspect. If no fallback method is provided, a default exception is thrown for global handling.

### Persistence and Caching 💾🚀
- **Spring Data JPA with PostgreSQL:**  
  Provides ORM for robust data management.
- **Spring Data Redis:**  
  Caches critical data (e.g., HubSpot tokens) using TTL to improve performance and reduce unnecessary external calls.

### API Documentation with Swagger (OpenAPI 3.1) 📚
- **Motivation:**  
  Enables interactive API documentation and testing. The configuration specifies servers for both local (HTTP) and production (HTTPS) environments.

### Testing 🧪
- **JUnit 5 and Mockito:**  
  Ensure thorough unit testing of business logic and fault-tolerance implementations, reducing production surprises.

### Deployment Strategy ☁️
- **Google Cloud:**  
  The API is deployed on Compute Engine, Redis on Memorystore, and PostgreSQL on NeonDB.
- **Docker Compose:**  
  A Docker Compose file is provided for local development, simplifying the setup of PostgreSQL and Redis.

---

## 4. Key Configuration Snippets

### JWT and HubSpot Configuration
```properties
jwt.token.expires-in=3600
jwt.public.key=classpath:app.pub
jwt.private.key=classpath:app.key

hubspot.client.secret=YOUR-CLIENT-SECRET-HERE
hubspot.client.id=YOUR-CLIENT-ID-HERE
hubspot.redirect-uri=http://localhost:8080/api/hubspot/callback
hubspot.oauth.authorization-url=https://app.hubspot.com/oauth/authorize
hubspot.api.base-url=https://api.hubapi.com
```

### Data Source and Redis
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/kontakt-db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

spring.data.redis.host=localhost
spring.data.redis.port=6379
```

### Resilience4j Configuration
```properties
# Rate Limiter Configuration
resilience4j.ratelimiter.instances.RateLimiter.limit-for-period=5
resilience4j.ratelimiter.instances.RateLimiter.limit-refresh-period=10s
resilience4j.ratelimiter.instances.RateLimiter.timeout-duration=0s

resilience4j.ratelimiter.instances.hubspotRateLimiter.limit-for-period=5
resilience4j.ratelimiter.instances.hubspotRateLimiter.limit-refresh-period=10s
resilience4j.ratelimiter.instances.hubspotRateLimiter.timeout-duration=0s

# Circuit Breaker Configuration
resilience4j.circuitbreaker.instances.CircuitBreaker.register-health-indicator=true
resilience4j.circuitbreaker.instances.CircuitBreaker.sliding-window-type=COUNT_BASED
resilience4j.circuitbreaker.instances.CircuitBreaker.sliding-window-size=10
resilience4j.circuitbreaker.instances.CircuitBreaker.minimum-number-of-calls=5
resilience4j.circuitbreaker.instances.CircuitBreaker.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.CircuitBreaker.wait-duration-in-open-state=10s
resilience4j.circuitbreaker.instances.CircuitBreaker.permitted-number-of-calls-in-half-open-state=3
resilience4j.circuitbreaker.instances.CircuitBreaker.automatic-transition-from-open-to-half-open-enabled=true

# Actuator and Resilience4j Metrics
management.endpoints.web.exposure.include=*
management.endpoint.ratelimiterevents.enabled=true
resilience4j.ratelimiter.metrics.enabled=true

resilience4j.ratelimiter.configs.default.allow-health-indicator-to-fail=true
resilience4j.ratelimiter.configs.default.subscribe-for-events=true
resilience4j.ratelimiter.configs.default.event-consumer-buffer-size=50

resilience4j.ratelimiter.instances.externalService.base-config=default
management.endpoints.web.base-path=/actuator
```

### Swagger and CORS
```properties
default.access.origin=https://kontaktapplication.ddns.net
springdoc.swagger-ui.server-url=https://kontaktapplication.ddns.net
```

---

## 5. API Endpoints Overview

### 5.1 ContactController
- **POST /api/contact**  
  **Description:** Creates a new contact in HubSpot.  
  **Behavior:**
    - Receives a JSON payload (`ContactRequest`) and calls the `createContact` use case.
    - Validates input and creates the contact.
    - Protected by Resilience4j’s Rate Limiter & Circuit Breaker.
    - **Responses:**
        - **201 CREATED** on success.
        - **400 BAD REQUEST** on invalid input.

- **GET /api/contact**  
  **Description:** Lists contacts from HubSpot.  
  **Behavior:**
    - Returns a reactive response with a map of contacts.
    - Calls the `listContacts` use case.
    - **Responses:**
        - **200 OK** when contacts are successfully retrieved.
        - **400 BAD REQUEST** on errors.

### 5.2 HubspotOAuthController
- **GET /api/hubspot/url**  
  **Description:** Retrieves the HubSpot authorization URL to start the OAuth flow.  
  **Behavior:**
    - Calls the token service to generate the URL.
    - **Response:**
        - **200 OK** with the authorization URL.

- **GET /api/hubspot/callback**  
  **Description:** Processes the OAuth callback from HubSpot.  
  **Behavior:**
    - Accepts the authorization code and optional state as query parameters.
    - Executes a token exchange and returns an HTML view indicating the result.
    - **Response:**
        - A simple HTML page showing success or error.

### 5.3 UserAuthController
- **POST /api/auth/login**  
  **Description:** Authenticates a user and returns a JWT token.  
  **Behavior:**
    - Accepts a `LoginRequest` JSON payload.
    - Uses the authentication service to verify credentials.
    - **Responses:**
        - **200 OK** with a JWT token.
        - **400 BAD REQUEST** if input is invalid.

### 5.4 UserController
- **POST /api/user**  
  **Description:** Creates a new user in the system.  
  **Behavior:**
    - Receives a `RegisterRequest` payload.
    - Invokes the user creation use case.
    - Protected by resilience patterns.
    - **Responses:**
        - **201 CREATED** if successful.
        - **400 BAD REQUEST** on validation errors.

- **GET /api/user**  
  **Description:** Retrieves a list of all users (administrative access only).  
  **Behavior:**
    - Retrieves the list of users.
    - Protected by Spring Security (restricted to Admin role).
    - **Responses:**
        - **200 OK** with a list of users.
        - **400 BAD REQUEST** or **404 NOT FOUND** in case of errors.

### 5.5 WebhookController
- **POST /api/hubspot/webhook/contact**  
  **Description:** Processes incoming webhook events from HubSpot regarding contact creations.  
  **Behavior:**
    - Secured by HMAC validation via a custom annotation.
    - Accepts a list of `ContactCreationEventRequest` objects.
    - Persists events using the process use case.
    - **Response:**
        - **200 OK** upon successful processing.

- **GET /api/hubspot/webhook/contact**  
  **Description:** Retrieves a paginated list of contact creation events received through webhooks.  
  **Behavior:**
    - Accepts query parameters for pagination (e.g., pageNumber and size).
    - Returns a Page of `ContactCreationEventEntity` objects.
    - **Responses:**
        - **200 OK** with paginated results.
        - **Appropriate error responses** if invalid parameters are provided.

---

## 6. Future Improvements

- **Enhanced Monitoring:**  
  Integrate Micrometer (using the resilience4j-micrometer module) to push detailed metrics to Prometheus/Grafana dashboards for real-time insights.

- **Expanded Resilience Patterns:**  
  Extend the custom resilience annotation (`@Resilient`) to support additional fault-tolerance patterns such as Retry, Time Limiter, and Bulkhead for finer control.

- **Global Fallback Customization:**  
  Investigate strategies to define a global fallback behavior—using a dedicated error handler or advanced aspect—for consistent error responses across endpoints and reduced code duplication.

- **Advanced Reporting:**  
  Integrate Jasper Reports to generate comprehensive reports (e.g., audit trails, performance metrics, sales pipelines), enhancing business intelligence.

- **New Business Rules:**  
  Implement new business rules and workflows (such as lead scoring, customer segmentation, or cross-selling opportunities) to further evolve the CRM domain in a corporate setting.

- **Improved Webhook Processing with Messaging:**  
  **⚡ Consider leveraging a messaging service (e.g., GCP Pub/Sub) for webhook event processing.**  
  This approach decouples the ingestion of webhook events from their processing, allowing for asynchronous handling of spikes in traffic. By pushing events to a message queue, the API can quickly acknowledge receipt of the webhook, while dedicated worker processes consume, process, and persist the events. This not only improves scalability and resilience but also enables buffering during high-load periods.

- **Increased Test Coverage:**  
  Expand integration and stress tests using tools like WireMock and JMeter to validate system behavior under high load and fault conditions, ensuring robustness in production.

- **Container Orchestration:**  
  Consider adopting Kubernetes for container orchestration to improve scalability and reliability in production, accompanied by CI/CD pipelines for automated deployments.

---