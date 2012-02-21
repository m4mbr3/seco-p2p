/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.seco.qp.engine.routing.util;

import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.SocketChannel;

/**
 *
 * @author eros
 */
public class MessageStreamEnd implements Serializable {
    public static void closeSocketChannel(SocketChannel sc){
        try{
            sc.socket().shutdownInput();
        }catch(IOException e){}
        try{
            sc.socket().getInputStream().close();
        }catch(IOException e){}
        try{
            sc.socket().getOutputStream().close();
        }catch(IOException e){}
        try{
            sc.socket().shutdownOutput();
        }catch(IOException e){}
        try{
            sc.socket().close();
        }catch(IOException e){}
        try{
            sc.close();
        }catch(IOException e){}
    }
}
