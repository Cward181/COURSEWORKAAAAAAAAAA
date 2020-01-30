import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

@Path("FactionInfo/")
public class FactionInfo {
    @POST
    @Path("login")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public String loginUser(@FormDataParam("Faction") String Faction, @FormDataParam("Password") String Password) {

        try {

            PreparedStatement ps1 = Main.db.prepareStatement("SELECT Password FROM FactionInfo WHERE Faction = ?");
            ps1.setString(1, Faction);
            ResultSet loginResults = ps1.executeQuery();
            if (loginResults.next()) {

                String correctPassword = loginResults.getString(1);

                if (Password.equals(correctPassword)) {

                    String token = UUID.randomUUID().toString();

                    PreparedStatement ps2 = Main.db.prepareStatement("UPDATE FactionInfo SET Token = ? WHERE Username = ?");
                    ps2.setString(1, token);
                    ps2.setString(2, Faction);
                    ps2.executeUpdate();

                    return "{\"token\": \""+ token + "\"}";

                } else {

                    return "{\"error\": \"Incorrect password!\"}";

                }

            } else {

                return "{\"error\": \"Unknown user!\"}";

            }

        }catch (Exception exception){
            System.out.println("Database error during /FactionInfo/login: " + exception.getMessage());
            return "{\"error\": \"Server side error!\"}";
        }


    }

    @POST
    @Path("NewFaction")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public String NewFaction(
            @FormDataParam("Faction") String Faction, @FormDataParam("Password") String Password, @FormDataParam("Credits") Integer Credits) {
        try {
            if (Faction == null || Password == null || Credits == null) {
                throw new Exception("One or more form data parameters are missing in the HTTP request.");
            }
            System.out.println("FactionInfo/NewFaction Faction=" + Faction);

            PreparedStatement ps = Main.db.prepareStatement("INSERT INTO Things (Faction, Password, Credits) VALUES (?, ?, ?)");
            ps.setString(1, Faction);
            ps.setString(2, Password);
            ps.setInt(3, Credits);
            ps.execute();
            return "{\"status\": \"OK\"}";

        } catch (Exception exception) {
            System.out.println("Database error: " + exception.getMessage());
            return "{\"error\": \"Unable to create new item, please see server console for more info.\"}";
        }
    }

    public static void FactionInfoInsert(String Faction, String Password, int Credits){

        try {
            PreparedStatement ps = Main.db.prepareStatement("INSERT INTO FactionInfo(Faction, Password, Credits) VALUES (?, ?, ?, ?, ?)");

            ps.setString(1, Faction);
            ps.setString(2, Password);
            ps.setInt(3, Credits);
            ps.executeUpdate();

        } catch (Exception exception) {
            System.out.println("Database error: " + exception.getMessage());
        }
    }
    public static void Select_FactionInfo() {
        try {
            PreparedStatement ps = Main.db.prepareStatement("SELECT Faction, Password, Credits FROM FactionInfo");

            ResultSet results = ps.executeQuery();
            while (results.next()) {
                String Faction = results.getString(1);
                String Password = results.getString(2);
                int Credits = results.getInt(3);
                System.out.println(Faction + " " + Password + " " + Credits);
            }

        } catch (Exception exception) {
            System.out.println("Database error: " + exception.getMessage());
        }
    }
    public static void Update_FactionInfo (String Faction, String Password, int Credits){
        try {

            PreparedStatement ps = Main.db.prepareStatement("UPDATE FactionInfo SET Password = ?, Credits = ?, WHERE Faction = ?");
            ps.setString(1, Faction);
            ps.setString(2, Password);
            ps.setInt(3, Credits);
            ps.executeUpdate();

        } catch (Exception e) {
            System.out.println(e.getMessage());

        }
    }
    public static void delete_FactionInfo (int Faction){
        try {

            PreparedStatement ps = Main.db.prepareStatement("DELETE FROM FactionInfo WHERE Faction = ?");
            ps.setInt(1, Faction);
            ps.executeUpdate();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
