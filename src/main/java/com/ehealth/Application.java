package com.ehealth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;


@SpringBootApplication
@Configuration
@ComponentScan(basePackages = { "com.ehealth","com.ehealth.ui","com.ehealth.services" })
@PropertySource("classpath:config.properties")
public class Application {
	
    private static Logger logger = LogManager.getLogger(Application.class);

    /**
     * Application main() method.
     *
     * Uses the fluent {@link SpringApplicationBuilder} to create and run the
     * {@link SpringApplication} object.
     *
     * The options specified:
     *
     * <ul>
     * <li>headless(false) - allow AWT classes to be instantiated</li>
     * <li>web(false) - prevents the bundling of Tomcat or other Web components
     * </ul>
     *
     * Execution is picked up by the {@link Runner} class, which implements
     * {@link CommandLineRunner}.
     *
     * @param args
     */
    
    public static void main(String[] args) {
    	logger.info("Application.main() start");

        new SpringApplicationBuilder(Application.class)
                .headless(false)
                .web(false)
                .run(args);
        logger.info("Application.main() exit");
    }
}
