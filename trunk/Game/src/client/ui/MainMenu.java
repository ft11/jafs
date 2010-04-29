/*
 * Gui.java
 *
 * Created on 16.04.2010, 16:40:19
 */

package client.ui;

import client.Main;
import common.net.Client;
import common.net.Network;
import common.net.ProtocolCmd;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.DefaultCaret;

import static common.net.ProtocolCmdArgument.*;

/**
 *
 * @author Julian Sanio
 */
public class MainMenu extends javax.swing.JFrame implements ActionListener, MouseListener, KeyListener {

    private ArrayList<PrivateChatTab> clientlist = new ArrayList<PrivateChatTab>();
    private ArrayList<PrivateChatTab> clientlistOld = new ArrayList<PrivateChatTab>();
    
    /** Creates new form Gui */
    public MainMenu(UiActionListener uiaListener) {
        sModel = new ServerbrowserTableModel();
        listModel = new DefaultListModel();
        initComponents();
        DefaultCaret caret = (DefaultCaret)taLobbyChatPublic.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        btnMenuLobby.setBackground(btn_bg_aktive);
        btnMenuLobby.addActionListener(this);
        btnMenuOptions.setBackground(btn_bg_normal);
        btnMenuOptions.addActionListener(this);
        btnSendLobbyChat.setActionCommand(UiActionListener.CMD_LOBBYCHAT_SEND_MSG);
        btnSendLobbyChat.addActionListener(uiaListener);
        btnMenuExit.setBackground(btn_bg_normal);
        btnMenuExit.addActionListener(this);
        btnMenuExit.setActionCommand(UiActionListener.CMD_EXIT);
        btnMenuExit.addActionListener(uiaListener);
        btnRefresh.setActionCommand(UiActionListener.CMD_REFRESH_SERVERBROWSER);
        btnRefresh.addActionListener(uiaListener);
        btnConnect.setActionCommand(UiActionListener.CMD_CONNECT);
        btnConnect.addActionListener(uiaListener);
        btnMenuServerbrowser.setBackground(btn_bg_normal);
        btnMenuServerbrowser.addActionListener(this);
        btnMenuServerbrowser.setActionCommand(UiActionListener.CMD_TOGGLE_SERVERBROWSER);
        btnMenuServerbrowser.addActionListener(uiaListener);
        btnApplyOptionsPlayer.setActionCommand(UiActionListener.CMD_NICKCHANGE);
        btnApplyOptionsPlayer.addActionListener(uiaListener);
        lClientlist.addMouseListener(this);
        tbpLobbyChat.addMouseListener(this);
        tfLobbyChat.addKeyListener(this);

        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((d.width - getSize().width) / 2, (d.height - getSize().height) / 2);
    }

    public void clearClientlist(){
        clientlistOld.clear();
        for(int i=0; i<clientlist.size(); i++){
            clientlistOld.add(clientlist.get(i));
        }
        clientlist.clear();
        listModel.clear();
        repaint();
        pack();
    }

    public void addClientToList(Client client){
        clientlist.add(new PrivateChatTab(client));
        listModel.addElement(client.getPlayer().getName());
        repaint();
        pack();
    }

