# 💎 Jewelry Shop Backend

A professional e-commerce backend built with Spring Boot, featuring advanced authentication, inventory management, order processing, and payment integration.

## 🚀 Features

- **🔐 Multi-Authentication**: JWT + Google OAuth2
- **🛒 Complete E-commerce**: Orders, payments, inventory management
- **📧 Email Notifications**: HTML templates with order confirmations
- **🔍 Advanced Search**: Product filtering and pagination
- **🛡️ Enterprise Security**: Role-based access control
- **📊 Monitoring**: Backup system and health checks
- **🌐 RESTful APIs**: Well-documented endpoints

## 🏗️ Architecture

- **Framework**: Spring Boot 3.5.5
- **Database**: PostgreSQL with Flyway migrations
- **Security**: Spring Security with JWT
- **Email**: Thymeleaf templates with SMTP
- **Payment**: Stripe, PayPal, and Demo providers
- **Documentation**: OpenAPI/Swagger ready

## 🛠️ Setup Instructions

### Prerequisites

- Java 21+
- PostgreSQL 15+
- Maven 3.9+

### 1. Clone and Configure

```bash
git clone <your-repo-url>
cd jewelry-shop-backend
```

### 2. Database Setup

```sql
CREATE DATABASE jewelry_shop;
CREATE USER jewelry_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE jewelry_shop TO jewelry_user;
```

### 3. Environment Configuration

**Option A: Using application-local.properties**
```bash
cp src/main/resources/application-example.properties src/main/resources/application-local.properties
# Edit application-local.properties with your values
```

**Option B: Using Environment Variables**
```bash
cp env.example .env
# Edit .env with your values
```

### 4. Required Environment Variables

```bash
# Database
DB_URL=jdbc:postgresql://localhost:5432/jewelry_shop
DB_USERNAME=your_username
DB_PASSWORD=your_password

# JWT (Generate: openssl rand -base64 64)
JWT_SECRET=your_super_secret_key

# Google OAuth (Get from: https://console.cloud.google.com/)
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

# Email (Gmail recommended)
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your_gmail_app_password
EMAIL_FROM=your-email@gmail.com
```

### 5. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## 📚 API Endpoints

### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `GET /oauth2/authorization/google` - Google OAuth login
- `POST /api/auth/refresh` - Refresh JWT token

### Products
- `GET /api/products` - List products with pagination and search
- `GET /api/products/{id}` - Get product details
- `POST /api/products` - Create product (Admin only)

### Orders
- `POST /api/orders` - Create order
- `GET /api/orders` - Get user orders
- `GET /api/orders/{id}` - Get order details
- `POST /api/orders/{id}/cancel` - Cancel order

### Payments
- `POST /api/payments` - Process payment
- `GET /api/payments/{id}` - Get payment details
- `POST /api/payments/{id}/refund` - Refund payment (Admin)

### Inventory
- `GET /api/inventory/low-stock` - Get low stock products
- `POST /api/inventory/product/{id}/reserve` - Reserve stock
- `POST /api/inventory/bulk-update` - Bulk inventory update

## 🔧 Configuration

### Gmail SMTP Setup
1. Enable 2-factor authentication on Gmail
2. Generate App Password: Google Account → Security → App passwords
3. Use App Password (not regular password)

### Google OAuth Setup
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create project and enable Google+ API
3. Create OAuth 2.0 credentials
4. Add authorized redirect URIs

## 🧪 Testing

Test email functionality:
```bash
curl -X POST "http://localhost:8080/api/test/email?to=your-email@gmail.com"
```

## 🚨 Security Notes

- **Never commit real credentials to Git**
- Use environment variables for all secrets
- Generate strong JWT secrets
- Use HTTPS in production
- Regularly rotate API keys

## 📄 License

This project is for educational and portfolio purposes.

## 🤝 Contributing

This is a showcase project. For production use, please implement additional security measures and testing.

---

**Built with ❤️ using Spring Boot**