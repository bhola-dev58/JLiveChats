// JLiveChats Enhanced Web Application - Real-time Chat with New Features
// Features: User Presence, Typing Indicators, Message Reactions

let stompClient = null;
let currentUser = null;
let currentChannel = 'general';
let onlineUsers = [];
let typingUsers = {};
let messageIdMap = {}; // Map to store message IDs for reactions

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

    // Handle page visibility for presence
    document.addEventListener('visibilitychange', function() {
        if (document.hidden) {
            broadcastUserOffline();
        } else {
            broadcastUserOnline();
        }
    });

    // Handle window close
    window.addEventListener('beforeunload', function() {
        if (stompClient && stompClient.connected) {
            broadcastUserOffline();
        }
    });
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

    // Typing indicator
    messageInput.addEventListener('input', function() {
        if (stompClient && stompClient.connected) {
            sendTypingIndicator(true);
        }
    });

    messageInput.addEventListener('blur', function() {
        if (stompClient && stompClient.connected) {
            sendTypingIndicator(false);
        }
    });

    // Channel switching
    channelItems.forEach(item => {
        item.addEventListener('click', function () {
            // Stop typing when switching channels
            sendTypingIndicator(false);
            
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

    const messageId = generateMessageId();

    // Create message object
    const message = {
        id: messageId,
        sender: currentUser,
        content: content,
        channel: currentChannel,
        messageType: 'chat',
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' })
    };

    // Send via WebSocket if connected, otherwise use HTTP
    if (stompClient && stompClient.connected) {
        console.log('Sending message via WebSocket...');
        stompClient.send("/app/sendChannelMessage", {}, JSON.stringify(message));
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
            msg.id = messageId;
            msg.isCurrentUser = true;
            displayMessage(msg);
            messageInput.value = '';
            messageInput.focus();

            const messagesContainer = document.getElementById('messagesContainer');
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
        })
        .catch(error => {
            console.error('Error sending message:', error);
        });
    }
    
    // Clear input and stop typing indicator
    messageInput.value = '';
    messageInput.focus();
    sendTypingIndicator(false);
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
    messageDiv.id = `msg-${msg.id || Date.now()}`;
    if (msg.id) {
        messageIdMap[messageDiv.id] = msg.id;
    }

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

    const reactionsDiv = document.createElement('div');
    reactionsDiv.className = 'message-reactions';
    reactionsDiv.innerHTML = '<span class="reaction-btn" onclick="addReaction(this)">😊</span>';

    contentDiv.appendChild(headerDiv);
    contentDiv.appendChild(textDiv);
    contentDiv.appendChild(timeDiv);
    contentDiv.appendChild(reactionsDiv);

    messageDiv.appendChild(avatarDiv);
    messageDiv.appendChild(contentDiv);

    messagesContainer.appendChild(messageDiv);
}

function displayTypingIndicator() {
    const typingList = Object.keys(typingUsers).filter(u => u !== currentUser);
    const typingIndicatorEl = document.getElementById('typingIndicator');
    
    if (typingList.length > 0) {
        const typingText = typingList.length === 1 
            ? `${typingList[0]} is typing...`
            : `${typingList.join(', ')} are typing...`;
        if (typingIndicatorEl) {
            typingIndicatorEl.innerHTML = `<em>${typingText}</em>`;
            typingIndicatorEl.style.display = 'block';
        }
    } else {
        if (typingIndicatorEl) {
            typingIndicatorEl.style.display = 'none';
        }
    }
}

function displayUserPresence() {
    // Update online count
    const onlineCountEl = document.getElementById('onlineCount');
    if (onlineCountEl) {
        onlineCountEl.textContent = onlineUsers.length;
    }

    // Update user list if it exists
    const userListEl = document.getElementById('userList');
    if (userListEl) {
        userListEl.innerHTML = '';
        onlineUsers.forEach(user => {
            const userItemDiv = document.createElement('div');
            userItemDiv.className = 'user-item';
            userItemDiv.innerHTML = `<span class="user-status online"></span> ${user}`;
            userListEl.appendChild(userItemDiv);
        });
    }
}

function sendTypingIndicator(isTyping) {
    if (!stompClient || !stompClient.connected) return;
    
    const typingEvent = {
        username: currentUser,
        channel: currentChannel,
        isTyping: isTyping
    };
    
    stompClient.send("/app/user/typing", {}, JSON.stringify(typingEvent));
}

function broadcastUserOnline() {
    if (!stompClient || !stompClient.connected) return;
    
    const presenceEvent = {
        username: currentUser,
        status: 'online',
        channel: currentChannel,
        onlineCount: onlineUsers.length
    };
    
    stompClient.send("/app/user/online", {}, JSON.stringify(presenceEvent));
}

function broadcastUserOffline() {
    if (!stompClient || !stompClient.connected) return;
    
    const presenceEvent = {
        username: currentUser,
        status: 'offline',
        channel: currentChannel,
        onlineCount: onlineUsers.length
    };
    
    stompClient.send("/app/user/offline", {}, JSON.stringify(presenceEvent));
}

function addReaction(element) {
    const emoji = prompt('Add emoji reaction (e.g., 👍, ❤️, 😂, etc.):', '👍');
    if (emoji) {
        const messageDiv = element.closest('.message');
        if (messageDiv) {
            const reactionEl = document.createElement('span');
            reactionEl.className = 'reaction-badge';
            reactionEl.textContent = emoji;
            element.parentNode.insertBefore(reactionEl, element);
        }
    }
}

function connectWebSocket() {
    // Create WebSocket connection
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function(frame) {
        console.log('Connected: ' + frame.command);

        // Broadcast user online
        broadcastUserOnline();

        // Subscribe to messages topic
        stompClient.subscribe('/topic/messages', function(messageOutput) {
            const msg = JSON.parse(messageOutput.body);
            msg.isCurrentUser = (msg.sender === currentUser);
            displayMessage(msg);
            
            const messagesContainer = document.getElementById('messagesContainer');
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
        });

        // Subscribe to channel-specific messages
        stompClient.subscribe(`/topic/channel/${currentChannel}`, function(messageOutput) {
            const msg = JSON.parse(messageOutput.body);
            msg.isCurrentUser = (msg.sender === currentUser);
            displayMessage(msg);
            
            const messagesContainer = document.getElementById('messagesContainer');
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
        });

        // Subscribe to presence updates
        stompClient.subscribe('/topic/presence', function(output) {
            const event = JSON.parse(output.body);
            if (event.status === 'online') {
                if (!onlineUsers.includes(event.username)) {
                    onlineUsers.push(event.username);
                }
            } else if (event.status === 'offline') {
                onlineUsers = onlineUsers.filter(u => u !== event.username);
            }
            displayUserPresence();
        });

        // Subscribe to typing indicators
        stompClient.subscribe('/topic/typing', function(output) {
            const event = JSON.parse(output.body);
            if (event.isTyping) {
                typingUsers[event.username] = true;
            } else {
                delete typingUsers[event.username];
            }
            displayTypingIndicator();
        });

        // Subscribe to message reactions
        stompClient.subscribe('/topic/reactions', function(output) {
            const event = JSON.parse(output.body);
            const messageDiv = document.getElementById(`msg-${event.messageId}`);
            if (messageDiv && event.action === 'add') {
                const reactionsDiv = messageDiv.querySelector('.message-reactions');
                if (reactionsDiv) {
                    const existingReaction = reactionsDiv.querySelector(`.reaction-badge[data-emoji="${event.emoji}"]`);
                    if (existingReaction) {
                        existingReaction.textContent = event.emoji + ' ✓';
                    } else {
                        const reactionEl = document.createElement('span');
                        reactionEl.className = 'reaction-badge';
                        reactionEl.setAttribute('data-emoji', event.emoji);
                        reactionEl.textContent = event.emoji;
                        reactionsDiv.insertBefore(reactionEl, reactionsDiv.firstChild);
                    }
                }
            }
        });

        console.log('WebSocket fully subscribed with all features enabled');
    }, function(error) {
        console.error('WebSocket connection error:', error);
        console.log('Falling back to polling mode...');
    });
}

function generateMessageId() {
    return 'msg-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9);
}

// Utility functions
function formatTime(timestamp) {
    if (!timestamp) return '';
    const date = new Date(timestamp);
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
