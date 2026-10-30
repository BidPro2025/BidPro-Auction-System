package com.bidpro.config;
import com.bidpro.dao.UserDao;
import com.bidpro.entities.User;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
/** Creates the first administrator from operator-provided environment variables; never exposes a public registration route. */
@Component
@Order(0)
public class AdminBootstrap implements ApplicationRunner {
    private final UserDao users; private final PasswordEncoder encoder; private final String email; private final String password;
    public AdminBootstrap(UserDao users,PasswordEncoder encoder,@Value("${app.bootstrap.admin-email:}") String email,@Value("${app.bootstrap.admin-password:}") String password) {
        this.users=users;this.encoder=encoder;this.email=email.trim().toLowerCase(Locale.ROOT);this.password=password;
    }
    @Override @Transactional public void run(ApplicationArguments args) {
        if(email.isBlank())return;
        var existing=users.findByEmail(email);
        if(existing.isPresent()) {
            if(existing.get().getRole()!=User.Role.ADMIN)throw new IllegalStateException("Bootstrap email belongs to a non-administrator account");
            return;
        }
        if(!email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+"))throw new IllegalStateException("BOOTSTRAP_ADMIN_EMAIL must be a valid email");
        if(password.length()<8 || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)throw new IllegalStateException("Set BOOTSTRAP_ADMIN_PASSWORD to 8-72 bytes before creating the administrator");
        users.save(new User(UUID.randomUUID().toString(),"System","Administrator","admin-"+UUID.randomUUID().toString().substring(0,8),email,"",encoder.encode(password),User.Role.ADMIN,User.Status.APPROVED));
    }
}
