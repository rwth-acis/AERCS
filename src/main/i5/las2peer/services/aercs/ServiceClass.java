package i5.las2peer.services.aercs;

import i5.las2peer.api.Service;
import i5.las2peer.restMapper.HttpResponse;
import i5.las2peer.restMapper.MediaType;
import i5.las2peer.restMapper.RESTMapper;
import i5.las2peer.restMapper.annotations.GET;
import i5.las2peer.restMapper.annotations.Path;
import i5.las2peer.restMapper.annotations.Produces;
import i5.las2peer.restMapper.annotations.QueryParam;
import i5.las2peer.restMapper.annotations.Version;
import i5.las2peer.restMapper.tools.ValidationResult;
import i5.las2peer.restMapper.tools.XMLCheck;
import i5.las2peer.security.UserAgent;
import i5.las2peer.services.aercs.dbms.bdobjects.EventSeriesPeer;
import i5.las2peer.services.aercs.dbms.bdobjects.Media;
import i5.las2peer.services.aercs.dbms.bdobjects.ObjectQuery;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.StringTokenizer;
import java.util.Vector;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

/**
 * LAS2peer Service
 * 
 * This is a template for a very basic LAS2peer service
 * that uses the LAS2peer Web-Connector for RESTful access to it.
 * 
 */
@Path("aercs")
@Version("0.1")
@Produces(MediaType.APPLICATION_JSON)
public class ServiceClass extends Service {
	
	public ServiceClass() {
		// read and set properties values
		// IF THE SERVICE CLASS NAME IS CHANGED, THE PROPERTIES FILE NAME NEED TO BE CHANGED TOO!
		setFieldValues();
	}

	/**
	 * Simple function to validate a user login.
	 * Basically it only serves as a "calling point" and does not really validate a user
	 * (since this is done previously by LAS2peer itself, the user does not reach this method
	 * if he or she is not authenticated).
	 * 
	 */
	@GET
	@Path("validation")
	@Produces(MediaType.TEXT_PLAIN)
	public HttpResponse validateLogin() {
		String returnString = "";
		returnString += "You are " + ((UserAgent) getActiveAgent()).getLoginName() + " and your login is valid!";
		HttpResponse res = new HttpResponse(returnString);
		res.setStatus(200);
		return res;
	}

	/**
	 * Method for debugging purposes.
	 * Here the concept of restMapping validation is shown.
	 * It is important to check, if all annotations are correct and consistent.
	 * Otherwise the service will not be accessible by the WebConnector.
	 * Best to do it in the unit tests.
	 * To avoid being overlooked/ignored the method is implemented here and not in the test section.
	 * @return  true, if mapping correct
	 */
	public boolean debugMapping() {
		String XML_LOCATION = "./restMapping.xml";
		String xml = getRESTMapping();

		try {
			RESTMapper.writeFile(XML_LOCATION, xml);
		} catch (IOException e) {
			e.printStackTrace();
		}

		XMLCheck validator = new XMLCheck();
		ValidationResult result = validator.validate(xml);

		if (result.isValid())
			return true;
		return false;
	}

	/**
	 * This method is needed for every RESTful application in LAS2peer. There is no need to change!
	 * 
	 * @return the mapping
	 */
	public String getRESTMapping() {
		String result = "";
		try {
			result = RESTMapper.getMethodsAsXML(this.getClass());
		} catch (Exception e) {

			e.printStackTrace();
		}
		return result;
	}
	
	///////////////////////////////////////////////////////////////////////////////
	////////	Event Series Peer
	//////////////////////////////////////////////////////////////////////////////

	/*
	 * Returns all conferences whose name begin with startChar.
	 */
	@GET
	@Path("conferences")
	public HttpResponse selectConferences(
			@QueryParam(name="startChar", defaultValue="A") String startChar){
		int httpStatus = 200;
		String content = new String();
		if(startChar.length() != 1) {
			httpStatus = 400;
			content = "startChar must be of length 1";
		}
		else {
			EventSeriesPeer es = new EventSeriesPeer();
			ResultSet rs = es.selectConferences(startChar);
			JSONArray jsa = new JSONArray();
			try {
				while (rs.next()) {
					JSONObject jso = new JSONObject();
					jso.put("id", rs.getString(1));
					jso.put("name", rs.getString(2));
					jso.put("abbreviation", rs.getString(3));
					jso.put("series_key", rs.getString(4));
					jsa.add(jso);
				}
				rs.getStatement().close();
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
				httpStatus = 500;
				content = "A Database Error occured";
			}
			if(httpStatus == 200) {
				content = jsa.toJSONString();
			}
		}
		HttpResponse resp = new HttpResponse(content);
		resp.setStatus(httpStatus);
		return resp;
	}
	
