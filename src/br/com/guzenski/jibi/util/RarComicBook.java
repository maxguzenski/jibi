package br.com.guzenski.jibi.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Maximiliano Guzenski <max@guzenski.com.br>
 * @created 09/10/2008
 */
public class RarComicBook extends ComicBookFile {
    private static final String UNRAR_COMMAND = "unrar";
    private static final String UNFOLD_DIR = System.getenv("TMP");

    private int i;
    private File[] files;

    protected RarComicBook() {
    }
	
    private static boolean isAvailable() {
        try {
            Runtime.getRuntime().exec(UNRAR_COMMAND);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
	
    @Override
    public void start(File file) throws Exception {
        if (!RarComicBook.isAvailable())
            throw new JibiException("Unrar nao encontrado", "Aparentemente o comando unrar nao esta disponivel no sistema.");

        File dst = new File(UNFOLD_DIR + File.separator + FileUtils.getBaseName(file));
        FileUtils.resetDiretory(dst);
		
        Process p = Runtime.getRuntime().exec(new String[]
            {UNRAR_COMMAND, 
             "x", "-y", "-x-", "-idp", "-o+", "--",
             file.getAbsolutePath()}, null, dst);
        
        InputStream stderr = p.getInputStream();
        InputStreamReader isr = new InputStreamReader(stderr);
        BufferedReader br = new BufferedReader(isr);

        String line = null;
        while ( (line = br.readLine()) != null)
                System.out.println(line);

        int exitVal = p.waitFor();
        System.out.println("Process exitValue: " + exitVal);
        
        files = dst.listFiles();
    }

    @Override
    public InputStream readNext() throws Exception {
        if (i < size()) return new FileInputStream(files[i++]);
        return null;
    }

    @Override
    public int size() {
        return files.length;
    }
}

