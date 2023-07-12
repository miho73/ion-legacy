package com.github.miho73.ion.repository;

import com.github.miho73.ion.dto.NsRecord;
import com.github.miho73.ion.dto.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

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

    @Modifying
    @Query(
            value = "UPDATE users.users SET last_login=:ll WHERE uid=:uid",
            nativeQuery = true
    )
    void updateLastLogin(
            @Param("uid") int uid,
            @Param("ll") Timestamp ll
    );

    Optional<User> findByGradeAndClasAndScode(int grade, int clas, int scode);

    List<User> findByGradeOrderByClasAscScodeAsc(int grade);
}
