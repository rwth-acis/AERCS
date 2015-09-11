package i5.las2peer.services.aercs.dbms.bdobjects;

import i5.las2peer.services.aercs.dbms.dbconnection.DBConnection;
import i5.las2peer.services.aercs.usermanager.EventSeries;

import java.sql.ResultSet;
import java.util.Vector;

public class ObjectQuery {
    private static DBConnection con;
    public ObjectQuery() {
    }
    /**
     * 
     * @param startChar
     * @return
     */
    public ResultSet querySeries(String startChar){
    try{
        if(con == null){
            con = new DBConnection();
            con.setConnection();
        }
        return con.executeQuery("select id, name, abbreviation from eventseries where name like "+"'"+startChar+"%' order by name");
    }catch(Exception e){
        e.printStackTrace();
    }
    return null;
    }
    
    public ResultSet queryEvents(String seriesID){
        try{
                if(con == null){
                    con = new DBConnection();
                    con.setConnection();
                }
                return con.executeQuery("select a.id, a.name, a.abreviation, a.year, a.country from academicevent a where a.series_id  ="+seriesID+" order by a.year");
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param id
     * @return
     * @updated 20.09.2009 by Quan Trab
     */
    public ResultSet searchEvent(String id)
    {
        try
        {
            if(con == null)
            {
                con = new DBConnection();
                con.setConnection();
            }
            
            ResultSet rs =  con.executeQuery("select id, name, country, year, series_id from event where id="+id);
            return rs;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * @param id
     * @return
     * @updated 20.09.2009 by Quan Tran
     */
    public Vector<EventSeries> searchEventByParticipant(String id)
    {
        try
        {
            if (con == null)
            {
                con = new DBConnection();
                con.setConnection();
            }
            
            //ResultSet rs =  con.executeQuery("select a.id, a.name, a.year, a.series_id, b.role, a.country from academicevent a, participate b where b.scientist_id="+id+" and a.id = b.event_id order by a.year desc");
            /*
            ResultSet rs =  con.executeQuery("select count(a.id) c, a.series_id, e.name, upper(e.abbreviation) from academicevent a, \n" + 
            "participate b, eventseries e where b.scientist_id="+id+" and a.id = b.event_id and a.series_id = e.id \n" + 
            "group by a.series_id, e.name, e.abbreviation \n" + 
            "order by c desc");
            System.out.println("select count(a.id) c, a.series_id, e.name, upper(e.abbreviation) from academicevent a, \n" + 
            "participate b, eventseries e where b.scientist_id="+id+" and a.id = b.event_id and a.series_id = e.id \n" + 
            "group by a.series_id, e.name, e.abbreviation \n" + 
            "order by c desc");
            */
            //rs.next();
            //return rs;
            //Vector<Event> tmp = new Vector<Event>();
            
            ResultSet rs =  con.executeQuery("select count (ev.id) c, es.id, es.name, es.name, upper(es.abbreviation), es.series_key " + 
            "from participate pa " + 
            "inner join proceeding_event pe on pa.proceeding_id = pe.proceeding_id " + 
            "inner join event ev on pe.event_id = ev.id " + 
            "inner join eventseries es on ev.series_id = es.id " + 
            "where pa.author_id = " + id + " " + 
            "group by es.id, es.name, es.abbreviation, es.series_key " + 
            "order by c desc");

            System.out.println("select count (ev.id) c, es.id, es.name, es.name, upper(es.abbreviation), es.series_key " + 
            "from participate pa " + 
            "inner join proceeding_event pe on pa.proceeding_id = pe.proceeding_id " + 
            "inner join event ev on pe.event_id = ev.id " + 
            "inner join eventseries es on ev.series_id = es.id " + 
            "where pa.author_id = " + id + " " + 
            "group by es.id, es.name, es.abbreviation, es.series_key " + 
            "order by c desc");
             
            Vector<EventSeries> tmp = new Vector<EventSeries>();

            while (rs.next())
            {
                //Event etmp = new Event(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getString(6),rs.getInt(5));
                EventSeries etmp = new EventSeries(rs.getInt(2), rs.getString(3), rs.getString(4), rs.getInt(1), rs.getString(6));
                if (!tmp.contains(etmp))
                {
                    tmp.add(etmp);
                }
            }

            rs.getStatement().close();
            rs.close();

            return tmp;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }
//
//    public Vector<Event> searchEventByTopic(String topicid){
//        try{
//            if(con == null){
//                con = new DBConnection();
//                con.setConnection();
//            }
//            ResultSet rs =  con.executeQuery("select a.id, a.name, a.year, a.series_id, a.country from academicevent a, topic_interest b where b.topic_id="+topicid+" and a.id = b.event_id order by a.year desc");
//            //rs.next();
//            //return rs;
//            Vector<Event> tmp = new Vector<Event>();
//            while(rs.next()){
//                Event etmp = new Event(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getString(5));
//                if(!tmp.contains(etmp)){
//                    tmp.add(etmp);
//                }
//            }
//            rs.getStatement().close();
//            rs.close();
//            return tmp;
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//        return null;
//    }
//    public Vector<Event> searchEventByLocation(String location){
//        try{
//            if(con == null){
//                con = new DBConnection();
//                con.setConnection();
//            }
//            ResultSet rs =  con.executeQuery("select a.id, a.name, a.year, a.series_id, a.country from academicevent a where lower(country) like'%"+location.toLowerCase()+"%' order by a.year desc");
//            //rs.next();
//            //return rs;
//            Vector<Event> tmp = new Vector<Event>();
//            while(rs.next()){
//                Event etmp = new Event(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getString(5));
//                if(!tmp.contains(etmp)){
//                    tmp.add(etmp);
//                }
//            }
//            rs.getStatement().close();
//            rs.close();
//            return tmp;
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//        return null;
//    }
    public ResultSet searchEventUrl(String id){
        try{
            if(con == null){
                con = new DBConnection();
                con.setConnection();
            }
            ResultSet rs =  con.executeQuery("select u.url, u.description from url u, event_media e where e.media_id = u.id and e.event_id="+id);
            //rs.next();
            return rs;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public String querySSeries(String id){
        try{
            if(con == null){
                con = new DBConnection();
                con.setConnection();
            }
            ResultSet rs =  con.executeQuery("select id, name, abbreviation from eventseries where id="+id);
            rs.next();
            return rs.getString(2)+ " ("+rs.getString(3).toUpperCase()+")";
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }


    /**
     * @param data
     * @return
     * @created 21.09.2009
     */
    public int countEvent(String data)
    {
        int ret = 0;
        
        try
        {
            String sp[] = data.split(" ");
            if(con == null)
            {
                con = new DBConnection();
                con.setConnection();
            }

            String whereClause = "";
            for (int i = 0; i < sp.length; i++)
            {
                if(whereClause == "")
                    whereClause = "lower(ev.name) like '%"+sp[i].toLowerCase()+"%'";
                else
                    whereClause += "or lower(ev.name) like '%"+sp[i].toLowerCase()+"%'";
            }

            //ResultSet rs =  con.executeQuery("select b.id, b.name, b.series_id, count(a.scientist_id) from participate a, academicevent b where ("+whereClause+") and a.event_id = b.id group by b.id, b.name, b.series_id order by b.name");
            String query = "select ev.id, ev.name, es.series_key, count(pa.author_id) " + 
            "from participate pa " + 
            "inner join proceeding_event pe on pa.proceeding_id = pe.proceeding_id " + 
            "inner join event ev on pe.event_id = ev.id " + 
            "inner join eventseries es on ev.series_id = es.id " +
            "where (" + whereClause + ") " + 
            "group by ev.id, ev.name, es.series_key " + 
            "order by ev.name";
            
            ResultSet rs = con.executeQuery("select count(*) from (" + query + ")");

            if (rs.next())
            {
                ret = rs.getInt(1);
            }
            
            rs.getStatement().close();
            rs.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return ret;
    }

    /**
     * @param data
     * @return
     * @updated 20.09.2009 by Quan Tran
     */
    public ResultSet queryEvent(String data, int fromRow, int toRow)
    {
        try
        {
            String sp[] = data.split(" ");
            if(con == null)
            {
                con = new DBConnection();
                con.setConnection();
            }

            String whereClause = "";
            for (int i = 0; i < sp.length; i++)
            {
                if(whereClause == "")
                    whereClause = "lower(ev.name) like '%"+sp[i].toLowerCase()+"%'";
                else
                    whereClause += "or lower(ev.name) like '%"+sp[i].toLowerCase()+"%'";
            }

            //ResultSet rs =  con.executeQuery("select b.id, b.name, b.series_id, count(a.scientist_id) from participate a, academicevent b where ("+whereClause+") and a.event_id = b.id group by b.id, b.name, b.series_id order by b.name");
            String query = "select ev.id ev_id, ev.name ev_name, es.series_key ev_series_key, count(pa.author_id) ev_author_num " + 
            "from participate pa " + 
            "inner join proceeding_event pe on pa.proceeding_id = pe.proceeding_id " + 
            "inner join event ev on pe.event_id = ev.id " + 
            "inner join eventseries es on ev.series_id = es.id " +
            "where (" + whereClause + ") " + 
            "group by ev.id, ev.name, es.series_key " + 
            "order by ev.name";
            
            ResultSet rs = con.executeQuery("select ev_id, ev_name, ev_series_key, ev_author_num from (select ev_id, ev_name, ev_series_key, ev_author_num, rownum r_num from (" + query + ") where rownum <= " + toRow + ") where r_num >= " + fromRow);

            return rs;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

//    public Vector<Event> searchUpcomingEvent(){
//        try{
//            Vector<Event> tmp = new Vector<Event>();
//            if(con == null){
//                con = new DBConnection();
//                con.setConnection();
//            }
//            
//            ResultSet rs =  con.executeQuery("select id, name, year, month, country, series_id from academicevent where (to_date('01-'||substr(month,0,3)||'-'||year, 'DD-MON-YYYY', 'NLS_DATE_LANGUAGE=American') > (select CURRENT_DATE from dual)) and month is not null order by name");
//            while(rs.next()){
//                Event etmp = new Event(rs.getInt(1), rs.getString(2));
//                etmp.setYear(rs.getString(4)+","+ rs.getString(3));
//                etmp.setLocation(rs.getString(5));
//                etmp.setSeries_id(rs.getInt(6));
//                tmp.add(etmp);
//            }
//            rs.getStatement().close();
//            rs.close();
//            return tmp;
//            
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//        return null;
//    }


    public int countSeries(String data)
    {
        int ret = 0;

        try
        {
            String sp[] = data.split(" ");
            if(con == null)
            {
                con = new DBConnection();
                con.setConnection();
            }

            String whereClause = "";
            for (int i = 0; i < sp.length; i++)
            {
                if (whereClause == "")
                    whereClause = "lower(name||' '||abbreviation) like '%" + sp[i].toLowerCase() + "%'";
                else
                    whereClause += "or lower(name||' '||abbreviation) like '%" + sp[i].toLowerCase() + "%'";
            }

            String query = "select id, name, series_key from eventseries where " + whereClause;
            ResultSet rs = con.executeQuery("select count(*) from (" + query + ")");
            
            if (rs.next())
            {
                ret = rs.getInt(1);
            }
            
            rs.getStatement().close();
            rs.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return ret;
    }

    /**
     * @param data
     * @return
     * @updated 20.09.2009 by Quan Tran
     */
    public ResultSet searchSeries(String data, int fromRow, int toRow)
    {
        try
        {
            String sp[] = data.split(" ");
            if(con == null)
            {
                con = new DBConnection();
                con.setConnection();
            }

            String whereClause = "";
            for (int i = 0; i < sp.length; i++)
            {
                if (whereClause == "")
                    whereClause = "lower(name||' '||abbreviation) like '%" + sp[i].toLowerCase() + "%'";
                else
                    whereClause += "or lower(name||' '||abbreviation) like '%" + sp[i].toLowerCase() + "%'";
            }

            String query = "select id, name, series_key, rownum r_num from eventseries where rownum <= " + toRow + " and (" + whereClause + ")";
            ResultSet rs = con.executeQuery("select id, name, series_key from (" + query + ") where r_num >= " + fromRow);

            return rs;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * @param data
     * @return
     * @created 21.09.2009
     */
    public int countPerson(String data)
    {
        int ret = 0;
        
        try
        {
            if (con == null)
            {
                con = new DBConnection();
                con.setConnection();
            }
            
            String query = "select an.dblp_key, an.name, count(pa.proceeding_id) " + 
            "from author_name an inner join participate pa on an.author_id = pa.author_id " + 
            "where lower(an.name) like '%" + data.toLowerCase() + "%' " + 
            "group by an.dblp_key, an.name " + 
            "order by an.name";
            
            ResultSet rs =  con.executeQuery("select count(*) from (" + query + ")");
            System.out.println(query);
            
            if (rs.next())
            {
                ret = rs.getInt(1);
            }
            
            rs.getStatement().close();
            rs.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return ret;
    }

    /**
     * @param data
     * @param fromRow
     * @param toRow
     * @return
     * @updated 20.09.2009 by Quan Tran
     */
    public ResultSet queryPerson(String data, int fromRow, int toRow)
    {
        try
        {
            if (con == null)
            {
                con = new DBConnection();
                con.setConnection();
            }
            
            /*
            ResultSet rs =  con.executeQuery("select b.id, b.name, count(a.event_id) from participate a, scientist b where a.scientist_id = b.id and lower(name) like '%"+data.toLowerCase()+"%' group by b.id, b.name order by b.name");
            System.out.println("select b.id, b.name, count(a.event_id) from participate a, scientist b where a.scientist_id (+)= b.id and lower(name) like '%"+data.toLowerCase()+"%' group by b.id, b.name order by b.name");
            */
            
            String query = "select an.dblp_key a_key, an.name a_name, count(pa.proceeding_id) a_p_num " + 
            "from author_name an inner join participate pa on an.author_id = pa.author_id " + 
            "where lower(an.name) like '%" + data.toLowerCase() + "%' " + 
            "group by an.dblp_key, an.name " + 
            "order by an.name";
            
            ResultSet rs =  con.executeQuery("select a_key, a_name, a_p_num from (select a_key, a_name, a_p_num, rownum r_num from (" + query + ") where rownum <= " + toRow + ") where r_num >= " + fromRow);
            System.out.println(query);
            
            return rs;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * @param id
     * @return
     * @updated 20.09.2009 by Quan Tran
     */
    public ResultSet searchPerson(String id)
    {
        try
        {
            if(con == null)
            {
                con = new DBConnection();
                con.setConnection();
            }
            
            ResultSet rs =  con.executeQuery("select id, name from author where id = " + id);
            return rs;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public int queryClaimAuthor(int uid){
        try{
            if(con == null){
                con = new DBConnection();
                con.setConnection();
            }
            ResultSet rs =  con.executeQuery("select scientist_id from user_claim where user_id="+uid);
            if(rs.next())
                return rs.getInt(1);
            else
                return 0;
        }catch(Exception e){
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * @param id
     * @return
     * @updated 20.09.2009 by Quan Tran
     */
    public ResultSet searchPersonUrl(String id)
    {
        try
        {
            if(con == null)
            {
                con = new DBConnection();
                con.setConnection();
            }

            //ResultSet rs =  con.executeQuery("select url ,description from url u, scientist_url s where s.url_id= u.id and s.scientist_id="+id);
             ResultSet rs =  con.executeQuery("select u.url, u.description from url u, author_media am where am.media_id = u.id and am.author_id = " + id);
            return rs;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public ResultSet searchCoauthors(String id){
        try{
            if(con == null){
                con = new DBConnection();
                con.setConnection();
            }
            ResultSet rs =  con.executeQuery("select a.id, a.name from scientist a, authorlinkevent b where a.id="+id);
            return rs;
            
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
//    public Vector<CoauthorWeight> searchTopCoauthors(String id){
//        try{
//            if(con == null){
//                con = new DBConnection();
//                con.setConnection();
//            }
//            ResultSet rs =  con.executeQuery("select start_id, end_id, (select name from scientist where id = start_id) name1, (select name from scientist where id = end_id) name2  from authorlinkevent where start_id = "+id +" or end_id  = "+id);
//            Vector<CoauthorWeight> tmp = new Vector<CoauthorWeight>();
//            while(rs.next()){
//                int id1 = rs.getInt(1), id2 = rs.getInt(2);
//                if(id1== Integer.parseInt(id)){
//                    CoauthorWeight atmp = new CoauthorWeight(rs.getString(4),rs.getInt(2),1);
//                    if(tmp.contains(atmp)){
//                        atmp = tmp.get(tmp.indexOf(atmp));
//                        atmp.setWeight(atmp.getWeight()+1);
//                        
//                    }else{
//                        tmp.add(atmp);    
//                    }
//                }else{
//                    CoauthorWeight atmp = new CoauthorWeight(rs.getString(3),rs.getInt(1),1);
//                    if(tmp.contains(atmp)){
//                        atmp = tmp.get(tmp.indexOf(atmp));
//                        atmp.setWeight(atmp.getWeight()+1);
//                        
//                    }else{
//                        tmp.add(atmp);    
//                    }
//                }
//            }
//            //Arrays.sort(tmp.toArray());
//            
//            return tmp;
//            
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//        return null;
//    }

    /**
     * @param seriesID
     * @return
     * @updated 18.09.2009 by Quan Tran
     */
    public ResultSet searchAuthorKeyMembers(String seriesID)
    {
        try
        {
            if (con == null)
            {
                con = new DBConnection();
                con.setConnection();
            }
            
            //ResultSet rs =  con.executeQuery("select count(a.event_id) time, a.scientist_id id, s.name name from participate a, academicevent b, scientist s where s.id = a.scientist_id and a.event_id = b.id and a.role=1 and  b.series_id = "+seriesID+" group by a.scientist_id, s.name order by time desc");
            ResultSet rs =  con.executeQuery("select count(pa.proceeding_id) time , an.dblp_key key, an.name name from " + 
            "participate pa " + 
            "inner join proceeding_event pe on pa.proceeding_id = pe.proceeding_id " + 
            "inner join event ev on pe.event_id = ev.id " + 
            "inner join author au on pa.author_id = au.id " + 
            "inner join author_name an on au.id = an.author_id " + 
            //"where pa.role = 1 and ev.series_id = " + seriesID + " " +
            "where ev.series_id = " + seriesID + " " +
            "group by an.dblp_key, an.name " +
            "order by time desc");
            return rs;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * @param seriesID
     * @return
     * @updated 18.09.2009 by Quan Tran
     */
    public String querySeriesNewestYear(String seriesID)
    {
        try
        {
            if (con == null)
            {
                con = new DBConnection();
                con.setConnection();
            }
            
            //ResultSet rs =  con.executeQuery("select year,id from academicevent where series_id="+seriesID+" order by year desc");
            ResultSet rs =  con.executeQuery("select year, id from event where series_id = " + seriesID + " order by year desc");

            if (rs.next())
            {
                return String.valueOf(rs.getInt(2));
            }
            
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * @param seriesID
     * @return
     * @updated 18.09.2009 by Quan Tran
     
    public ResultSet queryDevelopmentChart(String seriesID)
    {
        try
        {
            if(con == null)
            {
                con = new DBConnection();
                con.setConnection();
            }
            
            //ResultSet rs = con.executeQuery("select count(a.scientist_id), b.year from participate a, academicevent b where a.event_id = b.id and a.role=1 and  b.series_id = "+seriesID+" group by b.year order by b.year");
            ResultSet rs = con.executeQuery("select count(pa.author_id), ev.year " + 
            "from participate pa " + 
            "inner join proceeding_event pe on pa.proceeding_id = pe.proceeding_id " + 
            "inner join event ev on pe.event_id = ev.id " + 
            //"where pa.role = 1 and ev.series_id = " + seriesID + " "  +
            "where ev.series_id = " + seriesID + " "  +
            "group by ev.year " + 
            "order by ev.year");
            
            String query = "select count(pa.author_id), ev.year " + 
            "from participate pa " + 
            "inner join proceeding_event pe on pa.proceeding_id = pe.proceeding_id " + 
            "inner join event ev on pe.event_id = ev.id " + 
            //"where pa.role = 1 and ev.series_id = " + seriesID + " "  +
            "where ev.series_id = " + seriesID + " "  +
            "group by ev.year " + 
            "order by ev.year";
            System.out.println(query);
            
            return rs;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    
     * @param seriesID
     * @return
     * @updated 18.09.2009 by Quan Tran
     
    public ResultSet queryContinuityChart(String seriesID)
    {
        try
        {
            if(con == null)
            {
                con = new DBConnection();
                con.setConnection();
            }
            
            //ResultSet rs = con.executeQuery("select count(id), time from (select count(a.event_id) time, a.scientist_id id from participate a, academicevent b where a.event_id = b.id and a.role=1 and  b.series_id = "+seriesID+" group by a.scientist_id) group by time order by time");
            ResultSet rs = con.executeQuery("select count(id), time from " + 
            "(select count(pa.proceeding_id) time, pa.author_id id " + 
            "from participate pa " + 
            "inner join proceeding_event pe on pa.proceeding_id = pe.proceeding_id " + 
            "inner join event ev on pe.event_id = ev.id " + 
            //"where pa.role = 1 and  ev.series_id = " + seriesID + " " +
            "where ev.series_id = " + seriesID + " " +
            "group by pa.author_id) " + 
            "group by time order by time");
            
            return rs;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        return null;
    }*/

    public ResultSet queryUser(String id){
        try{
            if(con == null){
                con = new DBConnection();
                con.setConnection();
            }
            ResultSet rs = con.executeQuery("select id, name, login_name from users where id ="+id);
            
            return rs;
            
            
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public int getUserId(String username){
        try{
            if(con == null){
                con = new DBConnection();
                con.setConnection();
            }
            ResultSet rs = con.executeQuery("select id from users where login_name ='"+username+"'");
            if(rs.next()){
                return rs.getInt(1);
            }else{
                return -1;
            }
            
        }catch(Exception e){
            e.printStackTrace();
        }
        return -1;
    }
//    public Vector<Topic> getUserTopicPre(int id){
//        Vector<Topic> topictmp = new Vector<Topic>();
//        try{
//            if(con == null){
//                con = new DBConnection();
//                con.setConnection();
//            }
//            ResultSet rs = con.executeQuery("select a.id, a.name from topic a, user_topic b where a.id = b.topic_id and b.user_id ="+id);
//           while(rs.next()){
//               Topic tmp = new Topic(rs.getInt(1), rs.getString(2));
//               topictmp.add(tmp);
//           }
//            rs.getStatement().close();
//            rs.close();
//           return topictmp;
//            
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//        return topictmp;
//    }
//    public Vector<Location> getUserLocationPre(int id){
//        Vector<Location> locationtmp = new Vector<Location>();
//        try{
//            if(con == null){
//                con = new DBConnection();
//                con.setConnection();
//            }
//            ResultSet rs = con.executeQuery("select location_id, location_name from user_location where user_id ="+id);
//           while(rs.next()){
//               Location tmp = new Location(rs.getInt(1), rs.getString(2));
//               locationtmp.add(tmp);
//           }
//            rs.getStatement().close();
//            rs.close();
//           return locationtmp;
//            
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//        return locationtmp;
//    }
//    public Vector<Event> getUserEventPre(int id){
//        Vector<Event> eventtmp = new Vector<Event>();
//        try{
//            if(con == null){
//                con = new DBConnection();
//                con.setConnection();
//            }
//            ResultSet rs = con.executeQuery("select a.id, a.name from academicevent a, user_event b where a.id = b.event_id and b.user_id ="+id);
//            System.out.println("select a.id, a.name from academicevent a, user_event b where a.id = b.event_id and b.user_id ="+id);
//           while(rs.next()){
//               Event tmp = new Event(rs.getInt(1), rs.getString(2));
//               eventtmp.add(tmp);
//           }
//            rs.getStatement().close();
//            rs.close();
//           return eventtmp;
//            
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//        return eventtmp;
//    } 
//    public Vector<Person> getUserPersonPre(int id){
//        Vector<Person> persontmp = new Vector<Person>();
//        try{
//            if(con == null){
//                con = new DBConnection();
//                con.setConnection();
//            }
//            ResultSet rs = con.executeQuery("select a.id, a.name from scientist a, user_person b where a.id = b.scientist_id and b.user_id ="+id);
//           while(rs.next()){
//               Person tmp = new Person(rs.getInt(1), rs.getString(2));
//               persontmp.add(tmp);
//           }
//            rs.getStatement().close();
//            rs.close();
//           return persontmp;
//            
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//        return persontmp;
//    }
   /* public Vector<Media> searchWebsiteByEvent(int eventid){
        Vector<Media> mtmp  = new Vector<Media>();
        Parser parser;
        NodeFilter filter;
        try{
            if(con == null){
                con = new DBConnection();
                con.setConnection();
            }
            parser = new Parser ();
            LinkTag lt = new LinkTag();
            
            parser.setFeedback (Parser.STDOUT);
            parser.getConnectionManager ().setMonitor (parser);
            parser.getConnectionManager ().setRedirectionProcessingEnabled (true);
            parser.getConnectionManager ().setCookieProcessingEnabled (true);
            
            ResultSet rs = con.executeQuery("select a.url, a.id, b.event_id, d.login_name, a.title, a.summary,a.description, d.id from url a, event_media b, user_media c, users d \n" + 
            "where a.id = b.media_id and c.media_id (+) = a.id and d.id (+) = c.user_id and b.event_id = "+eventid);
           while(rs.next()){
               Media tmp = new Media();
                try{
               StringExtractor se = new StringExtractor (rs.getString(1));
               String summary = se.extractStrings(false);
               String[] sentences = summary.split("\n");
               for(int i = 0; i < sentences.length; i ++){
                  if(sentences[i].split(" ").length > 20){
                       summary = sentences[i];
                       break;
                  }
               }
               //if(summary.length())
                parser.setResource (rs.getString(1));
               filter = new TagNameFilter ("title");
               NodeList nl = parser.parse(filter);
               String title = nl.elementAt(0).toPlainTextString();
               tmp.setTitle(title);
               tmp.setSummary(summary);
                }catch(Exception e){
                    tmp.setTitle(rs.getString(5));
                    tmp.setSummary(rs.getString(6));
                }
               
               tmp.setEventid(eventid);
               tmp.setId(rs.getInt(2));
               tmp.setUrl(rs.getString(1));
               tmp.setUsername(rs.getString(4));
               
               tmp.setCommend(rs.getString(7));
               tmp.setUserid(rs.getInt(8));
               mtmp.add(tmp);
           }
           return mtmp;
            
        }catch(Exception e){
            e.printStackTrace();
        }
        return mtmp;
    }*/
    public Vector<Media> searchWikiByEvent(int eventid){
        Vector<Media> mtmp  = new Vector<Media>();
        try{
            if(con == null){
                con = new DBConnection();
                con.setConnection();
            }
            ResultSet rs = con.executeQuery("select a.url, a.id, b.event_id, d.login_name, a.title, a.summary,a.description,d.id from wiki a, event_media b, user_media c, users d \n" + 
            "where a.id = b.media_id and c.media_id (+) = a.id and d.id (+) = c.user_id and b.event_id = "+eventid);
           while(rs.next()){
               Media tmp = new Media();
               tmp.setEventid(eventid);
               tmp.setId(rs.getInt(2));
               tmp.setUrl(rs.getString(1));
               tmp.setUsername(rs.getString(4));
               tmp.setTitle(rs.getString(5));
               tmp.setSummary(rs.getString(6));
               tmp.setCommend(rs.getString(7));
               tmp.setUserid(rs.getInt(8));
               mtmp.add(tmp);
           }
            rs.getStatement().close();
            rs.close();
           return mtmp;
            
        }catch(Exception e){
            e.printStackTrace();
        }
        return mtmp;
    }
    
    public Vector<Media> searchBlogByEvent(int eventid){
        Vector<Media> mtmp  = new Vector<Media>();
        try{
            if(con == null){
                con = new DBConnection();
                con.setConnection();
            }
            ResultSet rs = con.executeQuery("select a.url, a.id, b.event_id, d.login_name, a.title, a.summary,a.description,d.id from blog a, event_media b, user_media c, users d \n" + 
            "where a.id = b.media_id and c.media_id (+) = a.id and d.id (+) = c.user_id and b.event_id = "+eventid);
           while(rs.next()){
               Media tmp = new Media();
               tmp.setEventid(eventid);
               tmp.setId(rs.getInt(2));
               tmp.setUrl(rs.getString(1));
               tmp.setUsername(rs.getString(4));
               tmp.setTitle(rs.getString(5));
               tmp.setSummary(rs.getString(6));
               tmp.setCommend(rs.getString(7));
               tmp.setUserid(rs.getInt(8));
               mtmp.add(tmp);
           }
            rs.getStatement().close();
            rs.close();
           return mtmp;
            
        }catch(Exception e){
            e.printStackTrace();
        }
        return mtmp;
    }


    /**
     * @param eventid
     * @return
     * @created 29.09.2009 by Quan Tran
     */
    public Vector<Media> searchRelatedSeriesByEvent(int eventId)
    {
        Vector<Media> mtmp  = new Vector<Media>();
        try
        {
            if (con == null)
            {
                con = new DBConnection();
                con.setConnection();
            }
            
            String query = "select es.name, es.series_key, es.id from related_series rs inner join eventseries es on rs.series_id = es.id where rs.event_id = " + eventId;

            ResultSet rs = con.executeQuery(query);
            
            while (rs.next())
            {
                Media tmp = new Media();
                tmp.setEventid(eventId);
                tmp.setId(0);
                tmp.setUrl("EventList.html?series="+rs.getString(2)+"&item=1&id="+rs.getInt(3));
                tmp.setUsername(null);
                tmp.setTitle(rs.getString(1));
                tmp.setSummary(null);
                tmp.setCommend(null);
                tmp.setUserid(0);
                mtmp.add(tmp);
            }
            
            rs.getStatement().close();
            rs.close();
            
            return mtmp;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        return mtmp;
    }
    
    /**
     * @param eventid
     * @return
     * @updated 29.09.2009 by Quan Tran
     */
    public Vector<Media> searchWebsiteByEvent(int eventid)
    {
        Vector<Media> mtmp  = new Vector<Media>();
        try
        {
            if (con == null)
            {
                con = new DBConnection();
                con.setConnection();
            }
            
            /*
            ResultSet rs = con.executeQuery("select a.url, a.id, b.event_id, d.login_name, a.title, a.summary,a.description, d.id from url a, event_media b, user_media c, users d \n" + 
            "where a.id = b.media_id and c.media_id (+) = a.id and d.id (+) = c.user_id and b.event_id = "+eventid);
            */
            /*
            ResultSet rs = con.executeQuery("select a.id, a.url, a.description  from url a, event_media b \n" + 
                        "where a.id = b.media_id and b.event_id = "+eventid);
            */
            String seriesQuery = "select es.name, es.dblp_url from event ev inner join eventseries es on ev.series_id = es.id where ev.id = " + eventid;
            String proceedingQuery = "select pr.title, pr.dblp_url from proceeding_event pe inner join proceeding pr on pe.proceeding_id = pr.id where pe.event_id = " + eventid;

            ResultSet rs = con.executeQuery("(" + seriesQuery + ") union (" + proceedingQuery + ")");
            
            while (rs.next())
            {
                Media tmp = new Media();
                tmp.setEventid(eventid);
                tmp.setId(0);
                tmp.setUrl("http://dblp.uni-trier.de/"+rs.getString(2));
                tmp.setUsername(null);
                tmp.setTitle(rs.getString(1));
                tmp.setSummary(null);
                tmp.setCommend(null);
                tmp.setUserid(0);
                mtmp.add(tmp);
            }
            
            rs.getStatement().close();
            rs.close();
            
            return mtmp;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        return mtmp;
    }
    
//    public Vector<Video> searchVideoByEvent(int eventid){
//        Vector<Video> mtmp  = new Vector<Video>();
//        try{
//            if(con == null){
//                con = new DBConnection();
//                con.setConnection();
//            }
//            ResultSet rs = con.executeQuery("select a.id, a.external_id, b.event_id, d.login_name, d.id from video a, event_media b, user_media c, users d \n" + 
//            "where a.id = b.media_id and c.media_id (+) = a.id and d.id (+) = c.user_id and b.event_id = "+eventid);
//           YouTubeService service = new YouTubeService("ytapi-RWTHAachen-AERCSdeveloping-9u2385ah-1", "AI39si4KHhZIo-mKrcEPO_bOBl-PCLmjSQzy_LG191L3-cqjjfy9rsdii-oAjr0PGlOOZY0Ck_BdZy8_9cl4TREZvASwgrfiMw");
//           while(rs.next()){
//               Video tmp   = new Video();
//               
//               VideoEntry videoEntry = service.getEntry(new URL(rs.getString(2)), VideoEntry.class);
//               //System.out.println("Title: " + videoEntry.getTitle().getPlainText());
//               //System.out.println(videoEntry.getMediaGroup().getDescription().getPlainTextContent());
//               tmp.setAuthor(videoEntry.getAuthors().get(0).getName());
//               tmp.setTitle(videoEntry.getTitle().getPlainText());
//               tmp.setDescription(videoEntry.getMediaGroup().getDescription().getPlainTextContent());
//               tmp.setVideoURL(videoEntry.getMediaGroup().getPlayer().getUrl());
//               tmp.setAuthor(videoEntry.getAuthors().get(0).getName());
//               tmp.setVideoid(videoEntry.getId().substring(videoEntry.getId().lastIndexOf("/")+1));
//               DateTime d = videoEntry.getPublished();
//               d.setDateOnly(true);
//               tmp.setDate(d.toString());
//               if(videoEntry.getMediaGroup().getThumbnails().size() > 0){
//                   tmp.setThumbnailURL(videoEntry.getMediaGroup().getThumbnails().get(0).getUrl());
//               }
//               tmp.setUserid(rs.getInt(5));
//               tmp.setUsername(rs.getString(4));
//               tmp.setId(rs.getInt(1));
//               
//               mtmp.add(tmp);
//           }
//            rs.getStatement().close();
//            rs.close();
//           return mtmp;
//            
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//        return mtmp;
//    }
//    
//    public Vector<Image> searchImageByEvent(int eventid){
//        Vector<Image> mtmp  = new Vector<Image>();
//        try{
//            if(con == null){
//                con = new DBConnection();
//                con.setConnection();
//            }
//            ResultSet rs = con.executeQuery("select a.id, a.external_id, b.event_id, d.login_name, d.id from image a, event_media b, user_media c, users d \n" + 
//            "where a.id = b.media_id and c.media_id (+) = a.id and d.id (+) = c.user_id and b.event_id = "+eventid);
//            Properties p = new Properties();
//            p.setProperty("restUrl","http://flickr.com/services/rest");
//            p.setProperty("authUrl","http://flickr.com/services/auth");
//            p.setProperty("staticUrl","http://static.flickr.com");
//            p.setProperty("uploadUrl","http://api.flickr.com/services/upload");
//            p.setProperty("apiKey","3c276cb6ae1b7eb8ad1096e940b8da13");
//            p.setProperty("secret","809ec7133d806b2d");
//            int i = 0;
//           while(rs.next() && i < 6){
//               Image tmp   = new Image();
//               PhotosService ps = new PhotosService(p);
//               PhotoResponse pr = ps.getInfo(rs.getString(2));
//               //System.out.println("Title: " + videoEntry.getTitle().getPlainText());
//               //System.out.println(videoEntry.getMediaGroup().getDescription().getPlainTextContent());
//               tmp.setAuthor(pr.getOwner().getUserName());
//               tmp.setDescription(pr.getDescription());
//               tmp.setThumbnailURL(pr.getThumbnailUrl().toString());
//               tmp.setURL(pr.getMediumUrl().toString());
//               
//               tmp.setUserid(rs.getInt(5));
//               tmp.setUsername(rs.getString(4));
//               tmp.setId(rs.getInt(1));
//               
//               mtmp.add(tmp);
//               i++;
//           }
//            rs.getStatement().close();
//            rs.close();
//           return mtmp;
//            
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//        return mtmp;
//    }
//    
//    public Vector<Image> searchImageByEventAll(int eventid){
//        Vector<Image> mtmp  = new Vector<Image>();
//        try{
//            if(con == null){
//                con = new DBConnection();
//                con.setConnection();
//            }
//            ResultSet rs = con.executeQuery("select a.id, a.external_id, b.event_id, d.login_name, d.id from image a, event_media b, user_media c, users d \n" + 
//            "where a.id = b.media_id and c.media_id (+) = a.id and d.id (+) = c.user_id and b.event_id = "+eventid);
//            Properties p = new Properties();
//            p.setProperty("restUrl","http://flickr.com/services/rest");
//            p.setProperty("authUrl","http://flickr.com/services/auth");
//            p.setProperty("staticUrl","http://static.flickr.com");
//            p.setProperty("uploadUrl","http://api.flickr.com/services/upload");
//            p.setProperty("apiKey","3c276cb6ae1b7eb8ad1096e940b8da13");
//            p.setProperty("secret","809ec7133d806b2d");
//            PhotosService ps = new PhotosService(p);
//           while(rs.next()){
//               Image tmp   = new Image();
//                
//               try{
//                PhotoResponse pr = ps.getInfo(rs.getString(2));
//                   tmp.setAuthor(pr.getOwner().getUserName());
//                   tmp.setDescription(pr.getDescription());
//                   tmp.setThumbnailURL(pr.getThumbnailUrl().toString());
//                   tmp.setURL(pr.getMediumUrl().toString());
//                   
//                   tmp.setUserid(rs.getInt(5));
//                   tmp.setUsername(rs.getString(4));
//                   tmp.setId(rs.getInt(1));
//                   
//                   mtmp.add(tmp);
//               }catch(Exception ex){
//                   System.out.print("Image not found in Flickr: "+rs.getString(2));
//               }
//               //System.out.println("Title: " + videoEntry.getTitle().getPlainText());
//               //System.out.println(videoEntry.getMediaGroup().getDescription().getPlainTextContent());
//              
//               
//           }
//            rs.getStatement().close();
//            rs.close();
//           return mtmp;
//            
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//        return mtmp;
//    }
    
   /* public Vector<FeedbackForm> getFeedbackForm(String uid){
        Vector<FeedbackForm> tmp = new Vector<FeedbackForm>();
        Vector<Integer> multiquest = new Vector<Integer>();
        Integer[] l = {6,7,8,9,11,12,13,14,15};
        Collections.addAll(multiquest,l);
        try{
            if(con == null){
                con = new DBConnection();
                con.setConnection();
            }
            ResultSet rs =  con.executeQuery("select id, quest_id, value, text, user_id from answer where user_id ="+uid+" order by quest_id");
  
            while(rs.next()){
                FeedbackForm ftmp = new FeedbackForm();
                
                ftmp.setId(rs.getInt(1));
                ftmp.setQuest_id(rs.getInt(2));
                ftmp.setValue(rs.getInt(3));
                ftmp.setText(rs.getString(4));
                ftmp.setUid(rs.getInt(5));
                if(tmp.contains(ftmp)){
                    FeedbackForm ftmp1 = tmp.get(tmp.indexOf(ftmp));
                    ftmp1.setQuestiontype(1);
                    ftmp1.getSubanswers().add(ftmp);
                    
                }else
                    tmp.add(ftmp);
                
            }
            return tmp;
            
        }catch(Exception e){
            e.printStackTrace();
        }
        return tmp;
    }*/
    
//    public Vector<FeedbackForm> getFeedbackFormXML(String uid){
//        Vector<FeedbackForm> tmp = new Vector<FeedbackForm>();
//       
//        
//        try{
//            if(con == null){
//                con = new DBConnection();
//                con.setConnection();
//            }
//            ResultSet rs =  con.executeQuery("select x.id, x.user_id, extract(x.questionnaire,'/questionnaire') from questionnaire x where x.user_id ="+uid);
//            if(rs.next()){
//                OracleResultSet orset = (OracleResultSet) rs;
//            DocumentBuilderFactory factory = 
//                    DocumentBuilderFactory.newInstance();
//                  // then we have to create document-loader:
//                  DocumentBuilder loader = factory.newDocumentBuilder();
//                XMLType xml = XMLType.createXML(orset.getOPAQUE(3));
//                  // loading a DOM-tree...
//                 // Document document = loader.parse(new InputSource(new StringReader(rs.getString(3))));
//                Document document = xml.getDOM();
//                  // at last, we get a root element:
//                  Element tree = document.getDocumentElement();
//
//                  NodeList nl = tree.getElementsByTagName("question");
//                  for(int i = 0; i < nl.getLength(); i++){
//                      FeedbackForm ftmp = new FeedbackForm();
//                      ftmp.setUid(Integer.parseInt(uid));
//                      NodeList childs = nl.item(i).getChildNodes();
//                      for(int j = 0; j < childs.getLength(); j++){
//                        if(childs.item(j).getNodeName().equals("id"))
//                              ftmp.setId(Integer.parseInt(childs.item(j).getTextContent()));      
//                        else if(childs.item(j).getNodeName().equals("value"))
//                             ftmp.getValues().add((Integer.parseInt(childs.item(j).getTextContent())));    
//                        else
//                             if(childs.item(j).getNodeName().equals("text"))
//                               ftmp.setText(childs.item(j).getTextContent());    
//                      }
//                      tmp.add(ftmp);
//                  }
//            System.out.println(tmp.elementAt(0).getValues().elementAt(0));
//            return tmp;
//            }
//            
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//        return tmp;
//    }
    
    public ResultSet searchEventNetwork(String series_id, String year)
    {
        try
        {
            if(con == null)
            {
                con = new DBConnection();
                con.setConnection();
            }
            
            ResultSet rs =  con.executeQuery("select ap.start_id, ap.end_id, " + 
    				"(select name from author where id = ap.start_id) name1, " + 
    				"(select name from author where id = ap.end_id) name2 " + 
    				"from author_proceeding ap " + 
    				"inner join proceeding_event pe on ap.proceeding_id = pe.proceeding_id " + 
    				"inner join event ev on pe.event_id = ev.id " + 
    				"where ev.series_id = " + series_id + " " + 
    				"and ev.year <= '" + year + "'");
            return rs;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }
    
    public ResultSet searchPersonNetwork(String id)
    {
        try
        {
            if(con == null)
            {
                con = new DBConnection();
                con.setConnection();
            }
            
            ResultSet rs =  con.executeQuery("select " + 
            	      "author_id1, " + 
            	      "author_id2, " + 
            	      "(select name from author where author_id1 = id) n1, " + 
            	      "(select name from author where author_id2 = id) n2 " + 
            	      "from " + 
            	      "coauthorshipnetwork " + 
            	      "where " + 
            	      "author_id1 in " + 
            	      "( " + 
            	      "select author_id2 from coauthorshipnetwork where author_id1 = " + id + " and author_id2 <> " + id + " " + 
            	      "union " + 
            	      "select author_id1 from coauthorshipnetwork where author_id2 = " + id + " and author_id1 <> "  + id + " " + 
            	      ") " + 
            	      "and author_id2 in " + 
            	      "( " + 
            	      "select author_id2 from coauthorshipnetwork where author_id1 = " + id + " and author_id2 <> "  + id + " " + 
            	      "union " + 
            	      "select author_id1 from coauthorshipnetwork where author_id2 = "  + id + " and author_id1 <> "  + id + " " + 
            	      ")");
            return rs;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }
}

   
