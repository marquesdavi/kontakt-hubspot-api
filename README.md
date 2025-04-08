# Kontakt API - Technical Documentation

## 1. Project Overview

This project is a robust CRM API developed using Spring Boot. Although the original requirements requested only integration with HubSpot, the project has been extended to provide a truly corporate-grade API. It features its own JWT-based authentication (using Spring Security 6) before accessing the HubSpot API, and it is built on a scalable architecture inspired by a simplified version of Clean Architecture. The design emphasizes the Dependency Inversion Principle—ensuring layers depend only on abstractions—and the Single Responsibility Principle.

Key features include:

- **Robust Architecture:** Designed for scalability, allowing the project to grow as business needs evolve.
- **JWT Authentication:** Uses Spring Security 6 for secure JWT authentication, ensuring that only authorized users can access protected resources.
- **Resilience:** Fault tolerance is implemented using Resilience4j with patterns such as Rate Limiter and Circuit Breaker. A custom annotation (@Resilient) centralizes this logic across multiple service methods.
- **Caching:** Redis is used to cache HubSpot tokens with a configurable TTL (Time To Live), thereby facilitating token retrieval and reducing redundant calls.
- **Reusable Webhook Validation:** A custom annotation for HMAC validation ensures that requests coming from HubSpot (and potentially other sources) are validated in a reusable manner.
- **API Documentation:** Swagger (OpenAPI 3.1) is used for documenting the API, enabling interactive testing via Swagger UI.
- **Testing:** JUnit 5 and Mockito are used for unit testing to guarantee reliable logic and behavior.
- **Deployment:** The project is deployed on Google Cloud, with the API on Compute Engine, Redis on Memorystore, and PostgreSQL on NeonDB.
- **Development Tools:** A shell script (`generate_keys.sh`) is provided to generate RSA keys used for JWT authentication. A Docker Compose file is also available for local development with PostgreSQL and Redis.

---

## 2. Execution Instructions

### Prerequisites
- **Java 21+**
- **Maven**: For building the project.
- **Docker** Used to run PostgreSQL and Redis instances to local tests.
- **Environment Variables/Profiles**:
    - For local development, configure your `application-local.properties` with your HubSpot Client ID, Client Secret, and Redirect URI.
    - For production, adjust the properties accordingly.
- **SSL Setup (Production)**:  
  In production, the API is served over HTTPS. Ensure that all properties (e.g. `default.access.origin`) and URLs use HTTPS.

### Steps for Running the Project

1. **Set Environment Variables**  
   For example, if you use a `.env` file, load them using:
   ```bash
   export $(grep -v '^#' .env | xargs)
   ```

2. **Build the Project**  
   For Maven:
   ```bash
   mvn clean install
   ```

3. **Run the Application**  
   For local development, run the JAR with the local profile:
   ```
   mvn spring-boot:run -Dspring-boot.run.profiles=local -DskipTests
   ```

4. **Docker Compose (Local Testing)**  
   Use the provided Docker Compose file for PostgreSQL and Redis:
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
   Start the services with:
   ```bash
   docker-compose up
   ```

5. **Accessing the API and Documentation**
    - **API Endpoints:**  
      Local: `http://localhost:8080/api/...`  
      Production: `https://kontaktapplication.ddns.net/api/...`
    - **Swagger UI:**  
      Local: `http://localhost:8080/swagger-ui/index.html`  
      Production: `https://kontaktapplication.ddns.net/swagger-ui/index.html`
    - **Actuator Endpoints:**  
      e.g., `http://localhost:8080/actuator/` for health, metrics, and resilience events.

6. **Generate RSA Keys**  
   Run the script `generate_keys.sh` located at the project root to generate the necessary RSA keys for JWT authentication.

---

## 3. Architectural and Library Decisions

### Spring Boot and Spring Security 6
- **Motivation:**  
  Leverages auto-configuration and rapid development. Spring Security 6 is used for secure JWT authentication and authorization, ensuring only valid users can access the API.

### Resilience4j
- **Motivation:**  
  Resilience4j is lightweight and modular, offering fault-tolerance patterns such as Rate Limiter and Circuit Breaker.
- **Usage:**
    - **Rate Limiter:**  
      Protects API endpoints from overload by limiting the number of calls per time window.
    - **Circuit Breaker:**  
      Prevents cascading failures by breaking the circuit if too many calls to an external service (or critical operation) fail.

### Custom Resilience Annotation (@Resilient)
- **Motivation:**  
  To avoid repetitive code, a custom annotation is created that, in conjunction with an aspect, applies the Resilience4j features across methods. This ensures code reuse and simplifies maintenance.
- **Default Fallback:**  
  The aspect provides a default fallback message if no specific fallback method is specified. This default behavior throws an exception that is then handled by the global exception handler.

### Persistence and Caching
- **Spring Data JPA with PostgreSQL:**  
  Provides a robust ORM solution for managing data persistence.
- **Spring Data Redis:**  
  Caches tokens (e.g., HubSpot tokens) with TTL, reducing redundant calls and improving overall performance.

### API Documentation
- **Swagger (OpenAPI 3.1):**  
  Swagger is used to document the API endpoints. The configuration explicitly defines server URLs (both local and production) to ensure API calls are made with the correct protocol (HTTPS in production).

### Testing
- **JUnit 5 and Mockito:**  
  Comprehensive unit tests ensure business logic and resilience functionalities work as expected, minimizing the risk of issues in production.

