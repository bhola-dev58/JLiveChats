# QUICKSTART GUIDE - JLiveChats JavaFX Application

## 🚀 Quick Start (5 minutes)

### Step 1: Build the Application
```bash
cd "/home/bhola-dev58/colledge project/JLiveChats"
mvn clean package
```

### Step 2: Run the Application
```bash
java -jar target/jlivechats-1.0.0.jar
```

### Step 3: Login
Use these test credentials:
- **Username**: bhola
- **Password**: password123

## 🎨 What's New?

### Beautiful JavaFX UI
✅ Modern gradient-based interface  
✅ Professional purple/blue color scheme  
✅ Responsive design  
✅ Smooth animations and transitions  

### Authentication System
✅ Secure login/registration  
✅ User session management  
✅ Email validation on signup  
✅ Password strength requirements  

### Chat Features
✅ Real-time messaging interface  
✅ Channel management (#general, #random, etc.)  
✅ Online user presence  
✅ Message timestamps  
✅ Message history (50 messages)  

### Professional Design
✅ Dark sidebar with user list  
✅ Clean message bubbles  
✅ Color-coded message sender  
✅ Keyboard shortcut (Enter to send)  

## 📊 Project Architecture

```
JLiveChats/
├── UI Layer (JavaFX)
│   ├── LoginView.java          → Beautiful login screen
│   ├── RegisterView.java       → Registration screen with validation
│   ├── ChatView.java           → Main chat interface
│   └── SceneManager.java       → Navigation between screens
│
├── Service Layer
│   ├── AuthenticationService   → User login/register/logout
│   └── MessageService          → Message management
│
├── Configuration
│   └── JavaFXApplication.java  → Application entry point
│
└── Resources
    └── styles/application.css  → JavaFX styling
```

## 🎯 Key Features Implemented

### 1. Login Screen
- Gradient background (#667eea → #764ba2)
- Professional input fields with focus highlighting
- Error handling for invalid credentials
- Register link

### 2. Registration Screen
- Email validation
- Password confirmation matching
- Input field validation
- Duplicate username prevention

### 3. Chat Interface
- **Left Sidebar**:
  - User profile display
  - Channel list (#general, #random, #announcements, #help)
  - Online users indicator
  - Logout button

- **Main Area**:
  - Channel header
  - Message display area
  - Real-time message input
  - Send button

### 4. Message System
- Add messages with timestamp
- Display sender name
- Message history storage
- Current user message identification

## 🔐 Authentication Details

Test Users:
| Username | Password |
|----------|----------|
| bhola | password123 |
| john | john123 |
| jane | jane123 |

Create New Account:
1. Click "Sign Up" on login screen
2. Fill in all fields
3. Click "Create Account"

## 💻 System Requirements

- Java 17 or later
- Maven 3.8+
- 800x600 minimum screen resolution
- 100MB free disk space

## 🎨 Color Palette

- **Primary**: #667eea (Blue)
- **Secondary**: #764ba2 (Purple)
- **Dark Background**: #2c3e50
- **Light Text**: #ecf0f1
- **Accent Hover**: #5568d3

## ⌨️ Keyboard Shortcuts

| Key | Action |
|-----|--------|
| Enter | Send message |
| Tab | Switch input fields |
| Escape | Clear input (when implemented) |

## 📝 File Locations

| File | Purpose |
|------|---------|
| JavaFXApplication.java | Application entry point |
| LoginView.java | Login UI |
| RegisterView.java | Registration UI |
| ChatView.java | Main chat UI |
| SceneManager.java | View management |
| AuthenticationService.java | User authentication |
| MessageService.java | Chat messages |
| application.css | JavaFX styles |

## 🔧 Configuration Options

### Change Window Size
Edit `JavaFXApplication.java`:
```java
stage.setWidth(1200);    // Change width
stage.setHeight(700);    // Change height
```

### Add New Channels
Edit `ChatView.java`:
```java
ObservableList<String> channels = FXCollections.observableArrayList(
    "#general", "#random", "#announcements", "#help"  // Add more here
);
```

### Modify Colors
Edit `styles/application.css`:
```css
/* Change primary color */
-fx-background-color: linear-gradient(to right, #667eea, #764ba2);
```

## 🐛 Common Issues

### Application won't start
→ Check Java version: `java -version` (should be 17+)

### Port already in use
→ Application uses JavaFX GUI, not web port

### UI looks tiny/huge
→ Check monitor resolution, try resizing window

## 📞 Support

For issues or questions:
1. Check README.md for detailed documentation
2. Verify all prerequisites are installed
3. Try clean rebuild: `mvn clean package`

## 🎉 Success Indicators

When running correctly, you should see:
- ✅ Login window appears
- ✅ Can enter username/password
- ✅ Login successful → Chat screen appears
- ✅ Can see channels and users
- ✅ Can type and send messages
- ✅ Messages appear in chat area
- ✅ Logout button works

---

**Happy Chatting!** 🚀
