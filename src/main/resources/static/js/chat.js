// JLiveChats Enhanced Web Application - Real-time Chat with New Features
// Features: User Presence, Typing Indicators, Message Reactions

let stompClient = null;
let currentUser = null;
let currentChannel = 'general';
let onlineUsers = [];
let typingUsers = {};
let messageIdMap = {}; // Map to store message IDs for reactions
let userAvatarCache = {}; // Cache for avatar colors

// Avatar color palette - same as backend
const AVATAR_COLORS = [
    "#ff0000ff", "#00ffeeff", "#00caf8ff", "#f94700ff", "#00ffbfff",
    "#ffcc00ff", "#b300ffff", "#00a6ffff", "#ff6a00ff", "#00ff6aff",
    "#ff1500ff", "#b300ffff"
];

document.addEventListener('DOMContentLoaded', function () {
    initializeChat();
});

function initializeChat() {
    // Get username from hidden input (set by Thymeleaf) or fallback to .user-name
    const hiddenInput = document.getElementById('loggedInUsername');
    if (hiddenInput && hiddenInput.value) {
        currentUser = hiddenInput.value.trim();
    } else {
        const userElement = document.querySelector('.user-name');
        if (userElement) {
            currentUser = userElement.textContent.trim();
        }
    }

    // Load initial messages
    loadMessages();

    // Setup event listeners
    setupEventListeners();

    // Connect to WebSocket
    connectWebSocket();

    // Handle page visibility for presence
    document.addEventListener('visibilitychange', function () {
        if (document.hidden) {
            broadcastUserOffline();
        } else {
            broadcastUserOnline();
        }
    });

    // Handle window close
    window.addEventListener('beforeunload', function () {
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
    messageInput.addEventListener('input', function () {
        if (stompClient && stompClient.connected) {
            sendTypingIndicator(true);
        }
    });

    messageInput.addEventListener('blur', function () {
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

    // Setup mention autocomplete
    setupMentionAutocomplete();
}

function loadMessages() {
    fetch(`/api/history?channel=${encodeURIComponent(currentChannel)}`)
        .then(response => response.json())
        .then(messages => {
            const messagesContainer = document.getElementById('messagesContainer');
            messagesContainer.innerHTML = '';

            if (messages.length === 0) {
                messagesContainer.innerHTML = '<div class="loading-message">No messages yet. Start the conversation!</div>';
                return;
            }

            messages.forEach(msg => {
                msg.isCurrentUser = (msg.sender === currentUser);
                displayMessage(msg);
            });

            // Auto-scroll to bottom
            messagesContainer.scrollTop = messagesContainer.scrollHeight;

            // Update pin count
            updatePinCountBadge(currentChannel);
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
    avatarDiv.textContent = getInitials(msg.sender);
    avatarDiv.style.backgroundColor = getAvatarColor(msg.sender);
    avatarDiv.title = msg.sender; // Show full name on hover

    const contentDiv = document.createElement('div');
    contentDiv.className = 'message-content';

    const headerDiv = document.createElement('div');
    headerDiv.className = 'message-header';
    headerDiv.textContent = msg.sender;

    const textDiv = document.createElement('div');
    textDiv.className = 'message-text';
    textDiv.style.position = 'relative'; // Ensure bubble is the anchor for reactions
    textDiv.innerHTML = highlightMentions(msg.content);

    const timeDiv = document.createElement('div');
    timeDiv.className = 'message-time';
    timeDiv.textContent = msg.timestamp || new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

    const reactionsDiv = document.createElement('div');
    reactionsDiv.className = 'message-reactions';

    const reactionBtn = document.createElement('span');
    reactionBtn.className = 'reaction-btn';
    reactionBtn.textContent = '😊';
    reactionBtn.addEventListener('click', function() { addReaction(this); });

    const pinBtn = document.createElement('span');
    pinBtn.className = 'pin-btn';
    pinBtn.title = 'Pin message';
    pinBtn.textContent = '📌';
    pinBtn.dataset.messageId = msg.id || '';
    pinBtn.dataset.sender = msg.sender;
    pinBtn.dataset.content = msg.content;
    pinBtn.dataset.timestamp = msg.timestamp || '';
    pinBtn.dataset.channel = msg.channel || '';
    pinBtn.addEventListener('click', function() {
        pinMessage(
            this.dataset.messageId,
            this.dataset.sender,
            this.dataset.content,
            this.dataset.timestamp,
            this.dataset.channel
        );
    });

    const threadBtn = document.createElement('span');
    threadBtn.className = 'thread-btn';
    threadBtn.title = 'Reply in thread';
    threadBtn.textContent = '💬';
    threadBtn.dataset.messageId = msg.id || '';
    threadBtn.dataset.sender = msg.sender;
    threadBtn.dataset.content = msg.content;
    threadBtn.addEventListener('click', function() {
        openThread(this.dataset.messageId, this.dataset.sender, this.dataset.content);
    });

    reactionsDiv.appendChild(reactionBtn);
    reactionsDiv.appendChild(pinBtn);
    reactionsDiv.appendChild(threadBtn);

    textDiv.appendChild(reactionsDiv);

    contentDiv.appendChild(headerDiv);
    contentDiv.appendChild(textDiv);
    contentDiv.appendChild(timeDiv);

    // Add edit/delete buttons for own messages
    if (msg.isCurrentUser) {
        const actionsDiv = document.createElement('div');
        actionsDiv.className = 'message-actions';

        const editBtn = document.createElement('button');
        editBtn.className = 'action-btn edit-btn';
        editBtn.title = 'Edit message';
        editBtn.textContent = '✏️';
        editBtn.dataset.messageId = msg.id || messageDiv.id;
        editBtn.dataset.content = msg.content;
        editBtn.addEventListener('click', function() {
            editMessage(this.dataset.messageId, this.dataset.content);
        });

        const deleteBtn = document.createElement('button');
        deleteBtn.className = 'action-btn delete-btn';
        deleteBtn.title = 'Delete message';
        deleteBtn.textContent = '🗑️';
        deleteBtn.dataset.messageId = msg.id || messageDiv.id;
        deleteBtn.addEventListener('click', function() {
            deleteMessage(this.dataset.messageId);
        });

        actionsDiv.appendChild(editBtn);
        actionsDiv.appendChild(deleteBtn);
        contentDiv.appendChild(actionsDiv);
    }

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

    // Update the online users sidebar list (chat.html uses #onlineUsersList)
    const onlineListEl = document.getElementById('onlineUsersList');
    if (onlineListEl) {
        onlineListEl.innerHTML = '';
        onlineUsers.forEach(user => {
            const li = document.createElement('li');
            li.className = 'user-item';

            const indicator = document.createElement('span');
            indicator.className = 'online-indicator';

            const nameSpan = document.createElement('span');
            nameSpan.textContent = user;

            li.appendChild(indicator);
            li.appendChild(nameSpan);
            onlineListEl.appendChild(li);
        });
    }

    // Also update user list if it exists (index.html uses #userList)
    const userListEl = document.getElementById('userList');
    if (userListEl) {
        userListEl.innerHTML = '';
        onlineUsers.forEach(user => {
            const userItemDiv = document.createElement('div');
            userItemDiv.className = 'user-item';

            const avatarSpan = document.createElement('span');
            avatarSpan.className = 'user-avatar';
            avatarSpan.textContent = getInitials(user);
            avatarSpan.style.backgroundColor = getAvatarColor(user);
            avatarSpan.title = user;

            const statusSpan = document.createElement('span');
            statusSpan.className = 'user-status online';

            const nameSpan = document.createElement('span');
            nameSpan.textContent = user;

            userItemDiv.appendChild(avatarSpan);
            userItemDiv.appendChild(statusSpan);
            userItemDiv.appendChild(nameSpan);
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
    stompClient.debug = null; // Disable noisy logs

    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame.command);

        // Broadcast user online
        broadcastUserOnline();

        // Load initial online users from REST API
        fetch('/api/users/online')
            .then(r => r.json())
            .then(data => {
                if (data.users) {
                    data.users.forEach(u => {
                        if (!onlineUsers.includes(u)) onlineUsers.push(u);
                    });
                    displayUserPresence();
                }
            })
            .catch(err => console.error('Error loading online users:', err));

        // Subscribe to messages topic
        stompClient.subscribe('/topic/messages', function (messageOutput) {
            const msg = JSON.parse(messageOutput.body);
            msg.isCurrentUser = (msg.sender === currentUser);
            displayMessage(msg);

            const messagesContainer = document.getElementById('messagesContainer');
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
        });

        // Subscribe to presence updates
        stompClient.subscribe('/topic/presence', function (output) {
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
        stompClient.subscribe('/topic/typing', function (output) {
            const event = JSON.parse(output.body);
            if (event.isTyping) {
                typingUsers[event.username] = true;
            } else {
                delete typingUsers[event.username];
            }
            displayTypingIndicator();
        });

        // Subscribe to message reactions
        stompClient.subscribe('/topic/reactions', function (output) {
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
    }, function (error) {
        console.error('WebSocket connection error:', error);
        console.log('Falling back to polling mode...');
    });
}

function generateMessageId() {
    return 'msg-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9);
}

/**
 * Get consistent avatar color for a username
 */
function getAvatarColor(username) {
    if (!userAvatarCache[username]) {
        const hashCode = username.split('').reduce((a, b) => {
            a = ((a << 5) - a) + b.charCodeAt(0);
            return a & a;
        }, 0);
        const index = Math.abs(hashCode) % AVATAR_COLORS.length;
        userAvatarCache[username] = AVATAR_COLORS[index];
    }
    return userAvatarCache[username];
}

/**
 * Get user initials
 */
function getInitials(username) {
    if (!username) return "?";
    const parts = username.split(" ");
    if (parts.length >= 2) {
        return (parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
    } else {
        return username.substring(0, Math.min(2, username.length)).toUpperCase();
    }
}

/**
 * Search messages by keyword
 */
function searchMessages(keyword, sender = null) {
    let url = `/api/messages/search?keyword=${encodeURIComponent(keyword)}`;
    if (sender) {
        url += `&sender=${encodeURIComponent(sender)}`;
    }

    fetch(url)
        .then(response => response.json())
        .then(results => {
            displaySearchResults(results, keyword);
        })
        .catch(error => {
            console.error('Error searching messages:', error);
        });
}

/**
 * Display search results in messages container
 */
function displaySearchResults(results, keyword) {
    const messagesContainer = document.getElementById('messagesContainer');
    messagesContainer.innerHTML = '';

    if (results.length === 0) {
        messagesContainer.innerHTML = `<div class="loading-message">No messages found for "${keyword}"</div>`;
        return;
    }

    const headerDiv = document.createElement('div');
    headerDiv.className = 'search-header';
    headerDiv.innerHTML = `<strong>${results.length}</strong> result${results.length !== 1 ? 's' : ''} found for "<strong>${keyword}</strong>"
    <button onclick="closeSearch()" class="close-search">✕ Close Search</button>`;
    messagesContainer.appendChild(headerDiv);

    results.forEach(result => {
        const messageDiv = document.createElement('div');
        messageDiv.className = 'message search-result';

        const avatarDiv = document.createElement('div');
        avatarDiv.className = 'message-avatar';
        avatarDiv.textContent = getInitials(result.sender);
        avatarDiv.style.backgroundColor = getAvatarColor(result.sender);

        const contentDiv = document.createElement('div');
        contentDiv.className = 'message-content';

        const headerDiv = document.createElement('div');
        headerDiv.className = 'message-header';
        headerDiv.textContent = result.sender;

        // Highlight the keyword in the message
        const highlightedContent = result.content.replace(
            new RegExp(`(${keyword})`, 'gi'),
            '<mark>$1</mark>'
        );

        const textDiv = document.createElement('div');
        textDiv.className = 'message-text';
        textDiv.innerHTML = highlightedContent;

        const timeDiv = document.createElement('div');
        timeDiv.className = 'message-time';
        timeDiv.textContent = result.timestamp;

        contentDiv.appendChild(headerDiv);
        contentDiv.appendChild(textDiv);
        contentDiv.appendChild(timeDiv);

        messageDiv.appendChild(avatarDiv);
        messageDiv.appendChild(contentDiv);

        messagesContainer.appendChild(messageDiv);
    });
}

/**
 * Close search and reload normal messages
 */
function closeSearch() {
    loadMessages();
}

/**
 * Pin a message
 */
function pinMessage(messageId, sender, content, timestamp, channel) {
    const formData = new URLSearchParams();
    formData.append('sender', sender);
    formData.append('content', content);
    formData.append('timestamp', timestamp);
    formData.append('channel', channel);

    fetch(`/api/messages/${messageId}/pin`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: formData
    })
        .then(response => response.json())
        .then(data => {
            console.log('Message pinned:', data);
            // Update UI - add pin icon to message
            const messageDiv = document.getElementById(`msg-${messageId}`);
            if (messageDiv) {
                const pinIcon = document.createElement('span');
                pinIcon.className = 'pin-icon';
                pinIcon.innerHTML = '📌';
                pinIcon.title = 'Pinned';
                messageDiv.insertBefore(pinIcon, messageDiv.firstChild);
            }
            showNotification('Message pinned!');
        })
        .catch(error => console.error('Error pinning message:', error));
}

/**
 * Unpin a message
 */
function unpinMessage(messageId, channel) {
    fetch(`/api/messages/${messageId}/pin?channel=${encodeURIComponent(channel)}`, {
        method: 'DELETE'
    })
        .then(response => response.json())
        .then(data => {
            console.log('Message unpinned:', data);
            // Update UI - remove pin icon
            const messageDiv = document.getElementById(`msg-${messageId}`);
            if (messageDiv) {
                const pinIcon = messageDiv.querySelector('.pin-icon');
                if (pinIcon) {
                    pinIcon.remove();
                }
            }
            showNotification('Message unpinned!');
        })
        .catch(error => console.error('Error unpinning message:', error));
}

/**
 * Show pinned messages for channel
 */
function showPinnedMessages(channel) {
    fetch(`/api/messages/pinned?channel=${encodeURIComponent(channel)}`)
        .then(response => response.json())
        .then(pinnedMessages => {
            displayPinnedMessages(pinnedMessages, channel);
        })
        .catch(error => console.error('Error loading pinned messages:', error));
}

/**
 * Display pinned messages modal
 */
function displayPinnedMessages(pinnedMessages, channel) {
    const messagesContainer = document.getElementById('messagesContainer');
    messagesContainer.innerHTML = '';

    if (pinnedMessages.length === 0) {
        messagesContainer.innerHTML = `<div class="loading-message">No pinned messages in #${channel}</div>`;
        return;
    }

    const headerDiv = document.createElement('div');
    headerDiv.className = 'search-header';
    headerDiv.innerHTML = `<strong>📌 ${pinnedMessages.length}</strong> pinned message${pinnedMessages.length !== 1 ? 's' : ''} in <strong>#${channel}</strong>
    <button onclick="closeSearch()" class="close-search">✕ Close</button>`;
    messagesContainer.appendChild(headerDiv);

    pinnedMessages.forEach(pinned => {
        const messageDiv = document.createElement('div');
        messageDiv.className = 'message pinned-message';

        const avatarDiv = document.createElement('div');
        avatarDiv.className = 'message-avatar';
        avatarDiv.textContent = getInitials(pinned.sender);
        avatarDiv.style.backgroundColor = getAvatarColor(pinned.sender);

        const contentDiv = document.createElement('div');
        contentDiv.className = 'message-content';

        const headerDiv = document.createElement('div');
        headerDiv.className = 'message-header';
        headerDiv.textContent = pinned.sender;

        const textDiv = document.createElement('div');
        textDiv.className = 'message-text';
        textDiv.textContent = pinned.content;

        const timeDiv = document.createElement('div');
        timeDiv.className = 'message-time';
        timeDiv.innerHTML = `${pinned.timestamp} • Pinned by ${pinned.pinnedBy}`;

        const unpinBtn = document.createElement('button');
        unpinBtn.className = 'unpin-btn';
        unpinBtn.textContent = 'Unpin';
        unpinBtn.onclick = () => unpinMessage(pinned.messageId, channel);

        contentDiv.appendChild(headerDiv);
        contentDiv.appendChild(textDiv);
        contentDiv.appendChild(timeDiv);
        contentDiv.appendChild(unpinBtn);

        messageDiv.appendChild(avatarDiv);
        messageDiv.appendChild(contentDiv);

        messagesContainer.appendChild(messageDiv);
    });
}

/**
 * Show notification
 */
function showNotification(message) {
    const notification = document.createElement('div');
    notification.className = 'notification';
    notification.textContent = message;
    document.body.appendChild(notification);

    setTimeout(() => {
        notification.classList.add('show');
    }, 100);

    setTimeout(() => {
        notification.classList.remove('show');
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}

/**
 * Update pin count badge
 */
function updatePinCountBadge(channel) {
    fetch(`/api/messages/pinned/count?channel=${encodeURIComponent(channel)}`)
        .then(response => response.json())
        .then(data => {
            const badge = document.getElementById('pinCountBadge');
            if (badge) {
                badge.textContent = data.pinCount;
            }
        })
        .catch(error => console.error('Error fetching pin count:', error));
}

/**
 * Edit a message
 */
function editMessage(messageId, currentContent) {
    const newContent = prompt('Edit message:', currentContent.replace(' (edited)', ''));
    if (newContent === null || newContent.trim() === '') {
        return;
    }

    const formData = new URLSearchParams();
    formData.append('newContent', newContent.trim());

    fetch(`/api/messages/${messageId}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: formData
    })
        .then(response => response.json())
        .then(data => {
            console.log('Message edited:', data);
            // Update message display
            const messageDiv = document.getElementById(`msg-${messageId}`);
            if (messageDiv) {
                const textDiv = messageDiv.querySelector('.message-text');
                if (textDiv) {
                    textDiv.textContent = data.newContent;
                }
                // Add edited label
                const editedLabel = document.createElement('span');
                editedLabel.className = 'edited-label';
                editedLabel.textContent = 'edited';
                messageDiv.querySelector('.message-time').appendChild(editedLabel);
            }
            showNotification('Message edited!');
        })
        .catch(error => {
            console.error('Error editing message:', error);
            showNotification('Error editing message');
        });
}

/**
 * Delete a message with confirmation
 */
function deleteMessage(messageId) {
    if (!confirm('Delete this message? This action cannot be undone.')) {
        return;
    }

    fetch(`/api/messages/${messageId}`, {
        method: 'DELETE'
    })
        .then(response => response.json())
        .then(data => {
            console.log('Message deleted:', data);
            const messageDiv = document.getElementById(`msg-${messageId}`);
            if (messageDiv) {
                messageDiv.style.opacity = '0.5';
                const textDiv = messageDiv.querySelector('.message-text');
                if (textDiv) {
                    textDiv.textContent = '[Message deleted]';
                    textDiv.style.fontStyle = 'italic';
                    textDiv.style.color = '#999';
                }
                // Hide edit/delete buttons
                const actionsDiv = messageDiv.querySelector('.message-actions');
                if (actionsDiv) {
                    actionsDiv.style.display = 'none';
                }
            }
            showNotification('Message deleted!');
        })
        .catch(error => {
            console.error('Error deleting message:', error);
            showNotification('Error deleting message');
        });
}

/**
 * Highlight mentions in message text
 */
function highlightMentions(text) {
    // Replace @username with highlighted spans
    return text.replace(/@(\w+)/g, (match, username) => {
        return `<span class="mention" data-user="${username}">${match}</span>`;
    });
}

/**
 * Handle mention autocomplete
 */
function setupMentionAutocomplete() {
    const messageInput = document.getElementById('messageInput') || document.getElementById('message');
    let mentionStartIndex = -1;
    let autocompleteUsers = [];

    messageInput.addEventListener('input', function (e) {
        const text = this.value;
        const cursorPos = this.selectionStart;

        // Find if we're currently typing a mention
        const textBeforeCursor = text.substring(0, cursorPos);
        const lastAtIndex = textBeforeCursor.lastIndexOf('@');

        if (lastAtIndex !== -1) {
            const mentionText = textBeforeCursor.substring(lastAtIndex + 1);
            // Show autocomplete if @ is followed by letters
            if (/^\w*$/.test(mentionText) && mentionText.length > 0) {
                showMentionAutocomplete(mentionText, cursorPos);
                mentionStartIndex = lastAtIndex;
            } else if (mentionText.length === 0) {
                showMentionAutocomplete('', cursorPos);
                mentionStartIndex = lastAtIndex;
            } else {
                hideMentionAutocomplete();
            }
        } else {
            hideMentionAutocomplete();
        }
    });

    // Handle tab/enter to complete mention
    messageInput.addEventListener('keydown', function (e) {
        if (e.key === 'Tab' || e.key === 'Enter') {
            const dropdown = document.getElementById('mentionDropdown');
            if (dropdown && dropdown.style.display !== 'none') {
                e.preventDefault();
                const selectedItem = dropdown.querySelector('.mention-item.selected');
                if (selectedItem) {
                    completeMention(selectedItem.dataset.user, mentionStartIndex, this.selectionStart);
                }
            }
        }
    });
}

/**
 * Show mention autocomplete dropdown
 */
function showMentionAutocomplete(partialName, cursorPos) {
    let dropdown = document.getElementById('mentionDropdown');
    if (!dropdown) {
        dropdown = document.createElement('div');
        dropdown.id = 'mentionDropdown';
        dropdown.className = 'mention-dropdown';
        document.getElementById('messageForm').appendChild(dropdown);
    }

    // Get all online users (you may need to fetch from backend)
    const users = Array.from(document.querySelectorAll('.user-item')).map(item => {
        return item.textContent.trim();
    });

    // Filter users matching the partial name
    const matches = users.filter(u => u.toLowerCase().startsWith(partialName.toLowerCase()));

    if (matches.length > 0) {
        dropdown.innerHTML = matches.map((user, index) =>
            `<div class="mention-item ${index === 0 ? 'selected' : ''}" data-user="${user}" onclick="completeMention('${user}', ${cursorPos}, ${cursorPos})">${user}</div>`
        ).join('');
        dropdown.style.display = 'block';
    } else {
        dropdown.style.display = 'none';
    }
}

/**
 * Hide mention autocomplete dropdown
 */
function hideMentionAutocomplete() {
    const dropdown = document.getElementById('mentionDropdown');
    if (dropdown) {
        dropdown.style.display = 'none';
    }
}

/**
 * Complete a mention selection
 */
function completeMention(username, startIndex, endIndex) {
    const messageInput = document.getElementById('message');
    const text = messageInput.value;

    // Replace the partial mention with the complete mention
    const before = text.substring(0, startIndex);
    const after = text.substring(endIndex);
    messageInput.value = before + '@' + username + ' ' + after;

    // Move cursor to after the mention
    messageInput.selectionStart = messageInput.selectionEnd = before.length + username.length + 2;

    hideMentionAutocomplete();
}

/**
 * Open thread modal for a message
 */
function openThread(messageId, parentSender, parentContent) {
    // Fetch thread data
    fetch(`/api/threads/${messageId}`)
        .then(response => {
            if (!response.ok) {
                // Create new thread if doesn't exist
                return fetch(`/api/messages/${messageId}/thread`, {
                    method: 'POST'
                })
                    .then(r => r.json());
            }
            return response.json();
        })
        .then(thread => {
            displayThreadModal(thread, messageId, parentSender, parentContent);
        })
        .catch(error => {
            console.error('Error opening thread:', error);
            showNotification('Error opening thread');
        });
}

/**
 * Display thread modal
 */
function displayThreadModal(thread, messageId, parentSender, parentContent) {
    // Create modal overlay
    let modal = document.getElementById('threadModal');
    if (!modal) {
        modal = document.createElement('div');
        modal.id = 'threadModal';
        modal.className = 'modal-overlay';
        document.body.appendChild(modal);
    }

    // Close modal on background click
    modal.onclick = function (e) {
        if (e.target === modal) {
            closeThread();
        }
    };

    // Build thread content
    const contentHtml = `
        <div class="modal-content thread-modal">
            <div class="modal-header">
                <h3>💬 Thread</h3>
                <button class="close-btn" onclick="closeThread()">✕</button>
            </div>
            
            <div class="thread-container">
                <!-- Parent message -->
                <div class="thread-parent">
                    <div class="thread-message-header">${parentSender}</div>
                    <div class="thread-message-content">${highlightMentions(parentContent)}</div>
                </div>
                
                <!-- Replies -->
                <div id="threadReplies" class="thread-replies">
                    ${thread && thread.replies ? thread.replies.map(reply => `
                        <div class="thread-reply">
                            <div class="thread-reply-sender">${reply.sender}</div>
                            <div class="thread-reply-content">${highlightMentions(reply.content)}</div>
                            <div class="thread-reply-time">${reply.timestamp}</div>
                        </div>
                    `).join('') : ''}
                </div>
            </div>
            
            <!-- Reply input -->
            <div class="thread-input">
                <input type="text" id="threadReplyInput" placeholder="Reply to thread..." autocomplete="off" />
                <button onclick="sendThreadReply('${thread.threadId}')" class="thread-send-btn">Send</button>
            </div>
        </div>
    `;

    modal.innerHTML = contentHtml;
    modal.style.display = 'flex';

    // Focus on input
    setTimeout(() => {
        const input = document.getElementById('threadReplyInput');
        if (input) input.focus();
    }, 100);
}

/**
 * Send reply to thread
 */
function sendThreadReply(threadId) {
    const input = document.getElementById('threadReplyInput');
    const content = input.value.trim();

    if (!content) {
        return;
    }

    const formData = new URLSearchParams();
    formData.append('replyContent', content);

    fetch(`/api/threads/${threadId}/reply`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: formData
    })
        .then(response => response.json())
        .then(data => {
            console.log('Reply sent:', data);
            input.value = '';
            // Refresh thread to show new reply
            openThread(data.threadId);
            showNotification('Reply sent!');
        })
        .catch(error => {
            console.error('Error sending reply:', error);
            showNotification('Error sending reply');
        });
}

/**
 * Close thread modal
 */
function closeThread() {
    const modal = document.getElementById('threadModal');
    if (modal) {
        modal.style.display = 'none';
    }
}

/**
 * Send direct message
 */
function sendDM(recipient, content) {
    const formData = new URLSearchParams();
    formData.append('recipient', recipient);
    formData.append('content', content);

    fetch('/api/dms/send', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: formData
    })
        .then(response => response.json())
        .then(data => {
            console.log('DM sent:', data);
            // Clear input and refresh conversation
            document.getElementById('dmInput').value = '';
            loadDMConversation(recipient);
            showNotification('Message sent!');
        })
        .catch(error => {
            console.error('Error sending DM:', error);
            showNotification('Error sending message');
        });
}

/**
 * Load DM conversation with a user
 */
function loadDMConversation(otherUser) {
    fetch(`/api/dms/conversation?otherUser=${encodeURIComponent(otherUser)}`)
        .then(response => response.json())
        .then(messages => {
            displayDMConversation(messages, otherUser);
            // Mark as read
            fetch(`/api/dms/read?otherUser=${encodeURIComponent(otherUser)}`, {
                method: 'POST'
            });
        })
        .catch(error => console.error('Error loading DM conversation:', error));
}

/**
 * Display DM conversation
 */
function displayDMConversation(messages, otherUser) {
    let dmPanel = document.getElementById('dmPanel');
    if (!dmPanel) {
        dmPanel = document.createElement('div');
        dmPanel.id = 'dmPanel';
        dmPanel.className = 'dm-panel';
        document.body.appendChild(dmPanel);
    }

    const messagesHtml = messages.map(msg => `
        <div class="dm-message ${msg.sender === currentUser ? 'own' : ''}">
            <div class="dm-sender">${msg.sender}</div>
            <div class="dm-content">${msg.content}</div>
            <div class="dm-time">${msg.timestamp}</div>
        </div>
    `).join('');

    dmPanel.innerHTML = `
        <div class="dm-header">
            <h3>💬 ${otherUser}</h3>
            <button class="close-btn" onclick="closeDM()">✕</button>
        </div>
        <div class="dm-messages">
            ${messagesHtml}
        </div>
        <div class="dm-input-area">
            <input type="text" id="dmInput" placeholder="Message..." autocomplete="off" />
            <button onclick="sendDM('${otherUser}', document.getElementById('dmInput').value)" class="dm-send-btn">Send</button>
        </div>
    `;

    dmPanel.style.display = 'flex';
    // Scroll to bottom
    const messagesDiv = dmPanel.querySelector('.dm-messages');
    if (messagesDiv) {
        messagesDiv.scrollTop = messagesDiv.scrollHeight;
    }
}

/**
 * Close DM panel
 */
function closeDM() {
    const dmPanel = document.getElementById('dmPanel');
    if (dmPanel) {
        dmPanel.style.display = 'none';
    }
}

/**
 * Load DM list for user
 */
function loadDMList() {
    fetch('/api/dms/list')
        .then(response => response.json())
        .then(dmUsers => {
            displayDMList(dmUsers);
        })
        .catch(error => console.error('Error loading DM list:', error));
}

/**
 * Display DM list in sidebar
 */
function displayDMList(dmUsers) {
    const dmListEl = document.getElementById('dmList');
    if (!dmListEl) return;

    if (dmUsers.size === 0) {
        dmListEl.innerHTML = '<li class="empty-state">No messages yet</li>';
        return;
    }

    dmListEl.innerHTML = Array.from(dmUsers).map(user => `
        <li class="dm-item" onclick="loadDMConversation('${user}')">
            <span class="dm-user">${user}</span>
        </li>
    `).join('');
}

/**
 * Load user theme preference
 */
function loadUserTheme() {
    fetch('/api/theme')
        .then(response => response.json())
        .then(data => {
            applyTheme(data.theme);
        })
        .catch(error => console.error('Error loading theme:', error));
}

/**
 * Apply theme to page
 */
function applyTheme(theme) {
    const html = document.documentElement;
    if (theme === 'light') {
        html.setAttribute('data-theme', 'light');
    } else {
        html.removeAttribute('data-theme');
    }
}

/**
 * Toggle theme
 */
function toggleTheme() {
    fetch('/api/theme/toggle', {
        method: 'POST'
    })
        .then(response => response.json())
        .then(data => {
            applyTheme(data.theme);
            showNotification(`Switched to ${data.theme} theme!`);
        })
        .catch(error => console.error('Error toggling theme:', error));
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

/**
 * Get user profile
 */
function getUserProfile(username) {
    fetch(`/api/users/${username}/profile`)
        .then(response => response.json())
        .then(data => {
            showProfileModal(data);
        })
        .catch(error => console.error('Error getting profile:', error));
}

/**
 * Show user profile modal
 */
function showProfileModal(profile) {
    const modal = document.createElement('div');
    modal.className = 'profile-modal';
    modal.innerHTML = `
        <div class="profile-content">
            <h2>${profile.displayName}</h2>
            <p class="username">@${profile.username}</p>
            <p class="bio">${profile.bio || 'No bio'}</p>
            <p class="status">Status: ${profile.status}</p>
            <p class="joined">Joined: ${profile.joinedAt}</p>
            <button onclick="this.parentElement.parentElement.remove()">Close</button>
        </div>
    `;
    document.body.appendChild(modal);
}

/**
 * Get analytics
 */
function getAnalytics() {
    fetch('/api/analytics/stats')
        .then(response => response.json())
        .then(data => {
            showAnalyticsDashboard(data);
        })
        .catch(error => console.error('Error getting analytics:', error));
}

/**
 * Show analytics dashboard
 */
function showAnalyticsDashboard(stats) {
    const modal = document.createElement('div');
    modal.className = 'analytics-modal';
    modal.innerHTML = `
        <div class="analytics-content">
            <h2>Chat Analytics</h2>
            <p>Total Messages: ${stats.totalMessages}</p>
            <p>Uptime: ${(stats.uptime / 1000).toFixed(0)}s</p>
            <h3>Top Users</h3>
            <ul>
                ${stats.topUsers.map(u => `<li>${u.username}: ${u.messageCount} messages</li>`).join('')}
            </ul>
            <button onclick="this.parentElement.parentElement.remove()">Close</button>
        </div>
    `;
    document.body.appendChild(modal);
}

/**
 * Upload voice message
 */
function uploadVoiceMessage(channel, duration, audioUrl) {
    fetch('/api/voice/upload', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({
            channel: channel,
            duration: duration,
            audioUrl: audioUrl
        })
    })
        .then(response => response.json())
        .then(data => {
            showNotification(`Voice message uploaded! (${data.duration})`);
            // Broadcast to other users
            if (window.stompClient) {
                window.stompClient.send("/app/send", {}, JSON.stringify({
                    sender: currentUser,
                    content: `🎤 Voice message: ${data.duration}`,
                    type: "voice"
                }));
            }
        })
        .catch(error => console.error('Error uploading voice message:', error));
}

/**
 * Get notifications
 */
function getNotifications() {
    fetch('/api/notifications')
        .then(response => response.json())
        .then(data => {
            displayNotifications(data);
        })
        .catch(error => console.error('Error getting notifications:', error));
}

/**
 * Display notifications
 */
function displayNotifications(notifications) {
    const modal = document.createElement('div');
    modal.className = 'notifications-modal';
    modal.innerHTML = `
        <div class="notifications-content">
            <h2>Notifications (${notifications.length})</h2>
            <ul>
                ${notifications.map(n => `
                    <li class="${n.isRead ? 'read' : 'unread'}">
                        <strong>${n.type}</strong>: ${n.message}
                        <small>${n.timestamp}</small>
                    </li>
                `).join('')}
            </ul>
            <button onclick="this.parentElement.parentElement.remove()">Close</button>
        </div>
    `;
    document.body.appendChild(modal);
}

/**
 * Ban user (admin only)
 */
function banUser(username, reason) {
    fetch(`/api/moderation/ban`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({
            username: username,
            reason: reason
        })
    })
        .then(response => response.json())
        .then(data => {
            showNotification(`User ${username} banned: ${reason}`);
        })
        .catch(error => console.error('Error banning user:', error));
}

/**
 * Mute user (moderator only)
 */
function muteUser(username) {
    fetch(`/api/moderation/mute`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({
            username: username
        })
    })
        .then(response => response.json())
        .then(data => {
            showNotification(`User ${username} muted`);
        })
        .catch(error => console.error('Error muting user:', error));
}
