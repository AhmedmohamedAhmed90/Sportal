package com.example.Sportal.service;

import com.example.Sportal.models.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UsersService {
    User createUser(User user);
    Optional<User> findUserById(Long id);
    List<User> findAllUsers();
    Page<User> findAllUsersPaginated(Pageable pageable);
    User updateUserById(Long id, User user);
    String deleteUserById(Long id);
    String deleteAllUsers();

    long getTotalUsersCount();
    long getActiveUsersCount();
    long getUsersByRole(User.Role role);
    List<User> getRecentUsers(int limit);
    List<User> findUsersByRole(User.Role role);
    List<User> searchUsers(String keyword);
    boolean existsByEmail(String email);
}