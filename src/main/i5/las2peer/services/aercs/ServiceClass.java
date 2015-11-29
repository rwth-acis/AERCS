package i5.las2peer.services.aercs;

import i5.las2peer.api.Service;
import i5.las2peer.logging.L2pLogger;
import i5.las2peer.logging.NodeObserver.Event;
import i5.las2peer.restMapper.HttpResponse;
import i5.las2peer.restMapper.MediaType;
import i5.las2peer.restMapper.RESTMapper;
import i5.las2peer.restMapper.annotations.ContentParam;
import i5.las2peer.restMapper.annotations.Version;
import i5.las2peer.restMapper.tools.ValidationResult;
import i5.las2peer.restMapper.tools.XMLCheck;
import i5.las2peer.security.UserAgent;
import i5.las2peer.services.aercs.dbms.bdobjects.AuthorNamePeer;
import i5.las2peer.services.aercs.dbms.bdobjects.EventSeriesPeer;
import i5.las2peer.services.aercs.dbms.bdobjects.Media;
import i5.las2peer.services.aercs.dbms.bdobjects.ObjectQuery;
import i5.las2peer.services.aercs.usermanager.EventSeries;

import java.awt.Rectangle;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import y.base.Edge;
import y.base.Node;
import y.io.GraphMLIOHandler;
import y.io.IOHandler;
import y.layout.BufferedLayouter;
import y.layout.Layouter;
import y.layout.circular.CircularLayouter;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.organic.OrganicLayouter;
import y.layout.organic.SmartOrganicLayouter;
import y.util.DataProviders;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.NodeRealizer;
import yext.svg.io.SVGIOHandler;
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
	
	// instantiate the logger class
	private final L2pLogger logger = L2pLogger.getInstance(ServiceClass.class.getName());
	
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
			@DefaultValue("A") @QueryParam("startChar") String startChar){
		int httpStatus = 200;
		String content = new String();
		if(startChar.length() != 1) {
			httpStatus = 400;
			content = "startChar must be of length 1";
		}
		else {
			EventSeriesPeer es = new EventSeriesPeer();
			JSONArray jsa = new JSONArray();
			try {
				ResultSet rs = es.selectConferences(startChar);
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
			} catch (Exception e) {
				// write error to logfile and console
				logger.log(Level.SEVERE, e.toString(), e);
				// create and publish a monitoring message
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, e.toString());
				
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
	public HttpResponse selectJournals(
			@DefaultValue("A") @QueryParam("startChar") String startChar){
		int httpStatus = 200;
		String content = new String();
		if(startChar.length() != 1) {
			httpStatus = 400;
			content = "startChar must be of length 1";
		}
		else {
			EventSeriesPeer es = new EventSeriesPeer();
			JSONArray jsa = new JSONArray();
			try {
				ResultSet rs = es.selectJournals(startChar);
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
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.toString(), e);
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, e.toString());
				
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
			@DefaultValue("") @QueryParam("id") String id,
			@DefaultValue("1") @QueryParam("item") String item){
		int httpStatus = 200;
		String content = new String();
		JSONArray jsa = new JSONArray();

		if(id.equals("")){
			httpStatus = 400;
			content = "id should not be empty";
		}
		else{
			EventSeriesPeer es = new EventSeriesPeer();
			ObjectQuery events = new ObjectQuery();
			
			try {
				String seriesName = events.querySSeries(id);
	            String newID = events.querySeriesNewestYear(id);
	
				JSONObject jso_name = new JSONObject();
				jso_name.put("series_name", seriesName);
				jsa.add(jso_name);
				JSONObject jso_year = new JSONObject();
				jso_year.put("newest_year", newID);
				jsa.add(jso_year);
							
				if(item.equals("1")){
					ResultSet rs = es.selectEventsForASeries(id);
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
				}
				if(item.equals("3")){
					ResultSet rs = events.searchAuthorKeyMembers(id);
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
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.toString(), e);
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, e.toString());

				httpStatus = 500;
				content = "A Database Error occured";
			}
		}
			
		if(httpStatus == 200) {
			content = jsa.toJSONString();
		}
		HttpResponse resp = new HttpResponse(content);
		resp.setStatus(httpStatus);
		return resp;
	}

	@GET
	@Path("seriesCharts")
	public HttpResponse getChartUrls(
			@DefaultValue("") @QueryParam("id") String id){
		int httpStatus = 200;
		String content = new String();
		JSONArray jsa = new JSONArray();

		if(id.equals("")){
			httpStatus = 400;
			content = "id should not be empty";
		}
		else{			
			EventSeriesPeer es = new EventSeriesPeer();
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

			} catch (Exception e) {
				logger.log(Level.SEVERE, e.toString(), e);
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, e.toString());

				httpStatus = 500;
				content = "A Database Error occured";
			}
		}
		
		if(httpStatus == 200) {
			content = jsa.toJSONString();
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
			@DefaultValue("") @QueryParam("id") String id){
		int httpStatus = 200;
		String content = new String();
		JSONArray jsa = new JSONArray();

		if(id.equals("")){
			httpStatus = 400;
			content = "id should not be empty";
		}
		else{
			ObjectQuery events = new ObjectQuery();
			
		    try {
				// id, name, country, year, series_id
			    ResultSet rs = events.searchEvent(id);		    
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
				
			    Vector<Media> relatedSeries = events.searchRelatedSeriesByEvent(Integer.parseInt(id));
			    Vector<Media> urls = events.searchWebsiteByEvent(Integer.parseInt(id));
			
			    jsa.add(relatedSeries);
			    jsa.add(urls);
		    } catch (Exception e) {
				logger.log(Level.SEVERE, e.toString(), e);
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, e.toString());

				httpStatus = 500;
				content = "A Database Error occured";
			}
		}
		
		if(httpStatus == 200) {
			content = jsa.toJSONString();
		}
		HttpResponse resp = new HttpResponse(content);
		resp.setStatus(httpStatus);
		return resp;
	}
	
	@GET
	@Path("person")
	public HttpResponse selectPerson(
			@DefaultValue("") @QueryParam("id") String id,
			@DefaultValue("") @QueryParam("key") String key){
		int httpStatus = 200;
		String content = new String();
		JSONArray jsa = new JSONArray();

		if(id.equals("") && key.equals("")){
			httpStatus = 400;
			content = "id or key should be provided";
		}
		else{    
		    try {
			    if (id == ""){
			        AuthorNamePeer anp = new AuthorNamePeer();
			        id = anp.getIdFromKey(key);
			    }
			    else if(key == "")
			    {
			        AuthorNamePeer anp = new AuthorNamePeer();
			        key = anp.getKeyFromId(id);
			    }
		
				ObjectQuery series = new ObjectQuery();
				// id, name
			    ResultSet rs = series.searchPerson(id);
			    
			    // u.url, u.description
			    ResultSet rs1 = series.searchPersonUrl(id);
			    Vector<EventSeries> events = series.searchEventByParticipant(id) ;

				JSONObject jso = new JSONObject();
				if (rs.next()) {
					jso.put("id", rs.getString(1));
					jso.put("name", rs.getString(2));
					jso.put("key", key);
				}
				jsa.add(jso);
				rs.getStatement().close();
				rs.close();
				
				JSONArray jsa1 = new JSONArray();
				while(rs1.next()){
					jso = new JSONObject();
					jso.put("url", rs1.getString(1));
					jso.put("description", rs1.getString(2));
					jsa1.add(jso);
				}
				jsa.add(jsa1);
				rs1.getStatement().close();
				rs1.close();
				
				jsa1 = new JSONArray();
				for(int i=0; i<events.size();i++){
					jso = new JSONObject();
					jso.put("id", events.get(i).getId());
					jso.put("name", events.get(i).getName());
					jso.put("abbr", events.get(i).getAbbreviation());
					jso.put("part_no", events.get(i).getParticipantNo());
					jso.put("event_no", events.get(i).getEventNo());
					jso.put("key", events.get(i).getKey());
					jsa1.add(jso);
				}
				jsa.add(jsa1);
	
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.toString(), e);
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, e.toString());

				httpStatus = 500;
				content = "A Database Error occured";
			}
		}

		if(httpStatus == 200) {
			content = jsa.toJSONString();
		}
		
		HttpResponse resp = new HttpResponse(content);
		resp.setStatus(httpStatus);
		return resp;
	}
	
	@GET
	@Path("ranking")
	public HttpResponse getRanking(
			@DefaultValue("0") @QueryParam("conf")  Integer conf,
			@DefaultValue("0") @QueryParam("journal") Integer journal,
			@DefaultValue("0") @QueryParam("domain")  Integer domain,
			@DefaultValue("1") @QueryParam("page") Integer page,
			@DefaultValue("5") @QueryParam("col") Integer col,
			@DefaultValue("0") @QueryParam("order") Integer order){
		int httpStatus = 200;
		String content = new String();
		JSONArray jsa = new JSONArray();

		if(conf.equals("") || journal.equals("") || domain.equals("") || page.equals("") || col.equals("") || order.equals("")) {
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

		    try {

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
				if(rs!=null){
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
					rs.getStatement().close();
					rs.close();
				}
				jsa.add(jsa1);

			} catch (Exception e) {
				logger.log(Level.SEVERE, e.toString(), e);
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, e.toString());

				httpStatus = 500;
				content = "A Database Error occured";
			}
		}
		
		if(httpStatus == 200) {
			content = jsa.toJSONString();
		}
		HttpResponse resp = new HttpResponse(content);
		resp.setStatus(httpStatus);
		return resp;
	}

	@GET
	@Path("seriesComparison")
	public HttpResponse selectedSeriesComparison(
			@DefaultValue("") @QueryParam("selectedSeries") String selectedSeries,
			@DefaultValue("") @QueryParam("searchKeyword") String searchKeyword,
			@DefaultValue("both") @QueryParam("typeOfSeriesSearchIn") String typeOfSeriesSearchIn,
			@DefaultValue("A") @QueryParam("startChar") String startChar){
		int httpStatus = 200;
		String content = new String();
		JSONArray jsa = new JSONArray();

		if(startChar.length() != 1) {
			httpStatus = 400;
			content = "startChar must be of length 1";
		}
		else if(!searchKeyword.equals("^^") &&
				(!typeOfSeriesSearchIn.equals("both") && !typeOfSeriesSearchIn.equals("conferences") && !typeOfSeriesSearchIn.equals("journals"))){
				httpStatus = 400;
				content = "typeOfSeriesSearchIn should be one among: both, conferences or journals";
		}
		else{
	        try {
	    		JSONObject jso = new JSONObject();
	    		EventSeriesPeer es = new EventSeriesPeer();
	            ResultSet rs=null;
	        	//leftdiv
	        	//id, name, abbreviation, series_key
	        	JSONArray jsa1 = new JSONArray();
	        	if(!searchKeyword.equals("^^")){
	                rs = es.searchSeries(searchKeyword, typeOfSeriesSearchIn);
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
	        	if(!selectedSeries.equals(""))
	            {
	        		try {
						selectedSeries = URLDecoder.decode(selectedSeries, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						logger.log(Level.SEVERE, e.toString(), e);
						L2pLogger.logEvent(this, Event.SERVICE_ERROR, e.toString());

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
	                        String seriesId = rs.getString(1);
	                        String seriesName = rs.getString(2) + " (" + rs.getString(3).toUpperCase() + ")";
	                        String seriesKey = rs.getString(4);
	    					jso = new JSONObject();
	    					jso.put("seriesID", seriesId);
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
				rs.getStatement().close();
				rs.close();

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
				rs.getStatement().close();
				rs.close();

			} catch (Exception e) {
				logger.log(Level.SEVERE, e.toString(), e);
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, e.toString());

				httpStatus = 500;
				content = "A Database Error occured";
			}
		}
		if(httpStatus == 200) {
			content = jsa.toJSONString();
		}
		HttpResponse resp = new HttpResponse(content);
		resp.setStatus(httpStatus);
		return resp;
	}
	
	@GET
	@Path("search")
	public HttpResponse searchResults(
			@DefaultValue("") @QueryParam("searchdata") String searchdata,
			@DefaultValue("1") @QueryParam("searchfield") String searchfield,
			@DefaultValue("1") @QueryParam("page") String page){
		int httpStatus = 200;
		String content = new String();

	    int resultPerPage = 25;
	    int resultNum = 0;
	    int currentPage = Integer.parseInt(page);
	    
	    int fromRow = (currentPage - 1) * resultPerPage + 1;
	    int toRow = fromRow + resultPerPage - 1;
	    
        ObjectQuery series = new ObjectQuery();
	    ResultSet rs = null;
        JSONArray jsa = new JSONArray();

		try {

		    if (searchfield.equals("1"))
		    {
	            resultNum = series.countPerson(searchdata);
				JSONObject jso = new JSONObject();
				jso.put("resultNum", resultNum);
				jsa.add(jso);
	
	            // a_key, a_name, a_p_num
	            rs = series.queryPerson(searchdata, fromRow, toRow);
				while (rs.next()) {
					jso = new JSONObject();
					jso.put("key", rs.getString(1));
					jso.put("name", rs.getString(2));
					jso.put("p_num", rs.getInt(3));
					jsa.add(jso);
				}
				rs.getStatement().close();
				rs.close();
		    }
		    else if(searchfield.equals("2")){
		    	resultNum = series.countEvent(searchdata);
				JSONObject jso = new JSONObject();
				jso.put("resultNum", resultNum);
				jsa.add(jso);
	
		    	// ev_id, ev_name, ev_series_key, ev_author_num
	            rs = series.queryEvent(searchdata, fromRow, toRow);
	           
				while (rs.next()) {
					jso = new JSONObject();
					jso.put("id", rs.getInt(1));
					jso.put("name", rs.getString(2));
					jso.put("series_key", rs.getString(3));
					jso.put("author_num", rs.getInt(4));
					jsa.add(jso);
				}
				rs.getStatement().close();
				rs.close();
		    }
		    else if(searchfield.equals("3")){
	            resultNum = series.countSeries(searchdata);
				JSONObject jso = new JSONObject();
				jso.put("resultNum", resultNum);
				jsa.add(jso);
	
	            // id, name, series_key
	            rs = series.searchSeries(searchdata, fromRow, toRow);
	            
					while (rs.next()) {
					jso = new JSONObject();
					jso.put("id", rs.getInt(1));
					jso.put("name", rs.getString(2));
					jso.put("series_key", rs.getString(3));
					jsa.add(jso);
				}
				rs.getStatement().close();
				rs.close();
		    }
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.toString(), e);
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, e.toString());

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
	
	@GET
	@Path("drawSeriesComparison")
	public HttpResponse drawSeriesComparison(
			@DefaultValue("") @QueryParam("selectedSeriesData") String selectedSeriesData,
			@DefaultValue("") @QueryParam("chartType") String chartType){
		int httpStatus = 200;
		String content = new String();
		
		try {
			selectedSeriesData = URLDecoder.decode(selectedSeriesData, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.log(Level.SEVERE, e.toString(), e);
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, e.toString());

			httpStatus = 500;
			content = "Decoding error";
		}
		
		EventSeriesPeer es = new EventSeriesPeer();
        StringTokenizer seriesTokens = new StringTokenizer(selectedSeriesData, ",");
        int totalCounts = seriesTokens.countTokens();

        String selectedSeries[][] = new String[totalCounts][2];
        int counter=0;     
		JSONArray jsa = new JSONArray();
		JSONObject jso = new JSONObject();
		try{
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
	        jso.put("selectedSeries", selectedSeries);
	        jsa.add(jso);
	        
	        String color[] = new String[selectedSeries.length];
	        float dx = 1.0f / (float) (color.length - 1);
	        for (int i = 0; i < color.length; i++) 
	        {
	            String rgb = Integer.toHexString((es.getColor(i * dx)).getRGB());
	            color[i] = rgb.substring(2, rgb.length());
	        }
	        String url[] = es.createGoogleURL(selectedSeries, chartType, color, true);
	
	        jso = new JSONObject();
	        jso.put("colors", color);
	        jsa.add(jso);
	        jso = new JSONObject();
	        jso.put("urls", url);
	        jsa.add(jso);

		} catch (Exception e) {
			logger.log(Level.SEVERE, e.toString(), e);
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, e.toString());

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
	
	@POST
	@Path("eventNetworkVisualization")
	public HttpResponse getEventNetworkVisualization(
			@DefaultValue("") @QueryParam("id") String id,
			@DefaultValue("circular") @QueryParam("layout") String layoutStr,
			@DefaultValue("") @QueryParam("search") String search,
			@DefaultValue("") @QueryParam("width") String widthStr,
			@DefaultValue("") @QueryParam("height") String heightStr,
			@ContentParam String graphML){
		int httpStatus = 200;
		String content = new String();
		JSONArray jsa = new JSONArray();
		IOHandler iohSVG = new SVGIOHandler();
		IOHandler iohGML = new GraphMLIOHandler();

		Graph2D graph = new Graph2D();
		double viewPointX = 0;
		double viewPointY = 0;
		
		if(id.equals("")){
			httpStatus = 400;
			content = "id should not be empty";
		}
		else if(!layoutStr.equals("circular") && !layoutStr.equals("organic") 
				&& !layoutStr.equals("smartOrganic") && !layoutStr.equals("hierarchic")){
			httpStatus = 400;
			content = "layout should be circular, organic, smartOrganic or hierarchic";
		}
		else if(search.length() > 0){
			search = search.toLowerCase();
			try {
				byte[] input = graphML.getBytes();
			    ByteArrayInputStream inputstream = new ByteArrayInputStream(input);

				iohGML.read(graph, inputstream);
			    Node[] nodes = graph.getNodeArray();

			    boolean isFound = false;
			    for(int i = 0; i < nodes.length && !isFound; i++){
			    	NodeRealizer nr = graph.getRealizer(nodes[i]);
			    	if(nr.getLabelText().toLowerCase().indexOf(search) >= 0){
			    		viewPointX = nr.getX();
			    		viewPointY = nr.getY();
			    		isFound = true;
			    	}
			    }

			} catch (IOException e) {
				logger.log(Level.SEVERE, e.toString(), e);
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, e.toString());
				
				httpStatus = 400;
				content = "graph is not proper";
			}
		}
		else{
			ObjectQuery events = new ObjectQuery();
			
			try {
				ResultSet event = events.searchEvent(id);
				event.next();
				String year = event.getString(4);
				String series_id = String.valueOf(event.getInt(5));
				event.getStatement().close();
				event.close();
				
				ResultSet rs = events.searchEventNetwork(series_id, year);
				while(rs.next()){
					JSONObject jso = new JSONObject();
					jso.put("s", rs.getInt(1));
					jso.put("d", rs.getInt(2));
					jso.put("sl", rs.getString(3));
					jso.put("dl", rs.getString(4));
					jsa.add(jso);
				}
				rs.getStatement().close();
				rs.close();
				
				graph = createTree( jsa ); 
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.toString(), e);
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, e.toString());

				httpStatus = 500;
				content = "A Database Error occured";
			}
		}
		
		
		if(httpStatus == 200) {
			try {
				setGraphView(graph, layoutStr, widthStr, heightStr, viewPointX, viewPointY);
				
				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				ByteArrayOutputStream outStreamGML = new ByteArrayOutputStream();

//				iohSVG.write(graph, "MySVG.svg");
				iohSVG.write(graph, outStream);
				iohGML.write(graph, outStreamGML);
				JSONObject jso = new JSONObject();
				jso.put("graphSVG", outStream.toString());
				jso.put("graphML", outStreamGML.toString());
				content = jso.toJSONString();
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.toString(), e);
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, e.toString());
			}  
		}
		HttpResponse resp = new HttpResponse(content);
		resp.setStatus(httpStatus);
		return resp;
	}
	
	@POST
	@Path("personNetworkVisualization")
	public HttpResponse getPersonNetworkVisualization(
			@DefaultValue("") @QueryParam("id") String id,
			@DefaultValue("circular") @QueryParam("layout") String layoutStr,
			@DefaultValue("") @QueryParam("search") String search,
			@DefaultValue("") @QueryParam("width") String widthStr,
			@DefaultValue("") @QueryParam("height") String heightStr,
			@ContentParam String graphML){
		
		int httpStatus = 200;
		String content = new String();
		JSONArray jsa = new JSONArray();
		IOHandler iohSVG = new SVGIOHandler();
		IOHandler iohGML = new GraphMLIOHandler();
		
		Graph2D graph = new Graph2D();
		double viewPointX = 0;
		double viewPointY = 0;

		if(id.equals("")){
			httpStatus = 400;
			content = "id should not be empty";
		}
		else if(!layoutStr.equals("circular") && !layoutStr.equals("organic") 
				&& !layoutStr.equals("smartOrganic") && !layoutStr.equals("hierarchic")){
			httpStatus = 400;
			content = "layout should be circular, organic, smartOrganic or hierarchic";
		}
		else if(search.length() > 0){
			search = search.toLowerCase();
			try {
				byte[] input = graphML.getBytes();
			    ByteArrayInputStream inputstream = new ByteArrayInputStream(input);
				
			    iohGML.read(graph, inputstream);
			    Node[] nodes = graph.getNodeArray();

			    boolean isFound = false;
			    for(int i = 0; i < nodes.length && !isFound; i++){
			    	NodeRealizer nr = graph.getRealizer(nodes[i]);
			    	if(nr.getLabelText().toLowerCase().indexOf(search) >= 0){
			    		viewPointX = nr.getX();
			    		viewPointY = nr.getY();
			    		isFound = true;
			    	}
			    }

			} catch (IOException e) {
				logger.log(Level.SEVERE, e.toString(), e);
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, e.toString());
			
				httpStatus = 400;
				content = "graph is not proper";
			}
		}
		else{
			ObjectQuery events = new ObjectQuery();
			
			try {	
				ResultSet rs = events.searchPersonNetwork(id);
				while(rs.next()){
					JSONObject jso = new JSONObject();
					jso.put("s", rs.getInt(1));
					jso.put("d", rs.getInt(2));
					jso.put("sl", rs.getString(3));
					jso.put("dl", rs.getString(4));
					jsa.add(jso);
				}
				rs.getStatement().close();
				rs.close();
				
				graph = createTree( jsa ); 
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.toString(), e);
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, e.toString());
				
				httpStatus = 500;
				content = "A Database Error occured";
			}
		}
		
		if(httpStatus == 200) {
			try {
				setGraphView(graph, layoutStr, widthStr, heightStr, viewPointX, viewPointY);
				
				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				ByteArrayOutputStream outStreamGML = new ByteArrayOutputStream();

				iohSVG.write(graph, outStream);
				iohGML.write(graph, outStreamGML);
				JSONObject jso = new JSONObject();
				jso.put("graphSVG", outStream.toString());
				jso.put("graphML", outStreamGML.toString());
				content = jso.toJSONString();
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.toString(), e);
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, e.toString());
			}  
		}
		HttpResponse resp = new HttpResponse(content);
		resp.setStatus(httpStatus);
		return resp;
	}
	
	@GET
	@Path("networkVisualization")
	public HttpResponse getNetworkVisualization(
			@DefaultValue("") @QueryParam("graphml") String graphml,
			@DefaultValue("") @QueryParam("layout") String layoutStr,
			@DefaultValue("") @QueryParam("width") String widthStr,
			@DefaultValue("") @QueryParam("height") String heightStr){
		int httpStatus = 200;
		String content = new String();
		JSONArray jsa = new JSONArray();
		File[] listOfFiles = null;
		int index = 0;
		
		try {
			graphml = java.net.URLDecoder.decode(graphml, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.log(Level.SEVERE, e.toString(), e);
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, e.toString());
		
			httpStatus = 400;
			content = "cannot decode URL argument";
		}
		
		if(!layoutStr.equals("circular") && !layoutStr.equals("organic") 
				&& !layoutStr.equals("smartOrganic") && !layoutStr.equals("hierarchic")){
			httpStatus = 400;
			content = "layout should be circular, organic, smartOrganic or hierarchic";
		}
		else{
			File folder = new File("resources/maps");
			listOfFiles = folder.listFiles();

			for(int i=0; i < listOfFiles.length; i++) {
				JSONObject jso = new JSONObject();
				String name = listOfFiles[i].getName();
				if(name.endsWith(".graphml")){
					String fileNameWithoutExt = name.replaceFirst("[.][^.]+$", "");
					jso.put("graphml", fileNameWithoutExt);
					jsa.add(jso);
				}
			}
			if(!graphml.equals("")){
				JSONObject jso = new JSONObject();
				jso.put("graphml", graphml);
				index = jsa.indexOf( jso );
				if(index == -1){
					httpStatus = 400;
					content = "could not found the given graphml file";
				}
			}
			else{
				index = 0;
			}
		}
		
		if(httpStatus == 200) {
			Graph2D graph = new Graph2D(); 

			try {
			    byte[] input = Files.readAllBytes(listOfFiles[index].toPath());
			    ByteArrayInputStream inputstream = new ByteArrayInputStream(input);

				IOHandler ioh = new GraphMLIOHandler();
				ioh.read(graph, inputstream);

				setGraphView(graph, layoutStr, widthStr, heightStr, 0, 0);
				
				ioh = new SVGIOHandler();  
				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				ioh.write(graph, outStream);
				JSONObject jso = new JSONObject();
				jso.put("graph", outStream.toString());
				jsa.add(jso);
				content = jsa.toJSONString();
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.toString(), e);
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, e.toString());
			}  
		}
		HttpResponse resp = new HttpResponse(content);
		resp.setStatus(httpStatus);
		return resp;
	}
	
		
	private void setGraphView(Graph2D graph, String layout, String widthStr, String heightStr, double viewPointX, double viewPointY){
		
		///////////////////// Set Layout ////////////////////////////
		Layouter layouter = null;
		if(layout.equals("circular")){
			layouter = new CircularLayouter();
		}
		else if(layout.equals("organic")){
		    OrganicLayouter organicLayouter = new OrganicLayouter();
		    organicLayouter.setInitialPlacement(OrganicLayouter.AS_IS);
			layouter = organicLayouter;
		}
		else if(layout.equals("smartOrganic")){
		    SmartOrganicLayouter smartOrganicLayouter = new SmartOrganicLayouter();
		    graph.addDataProvider(SmartOrganicLayouter.NODE_SUBSET_DATA, DataProviders.createConstantDataProvider(Boolean.FALSE));
		    smartOrganicLayouter.setScope(SmartOrganicLayouter.SCOPE_ALL);
		    smartOrganicLayouter.setMinimalNodeDistance(20);
			layouter = smartOrganicLayouter;
		}
		else if(layout.equals("hierarchic")){
		    IncrementalHierarchicLayouter hierarchicLayouter = new IncrementalHierarchicLayouter();
		    hierarchicLayouter.getEdgeLayoutDescriptor().setOrthogonallyRouted(true);
			layouter = hierarchicLayouter;
		}
	    new BufferedLayouter(layouter).doLayout(graph);

	    ///////////////////// Set Dimensions and Zoom ///////////////
		int width, height;
		Rectangle box = graph.getBoundingBox();
		if( widthStr.equals("") || heightStr.equals("") ){
			width= box.x;
			height = box.y;
		}
		else{
			width = Integer.parseInt(widthStr);
			height = Integer.parseInt(heightStr);
		}
		
		Graph2DView viewPort = new SVGIOHandler().createDefaultGraph2DView(graph);
		viewPort.setSize(width, height);

		if(viewPointX != 0 || viewPointY != 0){
//			int centerSetOffX = (int)((viewPointX)-(width/2));
//			int centerSetOffY = (int)((viewPointY)-(height/2));
//			double zoomW = ((double)width)/(box.width-centerSetOffX);
//			double zoomH = ((double)height)/(box.height-centerSetOffY);
//			double zoom = (zoomH>zoomW)? zoomW: zoomH; 
//			viewPort.setZoom(zoom);
						
//			viewPort.setViewPoint(centerSetOffX, centerSetOffY);
//			int sizeX = centerSetOffX<0? (box.width-centerSetOffX): (box.width);
//			int sizeY = centerSetOffY<0? (box.height-centerSetOffY): (box.height);
//			viewPort.setSize(sizeX, sizeY);
			
			viewPort.fitContent();
			viewPort.setCenter(viewPointX, viewPointY);
//			zoom = viewPort.getZoom();
		}
		else{
			viewPort.fitContent();
		}

		graph.setCurrentView(viewPort);
	}
	
	private Graph2D createTree(JSONArray data) {

	    try {	      
	      Graph2D graph = new Graph2D();
	      Vector<Integer> nodelist = new Vector<Integer>();
	      int count = 0;

	      for(int j=0; j<data.size(); j++) {
	    	JSONObject obj = (JSONObject) data.get(j);
	        int s = (int) obj.get("s"), d = (int) obj.get("d");
	        String sl = (String) obj.get("sl"), dl = (String) obj.get("dl");
	        Node nlist[] = graph.getNodeArray();
	        Node sn, dn;
	        try {
	          if(!(nodelist.contains(s))){ 
	            nodelist.add(s);
	            sn = graph.createNode(0, 0, 80, 30, sl);
	            nlist = graph.getNodeArray();
	          } else {
	            sn = nlist[nodelist.indexOf(s)];
	          }
	          if (!(nodelist.contains(d))) { 
	            nodelist.add(d);
	            dn = graph.createNode(0, 0, 80, 30, dl);
	          } else {
	            dn = nlist[nodelist.indexOf(d)];
	          }
	          Edge e1 = sn.getEdgeTo(dn);
	          Edge e2 = sn.getEdgeFrom(dn);
	          if (e1 == null && e2 == null) {
	            graph.createEdge(sn,dn);
	          }
	        } catch(Exception e) {
	          logger.log(Level.SEVERE, e.toString(), e);
			  L2pLogger.logEvent(this, Event.SERVICE_ERROR, e.toString());

	          System.out.println("nodelist size: "+ nodelist.size());
	          System.out.println("nlist: ");
	          for(int i = 0; i < nlist.length; i++)
	            System.out.print(nlist[i].index()+",");
	        }
	        count++;
	      }
	      
	      if(count == 0)
	        graph.createNode(0, 0, 80, 30, "No members");
	      
	      return graph;
	    }
	    catch (Exception e) {
			logger.log(Level.SEVERE, e.toString(), e);
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, e.toString());
	    }

	    return null;
	  }
	
}
