/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package secop2p;

import java.sql.SQLException;

/**
 *
 * @author Nguyen Ho
 * Description: Building a http request to remote service
 */
public class RemoteInvoker implements HttpInvoker {

             /** Logger. */
            private static final Logger LOG = LoggerFactory.getLogger(RemoteInvoker.class);
            ServiceRepository sr ;
            public RemoteInvoker () throws ClassNotFoundException
            {
                try{
                        sr = new ServiceRepository();
                }
                catch(SQLException e)
                {

                }
                catch(ClassNotFoundException e)
                {

                }
            }
}
