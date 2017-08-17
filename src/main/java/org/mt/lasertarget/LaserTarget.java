package org.mt.lasertarget;

import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class LaserTarget
{

    public static void main(String[] args)
    {
        System.loadLibrary("opencv_java330");
        LaserTarget laserTarget = new LaserTarget();
        laserTarget.loadAndDetect();
    }

    private void loadAndDetect()
    {
        Mat m = Imgcodecs.imread("D:\\BDA\\lasertarget\\src\\main\\resources\\target3.bmp");
        Mat output = Imgcodecs.imread("D:\\BDA\\lasertarget\\src\\main\\resources\\target2.bmp");
        Mat shot = Imgcodecs.imread("D:\\BDA\\lasertarget\\src\\main\\resources\\shot.bmp");

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
                    // Imgproc.circle(output, new Point(rect.x + radius,rect.y+radius), 15, new Scalar(0,0,0));
                    Rect roi = new Rect(rect.x, rect.y, shot.width(), shot.height());
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
            BufferedImage image = mat2BufferedImage(output);
            displayImage(image);
        }
    }

    private double calculateScore(final int x1, final int y1, final int x2, final int y2)
    {
        int pixelsPerRing = 60;
        double distance = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
        double score = 11 - (distance / pixelsPerRing);
        return score;
    }

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

    private void displayImage(Image img2)
    {
        ImageIcon icon = new ImageIcon(img2);
        JFrame frame = new JFrame();
        frame.setLayout(new FlowLayout());
        frame.setSize(img2.getWidth(null) + 50, img2.getHeight(null) + 50);
        JLabel lbl = new JLabel();
        lbl.setIcon(icon);
        frame.add(lbl);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
