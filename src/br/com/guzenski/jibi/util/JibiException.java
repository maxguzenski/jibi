/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.com.guzenski.jibi.util;

import java.awt.Component;
import javax.swing.JOptionPane;

/**
 *
 * @author max
 */
public class JibiException extends Exception {
    private String title;
    private int type;

    public JibiException(String title, String msn) {
        this(title, msn, JOptionPane.ERROR_MESSAGE);
    }
    
    public JibiException(String title, String msn, int type) {
        super(msn);
        this.title = title;
        this.type  = type;
    }

    public void show(Component c) {
        JOptionPane.showMessageDialog(c, super.getMessage(), this.title, type);
    }
    
}
