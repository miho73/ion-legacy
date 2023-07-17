package com.github.miho73.ion.controller;

import com.github.miho73.ion.service.HangangService;
import com.github.miho73.ion.utils.RestResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/etc/api")
public class EtcController {
    @Autowired
    HangangService hangangService;

    @GetMapping(
            value = "/hangang",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public String getHangang() {
        return RestResponse.restResponse(HttpStatus.OK, hangangService.getHanGangTemp());
    }
}
