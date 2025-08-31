# Asset Management System

A comprehensive asset management system built with Spring Boot, featuring user authentication, asset tracking, loan management, and event-driven notifications.

## 🏗️ Architecture

### Tech Stack
- **Backend**: Java 17, Spring Boot 3.2.0, Spring Security, Spring Data JDBC
- **Database**: MySQL 8.0 with Flyway migrations
- **Message Queue**: Apache Kafka with Zookeeper
- **Authentication**: JWT-based authentication
- **Containerization**: Docker & Docker Compose
- **Email Testing**: MailHog for local development

### System Components
- **API Service**: Main Spring Boot application handling all business logic
- **Notification Service**: Kafka consumer for sending email notifications
- **Database**: MySQL with normalized schema
- **Message Broker**: Kafka for event-driven communication
- **Monitoring**: Kafka UI for message monitoring

## 📊 Database Schema

### Normalized Tables
- `departments` - Organizational departments
- `roles` - User roles (EMPLOYEE, MANAGER, SUPER_ADMIN)
- `users` - User accounts with hierarchical relationships
- `asset_categories` - Asset type classifications
- `assets` - Physical assets with status tracking
- `asset_loans` - Asset loan transactions
- `calendar_events` - Due date and reminder events
- `outbox_events` - Kafka event outbox for reliable messaging
- `system_settings` - Configuration parameters

### Sample Data
- **5 Employees**: emp01-emp05 with random usernames
- **2 Managers**: mgr01, mgr02
- **1 Super Admin**: adm01
- **25+ Assets**: Laptops, monitors, peripherals, badges, mobile devices
- **Organizational Structure**: 2 employees under mgr01, 3 employees under mgr02

## 🔐 Authentication & Authorization

### Default Credentials
All users have the same default password: `admin123` (encrypted)

### User Hierarchy
```
Super Admin (adm01)
├── Manager 1 (mgr01) - IT Department
│   ├── Employee 1 (emp01)
│   └── Employee 2 (emp02)
└── Manager 2 (mgr02) - HR Department
    ├── Employee 3 (emp03)
    ├── Employee 4 (emp04)
    └── Employee 5 (emp05) - Finance Department
```

### Role-Based Access
- **Employee**: Request/return assets, view own loans
- **Manager**: Approve long-term loans, manage team assets
- **Super Admin**: Full system access, user management

## 🚀 Quick Start

### Prerequisites
- Docker & Docker Compose
- Java 17 (for local development)
- Maven (for local development)

### 1. Clone and Setup
```bash
git clone <repository-url>
cd asset-management
```

### 2. Start the System
```bash
docker-compose up -d
```

### 3. Access Services
- **API**: http://localhost:8080/api
- **Kafka UI**: http://localhost:8081
- **MailHog**: http://localhost:8025

### 4. Database Migration
The system automatically runs Flyway migrations on startup.

## 📋 API Endpoints

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/change-password` - Change password

### Assets
- `GET /api/assets` - List available assets
- `GET /api/assets/{id}` - Get asset details
- `POST /api/assets/assign` - Assign asset to user
- `POST /api/assets/return` - Return asset
- `GET /api/assets/my-loans` - Get user's loans

### Management
- `GET /api/manager/pending-approvals` - List pending approvals
- `POST /api/manager/approve/{loanId}` - Approve loan
- `GET /api/manager/team-assets` - View team assets

### Calendar
- `GET /api/calendar/events` - Get calendar events
- `POST /api/calendar/events` - Create calendar event

## 🔄 Event-Driven Features

### Kafka Topics
- `assets.events` - Main event topic
- `assets.dlq` - Dead letter queue for failed events

### Event Types
- `AssetAssigned` - Asset assigned to user
- `AssetDueSoon` - Asset due within 48 hours
- `AssetOverdue` - Asset past due date
- `AssetReturned` - Asset returned

### Scheduled Jobs
- **Daily 09:00**: Scan for due-soon and overdue assets
- **Continuous**: Process outbox events for Kafka

## 🛠️ Development

### Local Development
```bash
# Start dependencies only
docker-compose up -d mysql kafka zookeeper mailhog

# Run application locally
mvn spring-boot:run
```

### Database Connection
```bash
# Connect to MySQL
mysql -h localhost -P 3306 -u root -p
# Password: password
```

### Testing
```bash
# Run tests
mvn test

# Integration tests
mvn verify
```

## 📁 Project Structure
```
asset-management/
├── src/main/java/com/example/asset_management/
│   ├── config/          # Configuration classes
│   ├── controller/      # REST controllers
│   ├── model/          # Entity models
│   ├── repository/     # Data access layer
│   ├── service/        # Business logic
│   ├── security/       # JWT and security
│   └── event/          # Kafka event handling
├── src/main/resources/
│   ├── db/migration/   # Flyway migrations
│   └── application.properties
├── docker-compose.yml  # Infrastructure setup
├── Dockerfile         # Application container
└── README.md
```

## 🔧 Configuration

### Environment Variables
- `SPRING_DATASOURCE_URL` - Database connection
- `SPRING_KAFKA_BOOTSTRAP_SERVERS` - Kafka connection
- `JWT_SECRET` - JWT signing secret
- `JWT_EXPIRATION` - Token expiration time

### System Settings
- `loan_approval_threshold_days` - Days requiring approval (default: 7)
- `due_reminder_days` - Days before due for reminder (default: 2)
- `max_retry_attempts` - Notification retry attempts (default: 3)

## 🧪 Testing Scenarios

### 1. Basic Asset Assignment
1. Login as employee (emp01/admin123)
2. Request asset assignment
3. Verify calendar event created
4. Check Kafka event published

### 2. Manager Approval Flow
1. Request long-term loan (>7 days)
2. Login as manager (mgr01/admin123)
3. Approve pending request
4. Verify asset status updated

### 3. Notification Testing
1. Trigger due-soon event
2. Check MailHog for email
3. Verify DLQ for failed notifications

## 🐛 Troubleshooting

### Common Issues
1. **Database Connection**: Ensure MySQL is running and accessible
2. **Kafka Connection**: Check Zookeeper and Kafka health
3. **Port Conflicts**: Verify ports 8080, 3306, 9092 are available

### Logs
```bash
# View application logs
docker-compose logs asset-management-api

# View Kafka logs
docker-compose logs kafka

# View database logs
docker-compose logs mysql
```

## 📈 Monitoring

### Health Checks
- Application: http://localhost:8080/api/actuator/health
- Kafka UI: http://localhost:8081
- MailHog: http://localhost:8025

### Metrics
- Asset utilization rates
- Loan approval statistics
- Notification delivery rates
- System performance metrics

## 🔒 Security Features

- JWT-based authentication
- Role-based access control
- Password encryption (BCrypt)
- Input validation and sanitization
- SQL injection prevention
- CORS configuration

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📞 Support

For questions or issues, please create an issue in the repository or contact the development team.
