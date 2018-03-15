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
import ch.qumo.sshcommander.telnet.TelnetConnection;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;



public class SshCommanderMainFrame extends JFrame {


    private static final long serialVersionUID = 1L;
    private static final String RESPONSE_DATE_FORMAT_STR = "yyyy/MM/dd-HH:mm:ss.SSS";
    private static final String FILE_CREATION_DATE_FORMAT_STR = "yyyy-MM-dd_HH-mm-ss-SSS";
    private static final String FILE_EXTENSION = ".log";
    private static final String SSH_DEFAULT_COMMAND_TEXT = "PATH=/usr/lib64/qt-3.3/bin:/usr/local/bin:/bin:/usr/bin:/usr/local/sbin:/usr/sbin:/sbin:/home/administrator/bin\n\nhostname && hostname -I\n";
    private static final String TELNET_DEFAULT_COMMAND_TEXT = "";
    private int screenHeight;
    private int screenWidth;
    private boolean isSshProtocol = true;
    private boolean isTelnetProtocol = false;
    private boolean isExecMode = true;
    private boolean isShellMode = false;
    private transient Thread t;
    private boolean isInterrupt = false;
    
    private JTextArea commandTextArea;
    private JTextArea responseTextArea;
    private JTextField ipAddressTextField;
    private JButton submitCommandButton;
    private JButton exitCommandButton;
    private JCheckBox exportAsFilesCheckBox;
    
    // For styling only
    private JPanel mainPanel;
    private JPanel mainPanelHeader;
    private JPanel protocolsPanel;
    private JRadioButton sshRadioButton;
    private JRadioButton telnetRadioButton;
    private JRadioButton execModeRadioButton;
    private JRadioButton shellModeRadioButton;
    private JPanel centerPanel;
    private JPanel optionsPanel;
    private JPanel commandSelectionPanel;
    private JPanel exportAsFilesSelectionPanel;
    private JPanel textAreasPanel;
    private JScrollPane ipAddressScrollPane;
    private JScrollPane commandTextScrollPane;
    private JScrollPane responseTextScrollPane;
    private TitledBorder protocolsTitledBorder;
    private TitledBorder optionsTitledBorder;
    private TitledBorder commandTitledBorder;
    private TitledBorder responseTitledBorder ;
    private TitledBorder ipAddressTitledBorder;
    
    // Backup colors
    Color editableColor;
    Color editableFontColor;
    Color nonEditableColor;
    Color nonEditableFontColor;
    Color caretColor;
    
    
    

