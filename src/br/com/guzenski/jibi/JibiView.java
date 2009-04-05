/*
 * JibiView.java
 */

package br.com.guzenski.jibi;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.Timer;

import org.jdesktop.application.Action;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.TaskMonitor;

import br.com.guzenski.jibi.ui.ImagePanel;
import br.com.guzenski.jibi.util.ComicBookFile;
import br.com.guzenski.jibi.util.JibiException;
import java.awt.event.KeyEvent;
import javax.swing.JOptionPane;
import org.jdesktop.application.Application;

/**
 * The application's main frame.
 */
public class JibiView extends FrameView {

    public JibiView(SingleFrameApplication app) {
        super(app);

        initComponents();

        statusPanel.setVisible(false);

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String)(evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer)(evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = JibiApp.getApplication().getMainFrame();
            aboutBox = new JibiAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        JibiApp.getApplication().show(aboutBox);
    }
    
    @Action
    public void open() {
        JFileChooser jc = new JFileChooser();
        int r = jc.showOpenDialog(this.getComponent());
        
        if (r == JFileChooser.APPROVE_OPTION) {           
            try {            
                File f = jc.getSelectedFile();
                cb = ComicBookFile.newInstance(f);
                
                ResourceMap resourceMap = Application.getInstance(JibiApp.class).getContext().getResourceMap(JibiView.class);
                this.getFrame().setTitle(resourceMap.getString("Application.title") + " - " + f.getName());
                
                imagePanel.drawImage(cb.getImage());                
                pageLabel.setText("1/"+cb.size());

            } catch (JibiException jex) {
                Logger.getLogger(JibiView.class.getName()).log(Level.SEVERE, null, jex);
                jex.show(this.getComponent());

            } catch (Exception ex) {
                Logger.getLogger(JibiView.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(this.getComponent(), ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }            
        }
    }
    
    @Action
    public void fitWidth() {
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        imagePanel.setScaleType(ImagePanel.ScaleType.Width);
    }
    
    @Action
    public void fitHeight() {
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        imagePanel.setScaleType(ImagePanel.ScaleType.Height);
    }    

    @Action
    public void fitNormal() {    
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        imagePanel.setScaleType(ImagePanel.ScaleType.Normal);
    }   
    
    @Action
    public void next() {
        if (cb != null) {
            imagePanel.drawImage(cb.getNextImage());
            scrollPane.getViewport().setViewPosition(new Point(0,0));
            pageLabel.setText(cb.getPageNum() + "/" + cb.size());
        }
    }     
   
    @Action
    public void prev() {
        if (cb != null) {
            imagePanel.drawImage(cb.getPrevImage());
            scrollPane.getViewport().setViewPosition(new Point(0,0));
            pageLabel.setText(cb.getPageNum() + "/" + cb.size());
        }
    }  
    
    @Action
    public void first() {
        if (cb != null) {
            imagePanel.drawImage(cb.getFirstImage());
            scrollPane.getViewport().setViewPosition(new Point(0,0));
            pageLabel.setText(cb.getPageNum() + "/" + cb.size());
        }
    }     
   
    @Action
    public void last() {
        if (cb != null) {
            imagePanel.drawImage(cb.getLastImage());
            scrollPane.getViewport().setViewPosition(new Point(0,0));
            pageLabel.setText(cb.getPageNum() + "/" + cb.size());
        }
    }     

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        scrollPane = new javax.swing.JScrollPane();
        imagePanel = new br.com.guzenski.jibi.ui.ImagePanel();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        toolBar = new javax.swing.JToolBar();
        btnOpen = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        btnOneForOne = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        btnFirst = new javax.swing.JButton();
        btnPrev = new javax.swing.JButton();
        pageLabel = new javax.swing.JLabel();
        btnNext = new javax.swing.JButton();
        btnLast = new javax.swing.JButton();

        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                mainPanelKeyPressed(evt);
            }
        });

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(br.com.guzenski.jibi.JibiApp.class).getContext().getResourceMap(JibiView.class);
        scrollPane.setBackground(resourceMap.getColor("scrollPane.background")); // NOI18N
        scrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        scrollPane.setName("scrollPane"); // NOI18N

        imagePanel.setBackground(resourceMap.getColor("imagePanel.background")); // NOI18N
        imagePanel.setName("imagePanel"); // NOI18N
        imagePanel.setPreferredSize(new java.awt.Dimension(100, 100));
        imagePanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                imagePanelMouseClicked(evt);
            }
        });

        org.jdesktop.layout.GroupLayout imagePanelLayout = new org.jdesktop.layout.GroupLayout(imagePanel);
        imagePanel.setLayout(imagePanelLayout);
        imagePanelLayout.setHorizontalGroup(
            imagePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 495, Short.MAX_VALUE)
        );
        imagePanelLayout.setVerticalGroup(
            imagePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 349, Short.MAX_VALUE)
        );

        scrollPane.setViewportView(imagePanel);

        org.jdesktop.layout.GroupLayout mainPanelLayout = new org.jdesktop.layout.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(scrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 497, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(scrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 351, Short.MAX_VALUE)
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem1.setText(resourceMap.getString("jMenuItem1.text")); // NOI18N
        jMenuItem1.setName("jMenuItem1"); // NOI18N
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        fileMenu.add(jMenuItem1);

        jSeparator3.setName("jSeparator3"); // NOI18N
        fileMenu.add(jSeparator3);

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(br.com.guzenski.jibi.JibiApp.class).getContext().getActionMap(JibiView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        org.jdesktop.layout.GroupLayout statusPanelLayout = new org.jdesktop.layout.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(statusPanelSeparator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 497, Short.MAX_VALUE)
            .add(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(statusMessageLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 327, Short.MAX_VALUE)
                .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(statusPanelLayout.createSequentialGroup()
                .add(statusPanelSeparator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(statusMessageLabel)
                    .add(statusAnimationLabel)
                    .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(3, 3, 3))
        );

        toolBar.setRollover(true);
        toolBar.setName("toolBar"); // NOI18N
        toolBar.setPreferredSize(new java.awt.Dimension(34, 34));

        btnOpen.setAction(actionMap.get("open")); // NOI18N
        btnOpen.setFocusable(false);
        btnOpen.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnOpen.setName("btnOpen"); // NOI18N
        btnOpen.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(btnOpen);

        jSeparator1.setName("jSeparator1"); // NOI18N
        toolBar.add(jSeparator1);

        btnOneForOne.setAction(actionMap.get("fitNormal")); // NOI18N
        btnOneForOne.setFocusable(false);
        btnOneForOne.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnOneForOne.setName("btnOneForOne"); // NOI18N
        btnOneForOne.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(btnOneForOne);

        jButton2.setAction(actionMap.get("fitWidth")); // NOI18N
        jButton2.setFocusable(false);
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setName("jButton2"); // NOI18N
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(jButton2);

        jButton3.setAction(actionMap.get("fitHeight")); // NOI18N
        jButton3.setFocusable(false);
        jButton3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton3.setName("jButton3"); // NOI18N
        jButton3.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(jButton3);

        jSeparator2.setName("jSeparator2"); // NOI18N
        toolBar.add(jSeparator2);

        btnFirst.setAction(actionMap.get("first")); // NOI18N
        btnFirst.setFocusable(false);
        btnFirst.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnFirst.setName("btnFirst"); // NOI18N
        btnFirst.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(btnFirst);

        btnPrev.setAction(actionMap.get("prev")); // NOI18N
        btnPrev.setFocusable(false);
        btnPrev.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnPrev.setName("btnPrev"); // NOI18N
        btnPrev.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(btnPrev);

        pageLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        pageLabel.setText(resourceMap.getString("pageLabel.text")); // NOI18N
        pageLabel.setMaximumSize(new java.awt.Dimension(35, 14));
        pageLabel.setMinimumSize(new java.awt.Dimension(28, 14));
        pageLabel.setName("pageLabel"); // NOI18N
        pageLabel.setPreferredSize(new java.awt.Dimension(28, 14));
        toolBar.add(pageLabel);

        btnNext.setAction(actionMap.get("next")); // NOI18N
        btnNext.setFocusable(false);
        btnNext.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnNext.setName("btnNext"); // NOI18N
        btnNext.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(btnNext);

        btnLast.setAction(actionMap.get("last")); // NOI18N
        btnLast.setFocusable(false);
        btnLast.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnLast.setName("btnLast"); // NOI18N
        btnLast.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(btnLast);

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
        setToolBar(toolBar);
    }// </editor-fold>//GEN-END:initComponents

    private void imagePanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_imagePanelMouseClicked
        next();
    }//GEN-LAST:event_imagePanelMouseClicked

    private void mainPanelKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_mainPanelKeyPressed
        System.out.println(evt.getKeyCode());
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            next();
        }
    }//GEN-LAST:event_mainPanelKeyPressed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        open();
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnFirst;
    private javax.swing.JButton btnLast;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnOneForOne;
    private javax.swing.JButton btnOpen;
    private javax.swing.JButton btnPrev;
    private br.com.guzenski.jibi.ui.ImagePanel imagePanel;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JLabel pageLabel;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JToolBar toolBar;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    
    private ComicBookFile cb;

    private JDialog aboutBox;
}
