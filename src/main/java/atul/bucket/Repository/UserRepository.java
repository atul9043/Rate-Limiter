package atul.bucket.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import atul.bucket.model.Users;

public interface UserRepository extends JpaRepository<Users, Long>{

    Optional<Users> findByUsername(String username);
}
