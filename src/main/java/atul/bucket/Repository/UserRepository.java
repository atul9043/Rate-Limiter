package atul.bucket.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import atul.bucket.model.User;

public interface UserRepository extends JpaRepository<User, Long>{

    Optional<User> findByUsername(String username);
}