    public SshCommanderMainFrame() {
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


        // On crée les afficheurs de texte
        commandTextArea = new JTextArea();
        commandTextArea.setVisible(true);
        responseTextArea = new JTextArea();
        ipAddressTextField = new JTextField();
        ipAddressTextField.setText("root:rcom-uevm@192.168.194.40,root:rcom-uevm@192.168.194.41,root:rcom-uevm@192.168.194.42");// TODO telnet variante
        ipAddressTextField.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        
        ipAddressTitledBorder = new TitledBorder("URLS (separated via char ',')");
        ipAddressScrollPane = new JScrollPane(ipAddressTextField);
        ipAddressScrollPane.setBorder(ipAddressTitledBorder);
        ipAddressScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        
        
        sshRadioButton = new JRadioButton("SSH");
        sshRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                isSshProtocol = true;
                isTelnetProtocol = false;
                execModeRadioButton.setEnabled(true);
                commandTextArea.setText(SSH_DEFAULT_COMMAND_TEXT);
            }
        });
        telnetRadioButton = new JRadioButton("Telnet");
        telnetRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                isSshProtocol = false;
                isTelnetProtocol = true;
                execModeRadioButton.setEnabled(false);
                shellModeRadioButton.setSelected(true);
                commandTextArea.setText(TELNET_DEFAULT_COMMAND_TEXT);
            }
        });
        
        ButtonGroup protocolGroup = new ButtonGroup();
        protocolGroup.add(sshRadioButton);
        protocolGroup.add(telnetRadioButton);
        // Default selection
        commandTextArea.setText(SSH_DEFAULT_COMMAND_TEXT);
        sshRadioButton.setSelected(true);
        
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
        
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(execModeRadioButton);
        modeGroup.add(shellModeRadioButton);
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
                sendCommandsInBackgroundThread();
            }
        });

        // On crée les jpanel pour l'affichage
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        
        // MainPanel header (ips + protocols)
        mainPanelHeader = new JPanel();
        mainPanelHeader.setLayout(new BorderLayout());
        
        protocolsPanel = new JPanel();
        protocolsPanel.setLayout(new GridLayout(1, 2));
        protocolsPanel.add(sshRadioButton);
        protocolsPanel.add(telnetRadioButton);
        protocolsTitledBorder = new TitledBorder("Protocol");
        protocolsPanel.setBorder(protocolsTitledBorder);
        
        mainPanelHeader.add(ipAddressScrollPane, BorderLayout.NORTH);
        mainPanelHeader.add(protocolsPanel, BorderLayout.SOUTH);
        
        
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
        mainPanel.add(mainPanelHeader, BorderLayout.NORTH);
        this.setContentPane(mainPanel);

        // Add mouse wheel listener to resize the font
        this.addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent e) {
                // Handle some events here and dispatch others
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
                    ipAddressTitledBorder.setTitleFont(newFont);
                    protocolsTitledBorder.setTitleFont(newFont);
                    sshRadioButton.setFont(newFont);
                    telnetRadioButton.setFont(newFont);
                    execModeRadioButton.setFont(newFont);
                    shellModeRadioButton.setFont(newFont);
                    exportAsFilesCheckBox.setFont(newFont);
                    commandTextArea.setFont(newFont);
                    responseTextArea.setFont(newFont);
                    ipAddressTextField.setFont(newFont);
                    submitCommandButton.setFont(newFont);
                    exitCommandButton.setFont(newFont);
                    
                    updateIpAddressHeight();
                    
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
        
        /////// Add CTRL + B listener to change colors ///////
        Action doChangeColors = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if(mainPanel.getBackground().equals(nonEditableColor)) {
                    setDarkColors();
                } else {
                    setOriginalsColors();
                }
            }
        };
        KeyStroke ctrlB = KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_DOWN_MASK);
        ipAddressTextField.getInputMap().put(ctrlB, doChangeColors);
        commandTextArea.getInputMap().put(ctrlB, doChangeColors);
        responseTextArea.getInputMap().put(ctrlB, doChangeColors);
        
        
        
        ///////  Dark mode ///////
        backupOriginalColors();
        setDarkColors();
        
        /////// Set Bold Font ///////
        ipAddressTextField.setFont(exportAsFilesCheckBox.getFont().deriveFont(Font.BOLD));
        commandTextArea.setFont(exportAsFilesCheckBox.getFont().deriveFont(Font.BOLD));
        responseTextArea.setFont(exportAsFilesCheckBox.getFont().deriveFont(Font.BOLD));
        sshRadioButton.setFont(exportAsFilesCheckBox.getFont().deriveFont(Font.BOLD));
        telnetRadioButton.setFont(exportAsFilesCheckBox.getFont().deriveFont(Font.BOLD));
        execModeRadioButton.setFont(exportAsFilesCheckBox.getFont().deriveFont(Font.BOLD));
        shellModeRadioButton.setFont(exportAsFilesCheckBox.getFont().deriveFont(Font.BOLD));
        exportAsFilesCheckBox.setFont(exportAsFilesCheckBox.getFont().deriveFont(Font.BOLD));
        submitCommandButton.setFont(exportAsFilesCheckBox.getFont().deriveFont(Font.BOLD));
        exitCommandButton.setFont(exportAsFilesCheckBox.getFont().deriveFont(Font.BOLD));
        
        /////// Set cursors ///////
        sshRadioButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        telnetRadioButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        execModeRadioButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        shellModeRadioButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exportAsFilesCheckBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitCommandButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exitCommandButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        /////// Redimension ipAddressTextField for scrollbar (overlap text oterwise) ///////
        updateIpAddressHeight();

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
                           Color nonEditableColor,
                           Color nonEditableFontColor,
                           Color caretColor) {
        
        mainPanel.setBackground(nonEditableColor);
        mainPanelHeader.setBackground(nonEditableColor);
        protocolsPanel.setBackground(nonEditableColor);
        centerPanel.setBackground(nonEditableColor);
        textAreasPanel.setBackground(nonEditableColor);
        ipAddressScrollPane.setBackground(nonEditableColor);
        commandTextScrollPane.setBackground(nonEditableColor);
        responseTextScrollPane.setBackground(nonEditableColor);
        optionsPanel.setBackground(nonEditableColor);
        commandSelectionPanel.setBackground(nonEditableColor);
        exportAsFilesSelectionPanel.setBackground(nonEditableColor);
        exportAsFilesCheckBox.setBackground(nonEditableColor);
        sshRadioButton.setBackground(nonEditableColor);
        telnetRadioButton.setBackground(nonEditableColor);
        execModeRadioButton.setBackground(nonEditableColor);
        shellModeRadioButton.setBackground(nonEditableColor);
        submitCommandButton.setBackground(nonEditableColor);
        exitCommandButton.setBackground(nonEditableColor);
        
        commandTextArea.setBackground(editableColor);
        responseTextArea.setBackground(editableColor);
        ipAddressTextField.setBackground(editableColor);
        
        exportAsFilesCheckBox.setForeground(editableFontColor);
        sshRadioButton.setForeground(editableFontColor);
        telnetRadioButton.setForeground(editableFontColor);
        execModeRadioButton.setForeground(editableFontColor);
        shellModeRadioButton.setForeground(editableFontColor);
        commandTextArea.setForeground(editableFontColor);
        responseTextArea.setForeground(editableFontColor);
        ipAddressTextField.setForeground(editableFontColor);
        submitCommandButton.setForeground(editableFontColor);
        exitCommandButton.setForeground(editableFontColor);
        
        // Borders
        ipAddressTitledBorder.setTitleColor(nonEditableFontColor);
        protocolsTitledBorder.setTitleColor(nonEditableFontColor);
        commandTitledBorder.setTitleColor(nonEditableFontColor);
        responseTitledBorder.setTitleColor(nonEditableFontColor);
        optionsTitledBorder.setTitleColor(nonEditableFontColor);
        
        // Scrollbars
        ipAddressScrollPane.getVerticalScrollBar().setBackground(editableColor);
        ipAddressScrollPane.getHorizontalScrollBar().setBackground(editableColor);
        commandTextScrollPane.getVerticalScrollBar().setBackground(editableColor);
        commandTextScrollPane.getHorizontalScrollBar().setBackground(editableColor);
        responseTextScrollPane.getVerticalScrollBar().setBackground(editableColor);
        responseTextScrollPane.getHorizontalScrollBar().setBackground(editableColor);
        
        // CaretColors
        ipAddressTextField.setCaretColor(caretColor);
        responseTextArea.setCaretColor(caretColor);
        commandTextArea.setCaretColor(caretColor);
    }
    
    
    
    private void updateIpAddressHeight() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Dimension dim = optionsPanel.getPreferredSize();
                dim.setSize(dim.width, dim.height);
                ipAddressScrollPane.setPreferredSize(dim);
            }
        });
    }
    
    
    
    private void backupOriginalColors() {
        editableColor = ipAddressTextField.getBackground();
        editableFontColor = exportAsFilesCheckBox.getForeground();
        nonEditableColor = mainPanel.getBackground();
        nonEditableFontColor = ipAddressTitledBorder.getTitleColor();
        caretColor = ipAddressTextField.getCaretColor();
    }
    
    
    
    private void setOriginalsColors() {
        setColors(editableColor,
                  editableFontColor,
                  nonEditableColor,
                  nonEditableFontColor,
                  caretColor);
    }
    
    
    
    private void setDarkColors() {
        Color editableColor = new Color(43, 43, 43);
        Color editableFontColor = new Color(189, 203, 218);
        Color nonEditableColor = new Color(43, 43, 43);
        Color nonEditableFontColor = new Color(104, 151, 187);
        Color caretColor = new Color(255, 255, 255);
        
        setColors(editableColor,
                  editableFontColor,
                  nonEditableColor,
                  nonEditableFontColor,
                  caretColor);
    }
    
    
    
    private void sendCommandsInBackgroundThread() {
        t = new Thread(new Runnable() {

            @Override
            public void run() {
                StringBuilder fullResponse = new StringBuilder(responseTextArea.getText());
                
                String command = commandTextArea.getText();
                String[] urls = ipAddressTextField.getText().split(",");
                
                SimpleDateFormat responseDateFormat = new SimpleDateFormat(RESPONSE_DATE_FORMAT_STR);
                
                String destFolderName = "";
                
                SimpleDateFormat dateFormatForFiles = new SimpleDateFormat(FILE_CREATION_DATE_FORMAT_STR);
                if(exportAsFilesCheckBox.isSelected()) {
                    destFolderName = dateFormatForFiles.format(new Date());
                    new File(destFolderName).mkdir();
                }
                
                try {
                    for(String url : urls) {
                        String urlTrimed = url.trim();
                        String host = url.substring(url.lastIndexOf('@')+1);
                        String formatedSourceDate = responseDateFormat.format(new Date());
                        fullResponse.append("*******************************************************************************\n")
                                .append("Response for url:").append(urlTrimed).append("\n")
                                .append("*******************************************************************************\n")
                                .append(formatedSourceDate)
                                .append(" - Start connection...\n");
                        responseTextArea.setText(fullResponse.toString());

                        try {
                            String response = "";
                            
                            if(isSshProtocol) {
                                SSHConnection connection = new SSHConnection();
                                connection.connect(urlTrimed);
                                
                                if (isExecMode) {
                                    response = connection.sendExecCommand(command);

                                } else if(isShellMode) {
                                    String[] commands = command.split("\n");
                                    response = connection.sendShellCommand(commands);
                                }
                                connection.disconnect();
                                
                                
                            } else if(isTelnetProtocol) {
                                TelnetConnection connection = new TelnetConnection();
                                String[] commands = command.split("\n");
                                response = connection.sendCommands(urlTrimed,
                                                                   commands);
                            }
                            
                            formatedSourceDate = responseDateFormat.format(new Date());
                            fullResponse.append(formatedSourceDate)
                                    .append(" - Done! Response:\n")
                                    .append("\n-------------------------\n")
                                    .append(response)
                                    .append("\n-------------------------\n\n\n");
                            if(exportAsFilesCheckBox.isSelected()) {
                                writeInAFile(destFolderName  +File.separator + host + FILE_EXTENSION, response);
                            }
                            
                        } catch(JSchException e) {
                            handleExceptionDunringCommand(fullResponse,
                                                          host,
                                                          destFolderName,
                                                          e);
                                    
                        } catch(IOException e) {
                            handleExceptionDunringCommand(fullResponse,
                                                          host,
                                                          destFolderName,
                                                          e);
                        }
                        
                        if(isInterrupt) {
                            String terminatedByUserMessage = "\n==> PROCESS TERMINATED BY USER !!\n";
                            fullResponse.append(terminatedByUserMessage);
                            System.out.println(terminatedByUserMessage);
                            isInterrupt = false;
                            break;
                        } else {
                            String fullResponseStr = fullResponse.toString();
                            responseTextArea.setText(fullResponseStr);
                        }
                        
                    }// for(String url : urls)
                }  finally {
                    String fullResponseStr = fullResponse.toString();
                    responseTextArea.setText(fullResponseStr);
                    if(exportAsFilesCheckBox.isSelected()) {
                        writeInAFile(destFolderName  +File.separator + "root" + FILE_EXTENSION, fullResponseStr);
                    }
                }
                notifyThreadDone();
            }
        });
        notifyThreadStarts();
        t.start();
    }
    
    
    
    private void handleExceptionDunringCommand(StringBuilder fullResponse,
                                               String host,
                                               String destFolderName,
                                               Exception e) {
        String errorStr = stackTraceToString(e);
        fullResponse.append("\n-------------------------\n")
                    .append(errorStr)
                    .append("\n-------------------------\n\n\n");
        if(exportAsFilesCheckBox.isSelected()) {
            writeInAFile(destFolderName  +File.separator + host + FILE_EXTENSION, errorStr);
        }
    }



    private static String stackTraceToString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();// stack trace as a string
    }
    
    
    
    public static void writeInAFile(String filePath, String text) {
        FileWriter fw = null;
        BufferedWriter output = null;
        try {
            fw = new FileWriter(filePath);
            output = new BufferedWriter(fw);

            output.write(text);// Write in BufferedWriter as buffer

            output.flush();// Flush the file for the BufferedWriter
            
        } catch(IOException ex) {
            ex.printStackTrace();
            
        } finally {
            if(fw != null) {
                try {
                    fw.close(); 
                } catch(IOException ex) {
                    ex.printStackTrace();
                }
            }
            if(output != null) {
                try {
                    output.close(); 
                } catch(IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

}
