import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by earlbozarth on 11/4/15.
 */
public class PeopleTest {

    public Connection startConnection() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:./test");
        People.createTables(conn);
        return conn;
    }

    public void endConnection(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.execute("DROP TABLE people");
        conn.close();
    }

    @Test
    public void testPerson() throws SQLException {
        Connection conn = startConnection();
        People.insertPerson(conn, "Test first Name", "Test Last Name", "Test Email",
                "Test country", "Test ip");
        //ArrayList<Person> events = Person.selectPeople(conn);
        ArrayList<Person> personArrayList = People.selectPeople(conn);
        endConnection(conn);

        assertTrue(personArrayList.size() == 1);
    }


}