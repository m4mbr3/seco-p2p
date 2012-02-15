/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package secop2p;
import java.sql.SQLException;
import java.util.Set;

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
        Set<Service> serviceList = serviceRepository.getServicesMappedToEngine(engine);
        for(Service service : serviceList)
        {
            if(service.getId()==serviceId)
            {
                isLocal=true;
                break;
            }
        }
        return isLocal;
    }
}
