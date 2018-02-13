package ch.qumo.sshcommander.display;



import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import com.jcraft.jsch.JSchException;

import ch.qumo.sshcommander.ssh.SSHConnection;
import java.awt.Color;
import java.awt.Cursor;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;



public class SshCommanderMainFrame extends JFrame {


    private static final long serialVersionUID = 1L;
    private static final String formatStr = "yyyy/MM/dd-HH:mm:ss.SSS";
    private static final String dateFormatStrForFilesCreation = "yyyy-MM-dd_HH-mm-ss-SSS";
    private static final String fileExtension = ".log";
    private int screenHeight;
    private int screenWidth;
    private boolean isExecMode = true;
    private boolean isShellMode = false;
    private Thread t;
    private boolean isInterrupt = false;
    
    private JTextArea commandTextArea;
    private JTextArea responseTextArea;
    private JTextField ipAdressTextField;
    private JButton submitCommandButton;
    private JButton exitCommandButton;
    private JCheckBox exportAsFilesCheckBox;
    
    // For styling only
    private JPanel mainPanel;
    private JRadioButton execModeRadioButton;
    private JRadioButton shellModeRadioButton;
    private JPanel centerPanel;
    private JPanel optionsPanel;
    private JPanel commandSelectionPanel;
    private JPanel exportAsFilesSelectionPanel;
    private JPanel textAreasPanel;
    private JScrollPane ipAdressScrollPane;
    private JScrollPane commandTextScrollPane;
    private JScrollPane responseTextScrollPane;
    private TitledBorder optionsTitledBorder;
    private TitledBorder commandTitledBorder;
    private TitledBorder responseTitledBorder ;
    private TitledBorder ipAdressTitledBorder;


