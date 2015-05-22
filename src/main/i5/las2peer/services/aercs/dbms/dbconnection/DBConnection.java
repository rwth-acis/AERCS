package i5.las2peer.services.aercs.dbms.dbconnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * Created on 09.03.2004
 * This class get the database connection information and set up the connection.
 * @author cao
 */
public class DBConnection {
    private static String dbConnectionString;

    private static String dbDriver;

    private static String dbUserName;

    private static String dbUserPassword;

    private static Connection dbConnection = null;

    private Statement dbStatement = null;

    private ResultSet rs = null;

    /*private static final String databaseDriver="oracle.jdbc.driver.OracleDriver";
  private static final String databaseHost="robinie";
  private static final String databaseUserName="amsdb";
  private static final String databaseUserPassword="amsdb";
  private static final String databasePort="1521";
  private static final String databaseProtocol="jdbc:oracle:thin:";
  private static final String databaseName="testbas2";*/


    /**
	 * set constants for data connection
	 */
    public DBConnection() {
        dbConnectionString =
        	"jdbc:oracle:thin:" + "@" + "137.226.232.104" +
            ":" +"1521" + ":" +
            "aercs";
        dbUserName = "aercs_tt";
        //dbUserName = "aercs";
        dbUserPassword = "aercsadmin";
        dbDriver = "oracle.jdbc.driver.OracleDriver";

    }

    /*public Connection getStaticConnection() throws ConnectionException
  {
    Connection con= null;
    try
    { 
      //System.out.println("test dbcon: "+dbConnectionString + " " + dbUserName + " " +dbDriver);
      Class.forName(databaseDriver);
      String databaseConnectionString = databaseProtocol +"@"
                          + databaseHost +":"
                          + databasePort +":"
                          + databaseName;
      con = DriverManager.getConnection(databaseConnectionString, databaseUserName, databaseUserPassword);
    }
    catch (SQLException e)
    {
      throw new ConnectionException( "DATABASE ACCESS ERROR (Static): " + e.getSQLState() + e.getMessage() );
    } // catch (SQLException e)
    catch (ClassNotFoundException e)
    {
      throw new ConnectionException( "ERROR DB-DRIVER: " + e.getMessage() );
    } // catch (ClassNotFoundException e)
   return con;
  }*/

    /**
   * makes a database connection
   * @throws i5.las2peer.services.aercs.dbms.dbconnection.las.acis.dbms.ConnectionException
   */
    public void setConnection() throws ConnectionException {
        try {
/*        if (dbConnection == null) {*/
/* changed if condition on Feb 05 2011*/
         if ((dbConnection == null) ||
             ((dbConnection != null) && (dbConnection.isClosed() == true))) {
            Connection con = null;
                System.out.println("test dbcon: "+dbConnectionString + " " + dbUserName + " " +dbDriver);
                Class.forName(dbDriver);
                con =
                    DriverManager.getConnection(dbConnectionString, dbUserName,
                                                  dbUserPassword);
            // catch (ClassNotFoundException e)
            dbConnection = con;
          }
        } catch (SQLException e) {
            throw new ConnectionException("DATABASE ACCESS ERROR: SQLSTATE: " +
                                          e.getSQLState() +
                                          e.getMessage());
        }// catch (SQLException e)
        catch (ClassNotFoundException e) {
            throw new ConnectionException("ERROR DB-DRIVER: " +
                                          e.getMessage());
        }
    }

    /**
   * @return the database connection
   */
    public Connection getConnection() {
        return this.dbConnection;
    }

    /**
	 * queries the database
   * @param query
	 * @return the resultset of the given query
	 */
    public ResultSet executeQuery(String query) {
        ResultSet rs = null;
        // System.out.println("DBConnection.executeQuery: " + query);
        try {
            if ((dbConnection == null) ||
                ((dbConnection != null) && (dbConnection.isClosed() ==
                                                                      true))) {
                setConnection();
            }
            dbStatement = dbConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, 
   ResultSet.CONCUR_READ_ONLY);
            rs = dbStatement.executeQuery(query);
            
            /*if (rs != null)	rs.close();
      if (dbStatement != null)	dbStatement.close();
      if (dbConnection != null)	*/
            //dbConnection.close();
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
            System.out.println(query);
        }
        return rs;
    }

    /**
   * execute an update, delete or insert query
   * @return the row count
   * @param sql
   */
    public int executeUpdate(String sql) {
        int rowCount = 0;
        try {
            if ((dbConnection == null) ||
                ((dbConnection != null) && (dbConnection.isClosed() ==
                                                                      true))) {
                setConnection();
            }
            dbStatement = dbConnection.createStatement();
            rowCount = dbStatement.executeUpdate(sql);
            //dbConnection.close();
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
            System.out.println(sql);
        }
        return rowCount;
    }

    /**
	 * undo
	 * @param query
	 * @return
	 */
    public void UndoUpdate() {
        try {
            if (dbConnection != null)
                dbConnection.rollback();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }
    }

    /**
   * close the connection
   */
    public void closeConnection() {
        try {
            if (rs != null)
                rs.close();
            if (dbStatement != null)
                dbStatement.close();
            if (dbConnection != null) {
                dbConnection.close();
                dbConnection = null;
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
}