/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.seco.qp.engine.routing.util;

import java.nio.channels.SocketChannel;

/**
 *
 * @author eros
 */
public interface ListenerCallback {

    public void handleRequest( SocketChannel client );

}
