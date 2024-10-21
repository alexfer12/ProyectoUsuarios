package com.example.mediumRoles.exceptions;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<CustomErrorResponse> handleBadCredentialsException(BadCredentialsException exception) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
            "Unauthorized",
            401,
            "The username or password is incorrect."
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccountStatusException.class)
    public ResponseEntity<CustomErrorResponse> handleAccountStatusException(AccountStatusException exception) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
            "Forbidden",
            403,
            "The account is locked."
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<CustomErrorResponse> handleAccessDeniedException(AccessDeniedException exception) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
            "Forbidden",
            403,
            "You are not authorized to access this resource."
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<CustomErrorResponse> handleSignatureException(SignatureException exception) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
            "Forbidden",
            403,
            "The JWT signature is invalid."
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<CustomErrorResponse> handleExpiredJwtException(ExpiredJwtException exception) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
            "Forbidden",
            403,
            "The JWT token has expired."
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomErrorResponse> handleGenericException(Exception exception) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
            "Internal Server Error",
            500,
            "Unknown internal server error."
        );
        // Puedes registrar el stack trace para diagnóstico
        exception.printStackTrace();
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}