/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.com.guzenski.jibi.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;

import java.util.logging.Logger;
import javax.swing.JPanel;

/**
 *
 * @author max
 */
public class ImagePanel extends JPanel {

    public enum ScaleType {Normal, Width, Height};
    private static final String mutex = "JImagePanelMutex";
    
    private BufferedImage bi;    
    private Image biSmooth;

    private SmoothImage si;
    private ScaleType scaleType = ScaleType.Width;
    
    private Logger log = Logger.getLogger(ImagePanel.class.getName());
    
    public ImagePanel() {
        this.si = new SmoothImage();
        this.si.setPriority(Thread.MIN_PRIORITY);
        this.si.setDaemon(true);

        this.si.start();
        
        this.addComponentListener(new ComponentAdapter() {            
            @Override public void componentResized(ComponentEvent e) {
                si.updateImage();
            }        
        });
    }
            
    public void drawImage(Image image) {
        synchronized(mutex) {
            bi = (BufferedImage)image;
        }
        
        si.updateImage();

        revalidate();
        repaint();
    }

    public void setScaleType(ScaleType scaleType) {
        this.scaleType = scaleType;
        this.si.updateImage();

        revalidate();
        repaint();
    }

    private Dimension getDimension() {
        if (bi != null) {
            int w = bi.getWidth();
            int h = bi.getHeight();
            
            int newW = w;
            int newH = h;
            
            if (scaleType.equals( ScaleType.Width )) {
                newW = getParent().getWidth();
                newH = (h * ((newW * 100) / w)) / 100;

            } else if (scaleType.equals( ScaleType.Height )) {                
                newH = getParent().getHeight();
                newW = (w * ((newH * 100) / h)) / 100;
            }

            return new Dimension(newW, newH);
        }

        return new Dimension(1, 1);
    }

    @Override
    public Dimension getPreferredSize() {
        return getDimension(); 
    }    
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponents(g);
        
        Image tmpImage = null;
        int w = 0;
        int h = 0;
        
        synchronized(mutex) {
            if (bi == null) return;
                        
            tmpImage = (biSmooth != null) ? biSmooth : bi;
            w = bi.getWidth();
            h = bi.getHeight();
        }
        
        final Dimension d = getDimension();

        final int x = (getParent().getWidth() - d.width) / 2;
        final int y = (getParent().getHeight() - d.height) / 2;

        final Graphics2D g2d = (Graphics2D) g;

        g2d.clearRect(0, 0, w, h);
        g2d.drawImage(tmpImage, Math.max(x,0), Math.max(y, 0), d.width, d.height, null);
    }

    class SmoothImage extends Thread {
        private int i = 0;

        public void updateImage() {
            synchronized(this) {
                i++;                                
            }            
            synchronized(mutex) {
                biSmooth = null;
            }
        }
        
        @Override
        public void run() {
            while(true) {
                try {  
                    Thread.sleep(600);  
                } catch (InterruptedException e) {
                    e.printStackTrace();  
                }

                synchronized(this) {
                    if (i > 0) {
                        i = 0;
                        continue;
                    }                    
                    if (bi != null && biSmooth == null) {
                        final Dimension d = getDimension();
                        biSmooth = bi.getScaledInstance(d.width, d.height, Image.SCALE_SMOOTH);

                        revalidate();
                        repaint();                        
                    }
                }
                
            }
        }
    }

}
