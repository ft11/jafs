/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package masterserver;

import common.net.Network;
import common.net.Protocol;
import common.net.Server;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author miracle
 */
public class Main {
    public static int maxPingFailures = 2;
    public static final int pingRefreshInterval = 10000;

    private static Network net;
    private static ProtocolHandler handler;
    private static ArrayList<Server> serverlist = new ArrayList<Server>();

    private static TimerTask pinger = new TimerTask() {
        public void run() {
            int failures;
            for(int i=0;i<serverlist.size();i++) {
                serverlist.get(i).increasePingFailureCnt();
                failures = serverlist.get(i).getPingFailureCnt();

                if(failures >= maxPingFailures) {
                    removeServer(serverlist.get(i));
                    continue;
                }

                net.send(serverlist.get(i).getAddress(), Protocol.master_server_ping, new Object[0]);
            }
        }
    };
    private static Timer pingTimer;

    public static void main(String[] args) {
        net = new Network();
        net.listen(Network.masterPort);
        handler = new ProtocolHandler(net);
        Protocol.init();

        pingTimer = new Timer();
        pingTimer.schedule(pinger, pingRefreshInterval, pingRefreshInterval);
    }

    public static Server addServer(InetSocketAddress adr) {
        for(Server server : serverlist) {
            if(server.getAddress().equals(adr))
                return null;
        }
        Server server = new Server(adr);
        server.setServerId(serverlist.size());
        serverlist.add(server);
        System.out.println("Server " + server.getHost() + ":" + server.getPort() + " listed." );
        
        return server;
    }

    public static void removeServer(Server server) {
        serverlist.remove(server);

        System.out.println("Server " + server.getHost() + ":" + server.getPort() + " dropped." );
    }

    public static Server getServer(String host, int port) {
        for(Server cur : serverlist) {
            if(cur.getHost().equals(host) && cur.getPort() == port)
                return cur;
        }

        return null;
    }

    public static Server getServer(InetSocketAddress adr) {
        for(Server cur : serverlist) {
            if(cur.getAddress().equals(adr))
                return cur;
        }

        return null;
    }

    public static String[] getServerlist() {
        String[] srv = new String[serverlist.size()];
        int i = 0;
        for(Server current : serverlist) {
            srv[i] = current.getHost() + ":" + current.getPort();
            i++;
        }

        return srv;
    }

    public static void decreasePingFailures(InetSocketAddress adr) {
        Server serv = getServer(adr);

        if(serv != null) {
            serv.decreasePingFailureCnt();
        }
    }
    public static int serverCount(){
        return serverlist.size();
    }
}