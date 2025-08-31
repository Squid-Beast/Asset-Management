package com.example.asset_management.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String username, String type) {
        super("User not found with " + type + ": " + username);
    }

    public UserNotFoundException(Long userId) {
        super("User not found with ID: " + userId);
    }
}
