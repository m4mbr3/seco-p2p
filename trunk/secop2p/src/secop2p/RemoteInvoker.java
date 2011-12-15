/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package secop2p;

/**
 *
 * @author Nguyen Ho
 * Description: Building a http request to remote service
 */
public class RemoteInvoker implements HttpInvoker {

             /** Logger. */
            private static final Logger LOG = LoggerFactory.getLogger(RemoteInvoker.class);
            ServiceRepository sr = new ServiceRepository();
            public RemoteInvoker ()
            {

            }
}
