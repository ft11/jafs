/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package client;

import common.net.Network;

/**
 *
 * @author miracle
 */
public class ProtocolHandler extends common.net.ProtocolHandler {
    public ProtocolHandler(Network net) {
        super(net);
    }

    public void test(String a, Integer b, Double c) {
        System.out.println(a + " " + b + " " + c);
    }
}
