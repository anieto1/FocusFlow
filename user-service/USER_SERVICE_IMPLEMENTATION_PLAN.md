# User Service Implementation Plan

## 📋 **Project Overview**
Comprehensive implementation plan for building a production-ready user management service that supports authentication, user profiles, and microservices integration for the FocusFlow collaborative pomodoro application.

## 🔍 **Current Implementation Analysis**

### ✅ **Completed Features**
1. **Basic CRUD Operations**
   - Create, Read, Update, Delete users via REST API
   - Basic validation with Jakarta Bean Validation
   - Proper HTTP status codes and response formatting

2. **Database Layer**
   - PostgreSQL integration with JPA/Hibernate
   - Flyway database migrations (V1 & V2)
   - UUID primary keys for scalability
   - Unique constraints on email and username

3. **Security Foundations**
   - BCrypt password hashing with PasswordEncoderHelper
   - Email uniqueness validation
   - Basic input validation and sanitization

4. **Exception Handling**
   - Global exception handler with proper error responses
   - Custom exceptions: EmailAlreadyExistsException, UserNotFoundException
   - Validation error mapping

5. **API Documentation**
   - Swagger/OpenAPI annotations
   - Comprehensive endpoint documentation

### ❌ **Missing Critical Features**

#### **1. gRPC Microservices Integration** (PRIMARY FOCUS)
- gRPC server implementation for internal service communication
- Proto file definitions for user service
- Service-to-service authentication
- Load balancing and circuit breakers

#### **2. Advanced User Management**
- User profile management (preferences, settings)
- Role-based access control (RBAC)
- User activity logging and audit trails
- Account deactivation (soft delete)
- User search and filtering capabilities
- Pagination and sorting for user lists

#### **3. Security Enhancements**
- Rate limiting and DDoS protection
- Input sanitization and SQL injection prevention
- Account lockout mechanisms
- Password strength validation
- Security headers and CORS configuration

#### **4. Data Management**
- Profile picture upload and storage
- User preferences and settings
- Data export (GDPR compliance)
- User analytics and metrics
- Backup and disaster recovery

## 🚨 **Critical Security Issues**

### **1. Password Exposure in Response DTOs**
```java
// CURRENT - SECURITY VULNERABILITY
public class UserResponseDTO {
    private String password; // ❌ NEVER expose passwords
}
```

### **2. Missing Service-to-Service Authentication**
- No gRPC authentication for internal calls
- No API authentication from auth-service
- No role-based access control for service endpoints

### **3. Input Validation Gaps**
- No SQL injection protection
- No XSS prevention
- No file upload validation
- No rate limiting

## 🏗️ **Implementation Roadmap**

### **Phase 1: Security Hardening & Service Architecture (Week 1-2)**

#### **1.1 Fix Critical Security Issues**
```java
// Enhanced UserResponseDTO (NO PASSWORD EXPOSURE)
public class UserResponseDTO {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String profilePictureUrl;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isActive;
    private boolean isEmailVerified;
    // NO PASSWORD FIELD!
}
```

#### **1.2 Service Role Definition**
**User Service Responsibilities:**
- User profile management (CRUD)
- User preferences and settings
- User search and discovery
- Profile picture management
- User activity tracking
- gRPC endpoints for other services

**NOT User Service Responsibilities:**
- Authentication (handled by auth-service)
- JWT token management (handled by auth-service)
- Login/logout (handled by auth-service)
- Password reset (handled by auth-service)
- Session management (handled by auth-service)

#### **1.3 Service Integration Architecture**
```java
// User service receives authenticated requests from API Gateway
// API Gateway validates JWT tokens with auth-service
// User service trusts API Gateway authentication

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    
    // These endpoints are protected by API Gateway
    // User ID comes from JWT token validated by auth-service
    
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(@RequestHeader("X-User-ID") UUID userId);
    
    @PutMapping("/me")
    public ResponseEntity<UserResponseDTO> updateCurrentUser(
        @RequestHeader("X-User-ID") UUID userId,
        @Valid @RequestBody UpdateUserRequestDTO request);
        
    @PostMapping("/me/profile-picture")
    public ResponseEntity<UserResponseDTO> uploadProfilePicture(
        @RequestHeader("X-User-ID") UUID userId,
        @RequestParam("file") MultipartFile file);
}
```

### **Phase 2: gRPC Microservices Integration (Week 2-3)**

