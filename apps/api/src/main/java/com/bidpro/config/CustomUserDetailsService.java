package com.bidpro.config;
import com.bidpro.dao.UserDao;
import com.bidpro.entities.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserDao users;
    public CustomUserDetailsService(UserDao users) { this.users = users; }
    @Override public UserDetails loadUserByUsername(String email) {
        User user = users.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Account not found"));
        return org.springframework.security.core.userdetails.User.withUsername(user.getEmail()).password(user.getPasswordHash())
            .roles(user.getRole().name()).disabled(user.getStatus() == User.Status.REJECTED).build();
    }
}
