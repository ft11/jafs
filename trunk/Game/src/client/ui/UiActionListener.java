package client.ui;

import client.Main;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author Julia Sanio
 */
public class UiActionListener implements ActionListener {

    // Command list
    public static final String CMD_TOGGLE_SERVERBROWSER =   "1";
    public static final String CMD_TOGGLE_OPTIONS =         "2";
    public static final String CMD_TOGGLE_CREDITS =         "3";
    public static final String CMD_TOGGLE_CREATESERVER =    "4";
    public static final String CMD_EXIT =                   "5";
    public static final String CMD_CONNECT =                "6";
    public static final String CMD_REFRESH_SERVERBROWSER =  "7";

    public void actionPerformed(ActionEvent e) {
//        System.out.println(e.getActionCommand() + " (actionPerformed by " + e.getSource().getClass().getSimpleName() + ")");
        // Server erstellen
        if(e.getActionCommand().equals(CMD_TOGGLE_CREATESERVER)) {
            Main.getUiCreateServer().setVisible(Main.getUiCreateServer().isVisible()?false:true);
        }
        // Serverbrowser
        else if(e.getActionCommand().equals(CMD_TOGGLE_SERVERBROWSER)) {
            Main.getUiServerbrowser().setVisible(Main.getUiServerbrowser().isVisible()?false:true);
        }
        // Options
        else if(e.getActionCommand().equals(CMD_TOGGLE_OPTIONS)) {
            Main.getUiOptions().setVisible(Main.getUiOptions().isVisible()?false:true);
        }
        // Serverliste aktualisieren
        else if(e.getActionCommand().equals(CMD_REFRESH_SERVERBROWSER)) {
            String[][] s = {{"127.0.0.1", "dust", "0/16", "500"},   // !!! SERVERLIST INPUT?
                            {"192.0.0.1", "italy", "2/16", "80"},
                            {"162.0.0.1", "aztec", "5/16", "63"}};
            Main.getUiServerbrowser().setServerlist(s);
        }
        // Mit Server verbinden
        else if(e.getActionCommand().equals(CMD_CONNECT)) {
            if(Main.getUiServerbrowser().getSelectedServer() != null)
                System.out.println(Main.getUiServerbrowser().getSelectedServer());
        }
        // Credits
        else if(e.getActionCommand().equals(CMD_TOGGLE_CREDITS)) {
            Main.getUiCredits().setVisible(Main.getUiCredits().isVisible()?false:true);
        }
        // Exit
        else if(e.getActionCommand().equals(CMD_EXIT)) {
            System.exit(0);
        }
    }
}