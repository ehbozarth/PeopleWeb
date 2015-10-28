import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by zach on 10/19/15.
 */
public class People {
    public static void main(String[] args) {
        ArrayList<Person> people = new ArrayList();

        String fileContent = readFile("people.csv");
        String[] lines = fileContent.split("\n");


        for (String line : lines) {
            if (line == lines[0])
                continue;

            String[] columns = line.split(",");
            Person person = new Person(Integer.valueOf(columns[0]), columns[1], columns[2], columns[3], columns[4], columns[5]);
            people.add(person);
        }//End of for loop

        // write Spark route here
        Spark.get(
                "/",
                (request, response) -> {
                    String offset= request.queryParams("page");
                    int counter;
                    if(offset == null) {
                        counter = 0;
                    }
                    else{
                        counter = Integer.valueOf(offset);
                    }
                    if(counter >= people.size()){
                        Spark.halt(403);
                    }
                    else{
                        ArrayList<Person> tempList = new ArrayList<Person>(people.subList(counter,counter+20));
                        HashMap m = new HashMap();
                        m.put("people", tempList);
                        m.put("new_counter", counter+20);
                        return new ModelAndView(m, "people.html");
                    }
                    return new ModelAndView(new HashMap<>(), "people.html");
                },
                new MustacheTemplateEngine()
        );//End of Spark.get() "/"

        Spark.get(
                "/person",
                ((request, response) -> {
                    HashMap m = new HashMap();
                    String id = request.queryParams("id");

                    try{
                        int idNum = Integer.valueOf(id);
                        Person person = people.get(idNum - 1);
                        m.put("person", person);

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
