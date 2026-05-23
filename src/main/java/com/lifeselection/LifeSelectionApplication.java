package com.lifeselection;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.lifeselection.mapper")
@SpringBootApplication
public class LifeSelectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(LifeSelectionApplication.class, args);
    }

}
