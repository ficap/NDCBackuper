package ml.storky.NDCBackuper;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.sun.istack.internal.NotNull;

import java.io.*;
import java.net.InetAddress;

/**
 * Created by storky on 25.4.16.
 */
public class Router extends NetworkDevice {
    private boolean hasHostname = false;
    private String hostname;
    private int deviceID;

    public Router(@NotNull String ipAddress) {
        super(ipAddress);
    }

    public Router(@NotNull String ipAddress, @NotNull int port) {
        super(ipAddress, port);
    }

    public Router(@NotNull String ipAddress, @NotNull int port, String username) {
        super(ipAddress, port, username);
    }

    public Router(@NotNull String ipAddress, @NotNull int port, String username, String password) {
        super(ipAddress, port, username, password);
    }

    public Router(@NotNull String ipAddress, String description, @NotNull int port, String username, String password) {
        super(ipAddress, port, username, password);
        this.setDescription(description);
    }

    public Router(@NotNull String ipAddress, String description, @NotNull int port, String username, String password, int backupPeriod) {
        super(ipAddress, port, username, password);
        this.setDescription(description);
        this.setBackupPeriod(backupPeriod);
    }

    public Router(@NotNull String ipAddress, @NotNull int port, String username, String password, String passwordPriviledgedMode) {
        super(ipAddress, port, username, password, passwordPriviledgedMode);
    }

    public Router(@NotNull String ipAddress, String username) {
        super(ipAddress, username);
    }

    public Router(@NotNull String ipAddress, String username, String password) {
        super(ipAddress, username, password);
    }

    public Router(@NotNull String ipAddress, String username, String password, String passwordPriviledgedMode) {
        super(ipAddress, username, password, passwordPriviledgedMode);
    }

    public Router(@NotNull int deviceID, @NotNull String ipAddress, int port, String username, String password) {
        this(ipAddress, port, username, password);
        this.deviceID = deviceID;
    }

    public Router(@NotNull int deviceID, @NotNull String ipAddress, int port, String username, String password, String description) {
        this(ipAddress, port, username, password);
        this.deviceID = deviceID;
        this.setDescription(description);
    }

    public Router(@NotNull int deviceID, @NotNull String ipAddress, int port, String username, String password, String description, int backupPeriod) {
        this(ipAddress, port, username, password);
        this.deviceID = deviceID;
        this.setDescription(description);
        this.setBackupPeriod(backupPeriod);
    }

    public int getDeviceID() {
        return this.deviceID;
    }

    public void tryConnection() throws IOException, JSchException {
        JSch jSch = new JSch();
        Session session = jSch.getSession(this.getUsername(), this.getIpAddress(), this.getPort());
        //session.getHostKeyRepository().add(new HostKey("localhost", HostKey.SSHRSA, ));

        session.setConfig("StrictHostKeyChecking", "no");
        session.setPassword(this.getPassword());
        session.connect();
        ChannelShell shell = (ChannelShell) session.openChannel("shell");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(shell.getOutputStream()));
        BufferedReader br = new BufferedReader(new InputStreamReader(shell.getInputStream()));
        shell.connect();
        shell.disconnect();


    }
}
