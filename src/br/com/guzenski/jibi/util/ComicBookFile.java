package br.com.guzenski.jibi.util;

import java.awt.Image;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public abstract class ComicBookFile {

    private static ComicBookFile cb;
    
    private List<Image> images; 
    private int pos;

    public static ComicBookFile newInstance(File file) throws Exception {
        String ext = FileUtils.getExtension(file);
        System.out.println(ext);
        
        if ("cbr".equals(ext)) {
            cb = new RarComicBook();
        } else if ("cbz".equals(ext)) {            
            cb = new ZipComicBook();
        }
        
        cb.initialize(file);
        return cb;
    }
    
    private final void initialize(File file) throws Exception {
        this.images = new ArrayList<Image>();
        this.start(file);
        
        new LoadImages().start();
    }
    
    public Image getImage() {
        pos = Math.min(pos, size()-1);
        pos = Math.max(pos, 0);

        try {
            while(true) {
                synchronized (images) {
                    if (images.size() > pos)
                        return images.get(pos);
                    else
                        images.wait();
                }

                Thread.sleep((int)(Math.random() * 100));
            }
        } catch (InterruptedException ie) {
             Logger.getLogger(ComicBookFile.class.getName()).log(Level.SEVERE, null, ie);
        }

        return null;
    }
    
    public Image getNextImage() {
        pos++;
        return getImage();
    }
    
    public Image getPrevImage() {
        pos--;
        return getImage();
    }   
    
    public Image getFirstImage() {
        pos = 0;
        return getImage();
    }   
    
    public Image getLastImage() {
        pos = size() - 1;
        return getImage();
    }       
    
    public int getPageNum() {
        return pos+1;
    }    
	
    protected abstract void start(File file) throws Exception;
	
    protected abstract InputStream readNext() throws Exception;
	
    public abstract int size();    
    
    
    class LoadImages extends Thread {
        @Override
        public void run() {
            for (int i=0; i < size(); i++) {
                synchronized (images) {
                    try {
                        InputStream is = readNext();
                        images.add( ImageIO.read(is));
                        is.close();

                    } catch (Exception ex) {
                        Logger.getLogger(ComicBookFile.class.getName()).log(Level.SEVERE, null, ex);
                    }                    
                    
                    images.notifyAll();
                }

                try {
                    Thread.sleep((int)(Math.random() * 10));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