#### **2.1 gRPC Proto Definitions** (Aligned with Microservices Plan)
```protobuf
// user.proto - Located in common/grpc-proto-files/
syntax = "proto3";

package user;

option java_package = "com.pm.common.grpc.user";
option java_outer_classname = "UserServiceProto";

service UserService {
  rpc GetUserById(GetUserByIdRequest) returns (UserResponse);
  rpc GetUserByUsername(GetUserByUsernameRequest) returns (UserResponse);
  rpc ValidateUserExists(ValidateUserExistsRequest) returns (ValidateUserExistsResponse);
  rpc GetUsersByIds(GetUsersByIdsRequest) returns (GetUsersByIdsResponse);
  
  // Additional methods for user management
  rpc CreateUser(CreateUserRequest) returns (UserResponse);
  rpc UpdateUser(UpdateUserRequest) returns (UserResponse);
  rpc DeactivateUser(DeactivateUserRequest) returns (DeactivateUserResponse);
  rpc GetUserPreferences(GetUserPreferencesRequest) returns (UserPreferencesResponse);
  rpc UpdateUserPreferences(UpdateUserPreferencesRequest) returns (UserPreferencesResponse);
}

message UserResponse {
  string user_id = 1;
  string username = 2;
  string email = 3;
  string first_name = 4;
  string last_name = 5;
  string profile_picture_url = 6;
  string role = 7;
  bool is_active = 8;
  bool is_email_verified = 9;
  int64 created_at = 10;
  int64 updated_at = 11;
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
  bool is_active = 3;
}

message GetUsersByIdsRequest {
  repeated string user_ids = 1;
}

message GetUsersByIdsResponse {
  repeated UserResponse users = 1;
}
```

#### **2.2 gRPC Service Implementation**
```java
@GrpcService
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {
    
    private final UserService userService;
    
    @Override
    public void getUserById(GetUserByIdRequest request, StreamObserver<UserResponse> responseObserver) {
        try {
            UUID userId = UUID.fromString(request.getUserId());
            UserResponseDTO user = userService.getUserById(userId);
            
            UserResponse response = UserResponse.newBuilder()
                .setUserId(user.getId())
                .setUsername(user.getUsername())
                .setEmail(user.getEmail())
                .setFirstName(user.getFirstName())
                .setLastName(user.getLastName())
                .setProfilePictureUrl(user.getProfilePictureUrl())
                .setRole(user.getRole())
                .setIsActive(user.isActive())
                .setIsEmailVerified(user.isEmailVerified())
                .setCreatedAt(user.getCreatedAt().toEpochSecond(ZoneOffset.UTC))
                .setUpdatedAt(user.getUpdatedAt().toEpochSecond(ZoneOffset.UTC))
                .build();
                
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }
    
    @Override
    public void validateUserExists(ValidateUserExistsRequest request, StreamObserver<ValidateUserExistsResponse> responseObserver) {
        try {
            UUID userId = UUID.fromString(request.getUserId());
            UserResponseDTO user = userService.getUserById(userId);
            
            ValidateUserExistsResponse response = ValidateUserExistsResponse.newBuilder()
                .setExists(true)
                .setUsername(user.getUsername())
                .setIsActive(user.isActive())
                .build();
                
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (UserNotFoundException e) {
            ValidateUserExistsResponse response = ValidateUserExistsResponse.newBuilder()
                .setExists(false)
                .build();
                
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}
```

### **Phase 3: Advanced User Management (Week 4-5)**

#### **3.1 Enhanced User Model**
```java
@Entity
@Table(name = "users")
public class User {
    
    @Id
    private UUID id = UUID.randomUUID();
    
    @NotNull
    @Column(name = "username", unique = true, length = 50)
    private String username;
    
    @NotNull
    @Email
    @Column(name = "email", unique = true, length = 100)
    private String email;
    
    @NotNull
    @Column(name = "password")
    private String password;
    
    @NotNull
    @Column(name = "first_name", length = 50)
    private String firstName;
    
    @NotNull
    @Column(name = "last_name", length = 50)
    private String lastName;
    
    @Column(name = "profile_picture_url")
    private String profilePictureUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role = UserRole.USER;
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
    
    @Column(name = "is_email_verified", nullable = false)
    private boolean isEmailVerified = false;
    
    @Column(name = "email_verification_token")
    private String emailVerificationToken;
    
    @Column(name = "password_reset_token")
    private String passwordResetToken;
    
    @Column(name = "password_reset_expires_at")
    private LocalDateTime passwordResetExpiresAt;
    
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts = 0;
    
    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // One-to-one relationship with UserPreferences
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserPreferences preferences;
    
    // One-to-many relationship with UserSessions
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserSession> sessions;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
```

