package com.wfm.experts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WfmBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(WfmBackendApplication.class, args);
    }

}