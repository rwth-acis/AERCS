package i5.las2peer.services.aercs.dbms.bdobjects;

import i5.las2peer.logging.L2pLogger;
import i5.las2peer.logging.NodeObserver.Event;
import i5.las2peer.services.aercs.dbms.dbconnection.DBConnection;

import java.sql.ResultSet;
import java.util.logging.Level;

/**
 * @author Quan Tran
 */
public class BasePeer
{
    protected static DBConnection conn;
	// instantiate the logger class
	private final L2pLogger logger = L2pLogger.getInstance(BasePeer.class.getName());
    
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
			// write error to logfile and console
			logger.log(Level.SEVERE, e.toString(), e);
			// create and publish a monitoring message
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, e.toString());
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
			// write error to logfile and console
			logger.log(Level.SEVERE, e.toString(), e);
			// create and publish a monitoring message
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, e.toString());
        }
        
        return null;
    }

}
