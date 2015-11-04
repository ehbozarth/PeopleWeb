import spark.ModelAndView;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.io.File;
import java.io.FileReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class People {

    static final int SHOW_COUNT = 20;

    public static void createTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("DROP TABLE IF EXISTS people");
        stmt.execute("CREATE TABLE people (id IDENTITY, first_name VARCHAR, last_name VARCHAR, " +
                "email VARCHAR, country VARCHAR, ip VARCHAR)");
    }//End of createTables

    public static void insertPerson(Connection connection, String firstName, String lasName, String email,
                                    String country, String ip) throws SQLException{
        PreparedStatement statement = connection.prepareStatement("INSERT INTO people VALUES " +
                "(NULL, ?,?,?,?,?)");
        statement.setString(1, firstName);
        statement.setString(2, lasName);
        statement.setString(3, email);
        statement.setString(4, country);
        statement.setString(5, ip);
        statement.execute();
    }//End of insertPerson

    public static Person selectPerson(Connection connection, int id)throws SQLException{
        Person tempPerson = new Person();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM people WHERE id = ?");
        statement.setInt(1, id);
        ResultSet results = statement.executeQuery();
        while(results.next()){
            tempPerson = new Person();
            tempPerson.id = results.getInt("id");
            tempPerson.firstName = results.getString("first_name");
            tempPerson.lastName = results.getString("last_name");
            tempPerson.email = results.getString("email");
            tempPerson.country = results.getString("country");
            tempPerson.ip = results.getString("ip");
        }
        return tempPerson;
    }

    public static ArrayList<Person> selectPeople(Connection connection, int offset) throws SQLException{
        ArrayList<Person> personArrayList = new ArrayList<>();
        String query = String.format("SELECT * FROM people ORDER BY id LIMIT ? OFFSET ?");

        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, SHOW_COUNT);
        statement.setInt(2, offset);
        ResultSet results = statement.executeQuery();
        while(results.next()){
            Person tempPerson = new Person();
            tempPerson.id = results.getInt("id");
            tempPerson.firstName = results.getString("first_name");
            tempPerson.lastName = results.getString("last_name");
            tempPerson.email = results.getString("email");
            tempPerson.country = results.getString("country");
            tempPerson.ip = results.getString("ip");
            personArrayList.add(tempPerson);
        }
        return personArrayList;
    }//End of selectPeople

    public static ArrayList<Person> selectPeople(Connection connection) throws SQLException {
        return selectPeople(connection, 0);
    }

    public static int countPeople(Connection connection) throws SQLException{
        Statement statement = connection.createStatement();
        ResultSet results = statement.executeQuery("SELECT COUNT(*) AS counter FROM people");
        int count =0;
        if(results.next()){
            count = results.getInt("counter");
        }
        return count;
    }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void main(String[] args) throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:h2:./main");
        createTables(connection);


        ArrayList<Person> people = new ArrayList();

        String fileContent = readFile("people.csv");
        String[] lines = fileContent.split("\n");



        for (String line : lines) {
            if (line == lines[0])
                continue;
            String[] columns = line.split(",");
            //Person person = new Person(Integer.valueOf(columns[0]), columns[1], columns[2], columns[3], columns[4], columns[5]);
            insertPerson(connection, columns[1], columns[2], columns[3], columns[4], columns[5]);
            //people.add(person);
        }//End of for loop

        // write Spark route here
        Spark.get(
                "/",
                (request, response) -> {
                    String offsetStr= request.queryParams("page");
                    int offset;
                    if(offsetStr == null) {
                        offset = 0;
                    }
                    else{
                        offset = Integer.valueOf(offsetStr);
                    }
//                    ArrayList<Person> tempList = new ArrayList<Person>(people.subList(
//                            Math.max(0,Math.min(people.size(),counter)),
//                            Math.max(0,Math.min(people.size(),counter+ SHOW_COUNT))
//                    ));
                    HashMap m = new HashMap();
                    m.put("people", selectPeople(connection, offset));
                    m.put("person", selectPeople(connection));
                    m.put("old_counter", offset - SHOW_COUNT);
                    m.put("new_counter", offset + SHOW_COUNT);

                    boolean showPrevious = offset > 0;
                    m.put("showPrev", showPrevious);

                    boolean isAtEnd = offset + SHOW_COUNT < countPeople(connection);
                    m.put("showNext", isAtEnd);

                    return new ModelAndView(m, "people.html");
                },
                new MustacheTemplateEngine()
        );//End of Spark.get() "/"

        Spark.get(
                "/person",
                ((request, response) -> {
                    HashMap m = new HashMap();
                    String idStr = request.queryParams("id");
                    int id = Integer.valueOf(idStr);

                    try{
//                        int idNum = Integer.valueOf(id);
//                        Person person = people.get(idNum - 1);
//                        m.put("person", person);
                        //insertPerson(connection, firstName, lastName, email, country, ip);
                        m.put("person", selectPerson(connection, id));

                        //Without mustache hash tag
                        //Set one field at a time to one person
                        //m.put("firstName", person.firstName);
                    }
                    catch (Exception e){

                    }
                    return new ModelAndView(m,"person.html");
                }),
                new MustacheTemplateEngine()

        );//End of Spark.get() "/person"

    }//End od Main Method

    static String readFile(String fileName) {
        File f = new File(fileName);
        try {
            FileReader fr = new FileReader(f);
            int fileSize = (int) f.length();
            char[] fileContent = new char[fileSize];
            fr.read(fileContent);
            return new String(fileContent);
        } catch (Exception e) {
            return null;
        }
    }
}
