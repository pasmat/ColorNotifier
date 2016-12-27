import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by PasiMatalamaki on 27.12.2016.
 */
public class ColorNotifier extends Thread {

    private static final int DEFAULT_MAX_DELTA = 80;
    public static final int DEFAULT_COUNT_DOWN_HEIGHT = 256;
    public static final int DEFAULT_COUNT_DOWN_WIDTH = 256;

    public static final int DEFAULT_EXIT_HEIGHT = 64;
    public static final int DEFAULT_EXIT_WIDTH = 64;
    public static final String FEED_ID_KEY = "feed_id";
    public static final String AIO_KEY_KEY = "aio_key";

    private int x, y;

    private Color color;

    private int maxDelta = DEFAULT_MAX_DELTA;

    private Robot robot;

    private String feedId;
    private String aioKey;

    public ColorNotifier(Robot robot, String feedId, String aioKey) {
        this.robot = robot;
        this.feedId = feedId;
        this.aioKey = aioKey;
    }

    public static void main(String[] args) {

        try {
            File configFile = new File("adafruit.cfg");

            String defaultFeedId = "";
            String defaultAIOKey = "";

            Properties properties = new Properties();
            if (configFile.exists()) {
                FileInputStream fis = null;
                try {

                    fis = new FileInputStream(configFile);
                    properties.load(fis);

                    defaultFeedId = properties.getProperty(FEED_ID_KEY, "");
                    defaultAIOKey = properties.getProperty(AIO_KEY_KEY, "");
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if(fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            String feedId = JOptionPane.showInputDialog("Adafruit IO feed id?", defaultFeedId);

            if (feedId.length() == 0) {
                feedId = defaultFeedId;
            } else {
                properties.setProperty(FEED_ID_KEY, feedId);
            }

            String aioKey = JOptionPane.showInputDialog("AIO key?", defaultAIOKey);

            if (aioKey.length() == 0) {
                aioKey = defaultAIOKey;
            } else {
                properties.setProperty(AIO_KEY_KEY, aioKey);
            }

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(configFile);
                properties.store(fos, "");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }


            ColorNotifier colorNotifier = new ColorNotifier(new Robot(), feedId, aioKey);
            colorNotifier.pickPixel();
        } catch (AWTException e) {
            e.printStackTrace();
        }

    }

    private void pickPixel() {
        DisplayMode displayMode = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();

        final TextAlphaFrame textFrame = new TextAlphaFrame("Select position to watch for!", 0, 0, displayMode.getWidth(), displayMode.getHeight());

        final JPanel colorPanel = new JPanel();
        textFrame.getContentPane().add(colorPanel);

        textFrame.setVisible(true);
        textFrame.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                textFrame.dispatchEvent(new WindowEvent(textFrame, WindowEvent.WINDOW_CLOSING));



                getAverageColor(e.getX(), e.getY());
            }
        });
        textFrame.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);

                Color pixel = robot.getPixelColor(e.getX(), e.getY());




                colorPanel.setBackground(pixel);
                colorPanel.setBounds(e.getX() + 1, e.getY() + 1, 50, 50);

                textFrame.repaint();
            }
        });

    }

    private void getAverageColor(final int x, final int y) {


        DisplayMode displayMode = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();

        final TextAlphaFrame textFrame = new TextAlphaFrame("", displayMode.getWidth() - DEFAULT_COUNT_DOWN_WIDTH, 0, DEFAULT_COUNT_DOWN_HEIGHT, DEFAULT_COUNT_DOWN_HEIGHT);


        textFrame.setFocusable(false);
        textFrame.setFocusableWindowState(false);
        textFrame.setVisible(true);

        new Thread() {
            long startTime = System.currentTimeMillis();

            int minR = Integer.MAX_VALUE;
            int minG = Integer.MAX_VALUE;
            int minB = Integer.MAX_VALUE;

            int maxR = -Integer.MAX_VALUE;
            int maxG = -Integer.MAX_VALUE;
            int maxB = -Integer.MAX_VALUE;

            @Override
            public void run() {
                super.run();
                while ((System.currentTimeMillis() - startTime) < 5500) {
                    Color pixelColor = robot.getPixelColor(x, y);

                    minR = Math.min(minR, pixelColor.getRed());
                    minG = Math.min(minG, pixelColor.getGreen());
                    minB = Math.min(minB, pixelColor.getBlue());

                    maxR = Math.max(maxR, pixelColor.getRed());
                    maxG = Math.max(maxG, pixelColor.getGreen());
                    maxB = Math.max(maxB, pixelColor.getBlue());

                    System.out.println(minR + ", " + minG + ", " + minB);
                    System.out.println(maxR + ", " + maxG + ", " + maxB);


                    long elapsedMs = System.currentTimeMillis() - startTime;

                    int elapsedS = (int) (elapsedMs / 1000L);

                    textFrame.setText("<html><center><font color='red'>" + Integer.toString(5 - elapsedS) + "</font></center></html>");

                    int avgR = maxR - (maxR - minR) / 2;
                    int avgG = maxG - (maxG - minG) / 2;
                    int avgB = maxB - (maxB - minB) / 2;

                    //textFrame.setBackground(new Color(avgR, avgG, avgB));

                    textFrame.repaint();


                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }

                textFrame.dispatchEvent(new WindowEvent(textFrame, WindowEvent.WINDOW_CLOSING));


                int avgR = maxR - (maxR - minR) / 2;
                int avgG = maxG - (maxG - minG) / 2;
                int avgB = maxB - (maxB - minB) / 2;

                setColor(new Color(avgR, avgG, avgB));


                ColorNotifier.this.start();
            }
        }.start();
    }

    private void setPixel(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void run() {
        DisplayMode displayMode = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();

        super.run();


        final TextAlphaFrame textFrame = new TextAlphaFrame("<html><center><font color='red'>X</font></center></html>", displayMode.getWidth() - DEFAULT_EXIT_WIDTH, 0, DEFAULT_EXIT_HEIGHT, DEFAULT_EXIT_HEIGHT);

        textFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        textFrame.setFocusable(false);
        textFrame.setFocusableWindowState(false);
        textFrame.setVisible(true);

        textFrame.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                textFrame.dispatchEvent(new WindowEvent(textFrame, WindowEvent.WINDOW_CLOSING));
            }
        });

        CloseableHttpClient client = HttpClients.createDefault();

        while (textFrame.isVisible()) {
            Color pixelColor = robot.getPixelColor(x, y);

            HttpPost post = new HttpPost("https://io.adafruit.com/api/feeds/" + feedId + "/data");
            post.setHeader("x-aio-key", aioKey);

            int rDelta = Math.abs(color.getRed() - pixelColor.getRed());
            int gDelta = Math.abs(color.getGreen() - pixelColor.getGreen());
            int bDelta = Math.abs(color.getBlue() - pixelColor.getBlue());

            int totalDelta = rDelta + gDelta + bDelta;

            String valueString = new JSONObject()
                    .put("rDelta", Integer.toString(rDelta))
                    .put("gDelta", Integer.toString(gDelta))
                    .put("bDelta", Integer.toString(bDelta))
                    .put("totalDelta", Integer.toString(totalDelta))
                    .toString();

            post.setEntity(EntityBuilder.create().setParameters(
                    new BasicNameValuePair("value", valueString))
                    .build());

            System.out.println(valueString);

            try {
                CloseableHttpResponse response = client.execute(post);

                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
