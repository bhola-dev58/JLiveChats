package com.jlivechats.config;

import com.jlivechats.model.User;
import com.jlivechats.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, UserRepository userRepository, PasswordEncoder passwordEncoder) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                // Allow public access to these paths
                .requestMatchers("/", "/static/**", "/css/**", "/js/**", "/ws/**", 
                                "/api/messages", "/api/**", "/login", "/register", 
                                "/oauth2/**", "/h2-console/**").permitAll()
                // Everything else requires authentication
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .successHandler((request, response, authentication) -> {
                    org.springframework.security.oauth2.core.user.OAuth2User oauthUser = 
                        (org.springframework.security.oauth2.core.user.OAuth2User) authentication.getPrincipal();
                    String email = oauthUser.getAttribute("email");
                    String username = email != null ? email.split("@")[0] : "user" + System.currentTimeMillis();
                    
                    // Register user in database via UserRepository if they don't exist
                    if (!userRepository.existsByUsername(username)) {
                        com.jlivechats.model.User user = new com.jlivechats.model.User();
                        user.setUsername(username);
                        user.setPassword(passwordEncoder.encode("oauth2-" + System.currentTimeMillis()));
                        userRepository.save(user);
                    }
                    
                    // Synchronize with our custom session handling used in WebController
                    request.getSession().setAttribute("username", username);
                    response.sendRedirect("/chat");
                })
            )
            // Add custom session-based authentication filter
            .addFilterBefore(new SessionAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            // Disable Spring Security's default form login - we use custom login in WebController
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                .permitAll()
            );
        
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> {
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getUsername())
                    .password(user.getPassword())
                    .roles("USER")
                    .build();
        };
    }
}