    public SshCommanderMainFrame() {
        /* // Less readable on Win7
         try {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch(ClassNotFoundException e) {
         e.printStackTrace();
         } catch(InstantiationException e) {
         e.printStackTrace();
         } catch(IllegalAccessException e) {
         e.printStackTrace();
         } catch(UnsupportedLookAndFeelException e) {
         e.printStackTrace();
         }
        */
        
        // Bug fix for:
        //        com.jcraft.jsch.JSchException: Algorithm negotiation fail
        //          at com.jcraft.jsch.Session.receive_kexinit(Session.java:582)
        //          at com.jcraft.jsch.Session.connect(Session.java:320)
        //          at ch.qumo.sshcommander.ssh.SSHConnection.connect(SSHConnection.java:62)
        //          at ch.qumo.sshcommander.display.SshCommanderMainFrame$1$1.run(SshCommanderMainFrame.java:118)
        //          at java.lang.Thread.run(Unknown Source)
        //
        // This is linked to available cyphers on both sides:
        // Server and client cannot agree on a common key exchange algorithm
        java.util.Properties configuration = new java.util.Properties();
        configuration.put("kex", "diffie-hellman-group1-sha1,diffie-hellman-group14-sha1,diffie-hellman-group-exchange-sha1,diffie-hellman-group-exchange-sha256");
        configuration.put("StrictHostKeyChecking", "no");
        

        // Définit un titre pour notre fenêtre
        this.setTitle("Ssh Commander");

        // Définit sa taille en fonction de la résolution de l'écran
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screenHeight = screenSize.height;
        screenWidth = screenSize.width;
        this.setSize(screenWidth / 2, screenHeight * 2 / 3);

        // Nous demandons maintenant à notre objet de se positionner au centre
        this.setLocationRelativeTo(null);

        // Termine le processus lorsqu'on clique sur la croix rouge
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Options diverses
        this.setResizable(true);
        this.setAlwaysOnTop(false);
        this.setUndecorated(false);


        // on crée les afficheurs de texte
        commandTextArea = new JTextArea();
        commandTextArea.setVisible(true);
        commandTextArea.setText("PATH=/usr/lib64/qt-3.3/bin:/usr/local/bin:/bin:/usr/bin:/usr/local/sbin:/usr/sbin:/sbin:/home/administrator/bin \n\n"
                                + "hostname && hostname -I \n");
        responseTextArea = new JTextArea();
        ipAdressTextField = new JTextField();
        ipAdressTextField.setText("root:rcom-uevm@192.168.194.40,root:rcom-uevm@192.168.194.41,root:rcom-uevm@192.168.194.42");
        ipAdressTextField.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        
        ipAdressTitledBorder = new TitledBorder("URLS (separated via char ',')");
        ipAdressScrollPane = new JScrollPane(ipAdressTextField);
        ipAdressScrollPane.setBorder(ipAdressTitledBorder);
        ipAdressScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        

        execModeRadioButton = new JRadioButton("Exec mode");
        execModeRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                isExecMode = true;
                isShellMode = false;
            }
        });
        shellModeRadioButton = new JRadioButton("Shell mode (WARNING!! You have to exit the shell via the last command)");
        shellModeRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                isExecMode = false;
                isShellMode = true;
            }
        });
        
        ButtonGroup group = new ButtonGroup();
        group.add(execModeRadioButton);
        group.add(shellModeRadioButton);
        execModeRadioButton.setSelected(true);
        
        
        exportAsFilesCheckBox = new JCheckBox("Export as files");
        
        exitCommandButton = new JButton("Stop execution");
        exitCommandButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                isInterrupt = true;
                t.interrupt();
            }
        });
        
        submitCommandButton = new JButton("Submit");
        submitCommandButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {

                t = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        StringBuilder fullResponse = new StringBuilder(responseTextArea.getText());

                        String command = commandTextArea.getText();
                        String[] urls = ipAdressTextField.getText().split(",");

                        SimpleDateFormat dateFormat = new SimpleDateFormat(formatStr);
                        
                        String destFolderName = "";
                        
                        SimpleDateFormat dateFormatForFiles = new SimpleDateFormat(dateFormatStrForFilesCreation);
                        if(exportAsFilesCheckBox.isSelected()) {
                            destFolderName = dateFormatForFiles.format(new Date());
                            new File(destFolderName).mkdir();
                        }

                        for(String url : urls) {
                            String urlTrimed = url.trim();
                            String host = url.substring(url.lastIndexOf('@')+1);
                            String formatedSourceDate = dateFormat.format(new Date());
                            fullResponse.append("*******************************************************************************\n")
                                    .append("Response for url:").append(urlTrimed).append("\n")
                                    .append("*******************************************************************************\n")
                                    .append(formatedSourceDate)
                                    .append(" - Start connection...\n");
                            responseTextArea.setText(fullResponse.toString());

                            try {
                                SSHConnection connection = new SSHConnection();
                                connection.connect(urlTrimed);
                                
                                String response = "";
                                
                                if (isExecMode) {
                                    response = connection.sendExecCommand(command);
                                    
                                } else if(isShellMode) {
                                    String[] commands = command.split("\n");
                                    response = connection.sendShellCommand(commands);
                                }
                                connection.disconnect();
                                
                                formatedSourceDate = dateFormat.format(new Date());
                                fullResponse.append(formatedSourceDate)
                                        .append(" - Done! Response:\n")
                                        .append("\n-------------------------\n")
                                        .append(response)
                                        .append("\n-------------------------\n\n\n");
                                if(exportAsFilesCheckBox.isSelected()) {
                                    try {
                                        writeInAFile(destFolderName  +File.separator + host + fileExtension, response);
                                    } catch(IOException ex) {
                                        ex.printStackTrace();
                                    }
                                }

                            } catch(JSchException e) {
                                String errorStr = stackTraceToString(e);
                                fullResponse.append("\n-------------------------\n")
                                            .append(errorStr)
                                            .append("\n-------------------------\n\n\n");
                                if(exportAsFilesCheckBox.isSelected()) {
                                    try {
                                        writeInAFile(destFolderName  +File.separator + host + fileExtension, errorStr);
                                    } catch(IOException ex) {
                                        ex.printStackTrace();
                                    }
                                }

                            } finally {
                                if(isInterrupt) {
                                    fullResponse.append("\n==> PROCESS TERMINATED BY USER !!\n");
                                }
                                String fullResponseStr = fullResponse.toString();
                                responseTextArea.setText(fullResponseStr);
                                if(exportAsFilesCheckBox.isSelected()) {
                                    try {
                                        writeInAFile(destFolderName  +File.separator + "root" + fileExtension, fullResponseStr);
                                    } catch(IOException ex) {
                                        ex.printStackTrace();
                                    }
                                }
                                
                                if(isInterrupt) {
                                    isInterrupt = false;
                                    System.out.println("Process exited by user");
                                    break;
                                }
                            }
                        }
                        notifyThreadDone();
                    }
                });
                notifyThreadStarts();
                t.start();
            }
        });

        // On crée les jpanel pour l'affichage
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        
        optionsPanel = new JPanel();
        optionsTitledBorder = new TitledBorder("Options");
        optionsPanel.setBorder(optionsTitledBorder);
        optionsPanel.setLayout(new GridLayout(1, 2));
        
        commandSelectionPanel = new JPanel();
        commandSelectionPanel.setLayout(new GridLayout(2, 1));
        commandSelectionPanel.add(execModeRadioButton);
        commandSelectionPanel.add(shellModeRadioButton);
        
        exportAsFilesSelectionPanel = new JPanel();
        exportAsFilesSelectionPanel.setLayout(new GridLayout(1, 1));
        exportAsFilesSelectionPanel.add(exportAsFilesCheckBox);
        
        textAreasPanel = new JPanel();
        textAreasPanel.setLayout(new GridLayout(1, 2));
        commandTextScrollPane = new JScrollPane(commandTextArea);
        responseTextScrollPane = new JScrollPane(responseTextArea);

        commandTitledBorder = new TitledBorder("Command");
        commandTextScrollPane.setBorder(commandTitledBorder);
        responseTitledBorder = new TitledBorder("Response");
        responseTextScrollPane.setBorder(responseTitledBorder);

        textAreasPanel.add(commandTextScrollPane);
        textAreasPanel.add(responseTextScrollPane);
        
        optionsPanel.add(commandSelectionPanel);
        optionsPanel.add(exportAsFilesSelectionPanel);
        centerPanel.add(optionsPanel, BorderLayout.NORTH);
        centerPanel.add(textAreasPanel, BorderLayout.CENTER);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(submitCommandButton, BorderLayout.SOUTH);
        mainPanel.add(ipAdressScrollPane, BorderLayout.NORTH);
        this.setContentPane(mainPanel);

        // on ajoute un listener de molette de souris pour resizer la font
        this.addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent e) {
                // handle some events here and dispatch others
                if(shouldHandleHere(e)) {
                    // Si CTRL down, on resize la police
                    Font newFont = null;
                    if(e.getWheelRotation() > 0) {
                        Font font = commandTextArea.getFont();
                        int fontSize = font.getSize() - 2;
                        newFont = font.deriveFont((float)(fontSize));
                    } else if(e.getWheelRotation() < 0) {
                        Font font = commandTextArea.getFont();
                        int fontSize = font.getSize() + 2;
                        newFont = font.deriveFont((float)(fontSize));
                    }
                    optionsTitledBorder.setTitleFont(newFont);
                    commandTitledBorder.setTitleFont(newFont);
                    responseTitledBorder.setTitleFont(newFont);
                    ipAdressTitledBorder.setTitleFont(newFont);
                    execModeRadioButton.setFont(newFont);
                    shellModeRadioButton.setFont(newFont);
                    exportAsFilesCheckBox.setFont(newFont);
                    commandTextArea.setFont(newFont);
                    responseTextArea.setFont(newFont);
                    ipAdressTextField.setFont(newFont);
                    submitCommandButton.setFont(newFont);
                    exitCommandButton.setFont(newFont);
                    
                    updateIpAdressHeight();
                    
                    mainPanel.repaint();
                    SshCommanderMainFrame.this.invalidate();
                    SshCommanderMainFrame.this.validate();
                } else {
                    if(e.getComponent().getParent() != null) {
                        e.getComponent().getParent().dispatchEvent(e);
                    }
                }
            }



            public boolean shouldHandleHere(MouseWheelEvent e) {
                return (e.getModifiersEx() & MouseWheelEvent.CTRL_DOWN_MASK) != 0;
            }
        });
        
        
        
        ///////  Dark mode ///////
        setDarkColors();
        
        /////// Set Bold Font ///////
        execModeRadioButton.setFont(exportAsFilesCheckBox.getFont().deriveFont(Font.BOLD));
        shellModeRadioButton.setFont(exportAsFilesCheckBox.getFont().deriveFont(Font.BOLD));
        exportAsFilesCheckBox.setFont(exportAsFilesCheckBox.getFont().deriveFont(Font.BOLD));
        commandTextArea.setFont(exportAsFilesCheckBox.getFont().deriveFont(Font.BOLD));
        responseTextArea.setFont(exportAsFilesCheckBox.getFont().deriveFont(Font.BOLD));
        ipAdressTextField.setFont(exportAsFilesCheckBox.getFont().deriveFont(Font.BOLD));
        submitCommandButton.setFont(exportAsFilesCheckBox.getFont().deriveFont(Font.BOLD));
        exitCommandButton.setFont(exportAsFilesCheckBox.getFont().deriveFont(Font.BOLD));
        
        /////// Set cursors ///////
        execModeRadioButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        shellModeRadioButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exportAsFilesCheckBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitCommandButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exitCommandButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        /////// Redimension ipAdressTextField for scrollbar (overlap text oterwise) ///////
        updateIpAdressHeight();

        // Et enfin, la rendre visible
        this.setVisible(true);
    }
    
    
    public void notifyThreadDone() {
        System.out.println("Process terminated");
        mainPanel.remove(exitCommandButton);
        mainPanel.add(submitCommandButton, BorderLayout.SOUTH);
        mainPanel.repaint();
        this.invalidate();
        this.validate();
    }
    
    
    public void notifyThreadStarts() {
        mainPanel.remove(submitCommandButton);
        mainPanel.add(exitCommandButton, BorderLayout.SOUTH);
        mainPanel.repaint();
        this.invalidate();
        this.validate();
    }
    
    
    
    private void setColors(Color editableColor,
                           Color editableFontColor,
                           Color nonEditableFontColor,
                           Color caretColor) {
        
        mainPanel.setBackground(editableColor);
        centerPanel.setBackground(editableColor);
        optionsPanel.setBackground(editableColor);
        textAreasPanel.setBackground(editableColor);
        
        textAreasPanel.setBackground(editableColor);
        ipAdressScrollPane.setBackground(editableColor);
        commandTextScrollPane.setBackground(editableColor);
        responseTextScrollPane.setBackground(editableColor);
        optionsPanel.setBackground(editableColor);
        commandSelectionPanel.setBackground(editableColor);
        exportAsFilesSelectionPanel.setBackground(editableColor);
        exportAsFilesCheckBox.setBackground(editableColor);
        execModeRadioButton.setBackground(editableColor);
        shellModeRadioButton.setBackground(editableColor);
        commandTextArea.setBackground(editableColor);
        responseTextArea.setBackground(editableColor);
        ipAdressTextField.setBackground(editableColor);
        submitCommandButton.setBackground(editableColor);
        exitCommandButton.setBackground(editableColor);
        
        exportAsFilesCheckBox.setForeground(editableFontColor);
        execModeRadioButton.setForeground(editableFontColor);
        shellModeRadioButton.setForeground(editableFontColor);
        commandTextArea.setForeground(editableFontColor);
        responseTextArea.setForeground(editableFontColor);
        ipAdressTextField.setForeground(editableFontColor);
        submitCommandButton.setForeground(editableFontColor);
        exitCommandButton.setForeground(editableFontColor);
        
        // Borders
        ipAdressTitledBorder.setTitleColor(nonEditableFontColor);
        commandTitledBorder.setTitleColor(nonEditableFontColor);
        responseTitledBorder.setTitleColor(nonEditableFontColor);
        optionsTitledBorder.setTitleColor(nonEditableFontColor);
        
        // Scrollbars
        ipAdressScrollPane.getVerticalScrollBar().setBackground(editableColor);
        ipAdressScrollPane.getHorizontalScrollBar().setBackground(editableColor);
        commandTextScrollPane.getVerticalScrollBar().setBackground(editableColor);
        commandTextScrollPane.getHorizontalScrollBar().setBackground(editableColor);
        responseTextScrollPane.getVerticalScrollBar().setBackground(editableColor);
        responseTextScrollPane.getHorizontalScrollBar().setBackground(editableColor);
        
        // CaretColors
        ipAdressTextField.setCaretColor(caretColor);
        responseTextArea.setCaretColor(caretColor);
        commandTextArea.setCaretColor(caretColor);
    }
    
    
    
    private void updateIpAdressHeight() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Dimension dim = optionsPanel.getPreferredSize();
                dim.setSize(dim.width, dim.height);
                ipAdressScrollPane.setPreferredSize(dim);
            }
        });
    }
        
    
    
    private void setDarkColors() {
        Color editableDarkColor = new Color(43, 43, 43);
        Color editableFontColor = new Color(189, 203, 218);
        Color nonEditableFontColor = new Color(104, 151, 187);
        Color caretColor = new Color(255, 255, 255);
        
        setColors(editableDarkColor,
                  editableFontColor,
                  nonEditableFontColor,
                  caretColor);
    }



    private static String stackTraceToString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();// stack trace as a string
    }
    
    
    
    public static void writeInAFile(String filePath, String text) throws IOException {
        FileWriter fw = new FileWriter(filePath);
        BufferedWriter output = new BufferedWriter(fw);

        output.write(text);// Write in BufferedWriter as buffer

        output.flush();// Flush the file for the BufferedWriter

        output.close();
    }

}
