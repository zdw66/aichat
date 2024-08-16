package com.aichat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.aichat")
public class AiChatApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiChatApplication.class);
    }
}
