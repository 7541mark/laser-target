package org.mt.lasertarget;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.Properties;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.*;

public class TargetCamera
{
    VideoCapture cap;
    TargetImage targetImage = new TargetImage();
    private Properties applicationProperties;
    private Mat input;
    private Mat output;
    private Mat shot;

    public TargetCamera(final Properties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
        this.input = Imgcodecs.imread(applicationProperties.getProperty(LaserTargetFrame.IMAGE_FILENAME_PROPERTY));
        setOutputFile();
        this.shot = Imgcodecs.imread(applicationProperties.getProperty(LaserTargetFrame.SHOT_FILENAME_PROPERTY));
        // cap = new VideoCapture();
        // cap.open(0);
    }

    /**
     * Given a single frame containing a target, detects where the target has been hit
     * 
     * @param preserveHistory. Keep previous shots in the image or clear the image on each new shot
     * @return An image representing the target with an overlay image of the shot.
     * @throws TargetException
     */
    public BufferedImage getOneFrame(boolean preserveHistory) throws TargetException
    {
        setInputFile();
        if (!preserveHistory)
        {
            setOutputFile();
        }
        if (input.empty())
        {
            throw new TargetException("Failed to load image from " + applicationProperties.getProperty(LaserTargetFrame.IMAGE_FILENAME_PROPERTY));
        }
        else if (output.empty())
        {
            throw new TargetException("Failed to load output image from " + applicationProperties.getProperty(LaserTargetFrame.OUTPUT_FILENAME_PROPERTY));
        }
        else if (shot.empty())
        {
            throw new TargetException("Failed to load shot image from " + applicationProperties.getProperty(LaserTargetFrame.SHOT_FILENAME_PROPERTY));
        }
        targetImage.detectShot(input, output, shot);
        BufferedImage image = mat2BufferedImage(output);
        // cap.read(targetImage.mat);
        // return targetImage.getImage(targetImage.mat);
        return image;

    }

    /**
     * Converts an Mat to a Java buffered image for display
     * @param m
     * @return
     */
    private BufferedImage mat2BufferedImage(Mat m)
    {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (m.channels() > 1)
        {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = m.channels() * m.cols() * m.rows();
        byte[] b = new byte[bufferSize];
        m.get(0, 0, b); // get all the pixels
        BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
        final byte[] targetPixels = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);
        return image;
    }

    private void setOutputFile()
    {
        this.output = Imgcodecs.imread(applicationProperties.getProperty(LaserTargetFrame.OUTPUT_FILENAME_PROPERTY));
    }

    private void setInputFile()
    {
        this.input = Imgcodecs.imread(applicationProperties.getProperty(LaserTargetFrame.IMAGE_FILENAME_PROPERTY));
    }
}