package atul.bucket.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import atul.bucket.repository.UserRepository;

@Service
public class UserService implements UserDetailsService{

    @Autowired
    private UserRepository repo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired 
    JwtService service;

    @Override
    public UserDetails loadUserByUsername(String username) {
        // TODO Auto-generated method stub
        atul.bucket.model.Users user = repo.findByUsername(username).orElseThrow(()->new UsernameNotFoundException("User not found"+username));

        return User.builder()
                   .username(user.getUsername())
                   .password(user.getPassword())
                   .authorities("USER")
                   .build();
    }

public ResponseEntity<String> register(atul.bucket.model.Users request) {

        if(repo.findByUsername(request.getUsername()).isPresent()){
            return new ResponseEntity<>("Username already exists", HttpStatus.NOT_ACCEPTABLE);
        }

        atul.bucket.model.Users user = new atul.bucket.model.Users();

        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        repo.save(user);
        
        return new ResponseEntity<>("Registered Successfully", HttpStatus.ACCEPTED);
       
    }

}
