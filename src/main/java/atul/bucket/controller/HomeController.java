package atul.bucket.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import atul.bucket.model.Users;
import atul.bucket.service.LoginService;
import atul.bucket.service.UserService;

@RestController
@RequestMapping("/api")
public class HomeController {

    @Autowired
    UserService service;

    @Autowired
    LoginService loginService;

    @GetMapping("/test")
    public String greet(){
        return "helloe";
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Users user){

        return service.register(user);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Users user){

        return loginService.login(user);
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication) {
        return "Hello, "+authentication.getName() + "| This endpoint requires login.";
    }

}
