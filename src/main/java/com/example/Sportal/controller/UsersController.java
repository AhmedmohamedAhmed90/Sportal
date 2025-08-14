package com.example.Sportal.controller;

import com.example.Sportal.mapper.Mapper;
import com.example.Sportal.models.dto.user.UserDto;
import com.example.Sportal.models.entities.User;
import com.example.Sportal.service.UsersService;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
public class UsersController {

    private UsersService usersService;

    private Mapper<User,UserDto> userMapper;

    public UsersController(UsersService usersService, Mapper<User,UserDto> userMapper) {
        this.usersService = usersService;
        this.userMapper = userMapper;
    }


    @GetMapping()
    public List<User> getUsers() {
        return usersService.findAllUsers();
    }

    @GetMapping(path="/{id}")
    public User getUserById(@PathVariable Long id){
        User user = usersService.findUserById(id);
        return user;

    }

    @PostMapping
    public UserDto createUser(@RequestBody UserDto user){
        System.out.println("Email in DTO: " + user.getEmail());
        User newUser = userMapper.mapFrom(user);
        User savedUser = usersService.createUser(newUser);
        return userMapper.mapTo(savedUser);
    }

    @PutMapping(path="/{id}")
    public UserDto updateUser(@PathVariable Long id, @RequestBody UserDto user){
        User newUser = userMapper.mapFrom(user);
        User updatedUser = usersService.updateUserById(id, newUser);
        return userMapper.mapTo(updatedUser);
    }

    @DeleteMapping(path="/{id}")
    public String deleteUser(@PathVariable Long id)  {
        return usersService.deleteUserById(id);
    }

    @DeleteMapping
    public String deleteAllUsers(){
        return usersService.deleteAllUsers();
    }


}
