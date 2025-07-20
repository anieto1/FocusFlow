# FocusFlow Microservices Implementation Plan

## 📋 **Project Overview**
Comprehensive roadmap for implementing a production-ready collaborative pomodoro application using microservices architecture with Docker, gRPC, Kafka, and WebSockets.

## 🏗️ **Current Architecture Status**

### ✅ **Completed Services**
- **session-service**: **95% COMPLETE** - Production-ready with comprehensive validation, transaction management, and helper method organization
  - ✅ Core CRUD operations (create, update, delete)
  - ✅ Session lifecycle management (end, pause, resume)
  - ✅ Complete participant management (join, leave, remove, list)
  - ✅ Access control utilities (isUserSessionOwner)
  - ❌ Remaining: 2 utility methods, pomodoro phase management, task management
- **user-service**: Basic CRUD operations with database migrations
- **task-service**: Basic structure implemented
- **Common module**: gRPC proto file structure established

### 🔄 **Services Requiring Implementation**
- **auth-service**: JWT authentication and authorization
- **api-gateway**: Request routing and rate limiting
- **chat-service**: WebSocket real-time messaging
- **notification-service**: Real-time notifications via WebSockets
- **analytics-service**: Metrics collection and reporting

## 🎯 **Implementation Phases**

### **Phase 1: Foundation Infrastructure (Weeks 1-2)**

#### **1.1 gRPC Proto Definitions**
**Location**: `common/grpc-proto-files/`

**user.proto**:
```protobuf
syntax = "proto3";

package user;

option java_package = "com.pm.common.grpc.user";
option java_outer_classname = "UserServiceProto";

service UserService {
  rpc GetUserById(GetUserByIdRequest) returns (UserResponse);
  rpc GetUserByUsername(GetUserByUsernameRequest) returns (UserResponse);
  rpc ValidateUserExists(ValidateUserExistsRequest) returns (ValidateUserExistsResponse);
  rpc GetUsersByIds(GetUsersByIdsRequest) returns (GetUsersByIdsResponse);
}

message GetUserByIdRequest {
  string user_id = 1;
}

message GetUserByUsernameRequest {
  string username = 1;
}

message ValidateUserExistsRequest {
  string user_id = 1;
}

message ValidateUserExistsResponse {
  bool exists = 1;
  string username = 2;
}

message UserResponse {
  string user_id = 1;
  string username = 2;
  string email = 3;
  string first_name = 4;
  string last_name = 5;
  int64 created_at = 6;
}

message GetUsersByIdsRequest {
  repeated string user_ids = 1;
}

message GetUsersByIdsResponse {
  repeated UserResponse users = 1;
}
```

**session.proto**:
```protobuf
syntax = "proto3";

package session;

option java_package = "com.pm.common.grpc.session";
option java_outer_classname = "SessionServiceProto";

service SessionService {
  rpc CreateSession(CreateSessionRequest) returns (SessionResponse);
  rpc JoinSession(JoinSessionRequest) returns (SessionResponse);
  rpc LeaveSession(LeaveSessionRequest) returns (LeaveSessionResponse);
  rpc GetSessionParticipants(GetSessionParticipantsRequest) returns (GetSessionParticipantsResponse);
}

message CreateSessionRequest {
  string owner_id = 1;
  string session_name = 2;
  string description = 3;
  int32 work_duration_minutes = 4;
  int32 short_break_minutes = 5;
  int32 long_break_minutes = 6;
  int32 max_participants = 7;
}

message JoinSessionRequest {
  string session_id = 1;
  string user_id = 2;
  string invite_code = 3;
}

message LeaveSessionRequest {
  string session_id = 1;
  string user_id = 2;
}

message LeaveSessionResponse {
  bool success = 1;
  string message = 2;
}

message SessionResponse {
  string session_id = 1;
  string session_name = 2;
  string description = 3;
  string owner_username = 4;
  string status = 5;
  int32 current_participant_count = 6;
  int32 max_participants = 7;
  string invite_code = 8;
  int64 created_at = 9;
  int64 start_time = 10;
}

message GetSessionParticipantsRequest {
  string session_id = 1;
  string requester_id = 2;
}

message GetSessionParticipantsResponse {
  repeated string participant_user_ids = 1;
}
```

#### **1.2 Docker Infrastructure Setup**

