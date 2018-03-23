package ml.storky.NDCBackuper;

import java.util.ArrayList;

/**
 * Created by storky on 28.4.16.
 */
public interface NetDev {
    int ONLINE = 1;
    int OFFLINE = 2;

    String deviceID = null;
    String ipAddress = null;
    int port = 0;
    String username = null;
    String password = null;
    String passwordPriviledgedMode = null;
    int state = 0;
    ArrayList<Config> configs = null;
    long backupPeriod = 0; // in seconds?
}
