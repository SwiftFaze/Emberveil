package com.swiftfaze.veil.exceptions;


public class PlayerClassException extends RuntimeException {

    public PlayerClassException(String message) {
        super(message);
    }

    public PlayerClassException(String message, Throwable cause) {
        super(message, cause);
    }
}
