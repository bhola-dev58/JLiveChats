# JLiveChats - Spring Boot Real-time Chat Application

Modern, professional real-time chat application built with **Spring Boot 3.3.0** and **WebSocket**, featuring user authentication, live messaging, and responsive web interface.

> 🚀 **Live Demo**: Currently deploying on Render (free tier) - Check deployment status in [DEPLOYMENT.md](DEPLOYMENT.md)

## ✨ Features

### User Authentication
- **Login System**: Secure user login with credentials validation
- **Registration**: Create new accounts with email verification
- **Session Management**: Automatic login state tracking
- **User Profiles**: Display current user information

### Chat Features
- **Real-time Messaging**: Send and receive messages instantly
- **Channel Support**: Multiple channels for organized conversations
- **User Presence**: See online users and their status
- **Message Timestamps**: All messages are timestamped
- **Message History**: View recent chat history (50 messages)

### User Interface
- **Modern Design**: Professional gradient-based UI with blue and purple accents
- **Responsive Layout**: Adapts to different screen sizes
- **Dark Sidebar**: Easy-to-read chat list and user management
- **Clean Chat Area**: Message bubbles with clear sender information
- **Professional Colors**: Purple (#667eea) and (#764ba2) gradient theme

## 🛠 Tech Stack

- **Backend Framework**: Spring Boot 3.3.0
- **Real-time Communication**: WebSocket
- **Frontend**: HTML5, CSS3, JavaScript
- **Build Tool**: Maven 3.8+
- **Language**: Java 17
- **Database**: H2 (in-memory)
- **Server**: Embedded Tomcat

## 📋 Prerequisites

- **Java 17** or higher installed
- **Maven** 3.8+ installed
- **Git** (for cloning the repository)

## 🚀 Getting Started

### Option 1: Run from Command Line

1. Navigate to project directory:
   ```bash
   cd JLiveChats
   ```

2. Build the project:
   ```bash
   mvn clean package
   ```

3. Set environment variables (optional for OAuth):
   ```bash
   export GOOGLE_CLIENT_ID="your-client-id"
   export GOOGLE_CLIENT_SECRET="your-client-secret"
   ```

4. Run the application:
   ```bash
   mvn spring-boot:run
   ```

5. Access the application:
   - Open browser: `http://localhost:8080`

### Option 2: Run JAR File

```bash
mvn clean package
java -jar target/jlivechats-1.0.0.jar
```

### Option 3: Run in IDE

1. Open project in IntelliJ IDEA or Eclipse
2. Right-click on `JLiveChatsApplication.java` (in `src/main/java/com/jlivechats/`)
3. Select "Run JLiveChatsApplication.main()"

## 🌐 Deployment

For complete deployment instructions to Render, Railway, Docker, or other platforms, see [DEPLOYMENT.md](DEPLOYMENT.md).

**Quick Deploy to Render (Free):**
1. Go to [render.com](https://render.com)
2. Create new Web Service from GitHub
3. Select this repository
4. Set environment variables (see DEPLOYMENT.md)
5. Deploy!

## 🔐 Default Test Users

The application comes with pre-configured test accounts:

| Username | Password | Notes |
|----------|----------|-------|
| bhola | password123 | Admin user |
| john | john123 | Regular user |
| jane | jane123 | Regular user |

Or create new accounts using the registration screen.

## 📱 UI Components

### Login Screen
- Email/Username input field
- Password input field
- Secure authentication
- Link to registration screen
- Beautiful gradient background

### Registration Screen
- Username input
- Email address input
- Password input with strength indicator
- Password confirmation
- Link back to login screen

### Chat Interface
- **Left Sidebar**:
  - Current user profile
  - List of available channels
  - Online users with status indicators
  - Logout button

- **Main Chat Area**:
  - Channel header with description
  - Message display area with sender information
  - Message timestamps
  - Input field for new messages
  - Send button with keyboard shortcut (Enter)

## 🎨 Color Scheme

| Element | Color | Hex |
|---------|-------|-----|
| Primary Accent | Blue | #667eea |
| Secondary Accent | Purple | #764ba2 |
| Background | Dark Blue-Gray | #2c3e50 |
| Text Primary | White | #ffffff |
| Text Secondary | Light Gray | #e0e0e0 |
| Borders | Light Gray | #e0e0e0 |

## 🔄 Message Flow

1. User logs in with credentials
2. Authentication Service validates credentials
3. User redirected to main chat interface
4. Select a channel to view conversation
5. Type message and press Enter or click Send
6. Message added to Message Service
7. Message displayed in chat area with timestamp

## 📦 Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/jlivechats/
│   │       ├── JavaFXApplication.java       # Main entry point
│   │       ├── ui/
│   │       │   ├── LoginView.java           # Login screen
│   │       │   ├── RegisterView.java        # Registration screen
│   │       │   ├── ChatView.java            # Main chat interface
│   │       │   └── SceneManager.java        # Scene navigation
│   │       ├── service/
│   │       │   ├── AuthenticationService.java  # User authentication
│   │       │   └── MessageService.java        # Message management
│   │       └── config/
│   │           └── (Spring configuration files)
│   └── resources/
│       ├── styles/
│       │   └── application.css           # JavaFX styles
│       └── static/
│           └── (Web resources, optional)
```

## 🎯 Usage

### Login
1. Start the application
2. Enter username and password
3. Click "Sign In" or press Enter
4. Invalid credentials will show error message

### Register New Account
1. Click "Sign Up" link on login screen
2. Fill in all fields (username, email, password)
3. Confirm password matches
4. Click "Create Account"

### Send Messages
1. Select a channel from left sidebar
2. Type message in input field at bottom
3. Press Enter or click Send button
4. Message appears in chat with timestamp

### Logout
1. Click "Logout" button in top-left sidebar
2. Redirected back to login screen

## 🔒 Security Features

- **Password Validation**: Passwords must be minimum 6 characters
- **Username Validation**: Usernames must be minimum 3 characters
- **Email Validation**: Email format validation on registration
- **Session Management**: Current user tracking with AuthenticationService
- **Data Privacy**: In-memory storage (no external database exposure)

## ⚙️ Configuration

### Font and Styling
Edit `src/main/resources/styles/application.css` to customize:
- Font families
- Colors and gradients
- Button styles
- Text field styling

### Window Size
Modify in `JavaFXApplication.java`:
```java
stage.setWidth(1200);
stage.setHeight(700);
stage.setMinWidth(800);
stage.setMinHeight(600);
```

### Default Channels
Edit in `ChatView.java`:
```java
ObservableList<String> channels = FXCollections.observableArrayList(
    "#general", "#random", "#announcements", "#help"
);
```

## 🐛 Troubleshooting

### Application won't start
- Ensure Java 17+ is installed: `java -version`
- Check Maven is installed: `mvn -version`
- Try: `mvn clean install`

### JavaFX not loading
- Verify JavaFX dependency in pom.xml
- Run: `mvn dependency:resolve`

### UI appears blank
- Check screen resolution is at least 800x600
- Verify CSS file is in resources/styles/

## 📈 Future Enhancements

- [ ] WebSocket integration for real-time messaging
- [ ] Database persistence (SQLite/PostgreSQL)
- [ ] User avatar uploads
- [ ] Message search functionality
- [ ] Chat notifications
- [ ] File sharing capability
- [ ] Emoji picker
- [ ] Dark/Light theme toggle
- [ ] User preferences settings
- [ ] Group chat support

## 📄 License

This project is open-source and available for educational purposes.

## 👨‍💻 Author

Created by: **Bhola Yadav**  
USN: 1CR23CS044  
Section: K  
Branch: Computer Science Engineering

---

**Enjoy your new JLiveChats application!** 🎉

