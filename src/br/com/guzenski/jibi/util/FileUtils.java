package br.com.guzenski.jibi.util;

import java.io.File;

public class FileUtils {
    public static String getBaseName(File file) {
        String name = file.getName();
	return name.substring(0, name.lastIndexOf('.'));
    }
	
    public static String getExtension(File file) {
        String name = file.getName();
	return name.substring(name.lastIndexOf('.')+1);
    }
	
    public static void resetDiretory(File dir) {
        if (dir.exists()) {
            FileUtils.cleanDiretory(dir);
        } else {
            dir.mkdir();
        }
    }
	
    public static void cleanDiretory(File dir) {
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                file.delete();
            }
        }
    }
}
