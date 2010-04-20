package server;

import common.net.Client;
import common.net.Network;
import common.net.Packet;
import common.net.Protocol;
import common.net.ProtocolCmd;
import java.net.InetSocketAddress;

import static common.net.ProtocolCmdArgument.*;

/**
 *
 * @author miracle
 */
public class ProtocolHandler extends common.net.ProtocolHandler {
    public ProtocolHandler(Network net) {
        super(net, ProtocolHandler.MODE_SERVER);
    }

    public void m_s_ping(InetSocketAddress adr) {
        net.send(adr, ProtocolCmd.SERVER_MASTER_PONG);
    }

    public void m_s_servercount(int i, InetSocketAddress adr){
        Main.setServerId(i);
    }

    public void m_s_auth_reply(int i, InetSocketAddress adr) {
        if(i == Protocol.REPLY_SUCCESS)
            System.out.println("MASTER_SERVER_AUTH_REPLY success, server listed");
//            System.out.println("server succesfully listed.");
        else
            System.out.println("MASTER_SERVER_AUTH_REPLY failure");
//            System.out.println("server failed to be listed.");
    }

    public void c_s_auth(InetSocketAddress adr){
        Client added = Main.addClient(adr);

        if(added != null) {
            net.send(adr, ProtocolCmd.SERVER_CLIENT_AUTH_REPLY, argInt(Protocol.REPLY_SUCCESS));
            net.send(adr, ProtocolCmd.SERVER_CLIENT_INIT, argStr(Main.getMapName()),
                     argInt(Main.getMaxPlayers()));
//            System.out.println("Client "+added.getHost()+":"+added.getPort()+" joined server");
            System.out.println("CLIENT_SERVER_AUTH success -> SERVER_CLIENT_AUTH_REPLY (REPLY_SUCCESS), SERVER_CLIENT_INIT");
        } else {
            net.send(adr, ProtocolCmd.SERVER_CLIENT_AUTH_REPLY, argInt(Protocol.REPLY_FAILURE));
//            System.out.println("Client "+added.getHost()+":"+added.getPort()+" not able joined server");
            System.out.println("CLIENT_SERVER_AUTH failure -> SERVER_CLIENT_AUTH_REPLY (REPLY_FAILURE)");
        }
    }

    public void c_s_init_reply(int i, InetSocketAddress adr) {
        if(i == Protocol.REPLY_SUCCESS) {
            net.send(adr, ProtocolCmd.SERVER_CLIENT_REQUEST_NAME);
            System.out.println("CLIENT_SERVER_INIT_REPLY success -> SERVER_CLIENT_REQUEST_NAME");
        } else {
            Main.removeClient(adr);
            System.out.println("CLIENT_SERVER_INIT_REPLY failure -> client removed");
        }
    }

    public void c_s_request_name_reply(String name, InetSocketAddress adr) {
        boolean changed = false;

        while(Main.nameExists(name)) {
            name = name.concat("*");
            changed = true;
        }

        if(changed)     
            net.send(adr, ProtocolCmd.SERVER_CLIENT_FORCED_NICKCHANGE, argStr(name));

        Client c = Main.getClient(adr);

        c.getPlayer().setName(name);

        c.setStatus(Client.STATUS_CONNECTED);

        int team = c.getTeamId();
        int id = c.getId();

        net.send(adr, ProtocolCmd.SERVER_CLIENT_PLAYER_DATA,
                argStr(name), argInt(id), argInt(team));

        net.send(adr, ProtocolCmd.SERVER_CLIENT_CONNECTION_ESTABLISHED);

        Main.broadcast(ProtocolCmd.SERVER_CLIENT_EVENT_PLAYER_JOINED,
                        argStr(c.getPlayer().getName()), argInt(c.getId()));

        System.out.println("CLIENT_SERVER_REQUEST_NAME_REPLY "+name+"(no SERVER_CLIENT_FORCED_NICKCHANGE) -> SERVER_CLIENT_CONNECTION_ESTABLISHED");
    }

    public void c_s_connection_established_ok(InetSocketAddress adr) {

    }

    public void c_s_forced_nickchange_ok(InetSocketAddress adr) {

    }

    public void c_s_request_server_info(InetSocketAddress adr) {
        String name = Main.getServerName();
        String map = Main.getMapName();
        int cur = Main.getCurPlayers();
        int max = Main.getMaxPlayers();

        net.send(adr, ProtocolCmd.SERVER_CLIENT_REQUEST_SERVER_INFO_REPLY,
                argStr(name), argStr(map), argInt(cur), argInt(max));
    }

