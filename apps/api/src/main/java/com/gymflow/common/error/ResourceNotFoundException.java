package com.gymflow.common.error;

/**
 * Thrown when a requested resource does not exist in the current tenant.
 * Mapped to a 404 problem response by {@link GlobalExceptionHandler}.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
