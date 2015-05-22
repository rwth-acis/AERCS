package i5.las2peer.services.aercs.dbms.bdobjects;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthorNamePeer extends BasePeer
{
    public AuthorNamePeer()
    {
    }
    
    String getAValue(String query)
    {
        String ret = null;
        ResultSet rs = this.executeQuery(query);

        try
        {
            if (rs.next())
            {
                ret = rs.getString(1);
            }

            rs.getStatement().close();
            rs.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        
        return ret;
    }
    
    public String getKeyFromId(String id)
    {
        String query = "select dblp_key from author_name where author_id = '" + id + "'";
        return this.getAValue(query);
    }
    
    public String getIdFromKey(String key)
    {
        String query = "select author_id from author_name where dblp_key = '" + key + "'";
        return this.getAValue(query);
    }
    
}