    public void completeClientlist(){
        System.out.println("completeClientlist() "+(tbpLobbyChat.getComponentCount()-1)+" opened private tabs");
        System.out.print(" sync clientlist with old data...");
        for(int i=0; i<clientlist.size(); i++){
            for(int j=0; j<clientlistOld.size(); j++){
                if(clientlist.get(i).getID() == clientlistOld.get(j).getID()){
                    System.out.print("id"+clientlistOld.get(j).getID()+" ");
                    clientlist.get(i).appendText(clientlistOld.get(j).getTextArea().getText());
                    break;
                }
            }
        }
        System.out.println("done!\n correcting opened tabs...");
        for(int i=1; i<tbpLobbyChat.getComponentCount(); i++){
            if(!tbpLobbyChat.getComponentAt(i).getName().equals("offline")){
                int id = Integer.parseInt(tbpLobbyChat.getComponentAt(i).getName());
                System.out.print("id"+id+" ");
                int index = getClientlistIndex(id, clientlist);
                if(index == -1) { // not in clientlist -> in clientlistOld
                    int indexOld = getClientlistIndex(id, clientlistOld);
                    JTextArea a = new JTextArea();
                    a.setText(clientlistOld.get(indexOld).getTextArea().getText());
                    JScrollPane s = new JScrollPane();
                    s.setName("offline");
                    s.setViewportView(a);
                    tbpLobbyChat.setComponentAt(i, s);
                    tbpLobbyChat.setTitleAt(i, tbpLobbyChat.getTitleAt(i)+ " (offline)");
                } else {
                    String name = clientlist.get(index).getName();
                    if(!name.equals(tbpLobbyChat.getTitleAt(i))) {
                        clientlist.get(index).appendText("\n= Nick changed =\n\n");
                        tbpLobbyChat.setTitleAt(i, name);
                    }
                    tbpLobbyChat.setComponentAt(i, clientlist.get(index).getScrollPane());
                }
            }
        }
        System.out.println("done!");
    }

    private void openPrivateChatTab(int clientID) {
        System.out.print("openPrivateChatTab(...) clientID="+clientID+" -> ");
        System.out.print(""+(tbpLobbyChat.getComponentCount()-1)+" private tabs opened -> ");
        for(int i=1; i<tbpLobbyChat.getComponentCount(); i++){
            System.out.println(" tab"+i+": "+tbpLobbyChat.getTitleAt(i));
            if(tbpLobbyChat.getTitleAt(i).equals(Main.getClientName(clientID))){
                tbpLobbyChat.setSelectedIndex(i);
                System.out.println("tab allready opened -> nothing to do!");
                return;
            }
        }
        System.out.println("specific tab not opened -> opening new tab!");
        tbpLobbyChat.addTab(clientlist.get(clientID).getName(), clientlist.get(clientID).getScrollPane());
        tbpLobbyChat.setToolTipTextAt(tbpLobbyChat.getTabCount()-1, "Doubleclick to close private chat tab.");
        tbpLobbyChat.setSelectedIndex(tbpLobbyChat.getTabCount()-1);
    }

    public void setServerlist(String[][] list) {
        sModel.setServerlist(list);
    }

    public int getSelectedServerlistIndex(){
        return tblServerlist.getSelectedRow();
    }

    public void refreshValue(String v, int row, int col){
        sModel.setValueAt(v, row, col);
        repaint();
        pack();
    }

    private int getClientlistIndex(int clientID, ArrayList<PrivateChatTab> list){
        System.out.print(" getClientlistIndex(...) -> ");
        for(int i=0; i<list.size(); i++) {
            if(list.get(i).getID() == clientID){
                System.out.println("found index:"+i);
                return i;
            }
        }
        System.out.println("no id matched");
        return -1;
    }

    public void enableLobby(boolean b){
        tbpLobbyChat.setEnabled(b);
        tfLobbyChat.setEnabled(b);
        btnSendLobbyChat.setEnabled(b);
        lClientlist.setEnabled(b);
        lblLobbyChatPlayerName.setEnabled(b);
    }

    public void enableOptions(boolean b){
        pnlOptions.setEnabled(b);
    }

    public void appendIncommingMSG(boolean privateMsg, int senderID, int recieverID, String msg){
        System.out.print("appendIncommingMSG(...)");
        // PRIVATE
        if(privateMsg) {
            System.out.print(" (private msg, id"+senderID+" to id"+recieverID+" :"+msg+")");
            if(recieverID == Main.getGameData().getSelfId()) {
                System.out.println(" (recieverID==SefID)");
//                System.out.println("msg to this client -> from id"+senderID);
                openPrivateChatTab(getClientlistIndex(senderID, clientlist));
                clientlist.get(getClientlistIndex(senderID, clientlist)).getTextArea().append(Main.getClientName(senderID) + ": " + msg + "\n");
            } else {
                System.out.println("");
//                System.out.println("msg to other client -> to id"+recieverID);
                clientlist.get(getClientlistIndex(recieverID, clientlist)).getTextArea().append(Main.getClientName(senderID) + ": " + msg + "\n");
            }
        }
        // PUBLIC
        else {
            System.out.println(" (public msg, senderID="+senderID+")");
            taLobbyChatPublic.append(Main.getClientName(senderID) + ": " + msg + "\n");
        }
    }

