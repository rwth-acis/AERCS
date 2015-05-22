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

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

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

}
