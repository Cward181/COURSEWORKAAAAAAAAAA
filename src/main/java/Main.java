import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.json.simple.JSONArray;
import org.sqlite.SQLiteConfig;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.sql.Connection;
import java.sql.DriverManager;


public class Main {

    public static Connection db = null;

    private static void openDatabase(String dbFile) {

        try {

            Class.forName("org.sqlite.JDBC");
            SQLiteConfig config = new SQLiteConfig();
            config.enforceForeignKeys(true);
            db = DriverManager.getConnection("jdbc:sqlite:resources/" + dbFile, config.toProperties());
            System.out.println("Database connection successfully established.");

        } catch (Exception exception) {

            System.out.println("Database connection error: " + exception.getMessage());
        }

    }

    private static void closeDatabase() {
        try {

            db.close();
            System.out.println("Disconnected from database.");

        } catch (Exception exception) {

            System.out.println("Database disconnection error: " + exception.getMessage());
        }
    }

    private Stats_Units objSU = new Stats_Units();
    private Attributes_Units objAU = new Attributes_Units();

    @Path("Main/")//api path to main
    public void main(String[] args) {

        openDatabase("database placeholder.db");

        ResourceConfig config = new ResourceConfig();       // Prepare the server configuration (uses the Jetty library).
        config.packages("Controllers");                     // The package containing the HTTP request handlers for the API.
        config.register(MultiPartFeature.class);            // Enables support for multi-part forms (important!)
        Server server = new Server(8081);             // The port number to connect to (part of the URL).

        ServletHolder servlet = new ServletHolder(new ServletContainer(config));            // Creates the Jersey 'servlet' to run on the server.
        ServletContextHandler context = new ServletContextHandler(server, "/");
        context.addServlet(servlet, "/*");                                         // Connect the servlet to the server.

        try {
            server.start();                                 // Actually start the server!
            System.out.println("Server successfully started.");
            server.join();                                  // This line of code leaves the program running indefinitely, waiting for HTTP requests.
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GET
    @Path("Moveunit")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String Moveunit(@FormDataParam("Moverequest") String Moverequest) {
        System.out.println("Main/Combat");
        JSONArray list = new JSONArray();
        String[] Moverequest = {"2 A2", "3, C14"};//Api movement list Array
        for (int i = 0; i < Moverequest.length; i++) {//for loop iterates equal to the amount of objects in the array
            String[] splitreq = Moverequest[i].split("\\s+");//splits each request at the space and assigns to Splitreq
            String UnitID = splitreq[0];//assigns UnitID and TileID to their respective number
            String TileID = splitreq[1];
            objSU.Update_Stats_Units(UnitID, TileID);//calls the Update controller class with the correct information
        }
    }

    @GET
    @Path("Combat")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String Combat() {
        System.out.println("Main/Combat");
        JSONArray list = new JSONArray();

        String[] Friendlyship = {"12", "19", "4", "7"};//Api Array aggressor's ships
        String[] Enemyship = {"13", "2", "9"};//Api Array defender's ships
        int Fdamagetotal = 0;// declaring friendly damage total
        int Edamagetotal = 0;// declaring enemy damage total
        for (int i = 0; i < Friendlyship.length; i++) {//for loop iterates equal to the amount of objects in the array
            String UnitID = Friendlyship[i];
            String SelectReturn = objSU.Select_Stats_Units(UnitID);
            String[] UnitType = SelectReturn.split("\\s+");//splits the values that make up the Unit at the spaces and assigns them to UniType
            SelectReturn = objAU.Select_Attributes_Units(UnitType[2]); //searches for second value of "UnitType" array as it is the foreign key "UnitType" in that row of the database
            String[] R = SelectReturn.split("\\s+");//splits the values that make up the Unit at the spaces and assigns them to "R"
            int damage = Integer.valueOf(R[6]);//Sixth column contains the Attack value
            Fdamagetotal = Fdamagetotal + damage;//adds UnitDamage to
        }

        for (int i = 0; i < Enemyship.length; i++) {//for loop iterates equal to the amount of objects in the array
            if (Fdamagetotal > 0) {//stops loop when all damage has been dealt
                String UnitID = Enemyship[i];
                String SelectReturn = objSU.Select_Stats_Units(UnitID);
                String[] R = SelectReturn.split("\\s+");
                int UnitHealth = Integer.valueOf(R[4]);//see above
                if (Fdamagetotal > UnitHealth) {//checks to see if the ship is destroyed
                    Fdamagetotal = Fdamagetotal - UnitHealth;
                    UnitHealth = 0;
                    objSU.Update_Stats_Units(UnitID, UnitHealth);
                    Enemyship[i] = ("1");// 1 is the UnitID for a dead ship used in combat so it does no damage to the enemy on retaliatory strike
                } else {//if ship is not destroyed the remaining damage is dealt to the next ship
                    UnitHealth = UnitHealth - Fdamagetotal;
                    objSU.Update_Stats_Units(UnitID, UnitHealth);
                }
            } else {
                i = Enemyship.length;
            }
        }

        //exact same process is completed for retaliatory strike

        for (int i = 0; i < Enemyship.length; i++) {//for loop iterates equal to the amount of objects in the array
            String UnitID = Enemyship[i];
            String SelectReturn = objSU.Select_Stats_Units(UnitID);
            String[] UnitType = SelectReturn.split("\\s+");//splits the values that make up the Unit at the spaces and assigns them to UniType
            SelectReturn = objAU.Select_Attributes_Units(UnitType[2]); //searches for second value of "UnitType" array as it is the foreign key "UnitType" in that row of the database
            String[] R = SelectReturn.split("\\s+");//splits the values that make up the Unit at the spaces and assigns them to "R"
            int damage = Integer.valueOf(R[6]);//sixth column contains the Attack value
            Edamagetotal = Edamagetotal + damage;//adds UnitDamage to
        }

        for (int i = 0; i < Enemyship.length; i++) {//for loop iterates equal to the amount of objects in the array
            if (Edamagetotal > 0) {//stops loop when all damage has been dealt
                String UnitID = Enemyship[i];
                String SelectReturn = objSU.Select_Stats_Units(UnitID);
                String[] R = SelectReturn.split("\\s+");
                int UnitHealth = Integer.valueOf(R[4]);//see above
                if (Edamagetotal > UnitHealth) {//checks to see if the ship is destroyed
                    Edamagetotal = Edamagetotal - UnitHealth;
                    UnitHealth = 0;
                    objSU.Update_Stats_Units(UnitID, UnitHealth);
                    Enemyship[i] = ("1");// 1 is the UnitID for a dead ship used in combat so it does no damage to the enemy
                } else {//if ship is not destroyed the remaining damage is dealt to the next ship
                    UnitHealth = UnitHealth - Edamagetotal;
                    objSU.Update_Stats_Units(UnitID, UnitHealth);
                }
            } else {
                i = Enemyship.length;
            }
        }
    }

            objSU.DeadUnitCleanup(
    int UnitHealth);


    // code using the database goes here!

    closeDatabase();

}
    }