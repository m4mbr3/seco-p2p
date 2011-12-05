/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package javaapplication1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author eros
 */
public class ServiceRepository {

    private final static String DB_CONN = "jdbc:sqlite:ServiceRepository.sqlite";
    private Connection conn;
    private PreparedStatement selectServicesList;
    private PreparedStatement selectEnginesList;
    private PreparedStatement selectEngineServices;
    private PreparedStatement selectServiceEngines;

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
        selectEngineServices = conn.prepareStatement(
                "SELECT * FROM services_to_engines WHERE engine_id = ?");
        selectServiceEngines = conn.prepareStatement(
                "SELECT * FROM services_to_engines WHERE service_id = ?");
    }

    public Service[] getServicesList() throws SQLException{
        ResultSet rs;
        synchronized(selectServicesList){
            rs = selectServicesList.executeQuery();
        }
        List<Service> sl = new ArrayList<Service>();
        while(rs.next()){
            sl.add( new Service( rs.getInt("id"), rs.getString("name") ) );
        }
        rs.close();
        return sl.toArray(new Service[0]);
    }

    public EngineInfo[] getEnginesList() throws SQLException{
        ResultSet rs;
        synchronized(selectEnginesList){
            rs = selectEnginesList.executeQuery();
        }
        List<EngineInfo> sl = new ArrayList<EngineInfo>();
        while(rs.next()){
            sl.add( new EngineInfo(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("host"),
                    rs.getInt("host") ) );
        }
        rs.close();
        return sl.toArray(new EngineInfo[0]);
    }

    public Service[] getServicesMappedToEngine(EngineInfo eng) throws SQLException{
        ResultSet rs;
        synchronized(selectEngineServices){
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

    public EngineInfo[] getEnginesMappedToService(Service ser) throws SQLException{
        ResultSet rs;
        synchronized(selectServiceEngines){
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


}