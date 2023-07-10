package com.github.miho73.ion.repository;

import com.github.miho73.ion.dto.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    List<User> findById(String id);

    @Modifying
    @Query(
            value = "UPDATE users.users SET status=:ns WHERE id=:id",
            nativeQuery = true
    )
    void updateActiveById(
            @Param("id") String id,
            @Param("ns") int ns
    );

    @Modifying
    @Query(
            value = "UPDATE users.users SET privilege=:privilege WHERE id=:id",
            nativeQuery = true
    )
    void updatePrivilegeById(
            @Param("id") String id,
            @Param("privilege") int privilege
    );
}
