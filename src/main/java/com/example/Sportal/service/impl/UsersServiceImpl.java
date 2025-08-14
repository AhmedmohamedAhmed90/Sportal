package com.example.Sportal.service.impl;

import com.example.Sportal.models.dto.user.UserDto;
import com.example.Sportal.models.entities.User;
import com.example.Sportal.repository.UserRepository;
import com.example.Sportal.service.UsersService;
import jakarta.validation.constraints.Null;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsersServiceImpl implements UsersService {

    private UserRepository userRepository;

    public UsersServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User createUser(User user) {
        User newUser =  userRepository.save(user);
        return newUser;
    }

    @Override
    public User findUserById(Long id) {
        User user =  userRepository.findById(id).get();
        return user;
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User updateUserById(Long id, User user) {
        User updatedUser =  userRepository.findById(id).get();
        updatedUser.setName(user.getName());
        updatedUser.setEmail(user.getEmail());
        updatedUser.setPassword(user.getPassword());
        updatedUser.setRole(user.getRole());
        return userRepository.save(updatedUser);
    }

    @Override
    public String deleteUserById(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);

        if (optionalUser.isEmpty()) {
            return "User not found";
        }

        userRepository.delete(optionalUser.get());
        return "Deleted User Successfully";
    }

    @Override
    public String deleteAllUsers() {
        userRepository.deleteAll();
        return "Deleted All Users Successfully";
    }
}