    public void setPlayerName(String name){
        tfPlayerName.setText(name);
        lblLobbyChatPlayerName.setText(" "+name);
    }

    public String getPlayerName(){
        return tfPlayerName.getText();
    }

    public void sendLobbyMsg(){
        if(!tfLobbyChat.getText().equals("")){
            // PRIVATE LOBBY CHAT
            if(tbpLobbyChat.getSelectedIndex()>0){
                Main.getNetwork().send(Network.MASTERHOST,
                                       Network.MASTERPORT,
                                       ProtocolCmd.CLIENT_MASTER_CHAT_PRIVATE,
                                       argInt(Integer.parseInt(tbpLobbyChat.getSelectedComponent().getName())),
                                       argStr(tfLobbyChat.getText()));
            }
            // PUBLIC LOBBY CHAT
            else {
                if(tfLobbyChat.getText() != null && !tfLobbyChat.getText().equals(""))
                Main.getNetwork().send(Network.MASTERHOST,
                                       Network.MASTERPORT,
                                       ProtocolCmd.CLIENT_MASTER_CHAT_LOBBY,
                                       argStr(tfLobbyChat.getText()));
            }
            tfLobbyChat.setText("");
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlServerbrowser = new javax.swing.JPanel();
        scpServerlist = new javax.swing.JScrollPane();
        tblServerlist = new javax.swing.JTable();
        btnRefresh = new javax.swing.JButton();
        btnConnect = new javax.swing.JButton();
        pnlOptions = new javax.swing.JPanel();
        pnlOptionsPlayer = new javax.swing.JPanel();
        tfPlayerName = new javax.swing.JTextField();
        lblPlayerName = new javax.swing.JLabel();
        btnApplyOptionsPlayer = new javax.swing.JButton();
        pnlOptionsNetwork = new javax.swing.JPanel();
        tfMasterserverIP = new javax.swing.JTextField();
        lblMasterserverIP = new javax.swing.JLabel();
        lblPassword = new javax.swing.JLabel();
        tfUsername = new javax.swing.JTextField();
        lblUsername = new javax.swing.JLabel();
        pfPassword = new javax.swing.JPasswordField();
        btnApplyOptionsNetwork = new javax.swing.JButton();
        pnlOptionsGameplay = new javax.swing.JPanel();
        pnlMenu = new javax.swing.JPanel();
        btnMenuLobby = new javax.swing.JButton();
        btnMenuServerbrowser = new javax.swing.JButton();
        btnMenuOptions = new javax.swing.JButton();
        btnMenuExit = new javax.swing.JButton();
        pnlLobbyChat = new javax.swing.JPanel();
        sppLobbyChat = new javax.swing.JSplitPane();
        pnlLobbyChatClientList = new javax.swing.JPanel();
        scpClientlist = new javax.swing.JScrollPane();
        lClientlist = new javax.swing.JList();
        lblLobbyChatPlayerName = new javax.swing.JLabel();
        pnlLobbyChatTabs = new javax.swing.JPanel();
        tfLobbyChat = new javax.swing.JTextField();
        btnSendLobbyChat = new javax.swing.JButton();
        tbpLobbyChat = new javax.swing.JTabbedPane();
        scpLobbyChat = new javax.swing.JScrollPane();
        taLobbyChatPublic = new javax.swing.JTextArea();

        tblServerlist.setFont(new java.awt.Font("Tahoma", 0, 10));
        tblServerlist.setModel(sModel);
        tblServerlist.getTableHeader().setReorderingAllowed(false);
        scpServerlist.setViewportView(tblServerlist);

        btnRefresh.setFont(new java.awt.Font("Tahoma", 0, 10));
        btnRefresh.setText("Aktualisieren");

        btnConnect.setFont(new java.awt.Font("Tahoma", 0, 10));
        btnConnect.setText("Verbinden");

        javax.swing.GroupLayout pnlServerbrowserLayout = new javax.swing.GroupLayout(pnlServerbrowser);
        pnlServerbrowser.setLayout(pnlServerbrowserLayout);
        pnlServerbrowserLayout.setHorizontalGroup(
            pnlServerbrowserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlServerbrowserLayout.createSequentialGroup()
                .addGap(448, 448, 448)
                .addComponent(btnRefresh, javax.swing.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnConnect)
                .addContainerGap())
            .addComponent(scpServerlist, javax.swing.GroupLayout.DEFAULT_SIZE, 642, Short.MAX_VALUE)
        );
        pnlServerbrowserLayout.setVerticalGroup(
            pnlServerbrowserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlServerbrowserLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(scpServerlist, javax.swing.GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(pnlServerbrowserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnConnect)
                    .addComponent(btnRefresh))
                .addContainerGap())
        );

