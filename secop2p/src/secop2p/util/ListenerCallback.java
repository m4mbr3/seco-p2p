/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package secop2p.util;

import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author eros
 */
public interface ListenerCallback {

    public void handleRequest( InputStream in, OutputStream out );

}
