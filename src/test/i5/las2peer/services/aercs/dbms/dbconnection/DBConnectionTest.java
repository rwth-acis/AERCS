package i5.las2peer.services.aercs.dbms.dbconnection;

import static org.junit.Assert.*;

import org.junit.Test;

public class DBConnectionTest {

	@Test
	public void test() throws ConnectionException {
		DBConnection connection = new DBConnection();
		connection.setConnection();
	}

}
