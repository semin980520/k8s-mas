package com.example.order.ordersystem.common.exception;

import com.example.order.ordersystem.common.dto.CommonErrorDto;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@RestControllerAdvice
@Hidden // swagger에서 제외
public class CommonExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> illegal(IllegalArgumentException e){
        CommonErrorDto dto = CommonErrorDto.builder()
                .status_code(400)
                .error_message(e.getMessage())
                .build();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(dto);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> notValid(MethodArgumentNotValidException e){
        String errorMessage = e.getBindingResult()
                .getFieldError()
                .getDefaultMessage();
        CommonErrorDto dto = CommonErrorDto.builder()
                .status_code(400)
                .error_message(errorMessage)
                .build();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(dto);
    }
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> notValid(NoSuchElementException e){
        CommonErrorDto dto = CommonErrorDto.builder()
                .status_code(404)
                .error_message(e.getMessage())
                .build();
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(dto);
    }
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> notValid(EntityNotFoundException e){
        CommonErrorDto dto = CommonErrorDto.builder()
                .status_code(404)
                .error_message(e.getMessage())
                .build();
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(dto);
    }
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<?> authorized(AuthorizationDeniedException e){
        CommonErrorDto dto = CommonErrorDto.builder()
                .status_code(403)
                .error_message(e.getMessage())
                .build();
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(dto);
    }
    @ExceptionHandler(Exception.class)
//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 분기처리 할 거 없어서 가능
    public ResponseEntity<?> exception(Exception e){
        CommonErrorDto dto = CommonErrorDto.builder()
                .status_code(500)
                .error_message(e.getMessage())
                .build();
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(dto);
    }
}
