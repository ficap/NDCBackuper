package ml.storky.NDCBackuper;

import com.sun.istack.internal.NotNull;

import java.net.InetAddress;

/**
 * Created by storky on 26.4.16.
 */
public class Switch extends NetworkDevice {
    private int deviceID;

    public Switch(@NotNull String ipAddress) {
        super(ipAddress);
    }

    public Switch(@NotNull String ipAddress, @NotNull int port) {
        super(ipAddress, port);
    }

    public Switch(@NotNull String ipAddress, @NotNull int port, String username) {
        super(ipAddress, port, username);
    }

    public Switch(@NotNull String ipAddress, @NotNull int port, String username, String password) {
        super(ipAddress, port, username, password);
    }

    public Switch(@NotNull String ipAddress, @NotNull int port, String username, String password, String passwordPriviledgedMode) {
        super(ipAddress, port, username, password, passwordPriviledgedMode);
    }

    public Switch(@NotNull String ipAddress, String username) {
        super(ipAddress, username);
    }

    public Switch(@NotNull String ipAddress, String username, String password) {
        super(ipAddress, username, password);
    }

    public Switch(@NotNull String ipAddress, String username, String password, String passwordPriviledgedMode) {
        super(ipAddress, username, password, passwordPriviledgedMode);
    }

    public Switch(@NotNull int deviceID, @NotNull String ipAddress, int port, String username, String password) {
        this(ipAddress, port, username, password);
        this.deviceID = deviceID;
    }

//    public String getHostname() {
//        return "SW1";
//    }

    public int getDeviceID() {
        return this.deviceID;
    }
}
