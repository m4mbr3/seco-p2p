/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package secop2p;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author eros
 */
public class ServiceRepository {

    private final static String DB_CONN = "jdbc:sqlite:ServiceRepository.sqlite";
    private final Connection conn;
    static private PreparedStatement selectServicesList;
    static private PreparedStatement selectEnginesList;
    static private PreparedStatement selectRelationList;
    static private PreparedStatement selectEngineServices;
    static private PreparedStatement selectServiceEngines;
    static private PreparedStatement addService;
    static private PreparedStatement addEngine;
    static private PreparedStatement addRelation;
    static private PreparedStatement delService;
    static private PreparedStatement delServiceEngines;
    static private PreparedStatement delEngine;
    static private PreparedStatement delEngineServices;
    static private PreparedStatement delRelation;
    static private PreparedStatement selectEngineById;
    static private PreparedStatement selectServiceById;

    /*
     * Default constructor, tries to connect to "ServiceRepository.sqlite" 
     * in the same directory (if it exists!)
     */
    public ServiceRepository() throws SQLException, ClassNotFoundException{
        this(DB_CONN);
    }

    /*
     * @param db_conn   jdbc connection string,
     * please use jdbc:sqlite:path-to-file.db
     * if you need to connect to sqlite
     */
    public ServiceRepository(String db_conn) throws SQLException, ClassNotFoundException{
        Class.forName("org.sqlite.JDBC");
        conn = DriverManager.getConnection( db_conn );
        selectServicesList = conn.prepareStatement(
                "SELECT * FROM services");
        selectEnginesList = conn.prepareStatement(
                "SELECT * FROM engines");
        selectRelationList = conn.prepareStatement(
                "SELECT * FROM services_to_engines");
        selectEngineServices = conn.prepareStatement(
                "SELECT * FROM services_to_engines WHERE engine_id = ?");
        selectServiceEngines = conn.prepareStatement(
                "SELECT * FROM services_to_engines WHERE service_id = ?");
        addService = conn.prepareStatement(
                "INSERT OR ROLLBACK INTO services (name) VALUES (? ) ");
        addEngine = conn.prepareStatement(
                "INSERT OR ROLLBACK INTO engines (name, host, port) VALUES (?,?,?)" );
        addRelation = conn.prepareStatement(
                "INSERT OR ROLLBACK INTO service_map (service_id,engine_id) VALUES (?,?)");
        delService = conn.prepareStatement(
                "DELETE FROM services  WHERE id = ?");
        delServiceEngines = conn.prepareStatement(
                "DELETE FROM service_map WHERE service_id = ?");
        delEngine = conn.prepareStatement(
                "DELETE FROM engines  WHERE id = ?");
        delEngineServices = conn.prepareStatement(
                "DELETE FROM service_map WHERE engine_id = ?");
        delRelation = conn.prepareStatement(
                "DELETE FROM service_map WHERE engine_id = ? AND service_id = ?");
        //Nguyen
        selectEngineById = conn.prepareStatement(
                "SELECT * FROM engines WHERE id = ?");
        selectServiceById = conn.prepareStatement(
                "SELECT * FROM services WHERE id = ?");
       }

    /*
     * Function to get list of ALL services
     */
    public Service[] getServicesList() throws SQLException{
        ResultSet rs;
        synchronized(conn){
            rs = selectServicesList.executeQuery();
        }
        List<Service> sl = new ArrayList<Service>();
        while(rs.next()){
            sl.add( new Service( rs.getInt("id"), rs.getString("name") ) );
        }
        rs.close();
        return sl.toArray(new Service[0]);
    }

    /*
     *  Function to get list of ALL engines
     */
    public EngineInfo[] getEnginesList() throws SQLException{
        ResultSet rs;
        synchronized(conn){
            rs = selectEnginesList.executeQuery();
        }
        List<EngineInfo> sl = new ArrayList<EngineInfo>();
        while(rs.next()){
            sl.add( new EngineInfo(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("host"),
                    rs.getInt("port") ) );
        }
        rs.close();
        return sl.toArray(new EngineInfo[0]);
    }
    
