package com.jlivechats.service;

import com.jlivechats.model.User;
import com.jlivechats.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean login(String username, String password) {
        if (username == null || password == null) return false;
        
        Optional<User> userOpt = userRepository.findByUsername(username.trim());
        if (userOpt.isPresent()) {
            return passwordEncoder.matches(password, userOpt.get().getPassword());
        }
        return false;
    }

    public boolean register(String username, String email, String password) {
        if (username == null || email == null || password == null) return false;
        
        username = username.trim();
        email = email.trim();
        
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) return false;
        if (username.length() < 3 || username.length() > 20) return false;
        if (!email.contains("@") || !email.contains(".")) return false;
        if (password.length() < 6) return false;
        
        if (userRepository.existsByUsername(username)) {
            return false;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        return true;
    }

    public boolean userExists(String username) {
        if (username == null) return false;
        return userRepository.existsByUsername(username.trim());
    }

    public long getUserCount() {
        return userRepository.count();
    }
}