**Project Root docker-compose.yml**:
```yaml
version: '3.8'

services:
  # Infrastructure
  zookeeper:
    image: confluentinc/cp-zookeeper:7.4.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - "2181:2181"

  kafka:
    image: confluentinc/cp-kafka:7.4.0
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes

  postgres-user:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: user_service
      POSTGRES_USER: user_service
      POSTGRES_PASSWORD: user_service_pass
    ports:
      - "5432:5432"
    volumes:
      - postgres_user_data:/var/lib/postgresql/data

  postgres-session:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: session_service
      POSTGRES_USER: session_service
      POSTGRES_PASSWORD: session_service_pass
    ports:
      - "5433:5432"
    volumes:
      - postgres_session_data:/var/lib/postgresql/data

  postgres-task:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: task_service
      POSTGRES_USER: task_service
      POSTGRES_PASSWORD: task_service_pass
    ports:
      - "5434:5432"
    volumes:
      - postgres_task_data:/var/lib/postgresql/data

  postgres-analytics:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: analytics_service
      POSTGRES_USER: analytics_service
      POSTGRES_PASSWORD: analytics_service_pass
    ports:
      - "5435:5432"
    volumes:
      - postgres_analytics_data:/var/lib/postgresql/data

  # Service Discovery
  eureka-server:
    image: steeltoeoss/eureka-server:latest
    ports:
      - "8761:8761"

volumes:
  postgres_user_data:
  postgres_session_data:
  postgres_task_data:
  postgres_analytics_data:
```

#### **1.3 Service Dockerfiles**

**user-service/Dockerfile**:
```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy Maven wrapper and pom.xml first for dependency caching
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build application
RUN ./mvnw clean package -DskipTests

# Create final image
FROM openjdk:17-jre-slim

WORKDIR /app

COPY --from=0 /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### **Phase 2: Core Service Integration (Weeks 3-4)**

#### **2.1 Authentication Service Implementation**

**auth-service Dependencies (pom.xml)**:
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.11.5</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.11.5</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.11.5</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    <dependency>
        <groupId>net.devh</groupId>
        <artifactId>grpc-client-spring-boot-starter</artifactId>
        <version>2.15.0.RELEASE</version>
    </dependency>
</dependencies>
```

**JWT Token Service Structure**:
```java
@Service
public class JwtService {
    private String secretKey = "your-secret-key";
    private long accessTokenExpiration = 900000; // 15 minutes
    private long refreshTokenExpiration = 604800000; // 7 days
    
    public String generateAccessToken(UserDetails userDetails);
    public String generateRefreshToken(UserDetails userDetails);
    public boolean isTokenValid(String token, UserDetails userDetails);
    public String extractUsername(String token);
    public Claims extractAllClaims(String token);
}
```

#### **2.2 API Gateway Configuration**

**api-gateway Dependencies**:
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-gateway</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
</dependencies>
```

**Gateway Routes Configuration**:
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
        
        - id: session-service
          uri: lb://session-service
          predicates:
            - Path=/api/sessions/**
          filters:
            - AuthenticationFilter
            
        - id: auth-service
          uri: lb://auth-service
          predicates:
            - Path=/api/auth/**
```

#### **2.3 gRPC Client Implementation**

**session-service gRPC Client Configuration**:
```java
@Configuration
public class GrpcClientConfig {
    
    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub userServiceStub;
    
    @Bean
    public UserServiceClient userServiceClient() {
        return new UserServiceClient(userServiceStub);
    }
}

@Service
public class UserServiceClient {
    private final UserServiceGrpc.UserServiceBlockingStub userServiceStub;
    
    public UserServiceClient(UserServiceGrpc.UserServiceBlockingStub userServiceStub) {
        this.userServiceStub = userServiceStub;
    }
    
    public String getUsernameFromUserId(UUID userId) {
        try {
            ValidateUserExistsRequest request = ValidateUserExistsRequest.newBuilder()
                .setUserId(userId.toString())
                .build();
            
            ValidateUserExistsResponse response = userServiceStub.validateUserExists(request);
            
            if (response.getExists()) {
                return response.getUsername();
            } else {
                throw new UserNotFoundException("User not found: " + userId);
            }
        } catch (StatusRuntimeException e) {
            log.error("gRPC call failed for user {}: {}", userId, e.getStatus());
            throw new UserServiceException("Failed to validate user", e);
        }
    }
}
```

### **Phase 3: Event-Driven Architecture (Weeks 5-6)**

#### **3.1 Kafka Topics and Schemas**

**Topic Definitions**:
```yaml
Topics:
  session-events:
    partitions: 3
    replication-factor: 1
    config:
      retention.ms: 604800000 # 7 days
  
  user-events:
    partitions: 3
    replication-factor: 1
    config:
      retention.ms: 2592000000 # 30 days
  
  task-events:
    partitions: 3
    replication-factor: 1
    config:
      retention.ms: 604800000 # 7 days
  
  notification-events:
    partitions: 6
    replication-factor: 1
    config:
      retention.ms: 86400000 # 1 day
```

