package com.pharmapack.qms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;

@SpringBootApplication
public class PharmaPackQmsApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(PharmaPackQmsApplication.class);
        // Desktop edition needs AWT (browser launch + tray icon); Spring Boot defaults to headless.
        if (isDesktop(args)) app.setHeadless(false);
        app.run(args);
    }

    private static boolean isDesktop(String[] args) {
        String profiles = System.getProperty("spring.profiles.active",
                System.getenv().getOrDefault("SPRING_PROFILES_ACTIVE", ""));
        return profiles.contains("desktop")
                || Arrays.stream(args).anyMatch(a -> a.startsWith("--spring.profiles.active") && a.contains("desktop"));
    }
}
