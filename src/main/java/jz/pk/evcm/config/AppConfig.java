package jz.pk.evcm.config;

import jz.pk.evcm.entity.User;
import jz.pk.evcm.entity.UserRole;
import jz.pk.evcm.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppConfig {

    private final UserRepository userRepository;

    public AppConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    @Bean
    public CommandLineRunner seedDefaultUsers(@Value("${admin.email}") String adminEmail,
                                              @Value("${admin.password}") String adminPassword) {
        return new CommandLineRunner() {
            @Override
            public void run(String... args) throws Exception {
                System.out.println("Instantiating default users during server startup...");

                if (userRepository.findByEmail(adminEmail).isEmpty()) {

                     User admin = new User();
                     admin.setEmail(adminEmail);
                     admin.setPassword(passwordEncoder().encode(adminPassword));
                     admin.assignRole(UserRole.USER);
                     admin.assignRole(UserRole.ADMIN);

                    System.out.println("Admin user created: " + adminEmail);
                } else {
                    System.out.println("Admin user already exists.");
                }

            }
        };
    }
}
