package i5.las2peer.services.aercs.dbms.bdobjects;


import java.sql.ResultSet;


/**
 * @author Quan Tran
 */
public class EventPeer extends BasePeer
{

    public EventPeer()
    {
    }
    
    
    public ResultSet selectEventsForASeries(String seriesId)
    {
        //"select a.id, a.name, a.abreviation, a.year, a.country from academicevent a where a.series_id  ="+seriesID+" order by a.year"
        String query = "select id, name, year, country, event_key from event where series_id = " + seriesId + " order by year";
        return this.executeQuery(query);
    }
}
