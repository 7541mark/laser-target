package org.mt.lasertarget;

import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class LaserTargetFrame extends JFrame
{
    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private Properties applicationProperties = new Properties();
    private static final int ERROR_STATUS = 1;
    private final String JNI_LIB_NAME = "opencv_java330";
    protected static final String IMAGE_FILENAME_PROPERTY = "imageFilename";
    protected static final String OUTPUT_FILENAME_PROPERTY = "outputFilename";
    protected static final String SHOT_FILENAME_PROPERTY = "shotFilename";
    private static final String PROPERTIES_FILENAME = "config.properties";
    private TargetCamera targetCamera;

    /**
     * Launch the application.
     */
    public static void main(String[] args)
    {
        EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                try
                {
                    LaserTargetFrame frame = new LaserTargetFrame();
                    frame.setVisible(true);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    System.exit(ERROR_STATUS);
                }
            }
        });
    }

    /**
     * Create the frame.
     */
    public LaserTargetFrame()
    {
        System.loadLibrary(JNI_LIB_NAME);
        try
        {
            loadProperties(LaserTargetFrame.PROPERTIES_FILENAME);
            targetCamera = new TargetCamera(applicationProperties);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            // LOGGER.error("Error loading application properties", e);
            System.exit(ERROR_STATUS);
        }
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 650, 490);
        this.setLayout(new FlowLayout());
        new MyThread().start();
    }

    protected void loadProperties(final String propertiesFilename) throws FileNotFoundException, IOException
    {
        try (InputStream inStream = this.getClass().getClassLoader().getResourceAsStream(propertiesFilename))
        {
            applicationProperties.load(inStream);
        }
    }

    private void displayImage(Image img2)
    {
        ImageIcon icon = new ImageIcon(img2);
        // JFrame frame = new JFrame();
        // frame.setLayout(new FlowLayout());
        this.setSize(img2.getWidth(null) + 50, img2.getHeight(null) + 50);
        JLabel lbl = new JLabel();
        lbl.setIcon(icon);
        this.add(lbl);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    class MyThread extends Thread
    {
        @Override
        public void run()
        {
            for (;;)
            {
                getContentPane().removeAll();
                getContentPane().revalidate();
                try
                {
                    Image image = targetCamera.getOneFrame(true);
                    displayImage(image);
                }
                catch (TargetException e)
                {
                    e.printStackTrace();
                    System.exit(ERROR_STATUS);
                }
                getContentPane().repaint();
                try
                {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e)
                {
                }
            }
        }
    }
}