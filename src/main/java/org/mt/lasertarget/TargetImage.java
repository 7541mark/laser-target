package org.mt.lasertarget;

import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TargetImage
{

    // private static Logger LOGGER =
    // LoggerFactory.getLogger(LaserTarget.class);

    public static void main(String[] args)
    {
        TargetImage laserTarget = new TargetImage();
        Mat m = Imgcodecs.imread(System.getProperty(LaserTargetFrame.IMAGE_FILENAME_PROPERTY));
        Mat output = Imgcodecs.imread(LaserTargetFrame.OUTPUT_FILENAME_PROPERTY);
        Mat shot = Imgcodecs.imread(LaserTargetFrame.SHOT_FILENAME_PROPERTY);
        laserTarget.detectShot(m, output, shot);
    }

    public void detectShot(final Mat m, final Mat output, final Mat shot)
    {
        if (!m.empty())
        {
            // Convert to hsv
            Imgproc.cvtColor(m, m, Imgproc.COLOR_RGB2HSV);
            List<Mat> mChannels = new ArrayList<Mat>(3);
            mChannels.clear();
            // Split into three channels
            Core.split(m, mChannels);

            // Get the three channels (Hue, Saturation and Value)
            Mat frameH = mChannels.get(0);
            Mat frameS = mChannels.get(1);
            Mat frameV = mChannels.get(2);

            Imgproc.threshold(frameH, frameH, 110, 360, Imgproc.THRESH_BINARY);
            // Imgproc.threshold(frameS, frameS, 0, 100, Imgproc.THRESH_BINARY);
            Imgproc.threshold(frameV, frameV, 100, 256, Imgproc.THRESH_BINARY);
            Core.bitwise_and(frameH, frameV, m);

            // Find the centre of the circle
            List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
            Imgproc.findContours(m, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
            // This is the centrepoint of the image
            // Imgproc.circle(output, new Point(output.width()/2,
            // output.height()/2), 15, new Scalar(0,0,255));
            for (int i = 0; i < contours.size(); i++)
            {
                Rect rect = Imgproc.boundingRect(contours.get(i));
                if (rect.height > 10)
                {
                    int radius = rect.height / 2;
                    // Imgproc.circle(output, new Point(rect.x +
                    // radius,rect.y+radius), 15, new Scalar(0,0,0));
                    Rect roi = new Rect(rect.x, rect.y, shot.width(), shot.height());
                    /*
                     * int lower = 0; int upper = 100; int x = (int)
                     * (Math.random() * (upper - lower)) + lower; int y = (int)
                     * (Math.random() * (upper - lower)) + lower; int midX =
                     * output.width() / 2; int midY = output.height() / 2; Rect
                     * roi = new Rect(midX + x, midY + y, shot.width(),
                     * shot.height());
                     */
                    shot.copyTo(new Mat(output, roi));
                    double score = calculateScore(output.width() / 2, output.height() / 2, rect.x + radius, rect.y + radius);
                    System.out.println("Score is: " + score);
                }
            }
            // Imgproc.circle(m, center, radius, color);
            frameH.release();
            // frameS.release();
            frameV.release();
            // m.release();
        }
    }

    private double calculateScore(final int x1, final int y1, final int x2, final int y2)
    {
        int pixelsPerRing = 60;
        double distance = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
        double score = 11 - (distance / pixelsPerRing);
        return score;
    }

}
