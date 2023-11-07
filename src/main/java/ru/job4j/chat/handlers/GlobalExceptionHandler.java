package ru.job4j.chat.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class.getSimpleName());
    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handle(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest().body(
                e.getFieldErrors().stream()
                        .map(f -> Map.of(
                                f.getField(),
                                String.format("%s. Actual value: %s", f.getDefaultMessage(), f.getRejectedValue())
                        ))
                        .collect(Collectors.toList())
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException e) {
        return ResponseEntity.badRequest().body("Validation failed: " + e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    //    @ExceptionHandler(value = {NullPointerException.class})
//    public void handleException(NullPointerException e, HttpServletRequest request, HttpServletResponse response) throws IOException {
//        response.setStatus(HttpStatus.BAD_REQUEST.value());
//        response.setContentType("application/json");
//        response.getWriter().write(objectMapper.writeValueAsString(new HashMap<>() {
//            {
//                put("message", "Some of fields are empty");
//                put("details", e.getMessage());
//            }
//        }));
//        LOGGER.error(e.getMessage());
//    }
//
//    @ExceptionHandler(value = {IllegalArgumentException.class})
//    public void handleException(IllegalArgumentException e, HttpServletRequest request, HttpServletResponse response) throws IOException {
//        response.setStatus(HttpStatus.BAD_REQUEST.value());
//        response.setContentType("application/json");
//        response.getWriter().write(objectMapper.writeValueAsString(new HashMap<>() {
//            {
//                put("message", "Wrong Id");
//                put("details", e.getMessage());
//            }
//        }));
//        LOGGER.error(e.getMessage());
//    }
//
    @ExceptionHandler(value = {ResponseStatusException.class})
    public void handleException(ResponseStatusException e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(new HashMap<>() {
            {
                put("message", "Not found");
                put("details", e.getMessage());
            }
        }));
        LOGGER.error(e.getMessage());
    }

    @ExceptionHandler(value = {PasswordException.class})
    public void handleException(PasswordException e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(new HashMap<>() {
            {
                put("message", "Password Exception");
                put("details", e.getMessage());
            }
        }));
        LOGGER.error(e.getMessage());
    }
}