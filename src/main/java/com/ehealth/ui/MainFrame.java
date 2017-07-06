
package com.ehealth.ui;

import java.io.File;

import javax.annotation.PostConstruct;
import javax.swing.JFileChooser;
import javax.swing.SwingWorker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import com.ehealth.Application;
import com.ehealth.Runner;
import com.ehealth.services.ExcelReader;

/**
 * The main frame of the tool.
 * 
 */

@Component
public class MainFrame extends javax.swing.JFrame {

	private static Logger logger = LogManager.getLogger(MainFrame.class);

	@Autowired
	private ExcelReader excelReader;

	@Value("${application.ui.title}")
	private String applicationTitle;
	
	
	/**
     * Creates new form MainFrame
     */
    public MainFrame() {
    	logger.info("MainFrame ctor start");
    	initComponents();
    	logger.info("MainFrame ctor end");
    	logger.info(excelReader);
    }
    
    @PostConstruct
    private void initValues() throws IllegalStateException{
        logger.info("initValues() - Frame title -" +applicationTitle);
        setTitle(applicationTitle);
        if (excelReader==null) throw new IllegalStateException("ExcelReader wasn't initialized");
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        
        //Create and configure file chooser
        fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);


        jButton1.setText("Close");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jTextArea1.setEditable(false);
        jTextArea1.setColumns(40);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(20);
        jTextArea1.setText("This is a demonstration on how to write a simple Spring Boot application to display a GUI interface.");
        jTextArea1.setWrapStyleWord(true);
        jScrollPane1.setViewportView(jTextArea1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton1)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jButton1)
                .addGap(20, 20, 20))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
    	//Handle open button action.
        if (evt.getSource() == jButton1) {
            int returnVal = fileChooser.showOpenDialog(MainFrame.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
            	File[] files = fileChooser.getSelectedFiles();
                //This is where a real application would open the file.
            	for (File file : files) { 
            		jTextArea1.setText(jTextArea1.getText()+System.getProperty("line.separator")+"Opening: " + file.getName());
            		logger.info("Opening "+file.getAbsolutePath());
            		// excelReader.readXls(file.getName());
            		processFile(file.getAbsolutePath());
            	}
            } 
        }
 
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
        */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
            	
            	ApplicationContext context = new AnnotationConfigApplicationContext(Application.class);
            	MainFrame mainFrame = (MainFrame) context.getBean(MainFrame.class);
                mainFrame.setVisible(true);
            }
        });
    } 
    
    private void processFile(String filename) {
    	
    	SwingWorker<Void,Void> worker=new SwingWorker<Void,Void>(){
    	       
            // The Integer returned here is either -1 or 0
            // representing unsuccessful and successful save
            // respectively.
            protected Void doInBackground()
            {
                // Disable the button
                jButton1.setEnabled(false);
               
                System.out.println("doInBackground()");
               
                // Save the text as saving
                // This text is not mostly seen, because
                // the process is done so faster, that the
                // with in less time, done() method is reached
                // For a larger file, you can see the text
               
                // Remember, save.setText() isn't thread safe
                // It is a GUI operation that repaints the button
                // So, execute it in the EDT
                java.awt.EventQueue.invokeLater(new Runnable(){
                    public void run(){
                    jButton1.setText("Saving..");
                    }
                });
               
              
                try
                {
                    // Now do the operation
                	logger.info("SwingWorker.doInBackground file"+filename);
                	excelReader.readXls(filename);
                }catch(Exception e){
                    logger.error("SwingWorker.doInBackground",e);
                }
           
            // If no exception occurs, the file is saved
            // successfully, meaning 0 should be returned
            return null;
            }
           
            // This method is executed once after, the
            // doInBackground() method is executed
           
            // done() runs in the EDT
            protected void done()
            {
                System.out.println("done()");
                jButton1.setEnabled(true);
                jButton1.setText("Close");
                // The get() method throws various exceptions
                // More on it in the post page
                /*try
                {
                // If savigs is not done within 2 seconds
                int i=get(2L,TimeUnit.SECONDS);
               
                    // If 0 is returned, file is saved
                    if(i==0) save.setText("Saved.");
                   
                   
                    // Else, file isn't saved
                    // One way to see this text is to set the
                    // saved.txt to Read only
                    else save.setText("Unable to save.");
                   
                // Using multi catch specification
                }catch(InterruptedException|ExecutionException|TimeoutException e){
                    // If saving doesn't happen within the time, time is out.
                    // i.e. a TimeoutException is thrown
                    if(e instanceof TimeoutException) {
                        save.setEnabled(true);
                        save.setText("Save again.");
                    }
                }*/
            }
        };
       
        // Start the worker thread
        worker.execute();
    	
    	
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1; 
    private JFileChooser fileChooser;
    // End of variables declaration//GEN-END:variables
}
