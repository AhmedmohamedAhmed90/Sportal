package com.example.Sportal.service;

import com.example.Sportal.models.dto.user.UserDto;
import com.example.Sportal.models.entities.User;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Service
public interface UsersService {
    User createUser(User user);
    User findUserById(Long id);
    List<User> findAllUsers();
    User updateUserById(Long id, User user);
    String deleteUserById(Long id);
    String deleteAllUsers();

}
