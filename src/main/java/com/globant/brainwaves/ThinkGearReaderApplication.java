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

    private static final Logger logger = Logger.getLogger(ThinkGearReaderApplication.class.getName());

    private ThinkGearConnector thinkGearConnector;

    public ThinkGearReaderApplication(@Autowired ThinkGearConnector thinkGearConnector){
        this.thinkGearConnector=thinkGearConnector;
        this.thinkGearConnector.registerEventHandler(in -> {
            logger.info("Event:"+Collections.singletonList(in.toHashMap()).toString());
        });
        this.thinkGearConnector.start();
    }


    public static void main(String[] args) {
        SpringApplication.run(ThinkGearReaderApplication.class, args);
    }
}
