'use strict';

const usernamePage = document.querySelector('#username-page');
const chatPage = document.querySelector('#chat-page');
const loginForm = document.querySelector('#loginForm');
const messageForm = document.querySelector('#messageForm');
const messageArea = document.querySelector('#messageArea');
const messageInput = document.querySelector('#message');
const loginInput = document.querySelector('#name');
const currentUsernameDisplay = document.querySelector('#currentUsername');
const currentUserAvatar = document.querySelector('#currentUserAvatar');

let stompClient = null;
let username = null;

const colors = [
    '#5865f2', '#3ba55d', '#faa81a', '#ed4245',
    '#a370f7', '#eb459e', '#556270', '#4ecdc4'
];

function stringToColor(str) {
    let hash = 0;
    for (let i = 0; i < str.length; i++) {
        hash = str.charCodeAt(i) + ((hash << 5) - hash);
    }
    return colors[Math.abs(hash) % colors.length];
}

function formatTime(date) {
    const d = new Date(date);
    return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

function connect(event) {
    username = loginInput.value.trim();

    if (username) {
        usernamePage.classList.add('hidden');
        chatPage.classList.remove('hidden');

        currentUsernameDisplay.textContent = username;
        currentUserAvatar.textContent = username.charAt(0).toUpperCase();
        currentUserAvatar.style.backgroundColor = stringToColor(username);

        const socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        stompClient.debug = null; // Disable noisy logs

        stompClient.connect({}, onConnected, onError);
    }
    event.preventDefault();
}

function onConnected() {
    stompClient.subscribe('/topic/messages', onMessageReceived);

    stompClient.send("/app/user/online",
        {},
        JSON.stringify({ username: username, status: 'online', channel: 'general' })
    );

    // Initial history fetch from ChatApiController
    fetch('/api/history?channel=general')
        .then(response => response.json())
        .then(messages => {
            messages.forEach(msg => {
                msg.messageType = msg.messageType || 'chat';
                displayMessage(msg);
            });
        })
        .catch(err => console.error('Error loading history:', err));
}

function onError(error) {
    console.error('WebSocket Error:', error);
}

function sendMessage(event) {
    const messageContent = messageInput.value.trim();
    if (messageContent && stompClient) {
        const chatMessage = {
            sender: username,
            content: messageInput.value,
            channel: 'general',
            messageType: 'chat',
            timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
        };
        stompClient.send("/app/sendChannelMessage", {}, JSON.stringify(chatMessage));
        messageInput.value = '';
    }
    event.preventDefault();
}

function onMessageReceived(payload) {
    const message = JSON.parse(payload.body);
    displayMessage(message);
}


function displayMessage(message) {
    if (message.messageType === 'chat') {
        const msgDiv = document.createElement('div');
        // Determine left/right alignment
        const isMe = message.sender === username;
        msgDiv.className = 'message ' + (isMe ? 'right' : 'left');

        // Avatar
        const avatar = document.createElement('div');
        avatar.className = 'avatar';
        avatar.textContent = message.sender.charAt(0).toUpperCase();
        avatar.style.backgroundColor = stringToColor(message.sender);

        // Content
        const content = document.createElement('div');
        content.className = 'message-content';

        // Header (username + time)
        const header = document.createElement('div');
        header.className = 'message-header';
        const usernameSpan = document.createElement('span');
        usernameSpan.className = 'message-username';
        usernameSpan.textContent = message.sender;
        const timeSpan = document.createElement('span');
        timeSpan.className = 'message-time';
        timeSpan.textContent = formatTime(message.timestamp);
        header.appendChild(usernameSpan);
        header.appendChild(timeSpan);

        // Body
        const body = document.createElement('div');
        body.textContent = message.content;

        content.appendChild(header);
        content.appendChild(body);

        msgDiv.appendChild(avatar);
        msgDiv.appendChild(content);

        messageArea.appendChild(msgDiv);
    } else {
        const status = document.createElement('div');
        status.className = 'status-msg';
        status.textContent = message.type === 'JOIN' ? `${message.sender} joined the chat` : `${message.sender} left the chat`;
        messageArea.appendChild(status);
    }
    messageArea.scrollTop = messageArea.scrollHeight;
}

loginForm.addEventListener('submit', connect, true);
messageForm.addEventListener('submit', sendMessage, true);

// Add logout functionality
document.getElementById('logout-btn').addEventListener('click', () => {
    window.location.reload();
});
