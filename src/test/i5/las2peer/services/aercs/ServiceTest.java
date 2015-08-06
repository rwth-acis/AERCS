package i5.las2peer.services.aercs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import i5.las2peer.p2p.LocalNode;
import i5.las2peer.security.ServiceAgent;
import i5.las2peer.security.UserAgent;
import i5.las2peer.services.aercs.ServiceClass;
import i5.las2peer.testing.MockAgentFactory;
import i5.las2peer.webConnector.WebConnector;
import i5.las2peer.webConnector.client.ClientResponse;
import i5.las2peer.webConnector.client.MiniClient;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Random;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;


/**
 * Example Test Class demonstrating a basic JUnit test structure.
 * 
 * 
 *
 */
public class ServiceTest {
	
	private static final String HTTP_ADDRESS = "http://127.0.0.1";
	private static final int HTTP_PORT = WebConnector.DEFAULT_HTTP_PORT;
	
	private static LocalNode node;
	private static WebConnector connector;
	private static ByteArrayOutputStream logStream;
	
	private static UserAgent testAgent;
	private static final String testPass = "adamspass";
	
	private static final String testServiceClass = ServiceClass.class.getCanonicalName();
	
	private static final String mainPath = "aercs/";
	
	
	/**
	 * Called before the tests start.
	 * 
	 * Sets up the node and initializes connector and users that can be used throughout the tests.
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void startServer() throws Exception {
		
		//start node
		node = LocalNode.newNode();
		node.storeAgent(MockAgentFactory.getAdam());
		node.launch();
		
		ServiceAgent testService = ServiceAgent.generateNewAgent(testServiceClass, "a pass");
		testService.unlockPrivateKey("a pass");
		
		node.registerReceiver(testService);
		
		//start connector
		logStream = new ByteArrayOutputStream ();
		
		connector = new WebConnector(true,HTTP_PORT,false,1000);
		connector.setSocketTimeout(10000);
		connector.setLogStream(new PrintStream (logStream));
		connector.start ( node );
        Thread.sleep(1000); //wait a second for the connector to become ready
		testAgent = MockAgentFactory.getAdam();
		
        connector.updateServiceList();
        //avoid timing errors: wait for the repository manager to get all services before continuing
        try
        {
            System.out.println("waiting..");
            Thread.sleep(10000);
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }
		
	}
	
	
	/**
	 * Called after the tests have finished.
	 * Shuts down the server and prints out the connector log file for reference.
	 * 
	 * @throws Exception
	 */
	@AfterClass
	public static void shutDownServer () throws Exception {
		
		connector.stop();
		node.shutDown();
		
        connector = null;
        node = null;
        
        LocalNode.reset();
		
		System.out.println("Connector-Log:");
		System.out.println("--------------");
		
		System.out.println(logStream.toString());
		
    }
	
	
	/**
	 * 
	 * Tests the validation method.
	 * 
	 */
	@Test
	public void testValidateLogin()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			c.setLogin(Long.toString(testAgent.getId()), testPass);
            ClientResponse result=c.sendRequest("GET", mainPath +"validation", "");
            assertEquals(200, result.getHttpCode());
            assertTrue(result.getResponse().trim().contains("adam")); //login name is part of response
			System.out.println("Result of 'testValidateLogin': " + result.getResponse().trim());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
		
    }

	/**
	 * Test the ServiceClass for valid rest mapping.
	 * Important for development.
	 */
	@Test
	public void testDebugMapping()
	{
		ServiceClass cl = new ServiceClass();
		assertTrue(cl.debugMapping());
	}
	
	/////////////////////////// conferences ////////////////////////////////////
	@Test
	public void testConferenceList()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			c.setLogin(Long.toString(testAgent.getId()), testPass);
            ClientResponse result=c.sendRequest("GET", mainPath +"conferences", "");
            assertEquals( 200, result.getHttpCode() ); // check if request returns successfully
            
            assertTrue( isJSONValid(result.getResponse()) ); // check if response is valid json
            
            // check if first and last conferences' names start with 'A'
            JSONArray jsonResponse = (JSONArray) JSONValue.parseStrict(result.getResponse());
            if(!jsonResponse.isEmpty()){
            	String nameOfFirst = (String) ((JSONObject) jsonResponse.get(0)).get("name");
            	assertNotNull( nameOfFirst ); // assert that name exists            	
            	assertTrue( nameOfFirst.charAt(0) == 'A'); // check that name starts with A
            	
            	String nameOfLast = (String) ((JSONObject) jsonResponse.get(jsonResponse.size()-1)).get("name");
            	assertNotNull( nameOfLast ); // assert that name exists
            	assertTrue( nameOfLast.charAt(0) == 'A'); // check that name starts with A
            }
   			System.out.println("Result of 'testConferenceList': " + result.getResponse().trim());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
	}
	
	@Test
	public void testConferenceListWithRandomStartChar()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			Random r = new Random();
			char startChar = (char)(r.nextInt(26) + 'A');

			c.setLogin(Long.toString(testAgent.getId()), testPass);
            ClientResponse result=c.sendRequest("GET", mainPath +"conferences?startChar="+startChar, "");
            assertEquals( 200, result.getHttpCode() ); // check if request returns successfully
            
            assertTrue( isJSONValid(result.getResponse()) ); // check if response is valid json
            
            // check if first and last conferences' names start with 'A'
            JSONArray jsonResponse = (JSONArray) JSONValue.parseStrict(result.getResponse());
            if(!jsonResponse.isEmpty()){
            	String nameOfFirst = (String) ((JSONObject) jsonResponse.get(0)).get("name");
            	assertNotNull( nameOfFirst ); // assert that name exists            	
            	assertTrue( nameOfFirst.charAt(0) == startChar); // check that name starts with A
            	
            	String nameOfLast = (String) ((JSONObject) jsonResponse.get(jsonResponse.size()-1)).get("name");
            	assertNotNull( nameOfLast ); // assert that name exists
            	assertTrue( nameOfLast.charAt(0) == startChar); // check that name starts with A
            }
   			System.out.println("Result of 'testConferenceListWithRandomStartChar': " + result.getResponse().trim());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
	}
	
	@Test
	public void testFailConferenceListWithBadRequest()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			c.setLogin(Long.toString(testAgent.getId()), testPass);
            ClientResponse result=c.sendRequest("GET", mainPath +"conferences?startChar=AB", "");
            assertEquals( 400, result.getHttpCode() ); // check if request fails
            
            assertTrue(result.getResponse().trim().contains("startChar must be of length 1")); // should return error message
   			System.out.println("Result of 'testFailConferenceListWithBadRequest': " + result.getResponse().trim());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
	}
	
	/////////////////////////// journals ////////////////////////////////////
	@Test
	public void testJournalList()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			c.setLogin(Long.toString(testAgent.getId()), testPass);
            ClientResponse result=c.sendRequest("GET", mainPath +"journals", "");
            assertEquals( 200, result.getHttpCode() ); // check if request returns successfully
            
            assertTrue( isJSONValid(result.getResponse()) ); // check if response is valid json
            
            // check if first and last conferences' names start with 'A'
            JSONArray jsonResponse = (JSONArray) JSONValue.parseStrict(result.getResponse());
            if(!jsonResponse.isEmpty()){
            	String nameOfFirst = (String) ((JSONObject) jsonResponse.get(0)).get("name");
            	assertNotNull( nameOfFirst ); // assert that name exists            	
            	assertTrue( nameOfFirst.charAt(0) == 'A'); // check that name starts with A
            	
            	String nameOfLast = (String) ((JSONObject) jsonResponse.get(jsonResponse.size()-1)).get("name");
            	assertNotNull( nameOfLast ); // assert that name exists
            	assertTrue( nameOfLast.charAt(0) == 'A'); // check that name starts with A
            }
   			System.out.println("Result of 'testJournalList': " + result.getResponse().trim());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
	}
	
	@Test
	public void testJournalListWithRandomStartChar()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			Random r = new Random();
			char startChar = (char)(r.nextInt(26) + 'A');

			c.setLogin(Long.toString(testAgent.getId()), testPass);
            ClientResponse result=c.sendRequest("GET", mainPath +"journals?startChar="+startChar, "");
            assertEquals( 200, result.getHttpCode() ); // check if request returns successfully
            
            assertTrue( isJSONValid(result.getResponse()) ); // check if response is valid json
            
            // check if first and last conferences' names start with 'A'
            JSONArray jsonResponse = (JSONArray) JSONValue.parseStrict(result.getResponse());
            if(!jsonResponse.isEmpty()){
            	String nameOfFirst = (String) ((JSONObject) jsonResponse.get(0)).get("name");
            	assertNotNull( nameOfFirst ); // assert that name exists            	
            	assertTrue( nameOfFirst.charAt(0) == startChar); // check that name starts with A
            	
            	String nameOfLast = (String) ((JSONObject) jsonResponse.get(jsonResponse.size()-1)).get("name");
            	assertNotNull( nameOfLast ); // assert that name exists
            	assertTrue( nameOfLast.charAt(0) == startChar); // check that name starts with A
            }
   			System.out.println("Result of 'testJournalListWithRandomStartChar': " + result.getResponse().trim());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
	}
	
	@Test
	public void testFailJournalListWithBadRequest()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			c.setLogin(Long.toString(testAgent.getId()), testPass);
            ClientResponse result=c.sendRequest("GET", mainPath +"journals?startChar=AB", "");
            assertEquals( 400, result.getHttpCode() ); // check if request fails
            
            assertTrue(result.getResponse().trim().contains("startChar must be of length 1")); // should return error message
   			System.out.println("Result of 'testFailJournalListWithBadRequest': " + result.getResponse().trim());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
	}
	
	/////////////////////////// events ////////////////////////////////////
	@Test
	public void testEventListEvents()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			c.setLogin(Long.toString(testAgent.getId()), testPass);
            ClientResponse result=c.sendRequest("GET", mainPath +"events?id=1&item=1", "");
            assertEquals( 200, result.getHttpCode() ); // check if request returns successfully
            
            assertTrue( isJSONValid(result.getResponse()) ); // check if response is valid json
            
            JSONArray jsonResponse = (JSONArray) JSONValue.parseStrict(result.getResponse());
        	
            String series_name = (String) ((JSONObject) jsonResponse.get(0)).get("series_name");
            String newest_year = (String) ((JSONObject) jsonResponse.get(1)).get("newest_year");
            String event_country = (String) ((JSONObject) jsonResponse.get(2)).get("country");

            assertNotNull( series_name ); // assert that series_name exists            	
            assertNotNull( newest_year ); // assert that newest_year exists            	
            assertNotNull( event_country ); // assert that event_country exists            	
        	
   			System.out.println("Result of 'testEventListEvents': " + result.getResponse().trim());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
	}
	
	@Test
	public void testEventListMembers()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			c.setLogin(Long.toString(testAgent.getId()), testPass);
            ClientResponse result=c.sendRequest("GET", mainPath +"events?id=1&item=3", "");
            assertEquals( 200, result.getHttpCode() ); // check if request returns successfully
            
            assertTrue( isJSONValid(result.getResponse()) ); // check if response is valid json
            
            JSONArray jsonResponse = (JSONArray) JSONValue.parseStrict(result.getResponse());
        	
            String series_name = (String) ((JSONObject) jsonResponse.get(0)).get("series_name");
            String newest_year = (String) ((JSONObject) jsonResponse.get(1)).get("newest_year");
            String event_authors = (String) ((JSONObject) jsonResponse.get(2)).get("authorKeys");

            assertNotNull( series_name ); // assert that series_name exists            	
            assertNotNull( newest_year ); // assert that newest_year exists            	
            assertNotNull( event_authors ); // assert that event_authors exists            	
        	
   			System.out.println("Result of 'testEventListMembers': " + result.getResponse().trim());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
	}
	
	@Test
	public void testFailEventListWithBadRequest()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			c.setLogin(Long.toString(testAgent.getId()), testPass);
            ClientResponse result=c.sendRequest("GET", mainPath +"events", "");
            assertEquals( 400, result.getHttpCode() ); // check if request fails
        	
            assertTrue(result.getResponse().trim().contains("id should not be empty")); // should return error message
   			System.out.println("Result of 'testFailEventListWithBadRequest': " + result.getResponse().trim());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
	}
	
	/////////////////////////// seriesCharts ////////////////////////////////////
	@Test
	public void testSeriesCharts()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			c.setLogin(Long.toString(testAgent.getId()), testPass);
            ClientResponse result=c.sendRequest("GET", mainPath +"seriesCharts?id=1", "");
            assertEquals( 200, result.getHttpCode() ); // check if request returns successfully
            
            assertTrue( isJSONValid(result.getResponse()) ); // check if response is valid json
            
            JSONArray jsonResponse = (JSONArray) JSONValue.parseStrict(result.getResponse());
        	
            String url1 = (String) ((JSONObject) jsonResponse.get(0)).get("url1");
            String url2 = (String) ((JSONObject) jsonResponse.get(0)).get("url2");
            JSONArray urls = (JSONArray) ((JSONObject) jsonResponse.get(0)).get("urls");

            assertNotNull( url1 ); // assert that url1 exists            	
            assertNotNull( url2 ); // assert that url2 exists            	
            assertNotNull( urls ); // assert that urls exists            	
        	
   			System.out.println("Result of 'testSeriesCharts': " + result.getResponse().trim());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
	}
	
	@Test
	public void testFailSeriesChartsWithBadRequest()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			c.setLogin(Long.toString(testAgent.getId()), testPass);
            ClientResponse result=c.sendRequest("GET", mainPath +"seriesCharts", "");
            assertEquals( 400, result.getHttpCode() ); // check if request fails
        	
            assertTrue(result.getResponse().trim().contains("id should not be empty")); // should return error message
   			System.out.println("Result of 'testFailSeriesChartsWithBadRequest': " + result.getResponse().trim());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
	}

	/////////////////////////// event ////////////////////////////////////
	@Test
	public void testEvent()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			c.setLogin(Long.toString(testAgent.getId()), testPass);
            ClientResponse result=c.sendRequest("GET", mainPath +"event?id=1", "");
            assertEquals( 200, result.getHttpCode() ); // check if request returns successfully
            
            assertTrue( isJSONValid(result.getResponse()) ); // check if response is valid json
            
            JSONArray jsonResponse = (JSONArray) JSONValue.parseStrict(result.getResponse());
        	
            assertEquals(3, jsonResponse.size()); // check if response size is 3: eventdetails, relatedevents, urls
            
            String id = (String) ((JSONObject) jsonResponse.get(0)).get("id");
            assertEquals("1", id); // check if id is correct
        	
   			System.out.println("Result of 'testEvent': " + result.getResponse().trim());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
	}
	
	@Test
	public void testFailEventWithBadRequest()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			c.setLogin(Long.toString(testAgent.getId()), testPass);
            ClientResponse result=c.sendRequest("GET", mainPath +"events", "");
            assertEquals( 400, result.getHttpCode() ); // check if request fails
        	
            assertTrue(result.getResponse().trim().contains("id should not be empty")); // should return error message
   			System.out.println("Result of 'testFailEventWithBadRequest': " + result.getResponse().trim());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
	}	
	
	/////////////////////////// person ////////////////////////////////////
	@Test
	public void testPersonWithId()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			c.setLogin(Long.toString(testAgent.getId()), testPass);
            ClientResponse result=c.sendRequest("GET", mainPath +"person?id=1", "");
            assertEquals( 200, result.getHttpCode() ); // check if request returns successfully
            
            assertTrue( isJSONValid(result.getResponse()) ); // check if response is valid json
            
            JSONArray jsonResponse = (JSONArray) JSONValue.parseStrict(result.getResponse());
        	
            assertEquals(3, jsonResponse.size()); // check if response size is 3: persondetails, urls, participatedevents
            
            String id = (String) ((JSONObject) jsonResponse.get(0)).get("id");
            assertEquals("1", id); // check if id is correct
        	
   			System.out.println("Result of 'testPersonWithId': " + result.getResponse().trim());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
	}
	
	@Test
	public void testPersonWithKey()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			c.setLogin(Long.toString(testAgent.getId()), testPass);
            ClientResponse result=c.sendRequest("GET", mainPath +"person?key=homepages/a/EdoardoAiroldi", "");
            assertEquals( 200, result.getHttpCode() ); // check if request returns successfully
            
            assertTrue( isJSONValid(result.getResponse()) ); // check if response is valid json
            
            JSONArray jsonResponse = (JSONArray) JSONValue.parseStrict(result.getResponse());
        	
            assertEquals(3, jsonResponse.size()); // check if response size is 3: persondetails, urls, participatedevents
            
            String id = (String) ((JSONObject) jsonResponse.get(0)).get("id");
            assertEquals("1", id); // check if id is correct
        	
   			System.out.println("Result of 'testPersonWithKey': " + result.getResponse().trim());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
	}
	
	@Test
	public void testFailPersonWithBadRequest()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			c.setLogin(Long.toString(testAgent.getId()), testPass);
            ClientResponse result=c.sendRequest("GET", mainPath +"person", "");
            assertEquals( 400, result.getHttpCode() ); // check if request fails
        	
            assertTrue(result.getResponse().trim().contains("id or key should be provided")); // should return error message
   			System.out.println("Result of 'testFailPersonWithBadRequest': " + result.getResponse().trim());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
	}		
	
	
	
	@Ignore
	@Test
	public void testEventListt()
	{
		MiniClient c = new MiniClient();
		c.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		
		try
		{
			c.setLogin(Long.toString(testAgent.getId()), testPass);
            ClientResponse result=c.sendRequest("GET", mainPath +"events?id=a", "");
            assertEquals(200, result.getHttpCode());
            assertTrue(result.getResponse().trim().contains("adam")); //login name is part of response
			System.out.println("Result of 'testValidateLogin': " + result.getResponse().trim());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			fail ( "Exception: " + e );
		}
	}
	
	public boolean isJSONValid(String test) {
	    try {
	    	JSONValue.parseStrict(test);
	    } catch (Exception ex) {
	    	return false;
	    }
	    return true;
	}
}
