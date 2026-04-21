// JLiveChats Web Application - Chat JavaScript

let stompClient = null;
let currentUser = null;
let currentChannel = 'general';

document.addEventListener('DOMContentLoaded', function () {
    initializeChat();
});

function initializeChat() {
    // Get username from page
    const userElement = document.querySelector('.user-name');
    if (userElement) {
        currentUser = userElement.textContent.trim();
    }

    // Load initial messages
    loadMessages();

    // Setup event listeners
    setupEventListeners();

    // Connect to WebSocket
    connectWebSocket();
}

function setupEventListeners() {
    const messageInput = document.getElementById('messageInput');
    const sendBtn = document.getElementById('sendBtn');
    const channelItems = document.querySelectorAll('.channel-item');

    // Send message on button click
    sendBtn.addEventListener('click', sendMessage);

    // Send message on Enter key
    messageInput.addEventListener('keypress', function (e) {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    });

    // Channel switching
    channelItems.forEach(item => {
        item.addEventListener('click', function () {
            // Remove active class from all items
            channelItems.forEach(i => i.classList.remove('active'));
            // Add active class to clicked item
            this.classList.add('active');
            // Update current channel
            currentChannel = this.dataset.channel;
            // Update header
            document.querySelector('.channel-name').textContent = '#' + currentChannel;
            document.querySelector('.channel-description').textContent = 
                `Welcome to the ${currentChannel} channel`;
            // Load messages for this channel
            loadMessages();
        });
    });
}

function loadMessages() {
    fetch('/api/messages')
        .then(response => response.json())
        .then(messages => {
            const messagesContainer = document.getElementById('messagesContainer');
            messagesContainer.innerHTML = '';

            if (messages.length === 0) {
                messagesContainer.innerHTML = '<div class="loading-message">No messages yet. Start the conversation!</div>';
                return;
            }

            messages.forEach(msg => {
                displayMessage(msg);
            });

            // Auto-scroll to bottom
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
        })
        .catch(error => {
            console.error('Error loading messages:', error);
        });
}

function sendMessage() {
    const messageInput = document.getElementById('messageInput');
    const content = messageInput.value.trim();

    if (!content) {
        return;
    }

    // Create message object
    const message = {
        sender: currentUser,
        content: content,
        channel: currentChannel,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' })
    };

    // Send via WebSocket if connected, otherwise use HTTP
    if (stompClient && stompClient.connected) {
        console.log('Sending message via WebSocket...');
        stompClient.send("/app/sendMessage", {}, JSON.stringify(message));
    } else {
        console.log('WebSocket not connected, sending via HTTP...');
        // Send message to server via HTTP POST
        fetch('/api/messages', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `sender=${encodeURIComponent(currentUser)}&content=${encodeURIComponent(content)}`
        })
        .then(response => response.json())
        .then(msg => {
            // Display message immediately
            msg.isCurrentUser = true;
            displayMessage(msg);
            
            // Clear input
            messageInput.value = '';
            messageInput.focus();

            // Auto-scroll to bottom
            const messagesContainer = document.getElementById('messagesContainer');
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
        })
        .catch(error => {
            console.error('Error sending message:', error);
        });
    }
    
    // Clear input
    messageInput.value = '';
    messageInput.focus();
}

function displayMessage(msg) {
    const messagesContainer = document.getElementById('messagesContainer');
    
    // Remove loading message if present
    const loadingMsg = messagesContainer.querySelector('.loading-message');
    if (loadingMsg) {
        loadingMsg.remove();
    }

    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${msg.isCurrentUser ? 'own' : ''}`;

    const avatarDiv = document.createElement('div');
    avatarDiv.className = 'message-avatar';
    avatarDiv.textContent = msg.sender.charAt(0).toUpperCase();

    const contentDiv = document.createElement('div');
    contentDiv.className = 'message-content';

    const headerDiv = document.createElement('div');
    headerDiv.className = 'message-header';
    headerDiv.textContent = msg.sender;

    const textDiv = document.createElement('div');
    textDiv.className = 'message-text';
    textDiv.textContent = msg.content;

    const timeDiv = document.createElement('div');
    timeDiv.className = 'message-time';
    timeDiv.textContent = msg.timestamp || new Date().toLocaleTimeString();

    contentDiv.appendChild(headerDiv);
    contentDiv.appendChild(textDiv);
    contentDiv.appendChild(timeDiv);

    // Always add both avatar and content - CSS handles the layout
    messageDiv.appendChild(avatarDiv);
    messageDiv.appendChild(contentDiv);

    messagesContainer.appendChild(messageDiv);
}

function connectWebSocket() {
    // Create WebSocket connection
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function(frame) {
        console.log('Connected: ' + frame.command);

        // Subscribe to messages topic
        stompClient.subscribe('/topic/messages', function(messageOutput) {
            const msg = JSON.parse(messageOutput.body);
            // Set isCurrentUser based on sender
            msg.isCurrentUser = (msg.sender === currentUser);
            displayMessage(msg);
            
            // Auto-scroll to bottom
            const messagesContainer = document.getElementById('messagesContainer');
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
        });

        // Subscribe to channel-specific messages
        stompClient.subscribe(`/topic/channel/${currentChannel}`, function(messageOutput) {
            const msg = JSON.parse(messageOutput.body);
            // Set isCurrentUser based on sender
            msg.isCurrentUser = (msg.sender === currentUser);
            displayMessage(msg);
            
            // Auto-scroll to bottom
            const messagesContainer = document.getElementById('messagesContainer');
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
        });

        console.log('WebSocket subscribed to /topic/messages and /topic/channel/general');
    }, function(error) {
        console.error('WebSocket connection error:', error);
        // If WebSocket fails, fall back to polling
        console.log('Falling back to polling mode...');
    });
}

// Utility function to format timestamp
function formatTime(timestamp) {
    if (!timestamp) return '';
    const date = new Date(timestamp);
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

// Utility function to escape HTML
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