#### **3.2 User Preferences Entity**
```java
@Entity
@Table(name = "user_preferences")
public class UserPreferences {
    
    @Id
    private UUID id = UUID.randomUUID();
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "theme", nullable = false)
    private String theme = "LIGHT";
    
    @Column(name = "language", nullable = false)
    private String language = "en";
    
    @Column(name = "timezone", nullable = false)
    private String timezone = "UTC";
    
    @Column(name = "email_notifications", nullable = false)
    private boolean emailNotifications = true;
    
    @Column(name = "push_notifications", nullable = false)
    private boolean pushNotifications = true;
    
    @Column(name = "work_duration_minutes", nullable = false)
    private int workDurationMinutes = 25;
    
    @Column(name = "short_break_minutes", nullable = false)
    private int shortBreakMinutes = 5;
    
    @Column(name = "long_break_minutes", nullable = false)
    private int longBreakMinutes = 15;
    
    @Column(name = "auto_start_breaks", nullable = false)
    private boolean autoStartBreaks = false;
    
    @Column(name = "auto_start_pomodoros", nullable = false)
    private boolean autoStartPomodoros = false;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
```

#### **3.3 Enhanced Repository Layer**
```java
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    // Basic queries
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmailAndIdNot(String email, UUID id);
    boolean existsByUsernameAndIdNot(String username, UUID id);
    
    // Authentication queries
    Optional<User> findByEmailAndIsActiveTrue(String email);
    Optional<User> findByUsernameAndIsActiveTrue(String username);
    Optional<User> findByEmailVerificationToken(String token);
    Optional<User> findByPasswordResetToken(String token);
    
    // Advanced queries
    @Query("SELECT u FROM User u WHERE u.isActive = true AND " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.isActive = true")
    Page<User> findByRole(@Param("role") UserRole role, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    List<User> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :date")
    long countUsersCreatedAfter(@Param("date") LocalDateTime date);
    
    // Session management
    @Query("SELECT u FROM User u WHERE u.accountLockedUntil > :now")
    List<User> findLockedAccounts(@Param("now") LocalDateTime now);
    
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = 0, u.accountLockedUntil = null WHERE u.id = :userId")
    void unlockAccount(@Param("userId") UUID userId);
}
```

### **Phase 4: Performance & Scalability (Week 6-7)**

#### **4.1 Security Configuration**
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**", "/api/v1/users/register", "/actuator/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/users/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/users/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/users/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/users/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthenticationEntryPoint())
                .accessDeniedHandler(jwtAccessDeniedHandler())
            );
            
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
    
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }
}
```

#### **4.2 Rate Limiting**
```java
@Component
public class RateLimitingInterceptor implements HandlerInterceptor {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = getClientIp(request);
        String key = "rate_limit:" + clientIp;
        
        String count = redisTemplate.opsForValue().get(key);
        if (count == null) {
            redisTemplate.opsForValue().set(key, "1", Duration.ofMinutes(1));
        } else {
            int currentCount = Integer.parseInt(count);
            if (currentCount >= 100) { // 100 requests per minute
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                return false;
            }
            redisTemplate.opsForValue().increment(key);
        }
        
        return true;
    }
}
```

### **Phase 5: Advanced Features & Testing (Week 8-9)**

#### **5.1 File Upload Service**
```java
@Service
public class FileUploadService {
    
    private final AmazonS3 s3Client;
    private final String bucketName;
    
    public String uploadProfilePicture(MultipartFile file, UUID userId) {
        // Validate file type and size
        validateProfilePictureFile(file);
        
        // Generate unique filename
        String filename = generateProfilePictureFilename(userId, file.getOriginalFilename());
        
        // Upload to S3
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());
            
            s3Client.putObject(bucketName, filename, file.getInputStream(), metadata);
            
            return s3Client.getUrl(bucketName, filename).toString();
        } catch (Exception e) {
            throw new FileUploadException("Failed to upload profile picture", e);
        }
    }
    
    private void validateProfilePictureFile(MultipartFile file) {
        // Check file size (max 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new InvalidFileException("Profile picture size must be less than 5MB");
        }
        
        // Check file type
        String contentType = file.getContentType();
        if (!Arrays.asList("image/jpeg", "image/png", "image/gif").contains(contentType)) {
            throw new InvalidFileException("Only JPEG, PNG, and GIF images are allowed");
        }
    }
}
```

#### **5.2 Email Service**
```java
@Service
public class EmailService {
    
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    
    public void sendEmailVerification(User user, String verificationToken) {
        Context context = new Context();
        context.setVariable("user", user);
        context.setVariable("verificationLink", buildVerificationLink(verificationToken));
        
        String htmlContent = templateEngine.process("email-verification", context);
        
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        
        try {
            helper.setTo(user.getEmail());
            helper.setSubject("Verify your FocusFlow account");
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new EmailSendException("Failed to send verification email", e);
        }
    }
    
