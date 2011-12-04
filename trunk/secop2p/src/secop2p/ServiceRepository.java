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

/**
 *
 * @author eros
 */
public class ServiceRepository {

    private final static String DB_CONN = "jdbc:sqlite:ServiceRepository.sqlite";
    private Connection conn;
    private PreparedStatement selectServicesList;
    private PreparedStatement selectEnginesList;

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
        selectServicesList = conn.prepareStatement("SELECT name FROM services");
        selectEnginesList = conn.prepareStatement("SELECT name, host, port FROM engines");
    }

    public Service[] getServicesList() throws SQLException{
        ResultSet rs;
        //synchronized it over the statement to prevent multiple queries
        synchronized(selectServicesList){
            rs = selectServicesList.executeQuery();
        }
        //prevent waste of memory checking the size of the rs
        int len = -1;
        try{
            rs.last();
            len = rs.getRow();
            rs.first();
        }catch(Exception e){
            //Do nothing
        }
        List<Service> sl;
        if(len!=-1)
            sl = new ArrayList<Service>(len);
        else
            sl = new ArrayList<Service>();
        //populate the list
        while(rs.next()){
            sl.add( new Service( rs.getString(1) ) );
        }
        rs.close();
        return sl.toArray(new Service[0]);
    }

    public EngineInfo[] getEnginesList() throws SQLException{
        ResultSet rs;
        //synchronized it over the statement to prevent multiple queries
        synchronized(selectEnginesList){
            rs = selectEnginesList.executeQuery();
        }
        //prevent waste of memory checking the size of the rs
        int len = -1;
        try{
            rs.last();
            len = rs.getRow();
            rs.first();
        }catch(Exception e){
            //Do nothing
        }
        List<EngineInfo> sl;
        if(len!=-1)
            sl = new ArrayList<EngineInfo>(len);
        else
            sl = new ArrayList<EngineInfo>();
        //populate the list
        while(rs.next()){
            sl.add( new EngineInfo( rs.getString(1), rs.getString(2), rs.getInt(3) ) );
        }
        rs.close();
        return sl.toArray(new EngineInfo[0]);
    }


}