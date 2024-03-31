package ru.job4j.chat.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    @PostMapping("/login")
    public void swaggerLogin(@RequestBody UserDto userDto) {
        throw new IllegalStateException("This method shouldn't be called."
                + "It's implemented by Spring Security filters.");
    }

    @Data
    @RequiredArgsConstructor
    private class UserDto {
        private String login;
        private String password;
    }
}
