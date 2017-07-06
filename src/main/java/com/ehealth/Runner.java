package com.ehealth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.ehealth.ui.MainFrame;

/**
 * Launches the application GUI.
 *
 */
@Component
public class Runner implements CommandLineRunner {

    /**
     * Pull in the JFrame to be displayed.
     */
    @Autowired
    private MainFrame frame;
    
    private static Logger logger = LogManager.getLogger(Runner.class);
    
    @Override
    public void run(String... args) throws Exception {
    	logger.info("Runner.run start");
        /* display the form using the AWT EventQueue */
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                frame.setVisible(true);
            }
        });
        logger.info("Runner.run start");
    }

}
