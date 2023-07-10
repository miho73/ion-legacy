package com.github.miho73.ion.service;

import com.github.miho73.ion.dto.User;
import com.github.miho73.ion.exceptions.IonException;
import com.github.miho73.ion.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;

    public User getUserById(String id) throws IonException {
        List<User> users = userRepository.findById(id);
        if(users.size() != 1) throw new IonException();
        return users.get(0);
    }

    public boolean existsUserById(String id) {
        return userRepository.findById(id).isEmpty();
    }

    public User createUser(User user) {
        user.setJoinDate(new Timestamp(System.currentTimeMillis()));
        user.setStatus(User.USER_STATUS.INACTIVATED);
        user.setPrivilege(1);
        user.setScodeCFlag(false);
        return userRepository.save(user);
    }

    public void updatePrivilege(String id, int privilege) {
        userRepository.updatePrivilegeById(id, privilege);
    }

    public void updateLastLogin(int uid) {
        userRepository.updateLastLogin(uid, new Timestamp(System.currentTimeMillis()));
    }
}
