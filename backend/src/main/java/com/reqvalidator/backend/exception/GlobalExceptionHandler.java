package com.reqvalidator.backend.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleFileSizeException(
            MaxUploadSizeExceededException exception) {

        return createErrorResponse(
                HttpStatus.BAD_REQUEST,
                "File size exceeds the maximum allowed limit of 20MB."
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException exception) {

        return createErrorResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception) {

        return createErrorResponse(
                HttpStatus.METHOD_NOT_ALLOWED,
                "Request method '" + exception.getMethod()
                        + "' is not supported."
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(
            Exception exception) {

        // Log the actual exception on the backend
        exception.printStackTrace();

        // Do not expose internal exception details to the frontend
        return createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later."
        );
    }

    private ResponseEntity<Map<String, Object>> createErrorResponse(
            HttpStatus status,
            String message) {

        Map<String, Object> response = new HashMap<>();

        response.put(
                "timestamp",
                LocalDateTime.now().toString()
        );

        response.put(
                "status",
                status.value()
        );

        response.put(
                "error",
                status.getReasonPhrase()
        );

        response.put(
                "message",
                message
        );

        return ResponseEntity
                .status(status)
                .body(response);
    }
}