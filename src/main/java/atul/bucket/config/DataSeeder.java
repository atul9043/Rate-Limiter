package atul.bucket.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import atul.bucket.repository.UserRepository;
import atul.bucket.model.User;

@Component
@Profile("demo")
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("demo").isEmpty()) {
            User demoUser = new User();
            demoUser.setUsername("demo");
            demoUser.setPassword(passwordEncoder.encode("demo1234"));
            userRepository.save(demoUser);
        }
    }
}