package com.sutulovai.jobops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JobopsApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobopsApplication.class, args);
    }
}