	/*
	 * Returns all conferences whose name begin with startChar.
	 */
	@GET
	@Path("journals")
	public HttpResponse selectJournals(@QueryParam(
			name="startChar", defaultValue="A") String startChar){
		int httpStatus = 200;
		String content = new String();
		if(startChar.length() != 1) {
			httpStatus = 400;
			content = "startChar must be of length 1";
		}
		else {
			EventSeriesPeer es = new EventSeriesPeer();
			ResultSet rs = es.selectJournals(startChar);
			JSONArray jsa = new JSONArray();
			try {
				while (rs.next()) {
					JSONObject jso = new JSONObject();
					jso.put("id", rs.getString(1));
					jso.put("name", rs.getString(2));
					jso.put("abbreviation", rs.getString(3));
					jso.put("series_key", rs.getString(4));
					jsa.add(jso);
				}
				rs.getStatement().close();
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
				httpStatus = 500;
				content = "A Database Error occured";
			}
			if(httpStatus == 200) {
				content = jsa.toJSONString();
			}
		}
		HttpResponse resp = new HttpResponse(content);
		resp.setStatus(httpStatus);
		return resp;
	}

	
	/*
	 * Returns all events with given series.
	 */
	@GET
	@Path("events")
	public HttpResponse selectEvents(
			@QueryParam(name="series", defaultValue = "") String series,
			@QueryParam(name="id", defaultValue = "-1") String id,
			@QueryParam(name="item", defaultValue = "1") String item){
		int httpStatus = 200;
		String content = new String();
		if(series.length() == 0) {
			httpStatus = 400;
			content = "series should not be empty";
		}
		else if(id.equals("-1")) {
			httpStatus = 400;
			content = "id should not be empty";
		}
		else {
			EventSeriesPeer es = new EventSeriesPeer();
			ObjectQuery events = new ObjectQuery();
			String seriesName = events.querySSeries(id);
            String newID = events.querySeriesNewestYear(id);

			JSONArray jsa = new JSONArray();
			JSONObject jso_name = new JSONObject();
			jso_name.put("series_name", seriesName);
			jsa.add(jso_name);
			JSONObject jso_year = new JSONObject();
			jso_year.put("newest_year", newID);
			jsa.add(jso_year);
						
			if(item.equals("1")){
				ResultSet rs = es.selectEventsForASeries(id);
				try {
					while (rs.next()) {
						JSONObject jso = new JSONObject();
						jso.put("id", rs.getString(1));
						jso.put("name", rs.getString(2));
						jso.put("year", rs.getString(3));
						jso.put("country", rs.getString(4));
						jso.put("event_key", rs.getString(5));
						jsa.add(jso);
					}
					rs.getStatement().close();
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
					httpStatus = 500;
					content = "A Database Error occured";
				}
			}
			if(item.equals("3")){
				ResultSet rs = events.searchAuthorKeyMembers(id);
				try {
					while (rs.next() && rs.getInt(1) >= 4) {
	                	JSONObject jso = new JSONObject();
		                int count = rs.getInt(1);
		                JSONArray authorKeys = new JSONArray();
		                JSONArray authorNames = new JSONArray();
		                while(rs.getInt(1) == count)
		                {
		                	authorKeys.add(rs.getString(2));
		                	authorNames.add(rs.getString(3));
		                	if (!rs.next()) break;
		                }
						jso.put("count", count);
						jso.put("authorKeys", authorKeys);
						jso.put("authorNames", authorNames);
						jsa.add(jso);
					}
					rs.getStatement().close();
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
					httpStatus = 500;
					content = "A Database Error occured";
				}
			}
			if(httpStatus == 200) {
				content = jsa.toJSONString();
			}
		}
		HttpResponse resp = new HttpResponse(content);
		resp.setStatus(httpStatus);
		return resp;
	}
	
