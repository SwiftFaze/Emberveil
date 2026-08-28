package com.swiftfaze.veil.exceptions;


public class BuildingException extends RuntimeException {

    public BuildingException(String message) {
        super(message);
    }

    public BuildingException(String message, Throwable cause) {
        super(message, cause);
    }
}