### Deployment Strategy
- **Google Cloud Deployment:**
    - **Compute Engine** runs the API.
    - **Memorystore** is used for Redis.
    - **NeonDB** is used for PostgreSQL.  
      Additionally, a Docker Compose file is provided for local testing.

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
# Rate Limiter for API endpoints
resilience4j.ratelimiter.instances.RateLimiter.limit-for-period=5
resilience4j.ratelimiter.instances.RateLimiter.limit-refresh-period=10s
resilience4j.ratelimiter.instances.RateLimiter.timeout-duration=0s

resilience4j.ratelimiter.instances.hubspotRateLimiter.limit-for-period=5
resilience4j.ratelimiter.instances.hubspotRateLimiter.limit-refresh-period=10s
resilience4j.ratelimiter.instances.hubspotRateLimiter.timeout-duration=0s

# Circuit Breaker configuration
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
    - The endpoint receives a JSON payload (ContactRequest) and invokes the `createContact` use case.
    - Validates input and, if successful, creates the contact.
    - Protected by a rate limiter using Resilience4j.
    - **Responses:**
        - **201 CREATED** on success (contact created).
        - **400 Bad Request** if the input is invalid.

- **GET /api/contact**  
  **Description:** Lists contacts from HubSpot.  
  **Behavior:**
    - The endpoint returns a reactive response (Mono of Map) with the list of contacts.
    - Calls the `listContacts` use case, which retrieves contacts from HubSpot.
    - Handles errors by returning a 400 status.
    - **Responses:**
        - **200 OK** when contacts are successfully retrieved.
        - **400 Bad Request** on error.

### 5.2 HubspotOAuthController
- **GET /api/hubspot/url**  
  **Description:** Returns the HubSpot authorization URL to initiate the OAuth flow.  
  **Behavior:**
    - Calls `generateAuthorizationUrl()` on the token service.
    - **Responses:**
        - **200 OK** with the URL string.

- **GET /api/hubspot/callback**  
  **Description:** Processes the OAuth callback from HubSpot.  
  **Behavior:**
    - Receives the authorization code and state as query parameters.
    - Performs the token exchange.
    - Returns a simple HTML view indicating success or error.
    - **Responses:**
        - **HTML response** indicating whether authentication was successful or failed.

### 5.3 UserAuthController
- **POST /api/auth/login**  
  **Description:** Authenticates a user and returns a JWT token.  
  **Behavior:**
    - Receives a LoginRequest payload.
    - Invokes the authentication service to validate credentials.
    - **Responses:**
        - **200 OK** with a TokenResponse (JWT token).
        - **400 Bad Request** on invalid input.

### 5.4 UserController
- **POST /api/user**  
  **Description:** Creates a new user in the system.  
  **Behavior:**
    - Receives a RegisterRequest payload.
    - Invokes the user creation use case.
    - Protected (if desired) by resilience patterns.
    - **Responses:**
        - **201 CREATED** on successful creation.
        - **400 Bad Request** on validation errors.

- **GET /api/user**  
  **Description:** Lists all users (staff only).  
  **Behavior:**
    - Invokes the list use case.
    - Protected by Spring Security (restricted to Admin).
    - **Responses:**
        - **200 OK** with a list of users.
        - **400 Bad Request** if the input is invalid.
        - **404 Not Found** if no users are found.

### 5.5 WebhookController
- **POST /api/hubspot/webhook/contact**  
  **Description:** Processes incoming webhook events from HubSpot for contact creations.  
  **Behavior:**
    - Protected with HMAC validation via a custom annotation.
    - Receives a list of ContactCreationEventRequest objects.
    - Invokes the process use case to persist the events.
    - **Responses:**
        - **200 OK** upon successful processing.

- **GET /api/hubspot/webhook/contact**  
  **Description:** Retrieves a paginated list of contact creation events received via webhooks.  
  **Behavior:**
    - Accepts pagination parameters (pageNumber and size as query parameters).
    - Returns a Page of ContactCreationEventEntity objects.
    - **Responses:**
        - **200 OK** with paginated results.
        - **Appropriate error responses** if invalid parameters are supplied.

---

## 6. Future Improvements

- **Enhanced Monitoring:**  
  Integrate with Micrometer (e.g., via the resilience4j-micrometer module) to push detailed metrics to Prometheus/Grafana dashboards for real-time insights into application performance and resilience events.

- **Expanded Resilience Patterns:**  
  Extend the custom resilience annotation (@Resilient) to support additional patterns such as Retry, Time Limiter, and Bulkhead, enabling even finer-grained control of failure scenarios.

- **Global Fallback Customization:**  
  Investigate strategies to define a global fallback behavior (through a dedicated error handler or an advanced aspect) for consistent error responses across all endpoints, reducing code duplication.

- **Advanced Reporting:**  
  Integrate Jasper Reports to generate comprehensive reports (e.g., for audit trails, performance metrics, or sales pipelines). This will enhance business intelligence and help stakeholders obtain critical insights from CRM data.

- **New Business Rules:**  
  Further evolve the CRM domain by implementing new business rules and workflows pertinent to corporate environments. For example, automate lead scoring, customer segmentation, or cross-selling opportunities to improve overall business processes.

- **Increased Test Coverage:**  
  Improve integration and stress tests using tools like WireMock and JMeter to validate system behavior under high load and fault conditions, ensuring robustness in production.

- **Container Orchestration:**  
  Consider adopting Kubernetes for container orchestration to further enhance scalability and reliability in production deployments, along with continuous delivery pipelines for automated updates.

---