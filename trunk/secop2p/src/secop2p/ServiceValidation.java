/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package secop2p;
import java.sql.SQLException;

/**
 *
 * @author Nguyen Ho
 * Description: Check whether a service is local or not
 */
public class ServiceValidation {
    //
    public boolean isServiceLocal(int serviceId, int localEngineId) throws SQLException, ClassNotFoundException
    {
        boolean isLocal = false;
        ServiceRepository serviceRepository = new ServiceRepository();
        EngineInfo engine = serviceRepository.getEngineById(localEngineId);
        Service[] serviceList = serviceRepository.getServicesMappedToEngine(engine);
        for(int i=0; i<serviceList.length; i++)
        {
            Service service = serviceList[i];
            if(service.getId()==serviceId)
            {
                isLocal=true;
                break;
            }
        }
        return isLocal;
    }
}
