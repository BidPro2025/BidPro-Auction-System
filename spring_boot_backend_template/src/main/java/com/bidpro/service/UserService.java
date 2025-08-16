package com.bidpro.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bidpro.custom_exceptions.AuthenticationFailureException;
import com.bidpro.custom_exceptions.InvalidInputException;
import com.bidpro.dao.UserDao;
import com.bidpro.dto.SigninDTO;
import com.bidpro.dto.SignupDTO;
import com.bidpro.dto.UserDTO;
import com.bidpro.entities.User;
import com.bidpro.util.BidProUtils;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private BidProUtils bidProUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserDTO signup(SignupDTO signupDTO) {
        if (userDao.findByEmail(signupDTO.getEmail()).isPresent()) {
            throw new InvalidInputException("Email already exists");
        }
        if (userDao.findByUsername(signupDTO.getUsername()).isPresent()) {
            throw new InvalidInputException("Username already exists");
        }

        User user = new User();
        user.setUserID(UUID.randomUUID().toString());
        user.setFirstName(signupDTO.getFirstName());
        user.setLastName(signupDTO.getLastName());
        user.setUsername(signupDTO.getUsername());
        user.setEmail(signupDTO.getEmail());
        user.setPhone(signupDTO.getPhone());
        user.setPasswordHash(passwordEncoder.encode(signupDTO.getPassword()));
        user.setRole(User.Role.valueOf(signupDTO.getRole().name()));
        user.setStatus(User.Status.PENDING);
        user.setCreatedAt(LocalDateTime.now());
        user = userDao.save(user);

        UserDTO dto = new UserDTO();
        dto.setUserID(user.getUserID());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    public UserDTO signin(SigninDTO signinDTO) {
        logger.info("Sign-in attempt for email/username: {}", signinDTO.getEmail());

        User user = userDao.findByEmail(signinDTO.getEmail())
                .orElseGet(() -> userDao.findByUsername(signinDTO.getEmail())
                        .orElse(null));

        if (user == null) {
            logger.warn("No user found with email/username: {}", signinDTO.getEmail());
            throw new AuthenticationFailureException("User not found with provided email or username");
        }

        if (!passwordEncoder.matches(signinDTO.getPassword(), user.getPasswordHash())) {
            logger.warn("Password mismatch for user: {}", signinDTO.getEmail());
            throw new AuthenticationFailureException("Incorrect password");
        }

        UserDTO dto = new UserDTO();
        dto.setUserID(user.getUserID());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    public UserDTO findUserById(String userID) {
        User user = userDao.findById(userID)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userID));

        UserDTO dto = new UserDTO();
        dto.setUserID(user.getUserID());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    public List<UserDTO> searchUsers(String keyword) {
        List<User> users = userDao.searchUsers(keyword);
        return users.stream().map(user -> {
            UserDTO dto = new UserDTO();
            dto.setUserID(user.getUserID());
            dto.setFirstName(user.getFirstName());
            dto.setLastName(user.getLastName());
            dto.setUsername(user.getUsername());
            dto.setEmail(user.getEmail());
            dto.setPhone(user.getPhone());
            dto.setRole(user.getRole());
            dto.setStatus(user.getStatus());
            dto.setCreatedAt(user.getCreatedAt());
            return dto;
        }).collect(Collectors.toList());
    }

    public List<UserDTO> findAllUsers() {
        return userDao.findAll().stream().map(user -> {
            UserDTO dto = new UserDTO();
            dto.setUserID(user.getUserID());
            dto.setFirstName(user.getFirstName());
            dto.setLastName(user.getLastName());
            dto.setUsername(user.getUsername());
            dto.setEmail(user.getEmail());
            dto.setPhone(user.getPhone());
            dto.setRole(user.getRole());
            dto.setStatus(user.getStatus());
            dto.setCreatedAt(user.getCreatedAt());
            return dto;
        }).collect(Collectors.toList());
    }

    public UserDTO updateUser(String userID, UserDTO userDTO) {
        User user = userDao.findById(userID)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userID));

        User existingByEmail = userDao.findByEmail(userDTO.getEmail())
                .orElse(null);
        if (existingByEmail != null && !existingByEmail.getUserID().equals(userID)) {
            throw new InvalidInputException("Email already in use");
        }

        User existingByUsername = userDao.findByUsername(userDTO.getUsername())
                .orElse(null);
        if (existingByUsername != null && !existingByUsername.getUserID().equals(userID)) {
            throw new InvalidInputException("Username already in use");
        }

        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());

        if (userDTO.getPasswordHash() != null && !userDTO.getPasswordHash().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(userDTO.getPasswordHash()));
        }

        try {
            user.setRole(User.Role.valueOf(userDTO.getRole().name()));
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid role: " + userDTO.getRole());
        }

        try {
            user.setStatus(User.Status.valueOf(userDTO.getStatus().name()));
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid status: " + userDTO.getStatus());
        }

        user.setUpdatedAt(LocalDateTime.now());
        user = userDao.save(user);
        userDTO.setUserID(user.getUserID());
        userDTO.setCreatedAt(user.getCreatedAt());
        userDTO.setUpdatedAt(user.getUpdatedAt());
        return userDTO;
    }

    public List<User> findUsersByRole(User.Role role) {
        return userDao.findByRole(role);
    }

    public List<User> findUsersByStatus(User.Status status) {
        return userDao.findByStatus(status);
    }
}