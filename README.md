# 💬 JLiveChats — Real-Time Chat Application

> A modern, production-ready real-time web chat application built with **Spring Boot 3.3.0**, **WebSockets (STOMP)**, and **Google OAuth2** — featuring instant multi-user messaging, secure authentication, and a responsive chat UI.

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-brightgreen?style=flat-square&logo=springboot)
![WebSocket](https://img.shields.io/badge/WebSocket-STOMP-blue?style=flat-square)
![OAuth2](https://img.shields.io/badge/Auth-Google%20OAuth2-red?style=flat-square&logo=google)
![License](https://img.shields.io/badge/License-Educational-lightgrey?style=flat-square)

---

## ✨ Features

### 🔐 Authentication
- **Custom Login & Registration** — with BCrypt password hashing via Spring Security
- **Google OAuth2 Sign-In** — one-click sign-in with your Google account
- **Session Management** — secure HTTP session tracking after login
- **Duplicate User Prevention** — username/email uniqueness enforced at the database level

### 💬 Real-Time Messaging
- **WebSocket + STOMP** — instant bidirectional message delivery
- **SockJS Fallback** — works even in environments where WebSocket is blocked
- **Multi-Channel Support** — `#general`, `#random`, `#announcements`, `#help`
- **Message Persistence** — chat history stored in the H2 database (last 50 messages)
- **Typing Indicators** — see when someone else is typing in real-time

### 👥 User Presence
- **Online/Offline Status** — live tracking of who is connected
- **Active User List** — shown in the sidebar in real-time
- **User Join/Leave Notifications** — broadcast events on connect/disconnect

### 🎨 UI / UX
- **Responsive Design** — works on desktop and mobile browsers
- **Gradient Dark Theme** — modern blue-purple sidebar + clean chat area
- **Message Bubbles** — clear visual separation for sent vs. received messages
- **Smooth Animations** — fade-in transitions and micro-interactions
- **Emoji Reactions** — react to messages with emojis

---

## 🛠 Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend Framework | Spring Boot 3.3.0 |
| Language | Java 21 |
| Real-Time | WebSocket + STOMP + SockJS |
| Security | Spring Security + Google OAuth2 |
| Database | H2 In-Memory (JPA / Hibernate) |
| Templating | Thymeleaf |
| Frontend | HTML5 + CSS3 + Vanilla JavaScript |
| Build Tool | Maven 3.8+ |
| Server | Embedded Apache Tomcat 10 |

---

## 📁 Project Structure

```
JLiveChats/
├── src/main/java/com/jlivechats/
│   ├── JLiveChatsApplication.java          # Spring Boot entry point
│   ├── config/
│   │   ├── SecurityConfig.java             # Spring Security + OAuth2 config
│   │   ├── WebSocketConfig.java            # STOMP / SockJS WebSocket config
│   │   └── SessionAuthenticationFilter.java # Session → Security bridge
│   ├── controller/
│   │   ├── WebController.java              # Login, Register, Chat page routes
│   │   └── WebSocketController.java        # STOMP message handlers
│   ├── model/
│   │   ├── User.java                       # User JPA entity
│   │   └── ChatMessage.java                # Message JPA entity
│   ├── repository/
│   │   ├── UserRepository.java             # JPA Repository for users
│   │   └── ChatMessageRepository.java      # JPA Repository for messages
│   └── service/
│       └── AuthenticationService.java      # User auth + registration logic
├── src/main/resources/
│   ├── templates/                          # Thymeleaf HTML templates
│   │   ├── login.html
│   │   ├── register.html
│   │   └── chat.html
│   ├── static/                             # Static assets
│   │   ├── css/style.css
│   │   ├── js/
│   │   ├── index.html
│   │   └── script.js
│   └── application.properties              # App configuration
├── pom.xml
├── Dockerfile
├── render.yaml
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites
- **Java 17+** → `java -version`
- **Maven 3.8+** → `mvn -version`

### 1. Clone the Repository
```bash
git clone https://github.com/bhola-dev58/JLiveChats.git
cd JLiveChats
```

### 2. Build
```bash
mvn clean package
```

### 3. Run
```bash
java -jar target/jlivechats-1.0.0.jar
```

### 4. Open in Browser
```
http://localhost:8080
```

> The app will redirect you to `/login`. Register a new account or sign in with Google.

---

## 🔑 Google OAuth2 Setup (Optional)

By default, the Google credentials in `application.properties` are pre-configured for local use. If you want to use your own Google Cloud credentials:

1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Create an OAuth2 Client ID
3. Add `http://localhost:8080/login/oauth2/code/google` as an Authorized Redirect URI
4. Update `application.properties`:
```properties
spring.security.oauth2.client.registration.google.client-id=YOUR_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_CLIENT_SECRET
```

> For full setup instructions, see [GOOGLE_OAUTH_FIX.md](GOOGLE_OAUTH_FIX.md)

---

## 🌐 WebSocket Endpoints

| Endpoint | Type | Description |
|----------|------|-------------|
| `/ws` | WebSocket | SockJS connection endpoint |
| `/app/sendMessage` | STOMP Publish | Send a chat message |
| `/app/typing` | STOMP Publish | Broadcast typing indicator |
| `/topic/messages` | STOMP Subscribe | Receive new messages |
| `/topic/users` | STOMP Subscribe | Receive user presence updates |

---

## 🌍 Deployment

### Docker
```bash
docker build -t jlivechats .
docker run -p 8080:8080 jlivechats
```

### Render / Railway (Free Cloud)
- See [DEPLOYMENT.md](DEPLOYMENT.md) for step-by-step cloud deployment instructions.

---

## 🔒 Security

- Passwords are hashed with **BCrypt** before being stored in the database
- Sessions are secured via **Spring Security's filter chain**
- OAuth2 tokens are managed by **Spring Security OAuth2 Client**
- CSRF protection is disabled intentionally to support WebSocket connections

---

## 🐛 Troubleshooting

| Problem | Fix |
|---------|-----|
| Port 8080 already in use | Run `lsof -i :8080` and kill the process, or change port in `application.properties` |
| Google sign-in error 400 | See [GOOGLE_OAUTH_FIX.md](GOOGLE_OAUTH_FIX.md) |
| App won't start | Ensure Java 17+ is installed: `java -version` |
| Messages not updating | Check browser console for WebSocket connection errors |

---

## 📈 Roadmap

- [x] WebSocket real-time messaging
- [x] Google OAuth2 sign-in
- [x] BCrypt password encryption
- [x] Database-backed user registration
- [x] Typing indicators
- [x] Multi-channel support
- [ ] Direct Messages (1-on-1)
- [ ] File/image sharing
- [ ] PostgreSQL migration (production)
- [ ] Push notifications
- [ ] Admin dashboard

---

## 👨‍💻 Author

**Bhola Yadav**  
USN: `1CR23CS044` | Section: K | Branch: Computer Science Engineering  
[GitHub](https://github.com/bhola-dev58)

---

## 📄 License

This project is open-source and available for **educational purposes**.

---

> **JLiveChats** — Built with ❤️ using Spring Boot, WebSockets, and Google OAuth2.
