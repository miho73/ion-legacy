package com.github.miho73.ion.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

@Controller
@Slf4j
public class MainController {
    @GetMapping({
            "/",
            "/auth/signup", "/auth/login",
            "/docs/**",
            "/ns",
            "/manage"
    })
    public String index() {
        return "index";
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleResourceNotFoundException(HttpServletRequest request) {
        log.info(request.getMethod());
        return "index";
    }

}