	/*
	 * Returns all conferences whose name begin with startChar.
	 */
	@GET
	@Path("seriesCharts")
	public HttpResponse getChartUrls(
			@QueryParam(name="id", defaultValue="-1") String id){
		int httpStatus = 200;
		String content = new String();
		if(id == "-1") {
			httpStatus = 400;
			content = "Id must be given";
		}
		else {
			EventSeriesPeer es = new EventSeriesPeer();
			JSONArray jsa = new JSONArray();
			try {			    
			    String color1[] = {"0000FF"};
			    ResultSet rs1 = es.queryDevelopmentChart(id);
			    String url1 = "http:/ /chart.apis.google.com/chart"+
			                  "?chs=450x450"+
			                  "&cht=lxy"+
			                  "&chtt=Development of the Community (Authors/Year)"+
			                  "&chco=0000ff"+
			                  "&chls=2"+
			                  "&chma=0,0,10,40"+
			                  "&chds=0,2,0.0,0.0"+
			                  "&chd=t:0|0"+
			                  "&chm=o,0000ff,0,-1,5"+
			                  "&chg=10,10"+
			                  "&chxt=x,y,r"+
			                  "&chxr=0,0,2|1,0.0,0.0|2,0.0,0.0";
			    if(rs1!=null)
			        url1 = es.createSingleGoogleChartURL(color1, "Development of the Community (Authors/Year)", rs1, 2, false);

			    String color2[] = {"0000FF"};
			    ResultSet rs2 = es.queryContinuityChart(id);
			    String url2 = "http://chart.apis.google.com/chart"+
			                  "?chs=450x450"+
			                  "&cht=lxy"+
			                  "&chtt=Continuity of the Community (Preceedings/Authors)"+
			                  "&chco=0000ff"+
			                  "&chls=2"+
			                  "&chma=0,0,10,40"+
			                  "&chds=0,2,0.0,0.0"+
			                  "&chd=t:0|0"+
			                  "&chm=o,0000ff,0,-1,5"+
			                  "&chg=10,10"+
			                  "&chxt=x,y,r"+
			                  "&chxr=0,0,2|1,0.0,0.0|2,0.0,0.0";
			    if(rs2!=null)
			        url2 = es.createSingleGoogleChartURL(color2, "Continuity of the Community (Preceedings/Authors)", rs2, 2, false);
			    
			    id = ","+id;
			    StringTokenizer seriesTokens = new StringTokenizer(id, ",");
			    int totalCounts = seriesTokens.countTokens();
			                         
			    String selectedSeries[][] = new String[totalCounts][2];
			                        
			    int counter=0;     
			    while(seriesTokens.hasMoreTokens()) 
			    {
			        String seriesID = seriesTokens.nextToken();                    //Series ID
			        selectedSeries[counter][0] = seriesID;
			        ResultSet rs = es.getSeriesInfo(seriesID);
			                                                                        
			        while (rs.next()) 
			        {
			            String seriesName = rs.getString(2) + " (" + rs.getString(3).toUpperCase() + ")";   
			            selectedSeries[counter][1] = seriesName;                //Series Name
			        }
			        rs.getStatement().close();
			        rs.close();
			        counter++;
			    }
			    
			    String color3[] = {"0000FF"};
			    String url[] = es.createGoogleURL(selectedSeries, "r", color3, true);
			    
            	JSONObject jso = new JSONObject();
            	jso.put("url1", url1);
            	jso.put("url2", url2);
            	jso.put("urls", url);
            	jsa.add(jso);

			} catch (SQLException e) {
				e.printStackTrace();
				httpStatus = 500;
				content = "A Database Error occured";
			}
			if(httpStatus == 200) {
				content = jsa.toJSONString();
			}
		}
		HttpResponse resp = new HttpResponse(content);
		resp.setStatus(httpStatus);
		return resp;
	}
	
