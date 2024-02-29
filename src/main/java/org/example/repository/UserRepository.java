package org.example.repository;

import org.example.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.example.entities.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {


    Optional<User> findByEmail(String email);

    User findByRole(Role role);

}