    public void sendPasswordReset(User user, String resetToken) {
        Context context = new Context();
        context.setVariable("user", user);
        context.setVariable("resetLink", buildPasswordResetLink(resetToken));
        
        String htmlContent = templateEngine.process("password-reset", context);
        
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        
        try {
            helper.setTo(user.getEmail());
            helper.setSubject("Reset your FocusFlow password");
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new EmailSendException("Failed to send password reset email", e);
        }
    }
}
```

## 🧪 **Testing Strategy**

### **Unit Tests (70% Coverage)**
```java
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private EmailService emailService;
    
    @InjectMocks
    private UserServiceImpl userService;
    
    @Test
    void createUser_WithValidData_ShouldReturnUserResponseDTO() {
        // Given
        UserRequestDTO request = createValidUserRequest();
        User savedUser = createUserEntity();
        
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        // When
        UserResponseDTO result = userService.createUser(request);
        
        // Then
        assertThat(result.getEmail()).isEqualTo(request.getEmail());
        assertThat(result.getUsername()).isEqualTo(request.getUsername());
        verify(emailService).sendEmailVerification(any(User.class), any(String.class));
    }
    
    @Test
    void createUser_WithExistingEmail_ShouldThrowEmailAlreadyExistsException() {
        // Given
        UserRequestDTO request = createValidUserRequest();
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);
        
        // When & Then
        assertThatThrownBy(() -> userService.createUser(request))
            .isInstanceOf(EmailAlreadyExistsException.class)
            .hasMessage("A user with this email already exists: " + request.getEmail());
    }
}
```

### **Integration Tests (25% Coverage)**
```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class UserControllerIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void createUser_WithValidData_ShouldReturn201AndUserResponse() throws Exception {
        // Given
        UserRequestDTO request = createValidUserRequest();
        
        // When & Then
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(request.getEmail()))
                .andExpect(jsonPath("$.username").value(request.getUsername()))
                .andExpect(jsonPath("$.password").doesNotExist());
    }
}
```

### **End-to-End Tests (5% Coverage)**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class UserServiceE2ETest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");
    
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);
    
    @Test
    void userRegistrationFlow_ShouldCompleteSuccessfully() {
        // Test complete user registration flow
        // 1. Register user
        // 2. Verify email
        // 3. Login
        // 4. Update profile
        // 5. Change password
    }
}
```

## 🚀 **Performance Optimization**

### **Database Optimization**
```sql
-- Performance indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_created_at ON users(created_at);
CREATE INDEX idx_users_last_login ON users(last_login_at);
CREATE INDEX idx_users_role_active ON users(role, is_active);

-- Composite indexes for common queries
CREATE INDEX idx_users_search ON users(first_name, last_name, username, email);
CREATE INDEX idx_users_active_role ON users(is_active, role);
```

### **Caching Strategy**
```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        RedisCacheManager.Builder builder = RedisCacheManager
            .RedisCacheManagerBuilder
            .fromConnectionFactory(redisConnectionFactory())
            .cacheDefaults(cacheConfiguration());
        
        return builder.build();
    }
    
    private RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(15))
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }
}

@Service
public class UserServiceImpl implements UserService {
    
    @Cacheable(value = "users", key = "#userId")
    public UserResponseDTO getUserById(UUID userId) {
        // Implementation
    }
    
    @CacheEvict(value = "users", key = "#userId")
    public UserResponseDTO updateUser(UUID userId, UserRequestDTO request) {
        // Implementation
    }
}
```

## 📊 **Monitoring & Observability**

### **Metrics Collection**
```java
@Component
public class UserMetrics {
    
    private final MeterRegistry meterRegistry;
    private final Counter userRegistrationCounter;
    private final Counter userLoginCounter;
    private final Timer userCreationTimer;
    
    public UserMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.userRegistrationCounter = Counter.builder("user.registration.total")
            .description("Total number of user registrations")
            .register(meterRegistry);
        this.userLoginCounter = Counter.builder("user.login.total")
            .description("Total number of user logins")
            .register(meterRegistry);
        this.userCreationTimer = Timer.builder("user.creation.duration")
            .description("Time taken to create a user")
            .register(meterRegistry);
    }
    
    public void incrementUserRegistration() {
        userRegistrationCounter.increment();
    }
    
    public void recordUserCreationTime(Duration duration) {
        userCreationTimer.record(duration);
    }
}
```