    public  Relation[] getRelationList() throws SQLException{
        ResultSet rs;
        synchronized(conn){
            rs = selectRelationList.executeQuery();
        }
        List<Relation> sl = new ArrayList<Relation>();
        while(rs.next()){
            Service s = new Service(
                rs.getInt("service_id"),
                rs.getString("service_name")
            );
            EngineInfo e = new EngineInfo(
                rs.getInt("engine_id"),
                rs.getString("engine_name"),
                rs.getString("host"),
                rs.getInt("port")
            );
            sl.add( new Relation( s, e ) );
        }
        rs.close();
        return sl.toArray(new Relation[0]);
    }
    /*
     * Function to get the Services of an engine searched by ID
     */
    public Service[] getServicesMappedToEngine(EngineInfo eng) throws SQLException{
        ResultSet rs;
        synchronized(conn){
            selectEngineServices.setInt(1, eng.getId());
            rs = selectEngineServices.executeQuery();
        }
        ArrayList<Service> sl = new ArrayList<Service>();
        while(rs.next()){
            sl.add( new Service( rs.getInt("service_id"), rs.getString("service_name") ) );
        }
        rs.close();
        return sl.toArray(new Service[0]);
    }
    /*
     * Function to get the Engines that provide a specific service searched by ID
     */
    public EngineInfo[] getEnginesMappedToService(Service ser) throws SQLException{
        ResultSet rs;
        synchronized(conn){
            selectServiceEngines.setInt(1, ser.getId());
            rs = selectServiceEngines.executeQuery();
        }
        ArrayList<EngineInfo> el = new ArrayList<EngineInfo>();
        while(rs.next()){
            el.add( new EngineInfo(
                    rs.getInt("engine_id"),
                    rs.getString("engine_name"),
                    rs.getString("host"),
                    rs.getInt("port")) );
        }
        rs.close();
        return el.toArray(new EngineInfo[0]);
    }
    /*
     * Function to add a new type of Service into list
     */
    public boolean  addNewService(Service ser) throws SQLException{
        boolean success;
        synchronized(conn){
            //addService.setInt(1, ser.getId());
            addService.setString(1, ser.getName());
            success = addService.execute();
            if(success){
                ResultSet rs = addService.getGeneratedKeys();
                rs.next();
                ser.setId( rs.getInt("id") );
            }
            return success;
        }
    }
    /*
     * Function to add a new instance of Engine into list
     */
    public boolean addNewEngine(EngineInfo eng) throws SQLException{
        boolean success;
        synchronized(conn){
            addEngine.setString(1,eng.getName());
            addEngine.setString(2,eng.getHost());
            addEngine.setInt(3, eng.getPort());
            success = addEngine.execute();
            if(success){
                ResultSet rs = addService.getGeneratedKeys();
                rs.next();
                eng.setId( rs.getInt("id") );
            }
            return success;
        }
    }
    /*
     * Function for adding a new relation between a service and an engine
     */
     public boolean addRelServiceEngine(Service s, EngineInfo e) throws SQLException {
         boolean result;
         synchronized(conn){
             addRelation.setInt(1,s.getId());
             addRelation.setInt(2,e.getId());
             result = addRelation.execute();
             return result;
         }
     }

    /*
     * Function that delete  a service from the system thinking also to all dependences
     * with engines
     */
    public boolean delService(Service ser) throws SQLException{
            boolean result;
            result = delServiceEngines(ser);
            synchronized(conn){
                System.out.println("L'id da eliminare è"+ser.getId());
                delService.setInt(1, ser.getId());
                result = delService.execute();
                return result;
            }
        }

    /*
     * Function that delete a relation between a service and an engine
     * by ServiceID
     */
    public boolean delServiceEngines(Service ser) throws SQLException{
        boolean result;
        synchronized(conn){
            delServiceEngines.setInt(1,ser.getId());
            result = delServiceEngines.execute();
            return result;
         }
    }

    /*
     * Function that delete an engine from the system thinking also to all dependences
     * with engines
     */
    public boolean delEngine(EngineInfo eng) throws SQLException{
        boolean result;
        result = delEngineServices(eng);
        synchronized(conn){
            delEngine.setInt(1,eng.getId());
            result = delEngine.execute();
            return result;
        }
    }

    /*
     * Function that delete a relation between a service and an engine
     * by EngineID
     */
    public boolean delEngineServices(EngineInfo eng) throws SQLException{
        boolean result;
        synchronized(conn){
            delEngineServices.setInt(1,eng.getId());
            result = delEngineServices.execute();
            return result;
        }
    }

    public boolean delRelSeviceEngine(Service s, EngineInfo e) throws SQLException{
         boolean result;
         synchronized(conn){
             delRelation.setInt(1,s.getId());
             delRelation.setInt(2,e.getId());
             result = delRelation.execute();
             return result;
         }
    }

    /*Nguyen Ho
     Get engine information bases on engineId
     */
    public EngineInfo getEngineById(int engineId) throws SQLException {
        ResultSet resultSet;
        EngineInfo engineInfo = null;
        selectEngineById.setInt(1, engineId);
        synchronized(conn){
            resultSet = selectEngineById.executeQuery();
        }
        if(resultSet.next()){
            engineInfo = new EngineInfo(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("host"),
                resultSet.getInt("port")
            ) ;
        }else
            throw new java.util.NoSuchElementException();
        resultSet.close();
        return engineInfo;
    }   
    
    /*Nguyen Ho
     Get service bases on serviceId
     */
    public Service getServiceById(int serviceId) throws SQLException{
        ResultSet resultSet;
        Service service = null;
        selectServiceById.setInt(1, serviceId);
        synchronized(conn){
            resultSet=selectServiceById.executeQuery();
        }
        if(resultSet.next()){
            service = new Service(
                resultSet.getInt("id"),
                resultSet.getString("name")
            );
        }else
            throw new java.util.NoSuchElementException();
        resultSet.close();
        return  service;
    }
    
}