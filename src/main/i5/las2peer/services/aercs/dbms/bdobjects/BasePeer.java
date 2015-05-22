package i5.las2peer.services.aercs.dbms.bdobjects;

import i5.las2peer.services.aercs.dbms.dbconnection.DBConnection;

import java.sql.ResultSet;

/**
 * @author Quan Tran
 */
public class BasePeer
{
    protected static DBConnection conn;
    
    public BasePeer()
    {
        try
        {
            if (BasePeer.conn == null)
            {
            	BasePeer.conn = new DBConnection();
            	BasePeer.conn.setConnection();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    
    public ResultSet executeQuery(String query)
    {
        try
        {
            return BasePeer.conn.executeQuery(query);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        return null;
    }

}
