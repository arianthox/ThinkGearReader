package com.globant.brainwaves;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;



@EnableFeignClients
@SpringBootApplication(scanBasePackages = {"com.globant.brainwaves.commons","com.globant.brainwaves"})
public class ThinkGearReaderApplication {


    public static void main(String[] args) {
        SpringApplication.run(ThinkGearReaderApplication.class, args);
    }
}