**Event Schema Examples**:
```java
// Session Events
@JsonIgnoreProperties(ignoreUnknown = true)
public class SessionCreatedEvent {
    private String sessionId;
    private String ownerUserId;
    private String sessionName;
    private int maxParticipants;
    private LocalDateTime createdAt;
    private String eventType = "SESSION_CREATED";
}

@JsonIgnoreProperties(ignoreUnknown = true)
public class SessionParticipantJoinedEvent {
    private String sessionId;
    private String userId;
    private LocalDateTime joinedAt;
    private int currentParticipantCount;
    private String eventType = "PARTICIPANT_JOINED";
}

// User Events
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRegisteredEvent {
    private String userId;
    private String username;
    private String email;
    private LocalDateTime registeredAt;
    private String eventType = "USER_REGISTERED";
}
```

#### **3.2 Kafka Producer Configuration**

**session-service Kafka Producer**:
```java
@Configuration
@EnableKafka
public class KafkaProducerConfig {
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configs.put(ProducerConfig.RETRIES_CONFIG, 3);
        configs.put(ProducerConfig.ACKS_CONFIG, "all");
        return new DefaultKafkaProducerFactory<>(configs);
    }
    
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}

@Service
public class SessionEventPublisher {
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    public void publishSessionCreated(Session session) {
        SessionCreatedEvent event = SessionCreatedEvent.builder()
            .sessionId(session.getSessionId().toString())
            .ownerUserId(session.getOwnerUsername()) // Will be updated to userId
            .sessionName(session.getSessionName())
            .maxParticipants(session.getMaxParticipants())
            .createdAt(session.getCreatedAt())
            .build();
            
        kafkaTemplate.send("session-events", session.getSessionId().toString(), event);
    }
}
```

### **Phase 4: Real-Time Features (Weeks 7-8)**

#### **4.1 WebSocket Configuration**

**chat-service WebSocket Setup**:
```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new ChatWebSocketHandler(), "/chat")
                .setAllowedOrigins("*")
                .withSockJS();
    }
}

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> sessionToUsers = new ConcurrentHashMap<>();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = getSessionId(session);
        String userId = getUserId(session);
        
        sessions.put(session.getId(), session);
        sessionToUsers.computeIfAbsent(sessionId, k -> new HashSet<>()).add(userId);
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Handle chat message and broadcast to session participants
        ChatMessage chatMessage = objectMapper.readValue(message.getPayload(), ChatMessage.class);
        broadcastToSession(chatMessage.getSessionId(), message);
    }
}
```

#### **4.2 Notification Service WebSocket**

**notification-service Real-time Notifications**:
```java
@Service
public class NotificationService {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    @KafkaListener(topics = "notification-events")
    public void handleNotificationEvent(NotificationEvent event) {
        switch (event.getType()) {
            case SESSION_INVITATION:
                sendSessionInvitation(event);
                break;
            case POMODORO_PHASE_CHANGE:
                sendPhaseChangeNotification(event);
                break;
            case TASK_COMPLETED:
                sendTaskCompletionNotification(event);
                break;
        }
    }
    
    private void sendSessionInvitation(NotificationEvent event) {
        messagingTemplate.convertAndSendToUser(
            event.getTargetUserId(),
            "/queue/invitations",
            event.getPayload()
        );
    }
}
```

### **Phase 5: Production Deployment (Weeks 9-10)**

#### **5.1 Kubernetes Deployment**

**session-service Deployment**:
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: session-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: session-service
  template:
    metadata:
      labels:
        app: session-service
    spec:
      containers:
      - name: session-service
        image: focusflow/session-service:latest
        ports:
        - containerPort: 8080
        - containerPort: 9090  # gRPC
        env:
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            secretKeyRef:
              name: session-db-secret
              key: url
        - name: SPRING_KAFKA_BOOTSTRAP_SERVERS
          value: "kafka-service:9092"
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
---
apiVersion: v1
kind: Service
metadata:
  name: session-service
spec:
  selector:
    app: session-service
  ports:
  - name: http
    port: 8080
    targetPort: 8080
  - name: grpc
    port: 9090
    targetPort: 9090
```

#### **5.2 Security Implementation**

**Service Mesh with Istio**:
```yaml
apiVersion: security.istio.io/v1beta1
kind: PeerAuthentication
metadata:
  name: default
spec:
  mtls:
    mode: STRICT

---
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
  name: session-service-policy
spec:
  selector:
    matchLabels:
      app: session-service
  rules:
  - from:
    - source:
        principals: ["cluster.local/ns/default/sa/api-gateway"]
    - source:
        principals: ["cluster.local/ns/default/sa/user-service"]
    to:
    - operation:
        methods: ["GET", "POST", "PUT", "DELETE"]
```

## 🔒 **Security Implementation Roadmap**

### **Authentication & Authorization**
```yaml
JWT Implementation:
  - Access tokens: 15 minutes expiration
  - Refresh tokens: 7 days expiration
  - Token rotation on refresh
  - Blacklist for revoked tokens (Redis)

