package com.globant.brainwaves;

import com.globant.brainwaves.adapter.ThinkGearConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.function.Supplier;
import java.util.logging.Logger;

@SpringBootApplication
public class ThinkGearReaderApplication {


    public static void main(String[] args) {
        SpringApplication.run(ThinkGearReaderApplication.class, args);
    }
}
