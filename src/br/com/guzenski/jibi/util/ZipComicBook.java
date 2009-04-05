/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.com.guzenski.jibi.util;

import java.io.File;
import java.io.InputStream;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 * @author max
 */
public class ZipComicBook extends ComicBookFile {

    private ZipFile zf;
    private Iterator<ZipEntry> itEntries;
    
    protected ZipComicBook() { }
    
    @Override
    protected void start(File file) throws Exception {
        this.zf = new ZipFile(file);
        
        Set<ZipEntry> entriesOrd = new TreeSet<ZipEntry>(new Comparator<ZipEntry>() {
            public int compare(ZipEntry o1, ZipEntry o2) {
                return o1.getName().compareTo( o2.getName() );
            }
        });

        Enumeration entries = zf.entries();
        while (entries.hasMoreElements()) {
            entriesOrd.add( (ZipEntry)entries.nextElement() );
        }        
        
        this.itEntries = entriesOrd.iterator();
    }

    @Override
    protected InputStream readNext() throws Exception {
        if (this.itEntries.hasNext()) {
            return this.zf.getInputStream(this.itEntries.next());
        }
        return null;
    }

    @Override
    public int size() {
        return this.zf.size();
    }

}
