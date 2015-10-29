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
    static final int SHOW_COUNT = 20;
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

                    ArrayList<Person> tempList = new ArrayList<Person>(people.subList(
                            Math.max(0,Math.min(people.size(),counter)),
                            Math.max(0,Math.min(people.size(),counter+ SHOW_COUNT))
                    ));
                    HashMap m = new HashMap();
                    m.put("people", tempList);
                    m.put("old_counter", counter - SHOW_COUNT);
                    m.put("new_counter", counter+ SHOW_COUNT);

                    boolean showPrevious = counter > 0;
                    m.put("showPrev", showPrevious);

                    boolean isAtEnd = counter + SHOW_COUNT < people.size();
                    m.put("showNext", isAtEnd);

                    return new ModelAndView(m, "people.html");
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
