package ml.storky.NDCBackuper;

import java.sql.SQLException;

/**
 * Created by storky on 3.6.16.
 */
public class TestDB {
    public static void main(String[] args) throws SQLException {

//        readConfFromDB();
        blah();

    }

    public static void downConfig() throws SQLException {
        Router rtr = new Router("12.0.0.2", 22, "paty", "cisco");
        rtr.setDeviceID(0);
        Database.getDefaultDatabase().addDevice(rtr);
        System.out.println(rtr.downloadConfigAndSave(Config.RUNNING_CONFIG).getRawContent());
    }

    public static void readConfFromDB() throws SQLException {
        // System.out.println(Database.getDefaultDatabase().getConfigById(1).getRawContent());

        NetworkDevice dev = Database.getDefaultDatabase().getNetworkDevices()[1];
        Router rtr = new Router("10.0.0.2", "paty", "cisco");
        rtr.setDeviceID(2);
        System.out.println(Database.getDefaultDatabase().getConfigsByDevice(dev)[0].getRawContent());
    }
    public static void blah() {
        try {
            Database db = Database.getDefaultDatabase();
            NetworkDevice nd = new Router(2, "12.0.0.5", 22, "paty", "cisco");
            Config[] cnfs = db.getConfigsByDevice(nd);

            if(cnfs == null) {
                System.out.println("No configs");
                return;
            }
            for(Config conf : cnfs) {
                System.out.println(conf.getRawContent());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
