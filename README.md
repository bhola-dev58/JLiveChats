# JLiveChats - Spring Boot & Hibernate Upgrade

Upgrade of the simple socket-based chat application to a modern **Spring Boot 3** application with **Hibernate (JPA)** and **WebSockets**.

## ✨ Features
- **Real-time Messaging**: Powered by Spring Boot WebSockets and STOMP.
- **Persistence**: Chat history is automatically saved to an **H2 In-memory database** using **Hibernate**.
- **Modern Web UI**: Premium Glassmorphism-style interface for a superior user experience.
- **REST API**: Endpoint available at `/api/history` to retrieve recent messages.
- **H2 Console**: Accessible at `/h2-console` for database inspection.

## 🛠 Tech Stack
- **Backend**: Spring Boot 3, Spring Data JPA, Hibernate, WebSockets (STOMP).
- **Frontend**: HTML5, Vanilla CSS (Glassmorphism), JavaScript (SockJS, Stomp.js).
- **Database**: H2 (In-memory).

## 🚀 Getting Started

### Prerequisites
- **Java 17** or higher.
- **Maven** installed.

### Run the Application
1.  Open your terminal in the project directory.
2.  Run the following command:
    ```bash
    mvn spring-boot:run
    ```
3.  Open your browser and navigate to:
    [http://localhost:9090](http://localhost:9090)

### Database Access
You can view the saved chat messages at the H2 console:
- **URL**: [http://localhost:9090/h2-console](http://localhost:9090/h2-console)
- **JDBC URL**: `jdbc:h2:mem:chatdb`
- **User**: `sa`
- **Password**: (leave empty)

## 📁 Project Structure
- `src/main/java/com/jlivechats/`: Backend source code.
- `src/main/resources/static/`: Modern web frontend.
- `legacy/`: Original Swing/Socket source code (preserved for reference).

---
*Upgraded with ❤️ by Antigravity*