Service-to-Service Auth:
  - mTLS certificates for gRPC
  - Service account tokens
  - API keys for internal services

API Security:
  - Rate limiting (Redis-based)
  - Input validation at gateway
  - SQL injection prevention
  - XSS protection headers
```

### **Network Security**
```yaml
Network Policies:
  - Ingress: Only from API Gateway
  - Egress: Database, Kafka, other services
  - No direct external access to services

TLS/SSL:
  - End-to-end encryption
  - Certificate management with cert-manager
  - HTTPS enforcement

Secrets Management:
  - Kubernetes secrets for sensitive data
  - Environment-specific configurations
  - Secret rotation policies
```

## 📊 **Monitoring & Observability**

### **Logging Strategy**
```yaml
Centralized Logging:
  - ELK Stack (Elasticsearch, Logstash, Kibana)
  - Structured JSON logging
  - Correlation IDs across services
  - Log levels per environment

Application Logs:
  - Business events (user actions, session events)
  - Error tracking with stack traces
  - Performance metrics
  - Security events
```

### **Metrics Collection**
```yaml
Prometheus Metrics:
  - JVM metrics (memory, GC, threads)
  - Application metrics (request counts, durations)
  - Business metrics (active sessions, user counts)
  - Infrastructure metrics (database connections, Kafka lag)

Grafana Dashboards:
  - Service health overview
  - Business metrics dashboard
  - Infrastructure monitoring
  - Error rate tracking
```

## 🧪 **Testing Strategy**

### **Test Pyramid Implementation**
```yaml
Unit Tests (70%):
  - Service layer business logic
  - Helper method validation
  - DTO mapping verification
  - Exception handling

Integration Tests (20%):
  - Database operations
  - gRPC client/server communication
  - Kafka producer/consumer
  - Redis caching

End-to-End Tests (10%):
  - Complete user workflows
  - Cross-service communication
  - WebSocket functionality
  - Authentication flows
```

### **Test Infrastructure**
```yaml
Testcontainers:
  - PostgreSQL test databases
  - Kafka test cluster
  - Redis test instance
  - Isolated test environments

Contract Testing:
  - gRPC contract verification
  - API contract testing with Pact
  - Event schema validation
  - Database migration testing
```

## 📋 **Implementation Checklist**

### **Week 1-2: Foundation**
- [ ] Complete gRPC proto definitions
- [ ] Set up Docker Compose for local development
- [ ] Implement user-service gRPC server
- [ ] Create Dockerfiles for all services
- [ ] Set up Kafka infrastructure
- [x] **session-service core implementation** - 95% complete with robust participant management

### **Week 3-4: Core Integration**
- [ ] Implement auth-service with JWT
- [ ] Replace session-service placeholder with gRPC client
- [ ] Set up API Gateway with Spring Cloud Gateway
- [ ] Implement service discovery with Eureka
- [ ] Add comprehensive error handling

### **Week 5-6: Event-Driven Architecture**
- [ ] Implement Kafka producers in all services
- [ ] Create event consumers in analytics-service
- [ ] Add event publishing to session lifecycle
- [ ] Implement retry and dead letter patterns
- [ ] Add event schema validation

### **Week 7-8: Real-Time Features**
- [ ] Implement WebSocket in chat-service
- [ ] Add real-time notifications
- [ ] Create WebSocket authentication
- [ ] Implement presence indicators
- [ ] Add connection management

### **Week 9-10: Production Deployment**
- [ ] Create Kubernetes deployment manifests
- [ ] Set up monitoring with Prometheus/Grafana
- [ ] Implement centralized logging
- [ ] Add health checks and readiness probes
- [ ] Set up CI/CD pipeline
- [ ] Implement backup and disaster recovery

## 🎯 **Success Metrics**

### **Technical Metrics**
- Service response time < 200ms (95th percentile)
- Database query time < 50ms (average)
- gRPC call latency < 100ms (95th percentile)
- System uptime > 99.9%
- Zero-downtime deployments

### **Business Metrics**
- Support 1000+ concurrent users
- Handle 10,000+ sessions per day
- Real-time message delivery < 500ms
- User session join success rate > 99%
- Data consistency across all services

## 🚀 **Future Enhancements**

### **Advanced Features**
- Auto-scaling based on user load
- Multi-region deployment
- Advanced analytics with machine learning
- Mobile app support
- Third-party integrations (Slack, Microsoft Teams)

### **Technical Improvements**
- Event sourcing for audit trails
- CQRS pattern for read/write separation
- GraphQL API for flexible queries
- Advanced caching strategies
- Database sharding for scalability

---

*This plan provides a comprehensive roadmap for building a production-ready microservices application. Adjust timelines based on team size and experience level.*