        pnlOptionsPlayer.setBorder(javax.swing.BorderFactory.createTitledBorder("Spieler"));

        lblPlayerName.setText("Spielername");

        btnApplyOptionsPlayer.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        btnApplyOptionsPlayer.setText("Übernehmen");

        javax.swing.GroupLayout pnlOptionsPlayerLayout = new javax.swing.GroupLayout(pnlOptionsPlayer);
        pnlOptionsPlayer.setLayout(pnlOptionsPlayerLayout);
        pnlOptionsPlayerLayout.setHorizontalGroup(
            pnlOptionsPlayerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlOptionsPlayerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlOptionsPlayerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlOptionsPlayerLayout.createSequentialGroup()
                        .addComponent(lblPlayerName)
                        .addGap(29, 29, 29)
                        .addComponent(tfPlayerName, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE))
                    .addComponent(btnApplyOptionsPlayer, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        pnlOptionsPlayerLayout.setVerticalGroup(
            pnlOptionsPlayerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlOptionsPlayerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlOptionsPlayerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPlayerName)
                    .addComponent(tfPlayerName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 110, Short.MAX_VALUE)
                .addComponent(btnApplyOptionsPlayer)
                .addContainerGap())
        );

        pnlOptionsNetwork.setBorder(javax.swing.BorderFactory.createTitledBorder("Netzwerk"));

        tfMasterserverIP.setText("xxx.xxx.xxx.xxx:xxxxx");

        lblMasterserverIP.setText("Masterserver");

        lblPassword.setText("Passwort");

        lblUsername.setText("Benutzername");

        pfPassword.setText("jPasswordField1");

        btnApplyOptionsNetwork.setFont(new java.awt.Font("Tahoma", 0, 10));
        btnApplyOptionsNetwork.setText("Übernehmen");

        javax.swing.GroupLayout pnlOptionsNetworkLayout = new javax.swing.GroupLayout(pnlOptionsNetwork);
        pnlOptionsNetwork.setLayout(pnlOptionsNetworkLayout);
        pnlOptionsNetworkLayout.setHorizontalGroup(
            pnlOptionsNetworkLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlOptionsNetworkLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlOptionsNetworkLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlOptionsNetworkLayout.createSequentialGroup()
                        .addComponent(lblMasterserverIP)
                        .addGap(23, 23, 23)
                        .addComponent(tfMasterserverIP, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE))
                    .addGroup(pnlOptionsNetworkLayout.createSequentialGroup()
                        .addGroup(pnlOptionsNetworkLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblUsername)
                            .addComponent(lblPassword))
                        .addGap(18, 18, 18)
                        .addGroup(pnlOptionsNetworkLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pfPassword, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)
                            .addComponent(tfUsername, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)))
                    .addComponent(btnApplyOptionsNetwork, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        pnlOptionsNetworkLayout.setVerticalGroup(
            pnlOptionsNetworkLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlOptionsNetworkLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlOptionsNetworkLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfMasterserverIP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblMasterserverIP))
                .addGap(18, 18, 18)
                .addGroup(pnlOptionsNetworkLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblUsername))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlOptionsNetworkLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pfPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPassword))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 46, Short.MAX_VALUE)
                .addComponent(btnApplyOptionsNetwork)
                .addContainerGap())
        );

        pnlOptionsGameplay.setBorder(javax.swing.BorderFactory.createTitledBorder("Spieleinstellungen"));

        javax.swing.GroupLayout pnlOptionsGameplayLayout = new javax.swing.GroupLayout(pnlOptionsGameplay);
        pnlOptionsGameplay.setLayout(pnlOptionsGameplayLayout);
        pnlOptionsGameplayLayout.setHorizontalGroup(
            pnlOptionsGameplayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 280, Short.MAX_VALUE)
        );
        pnlOptionsGameplayLayout.setVerticalGroup(
            pnlOptionsGameplayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 158, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout pnlOptionsLayout = new javax.swing.GroupLayout(pnlOptions);
        pnlOptions.setLayout(pnlOptionsLayout);
        pnlOptionsLayout.setHorizontalGroup(
            pnlOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlOptionsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(pnlOptionsGameplay, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlOptionsPlayer, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(pnlOptionsNetwork, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlOptionsLayout.setVerticalGroup(
            pnlOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlOptionsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(pnlOptionsNetwork, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlOptionsPlayer, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlOptionsGameplay, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("J.A.F.S.");

        pnlMenu.setLayout(new java.awt.GridLayout(1, 4));

        btnMenuLobby.setFont(new java.awt.Font("Verdana", 1, 18));
        btnMenuLobby.setText("Lobby");
        pnlMenu.add(btnMenuLobby);

        btnMenuServerbrowser.setFont(new java.awt.Font("Verdana", 1, 18));
        btnMenuServerbrowser.setText("Serverbrowser");
        pnlMenu.add(btnMenuServerbrowser);

        btnMenuOptions.setFont(new java.awt.Font("Verdana", 1, 18));
        btnMenuOptions.setText("Optionen");
        pnlMenu.add(btnMenuOptions);

        btnMenuExit.setFont(new java.awt.Font("Verdana", 1, 18));
        btnMenuExit.setText("Verlassen");
        pnlMenu.add(btnMenuExit);

        pnlLobbyChat.setAlignmentY(0.0F);
        pnlLobbyChat.setLayout(new java.awt.BorderLayout());

        sppLobbyChat.setBorder(null);
        sppLobbyChat.setDividerLocation(610);
        sppLobbyChat.setDividerSize(3);

        pnlLobbyChatClientList.setAlignmentX(0.0F);
        pnlLobbyChatClientList.setAlignmentY(0.0F);

        lClientlist.setModel(listModel);
        lClientlist.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lClientlist.setToolTipText("Doubleclick to open private chat tab.");
        lClientlist.setAlignmentX(0.0F);
        lClientlist.setAlignmentY(0.0F);
        lClientlist.setEnabled(false);
        scpClientlist.setViewportView(lClientlist);

        lblLobbyChatPlayerName.setEnabled(false);

        javax.swing.GroupLayout pnlLobbyChatClientListLayout = new javax.swing.GroupLayout(pnlLobbyChatClientList);
        pnlLobbyChatClientList.setLayout(pnlLobbyChatClientListLayout);
        pnlLobbyChatClientListLayout.setHorizontalGroup(
            pnlLobbyChatClientListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scpClientlist, javax.swing.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)
            .addComponent(lblLobbyChatPlayerName, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)
        );
        pnlLobbyChatClientListLayout.setVerticalGroup(
            pnlLobbyChatClientListLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlLobbyChatClientListLayout.createSequentialGroup()
                .addComponent(lblLobbyChatPlayerName, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scpClientlist, javax.swing.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE))
        );

        sppLobbyChat.setRightComponent(pnlLobbyChatClientList);

        tfLobbyChat.setEnabled(false);
        tfLobbyChat.setMinimumSize(new java.awt.Dimension(6, 21));
        tfLobbyChat.setPreferredSize(new java.awt.Dimension(6, 21));

        btnSendLobbyChat.setFont(new java.awt.Font("Tahoma", 0, 10));
        btnSendLobbyChat.setText("Senden");
        btnSendLobbyChat.setEnabled(false);
        btnSendLobbyChat.setMaximumSize(new java.awt.Dimension(67, 20));
        btnSendLobbyChat.setMinimumSize(new java.awt.Dimension(67, 20));
        btnSendLobbyChat.setPreferredSize(new java.awt.Dimension(67, 20));

        tbpLobbyChat.setEnabled(false);

        taLobbyChatPublic.setColumns(20);
        taLobbyChatPublic.setEditable(false);
        taLobbyChatPublic.setRows(5);
        scpLobbyChat.setViewportView(taLobbyChatPublic);

        tbpLobbyChat.addTab("Öffentlich", scpLobbyChat);

        javax.swing.GroupLayout pnlLobbyChatTabsLayout = new javax.swing.GroupLayout(pnlLobbyChatTabs);
        pnlLobbyChatTabs.setLayout(pnlLobbyChatTabsLayout);
        pnlLobbyChatTabsLayout.setHorizontalGroup(
            pnlLobbyChatTabsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlLobbyChatTabsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tfLobbyChat, javax.swing.GroupLayout.DEFAULT_SIZE, 508, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSendLobbyChat, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addComponent(tbpLobbyChat, javax.swing.GroupLayout.DEFAULT_SIZE, 610, Short.MAX_VALUE)
        );
        pnlLobbyChatTabsLayout.setVerticalGroup(
            pnlLobbyChatTabsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlLobbyChatTabsLayout.createSequentialGroup()
                .addComponent(tbpLobbyChat, javax.swing.GroupLayout.DEFAULT_SIZE, 415, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlLobbyChatTabsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfLobbyChat, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnSendLobbyChat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        sppLobbyChat.setLeftComponent(pnlLobbyChatTabs);

        pnlLobbyChat.add(sppLobbyChat, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlMenu, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pnlLobbyChat, javax.swing.GroupLayout.DEFAULT_SIZE, 732, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pnlMenu, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlLobbyChat, javax.swing.GroupLayout.DEFAULT_SIZE, 442, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnApplyOptionsNetwork;
    private javax.swing.JButton btnApplyOptionsPlayer;
    private javax.swing.JButton btnConnect;
    private javax.swing.JButton btnMenuExit;
    private javax.swing.JButton btnMenuLobby;
    private javax.swing.JButton btnMenuOptions;
    private javax.swing.JButton btnMenuServerbrowser;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnSendLobbyChat;
    private javax.swing.JList lClientlist;
    private javax.swing.JLabel lblLobbyChatPlayerName;
    private javax.swing.JLabel lblMasterserverIP;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JLabel lblPlayerName;
    private javax.swing.JLabel lblUsername;
    private javax.swing.JPasswordField pfPassword;
    private javax.swing.JPanel pnlLobbyChat;
    private javax.swing.JPanel pnlLobbyChatClientList;
    private javax.swing.JPanel pnlLobbyChatTabs;
    private javax.swing.JPanel pnlMenu;
    private javax.swing.JPanel pnlOptions;
    private javax.swing.JPanel pnlOptionsGameplay;
    private javax.swing.JPanel pnlOptionsNetwork;
    private javax.swing.JPanel pnlOptionsPlayer;
    private javax.swing.JPanel pnlServerbrowser;
    private javax.swing.JScrollPane scpClientlist;
    private javax.swing.JScrollPane scpLobbyChat;
    private javax.swing.JScrollPane scpServerlist;
    private javax.swing.JSplitPane sppLobbyChat;
    private javax.swing.JTextArea taLobbyChatPublic;
    private javax.swing.JTable tblServerlist;
    private javax.swing.JTabbedPane tbpLobbyChat;
    private javax.swing.JTextField tfLobbyChat;
    private javax.swing.JTextField tfMasterserverIP;
    private javax.swing.JTextField tfPlayerName;
    private javax.swing.JTextField tfUsername;
    // End of variables declaration//GEN-END:variables
    private Color btn_bg_normal = new Color(238, 238, 238);
    private Color btn_bg_aktive = new Color(200, 221, 242);
    private ServerbrowserTableModel sModel;
    private DefaultListModel listModel;


    public void actionPerformed(ActionEvent e) {
        pnlLobbyChat.removeAll();
        btnMenuLobby.setBackground(btn_bg_normal);
        btnMenuOptions.setBackground(btn_bg_normal);
        btnMenuExit.setBackground(btn_bg_normal);
        btnMenuServerbrowser.setBackground(btn_bg_normal);

        // Lobby
        if(e.getSource() == btnMenuLobby){
            btnMenuLobby.setBackground(btn_bg_aktive);
            pnlLobbyChat.add(sppLobbyChat, BorderLayout.CENTER);
        }
        // Serverbrowser
        else if(e.getSource() == btnMenuServerbrowser){
            btnMenuServerbrowser.setBackground(btn_bg_aktive);
            pnlLobbyChat.add(pnlServerbrowser, BorderLayout.CENTER);
        }
        // Optionen
        else if(e.getSource() == btnMenuOptions){
            btnMenuOptions.setBackground(btn_bg_aktive);
            pnlLobbyChat.add(pnlOptions, BorderLayout.CENTER);
        }
        // Verlassen
        else if(e.getSource() == btnMenuExit){
            btnMenuExit.setBackground(btn_bg_aktive);
            System.exit(0);
        }
        repaint();
        pack();
    }


    public void mouseClicked(MouseEvent e) {
        // Open private tab
        if (e.getSource() == lClientlist && !listModel.isEmpty() && e.getClickCount() == 2) {
            openPrivateChatTab(lClientlist.locationToIndex(e.getPoint()));
        }
        // Close private tab
        else if (e.getSource() == tbpLobbyChat && tbpLobbyChat.getSelectedIndex() != 0 && e.getClickCount() == 2) {
            tbpLobbyChat.remove(tbpLobbyChat.getSelectedIndex());
        }
    }

    public void mousePressed(MouseEvent e) {  }

    public void mouseReleased(MouseEvent e) {  }

    public void mouseEntered(MouseEvent e) {  }

    public void mouseExited(MouseEvent e) {  }

    
    public void keyTyped(KeyEvent e) {  }

    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ENTER){
            sendLobbyMsg();
        }
    }

    public void keyReleased(KeyEvent e) {  }


    /**
     *
     * @author Julian Sanio
     */
    public class PrivateChatTab {

        private Client cl;
        private JTextArea txt;
        private JScrollPane sp;

        public PrivateChatTab(Client client){
            cl = client;
            txt = new JTextArea();
            txt.setEditable(false);
            sp = new JScrollPane();
            sp.setName(""+client.getId());
            sp.setViewportView(txt);
            DefaultCaret caret = (DefaultCaret)txt.getCaret();
            caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        }

        public int getID(){ return cl.getId(); }

        public String getName(){ return cl.getPlayer().getName(); }

        public JTextArea getTextArea(){ return txt; }

        public void appendText(String s){ txt.append(s);}

        public JScrollPane getScrollPane(){ return sp; }

        public Client getClient(){ return cl; }
    }


    /**
     *
     * @author Julian Sanio
     */
    public class ServerbrowserTableModel extends AbstractTableModel {

        private String[] columnNames = { "Server", "Map", "Spieler", "Ping" };
        private String[][] serverList = new String [][] { {"No Masterserver.", "", "", ""} };

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return serverList.length;
        }

        public void setValueAt(String value, int row, int col) {
            serverList[row][col] = value;
        }

        public String getValueAt(int row, int col) {
            return (String)serverList[row][col];
        }

        public void setServerlist(String[][] list) {
            this.serverList = list;
        }

        public String[][] getServerlist() {
            return serverList;
        }
    }
}
