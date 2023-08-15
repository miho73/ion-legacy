package com.github.miho73.ion;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@SpringBootApplication
@EnableRedisHttpSession
@EnableEncryptableProperties
@EnableScheduling
public class IonApplication {
    public static void main(String[] args) {
        SpringApplication.run(IonApplication.class, args);
    }
}
