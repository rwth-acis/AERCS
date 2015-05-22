package i5.las2peer.services.aercs.dbms.dbconnection;


public class ConnectionException extends Exception {
    /**
   * ConnectionException
   * Wird geworfen bei Problemen beim Datenbankverbindungsaufbau
   */
    public ConnectionException() {
        super("Error while builing up the Database-Connection");
    }

    /**
   * ConnectionException ( String s )
   * Wird geworfen bei Problemen beim Datenbankverbindungsaufbau
   * @param String Enhält eine Fehlermeldung, die aussagt, wo und warum
   *        der Fehler aufgetreten ist
   */
    public ConnectionException(String s) {
        super(s);
    }
}