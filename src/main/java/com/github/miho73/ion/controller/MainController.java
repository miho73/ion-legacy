package com.github.miho73.ion.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

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
}