    public void c_s_all_player_data(InetSocketAddress adr) {
        Client[] c = Main.getClients();

        for(Client client : c) {
            if(client != null && !client.getAddress().equals(adr)) {
                String name = client.getPlayer().getName();
                int team = client.getTeamId();
                int id = client.getId();

                net.send(adr, ProtocolCmd.SERVER_CLIENT_PLAYER_DATA,
                        argStr(name), argInt(id), argInt(team));
            }
        }

        net.send(adr, ProtocolCmd.SERVER_CLIENT_ALL_PLAYER_DATA_OK);
    }

    public void c_s_player_info(int id, int wep, double posX, double posY,
                                double dirX, double dirY, InetSocketAddress adr) {
        Client c = Main.getClient(id);

        if(c != null) {
            c.getPlayer().setCurrentWeapon(wep);
            c.getPlayer().setPosition(posX, posY);
            c.getPlayer().setDirection(dirX, dirY);
        }
    }

    public void c_s_player_data_ok(InetSocketAddress adr) {
    }

    public void c_s_pong(InetSocketAddress adr) {
//        System.out.println("CLIENT_SERVER_PONG");
    }

    public void c_s_clientcount(InetSocketAddress adr){
        net.send(adr, ProtocolCmd.SERVER_CLIENT_CLIENTCOUNT,
                 argInt(Main.clientCount()));
    }

    public void c_s_clientid(InetSocketAddress adr){
        net.send(adr, ProtocolCmd.SERVER_CLIENT_CLIENTID_REPLY,
                 argInt(Main.getClient(adr).getId()));
    }

    public void c_s_logoff(InetSocketAddress adr){
        System.out.println("CLIENT_SERVER_LOGOFF");
        Main.removeClient(adr);
        net.send(adr, ProtocolCmd.SERVER_CLIENT_LOGOFF_REPLY,
                 argInt(Protocol.REPLY_SUCCESS));
    }

    // --- chat fkt
    public void c_s_chat_all(String msg, InetSocketAddress adr){
        Main.broadcast_chat(msg, adr);
//        net.send(adr, Protocol.server_client_chat, "(PUBLiC-CHAT) Player-"+Main.getClientId(adr)+" ("+adr.getHostName()+":"+adr.getPort()+"): "+msg);
    }

    // team wird aus der clientlist geholt, muss nicht übergeben werden
    public void c_s_chat_team(String msg, InetSocketAddress adr){
        Main.broadcast_chat_team(msg, adr);
    }
    
    public void c_s_chat_private(String msg, int to, InetSocketAddress adr){
        Main.broadcast_chat_private(msg, to, adr);
    }

    // --- chat end
    public void c_s_jointeam(int teamId, InetSocketAddress adr){
        if(Main.setClientTeamId(adr, teamId) == -1) {
            net.send(adr, ProtocolCmd.SERVER_CLIENT_JOINTEAM_REPLY,
                    argInt(Protocol.REPLY_FAILURE));
            System.out.println("CLIENT_SERVER_JOINTEAM failure -> SERVER_CLIENT_JOINTEAM_REPLY (failure)");
        }
        else {
            Client c = Main.getClient(adr);

            if(c != null) {

                if(Main.getClients().length % 2 == 1)
                    c.setTeamId(0);
                else
                    c.setTeamId(1);

//                c.setTeamId(teamId);
                net.send(adr, ProtocolCmd.SERVER_CLIENT_JOINTEAM_REPLY,
                        argInt(Protocol.REPLY_SUCCESS), argInt(teamId));

                Main.broadcast(ProtocolCmd.SERVER_CLIENT_EVENT_PLAYER_TEAM_CHANGED,
                        argInt(c.getId()) , argInt(teamId));
                System.out.println("CLIENT_SERVER_JOINTEAM success id="+teamId+" -> SERVER_CLIENT_JOINTEAM_REPLY (success)");
            }
        }
    }

    public void c_s_latency(InetSocketAddress adr){
        net.send(adr, ProtocolCmd.SERVER_CLIENT_LATENCY_REPLY);
    }

    public void c_s_current_map(InetSocketAddress adr){
        net.send(adr, ProtocolCmd.SERVER_CLIENT_CURRENT_MAP_REPLY, argStr(Main.getMapName()));
    }

    public void c_s_players(InetSocketAddress adr){
        net.send(adr, ProtocolCmd.SERVER_CLIENT_PLAYERS_REPLY, argStr(Main.getCurPlayers()+"/"+Main.getMaxPlayers()));
    }

    public void noReplyReceived(Packet p) {
        if(Protocol.getCmdById(p.getCmd()) == ProtocolCmd.SERVER_CLIENT_PING) {
            Main.removeClient(p.getAddress());
        }
    }   
}
