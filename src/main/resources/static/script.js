'use strict';

var usernamePage = document.querySelector('#username-page');
var chatPage = document.querySelector('#chat-page');
var messageArea = document.querySelector('#messageArea');
var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#message');
var joinBtn = document.querySelector('#join-btn');
var loginInput = document.querySelector('#name');

var stompClient = null;
var username = null;

var colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];

function connect(event) {
    username = loginInput.value.trim();

    if (username) {
        usernamePage.classList.add('hidden');
        chatPage.classList.remove('hidden');

        var socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);

        stompClient.connect({}, onConnected, onError);
    }
    event.preventDefault();
}

function onConnected() {
    // Subscribe to the Public Topic
    stompClient.subscribe('/topic/public', onMessageReceived);

    // Tell your username to the server
    stompClient.send("/app/chat.addUser",
        {},
        JSON.stringify({ sender: username, type: 'JOIN' })
    );

    // Load history
    fetch('/api/history')
        .then(response => response.json())
        .then(messages => {
            messages.forEach(msg => {
                displayMessage(msg);
            });
        });
}

function onError(error) {
    alert('Could not connect to WebSocket server. Please refresh this page to try again!');
}

function sendMessage(event) {
    var messageContent = messageInput.value.trim();
    if (messageContent && stompClient) {
        var chatMessage = {
            sender: username,
            content: messageInput.value,
            type: 'CHAT'
        };
        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
        messageInput.value = '';
    }
    event.preventDefault();
}

function onMessageReceived(payload) {
    var message = JSON.parse(payload.body);
    displayMessage(message);
}

function displayMessage(message) {
    var messageElement = document.createElement('li');

    if (message.type === 'JOIN') {
        messageElement.classList.add('status-message');
        messageElement.textContent = message.sender + ' joined the chat';
    } else if (message.type === 'LEAVE') {
        messageElement.classList.add('status-message');
        messageElement.textContent = message.sender + ' left the chat';
    } else {
        messageElement.classList.add('chat-message');
        messageElement.classList.add(message.sender === username ? 'me' : 'others');

        var usernameElement = document.createElement('span');
        usernameElement.classList.add('username');
        usernameElement.textContent = message.sender;
        messageElement.appendChild(usernameElement);

        var textElement = document.createElement('p');
        textElement.textContent = message.content;
        messageElement.appendChild(textElement);
    }

    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;
}

joinBtn.addEventListener('click', connect, true);
messageForm.addEventListener('submit', sendMessage, true);
