project files

com/example/studentportal/
│
├── config/
│   ├── SecurityConfig.java          # JWT + session security setup
│   ├── FileStorageConfig.java       # File upload folder config
│   └── WebConfig.java               # CORS, static resource handling
│
├── controller/
│   ├── web/
│   │   ├── AdminController.java     # Thymeleaf views for admin dashboard
│   │   ├── InstructorController.java# Instructor pages
│   │   └── StudentController.java   # Student pages
│   ├── api/
│   │   ├── AuthController.java      # /api/auth/login, register
│   │   ├── CourseController.java    # /api/courses CRUD
│   │   ├── EnrollmentController.java# /api/enrollments
│   │   ├── MaterialController.java  # /api/materials upload/download
│   │   ├── AssignmentController.java# /api/assignments
│   │   ├── SubmissionController.java# /api/submissions
│   │   └── ReportController.java    # /api/admin reports
│
├── dto/
│   ├── request/
│   │   ├── LoginRequest.java
│   │   ├── RegisterUserRequest.java
│   │   ├── CourseRequest.java
│   │   ├── AssignmentRequest.java
│   │   └── GradeRequest.java
│   └── response/
│       ├── JwtResponse.java
│       ├── UserResponse.java
│       ├── CourseResponse.java
│       └── ReportResponse.java
│
├── entity/
│   ├── User.java
│   ├── Course.java
│   ├── Enrollment.java
│   ├── Material.java
│   ├── Assignment.java
│   ├── Submission.java
│   └── Comment.java
│
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   ├── UnauthorizedException.java
│   └── FileStorageException.java
│
├── repository/
│   ├── UserRepository.java
│   ├── CourseRepository.java
│   ├── EnrollmentRepository.java
│   ├── MaterialRepository.java
│   ├── AssignmentRepository.java
│   ├── SubmissionRepository.java
│   └── CommentRepository.java
│
├── security/
│   ├── JwtUtils.java
│   ├── JwtAuthenticationFilter.java
│   ├── CustomUserDetails.java
│   ├── CustomUserDetailsService.java
│   └── SecurityConstants.java
│
├── service/
│   ├── AuthService.java
│   ├── UserService.java
│   ├── CourseService.java
│   ├── EnrollmentService.java
│   ├── MaterialService.java
│   ├── AssignmentService.java
│   ├── SubmissionService.java
│   └── ReportService.java
│
├── util/
│   ├── FileStorageService.java
│   └── PdfGenerator.java
│
└── StudentPortalApplication.java



rsources files


resources/
│
├── application.properties          # DB, JWT, file storage configs
│
├── static/                          # Public static resources
│   ├── css/
│   ├── js/
│   └── images/
│
├── templates/                       # Thymeleaf templates
│   ├── admin/
│   │   ├── dashboard.html
│   │   ├── courses.html
│   │   └── reports.html
│   ├── instructor/
│   │   ├── courses.html
│   │   ├── assignments.html
│   │   └── materials.html
│   └── student/
│       ├── courses.html
│       ├── assignments.html
│       └── grades.html
│
└── db/
    └── migration/                   # (Optional) Flyway or Liquibase scripts