### **Health Checks**
```java
@Component
public class UserServiceHealthIndicator implements HealthIndicator {
    
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;
    
    @Override
    public Health health() {
        try {
            // Check database connectivity
            long userCount = userRepository.count();
            
            // Check Redis connectivity
            redisTemplate.opsForValue().get("health-check");
            
            return Health.up()
                .withDetail("database", "accessible")
                .withDetail("redis", "accessible")
                .withDetail("totalUsers", userCount)
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

## 🔒 **Security Best Practices**

### **Input Validation & Sanitization**
```java
@Component
public class InputSanitizer {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,30}$");
    
    public String sanitizeInput(String input) {
        if (input == null) return null;
        
        return input.trim()
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll("\"", "&quot;")
            .replaceAll("'", "&#x27;")
            .replaceAll("/", "&#x2F;");
    }
    
    public void validateEmail(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidInputException("Invalid email format");
        }
    }
    
    public void validateUsername(String username) {
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new InvalidInputException("Username must be 3-30 characters and contain only letters, numbers, and underscores");
        }
    }
}
```

### **Account Security**
```java
@Service
public class AccountSecurityService {
    
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final Duration LOCKOUT_DURATION = Duration.ofMinutes(30);
    
    public void recordFailedLogin(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
        
        if (user.getFailedLoginAttempts() >= MAX_LOGIN_ATTEMPTS) {
            user.setAccountLockedUntil(LocalDateTime.now().plus(LOCKOUT_DURATION));
        }
        
        userRepository.save(user);
    }
    
    public boolean isAccountLocked(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        return user.getAccountLockedUntil() != null && 
               user.getAccountLockedUntil().isAfter(LocalDateTime.now());
    }
    
    public void unlockAccount(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        
        userRepository.save(user);
    }
}
```

## 📋 **Implementation Checklist**

### **Phase 1: Security Hardening & Service Architecture (Week 1-2)**
- [ ] Remove password from UserResponseDTO
- [ ] Define clear service boundaries (no auth responsibilities)
- [ ] Implement API Gateway authentication trust
- [ ] Add service-to-service rate limiting
- [ ] Add input validation and sanitization
- [ ] Configure internal service security

### **Phase 2: gRPC Integration (Week 2-3)**
- [ ] Create gRPC proto definitions aligned with microservices plan
- [ ] Implement gRPC service layer for internal calls
- [ ] Add user validation endpoints for session-service
- [ ] Configure gRPC load balancing and service discovery
- [ ] Add circuit breaker patterns for resilience

### **Phase 3: Advanced Features (Week 4-5)**
- [ ] Implement user preferences system
- [ ] Add role-based access control
- [ ] Create user search and filtering
- [ ] Add pagination and sorting
- [ ] Implement user activity logging
- [ ] Add profile picture upload

### **Phase 4: Performance & Scalability (Week 6-7)**
- [ ] Implement Redis caching for user data
- [ ] Add database connection pooling
- [ ] Create performance monitoring and metrics
- [ ] Add health checks for service mesh
- [ ] Implement graceful shutdown
- [ ] Add distributed tracing integration

### **Phase 5: Advanced Features & Testing (Week 8-9)**
- [ ] Write comprehensive unit tests
- [ ] Create integration tests with other services
- [ ] Add contract testing for gRPC interfaces
- [ ] Generate API documentation
- [ ] Create service deployment guides
- [ ] Add monitoring and alerting

## 🎯 **Success Metrics**

### **Performance Targets**
- User profile retrieval: < 200ms (95th percentile)
- User profile update: < 300ms (95th percentile)
- gRPC user validation: < 50ms (95th percentile)
- Database query time: < 50ms (average)
- Cache hit ratio: > 80%

### **Security Targets**
- Zero password exposures in API responses
- Service-to-service authentication: 100% coverage
- API Gateway trust validation: < 10ms overhead
- Rate limiting: 1000 requests/minute per service
- Input validation: 100% coverage

### **Reliability Targets**
- Service uptime: > 99.9%
- Error rate: < 0.1%
- Database connection pool utilization: < 80%
- Memory usage: < 2GB per instance
- CPU usage: < 70% per instance

---

*This implementation plan provides a comprehensive roadmap for building a production-ready user service that integrates seamlessly with the FocusFlow microservices architecture, focusing on user profile management while delegating authentication responsibilities to the dedicated auth-service.*