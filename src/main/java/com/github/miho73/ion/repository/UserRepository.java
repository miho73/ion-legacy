package com.github.miho73.ion.repository;

import com.github.miho73.ion.dto.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {
    List<User> findById(String id);
}