	/*
	 * Returns details of event.
	 */
	@GET
	@Path("event")
	public HttpResponse selectEvent(
			@QueryParam(name="id", defaultValue = "-1") String id){
		int httpStatus = 200;
		String content = new String();
		if(id.equals("-1")) {
			httpStatus = 400;
			content = "id should not be empty";
		}
		else {
			ObjectQuery events = new ObjectQuery();
			
			// id, name, country, year, series_id
		    ResultSet rs = events.searchEvent(id);
		    
			JSONArray jsa = new JSONArray();
		    try {
				if (rs.next()) {
					JSONObject jso = new JSONObject();
					jso.put("id", rs.getString(1));
					jso.put("name", rs.getString(2));
					jso.put("country", rs.getString(3));
					jso.put("year", rs.getString(4));
					jso.put("series_id", rs.getString(5));
					jsa.add(jso);
				}
				rs.getStatement().close();
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
				httpStatus = 500;
				content = "A Database Error occured";
			}

		    Vector<Media> relatedSeries = events.searchRelatedSeriesByEvent(Integer.parseInt(id));
		    Vector<Media> urls = events.searchWebsiteByEvent(Integer.parseInt(id));

		    jsa.add(relatedSeries);
		    jsa.add(urls);

			if(httpStatus == 200) {
				content = jsa.toJSONString();
			}
		}
		HttpResponse resp = new HttpResponse(content);
		resp.setStatus(httpStatus);
		return resp;
	}
	
	@GET
	@Path("ranking")
	public HttpResponse getRanking(
			@QueryParam(name="conf", defaultValue = "-1") Integer conf,
			@QueryParam(name="journal", defaultValue = "-1") Integer journal,
			@QueryParam(name="domain", defaultValue = "-1") Integer domain,
			@QueryParam(name="page", defaultValue = "-1") Integer page,
			@QueryParam(name="col", defaultValue = "-1") Integer col,
			@QueryParam(name="order", defaultValue = "-1") Integer order){
		int httpStatus = 200;
		String content = new String();
		if(conf.equals("-1") || journal.equals("-1") || domain.equals("-1") || page.equals("-1") || col.equals("-1") || order.equals("-1")) {
			httpStatus = 400;
			content = "missing argument";
		}
		else {
			EventSeriesPeer es = new EventSeriesPeer();
			
		    String queryString = "conf=" + conf + "&journal=" + journal + "&domain=" + domain;
		    String pagedQueryString = "page=1&" + queryString;
		    String sortedQueryString = queryString + "&col=" + col + "&order=" + order;

		    int resultPerPage = 25;
		    int firstRow = (page - 1) * resultPerPage + 1;
		    int lastRow = firstRow + resultPerPage - 1;
		    int domainId = domain;

		    ResultSet rs = null;
		    int resultNum = 0;

		    int type = -1;

		    if ( conf == 1 && journal == 1 )
		    {
		        type = 0;
		    }
		    else if ( conf == 1 )
		    {
		        type = 1;
		    }
		    else if ( journal == 1)
		    {
		        type = 2;
		    }
		    
		    if (type >= 0)
		    {
		        rs = es.getRankings(firstRow, lastRow, col, order, domainId, type);
		        resultNum = es.countRankings(col, order, domainId, type);
		    }
		    
		    // id, name
		    ResultSet domainRs = es.getDomains();

			JSONArray jsa = new JSONArray();
			JSONObject jso = new JSONObject();
			JSONArray jsa1 = new JSONArray();
			jso.put("resultNum", resultNum);
			jso.put("resultPerPage", resultPerPage);
			jso.put("conf", conf);
			jso.put("journal", journal);
			jso.put("domain", domain);
			jso.put("page", page);
			jso.put("col", col);
			jso.put("order", order);
			jso.put("queryString", queryString);
			jso.put("pagedQueryString", pagedQueryString);
			jso.put("sortedQueryString", sortedQueryString);

			jsa.add(jso);

		    try {
				jsa1 = new JSONArray();
				while (domainRs.next()) {
					jso = new JSONObject();
					jso.put("id", domainRs.getString(1));
					jso.put("name", domainRs.getString(2));
					jsa1.add(jso);
				}
				jsa.add(jsa1);
				domainRs.getStatement().close();
				domainRs.close();

				jsa1 = new JSONArray();
				while (rs.next()) {
					jso = new JSONObject();
					jso.put("id", rs.getString(1));
					jso.put("name", rs.getString(2));
					jso.put("abbr", rs.getString(3));
					jso.put("series_key", rs.getString(4));
					jso.put("bn", rs.getInt(5));
					jso.put("pr", rs.getInt(6));
					jso.put("au", rs.getInt(7));
					jso.put("hu", rs.getInt(8));
					jsa1.add(jso);
				}
				jsa.add(jsa1);
				rs.getStatement().close();
				rs.close();

			} catch (SQLException e) {
				e.printStackTrace();
				httpStatus = 500;
				content = "A Database Error occured";
			}

			if(httpStatus == 200) {
				content = jsa.toJSONString();
			}
		}
		HttpResponse resp = new HttpResponse(content);
		resp.setStatus(httpStatus);
		return resp;
	}

