package com.example.Sportal.service.impl;

import com.example.Sportal.models.entities.User;
import com.example.Sportal.repository.UserRepository;
import com.example.Sportal.service.UsersService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UsersServiceImpl implements UsersService {

    private final UserRepository userRepository;

    public UsersServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User createUser(User user) {
        if (existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("User with email " + user.getEmail() + " already exists");
        }
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Page<User> findAllUsersPaginated(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public User updateUserById(Long id, User user) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    existingUser.setName(user.getName());
                    existingUser.setEmail(user.getEmail());
                    if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                        existingUser.setPassword(user.getPassword());
                    }
                    existingUser.setRole(user.getRole());
                    return userRepository.save(existingUser);
                })
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
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

    @Override
    public long getTotalUsersCount() {
        return userRepository.count();
    }

    @Override
    public long getActiveUsersCount() {
        return userRepository.count();
    }

    @Override
    public long getUsersByRole(User.Role role) {
        return userRepository.countByRole(role);
    }

    @Override
    public List<User> getRecentUsers(int limit) {
        List<User> allUsers = userRepository.findAll();
        // Get last 'limit' users by ID (assuming higher ID = more recent)
        return allUsers.stream()
                .skip(Math.max(0, allUsers.size() - limit))
                .toList();
    }

    @Override
    public List<User> findUsersByRole(User.Role role) {
        return userRepository.findByRole(role);
    }

    @Override
    public List<User> searchUsers(String keyword) {
        List<User> allUsers = userRepository.findAll();
        return allUsers.stream()
                .filter(user ->
                        user.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                                user.getEmail().toLowerCase().contains(keyword.toLowerCase())
                )
                .toList();
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}