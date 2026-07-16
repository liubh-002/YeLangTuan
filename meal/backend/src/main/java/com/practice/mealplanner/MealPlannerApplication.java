package com.practice.mealplanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MealPlannerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MealPlannerApplication.class, args);
    }
}