	@GET
	@Path("seriesComparison")
	public HttpResponse selectedSeriesComparison(
			@QueryParam(name="seriesType", defaultValue = "") String seriesType,
			@QueryParam(name="selectedSeries", defaultValue = "") String selectedSeries,
			@QueryParam(name="searchKeyword", defaultValue = "") String searchKeyword,
			@QueryParam(name="typeOfSeriesSearchIn", defaultValue = "") String typeOfSeriesSearchIn,
			@QueryParam(name="startChar", defaultValue = "A") String startChar){
		int httpStatus = 200;
		String content = new String();
		
		EventSeriesPeer es = new EventSeriesPeer();

		JSONArray jsa = new JSONArray();
		JSONObject jso = new JSONObject();
        
        ResultSet rs=null;
        if(searchKeyword.equals("undefined"))
        {
            searchKeyword = "^^";
        }
        else
        {
            rs = es.searchSeries(searchKeyword, typeOfSeriesSearchIn);
        }
        
        try {
        	//leftdiv
        	//id, name, abbreviation, series_key
        	JSONArray jsa1 = new JSONArray();
        	if(!searchKeyword.equals("^^")){
				while (rs.next()) {
					jso = new JSONObject();
					jso.put("id", rs.getString(1));
					jso.put("name", rs.getString(2));
					jso.put("abbreviation", rs.getString(3));
					jso.put("series_key", rs.getString(4));
					jsa1.add(jso);
				}
				rs.getStatement().close();
				rs.close();
        	}
			jsa.add(jsa1);

			//rightdiv
        	jsa1 = new JSONArray();
        	if(!selectedSeries.equals("undefined"))
            {
        		try {
					selectedSeries = URLDecoder.decode(selectedSeries, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
					httpStatus = 500;
					content = "Decoding error";
				}
                StringTokenizer st1 = new StringTokenizer(selectedSeries , ",");
                while (st1.hasMoreTokens()) 
                {
                    String seriesID=st1.nextToken(); 
                    rs = es.getSeriesInfo(seriesID);
                    while (rs.next()) 
                    {
                        String seriesName = rs.getString(2) + " (" + rs.getString(3).toUpperCase() + ")";
                        String seriesKey = rs.getString(4);
    					jso = new JSONObject();
    					jso.put("seriesID", seriesID);
    					jso.put("seriesName", seriesName);
    					jso.put("seriesKey", seriesKey);
    					jsa1.add(jso);
                    }
    				rs.getStatement().close();
    				rs.close();
                }
            }
			jsa.add(jsa1);
			
        	jsa1 = new JSONArray();
        	//id, name, abbreviation, series_key
            rs = es.selectConferences(startChar);
            while(rs.next()){
				jso = new JSONObject();
				jso.put("id", rs.getString(1));
				jso.put("name", rs.getString(2));
				jso.put("abbreviation", rs.getString(3));
				jso.put("series_key", rs.getString(4));
				jsa1.add(jso);
            }
			jsa.add(jsa1);
			
			jsa1 = new JSONArray();
        	//id, name, abbreviation, series_key
            rs = es.selectJournals(startChar);
            while(rs.next()){
				jso = new JSONObject();
				jso.put("id", rs.getString(1));
				jso.put("name", rs.getString(2));
				jso.put("abbreviation", rs.getString(3));
				jso.put("series_key", rs.getString(4));
				jsa1.add(jso);
            }
			jsa.add(jsa1);

		} catch (SQLException e) {
			e.printStackTrace();
			httpStatus = 500;
			content = "A Database Error occured";
		}
		        
		if(httpStatus == 200) {
			content = jsa.toJSONString();
		}
		HttpResponse resp = new HttpResponse(content);
		resp.setStatus(httpStatus);
		return resp;
	}
}
