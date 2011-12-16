/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package secop2p;

import java.sql.SQLException;
import java.util.logging.Logger;

/**
 *
 * @author Nguyen Ho
 * Description: Building a http request to remote service
 */
public class RemoteInvoker /*implements HttpInvoker*/ {

    /** Logger. */
    private static final Logger LOG = Logger.getLogger(RemoteInvoker.class.getName());
    private final ServiceRepository sr ;

    public RemoteInvoker () throws ClassNotFoundException, SQLException {
        sr = new ServiceRepository();
    }

}
