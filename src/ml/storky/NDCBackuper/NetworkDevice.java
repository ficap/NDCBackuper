package ml.storky.NDCBackuper;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.sun.istack.internal.NotNull;
import javafx.scene.chart.PieChart;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by storky on 22.4.16.
 */
public abstract class NetworkDevice {
    public static final int ONLINE = 1;
    public static final int OFFLINE = 2;

    private int deviceID; // TODO: id should be always available
    private String ipAddress;
    private int port;
    private String hostname;
    private String username;
    private String password;
    private String passwordPrivilegedMode;
    private int state;
    private ArrayList<Config> configs;
    private int backupPeriod; // in seconds
    private String description = "";

    public NetworkDevice(@NotNull String ipAddress) {
        this.ipAddress = ipAddress;
        this.port = 22;
    }

    public NetworkDevice(@NotNull String ipAddress, @NotNull int port) {
        this(ipAddress);
        this.port = port;
    }

    public NetworkDevice(@NotNull String ipAddress, @NotNull int port, String username) {
        this(ipAddress, port);
        this.username = username;
    }

    public NetworkDevice(@NotNull String ipAddress, @NotNull int port, String username, String password) {
        this(ipAddress, port, username);
        this.password = password;
    }

    public NetworkDevice(@NotNull String ipAddress, String description, @NotNull int port, String username, String password) {
        this(ipAddress, port, username);
        this.password = password;
        this.description = description;
    }

    public NetworkDevice(@NotNull String ipAddress, @NotNull int port, String username, String password, String passwordPrivilegedMode) {
        this(ipAddress, port, username, password);
        this.passwordPrivilegedMode = passwordPrivilegedMode;
    }

    public NetworkDevice(@NotNull String ipAddress, String username) {
        this(ipAddress);
        this.username = username;
    }

    public NetworkDevice(@NotNull String ipAddress, String username, String password) {
        this(ipAddress, username);
        this.password = password;
    }

    public NetworkDevice(@NotNull String ipAddress, String username, String password, String passwordPrivilegedMode) {
        this(ipAddress, username, password);
        this.passwordPrivilegedMode = passwordPrivilegedMode;
    }

    public NetworkDevice(@NotNull int deviceID, @NotNull String ipAddress, int port, String username, String password, String passwordPrivilegedMode) {
        this(ipAddress, port, username, password, passwordPrivilegedMode);
        this.deviceID = deviceID;
    }

    // TODO: constructors should contain parameter deviceId

    private void loadConfigHeadersFromDB() {
        //TODO: Implement
    }

    private void loadFullConfigsFromDB() {
        //TODO:Implement
    }

    public String getHostname() {
        if(this.hostname != null && !this.hostname.equals("")) {
            return this.hostname;
        }
        try {
            Config cnfgs = null;
            try {
                cnfgs = Database.getDefaultDatabase().getLastConfigByDevice(this);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if(cnfgs == null) {
                String content = this.downloadConfig(Config.RUNNING_CONFIG).getRawContent();
                if(content.indexOf("hostname ") == -1) {
                    this.hostname = "N/A";
                    return this.hostname;
                }
                this.hostname = content.substring(content.indexOf("hostname ")+9, content.indexOf("\n", content.indexOf("hostname ")));
                return this.hostname;
            }
            String content = cnfgs.getRawContent();
            if(content.indexOf("hostname ") == -1) {
                this.hostname = "N/A";
                return this.hostname;
            }
            this.hostname = content.substring(content.indexOf("hostname ")+9, content.indexOf("\n", content.indexOf("hostname ")));
            return this.hostname;
        } catch (JSchException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.hostname = "N/A";
        return this.getHostname();
    }

    public int getPort() {
        return this.port;
    }

    public abstract int getDeviceID();

    public void setDeviceID(int deviceID) {
        this.deviceID = deviceID;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordPrivilegedMode() {
        return passwordPrivilegedMode;
    }

    public void setPasswordPrivilegedMode(String passwordPrivilegedMode) {
        this.passwordPrivilegedMode = passwordPrivilegedMode;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public ArrayList<Config> getConfigs() {
        return configs;
    }

    public void setConfigs(ArrayList<Config> configs) {
        this.configs = configs;
    }

    public int getBackupPeriod() {
        return backupPeriod;
    }

    public void setBackupPeriod(int backupPeriod) {
        this.backupPeriod = backupPeriod;
    }

    /**
     * Downloads and saves to DB
     * @param type
     * @return
     */
    public Config downloadConfigAndSave(int type) {
        Config conf = null;
        try {
            conf = this.downloadConfig(type);
        } catch (JSchException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(conf == null) {
            return null;
        }

        try {
            Database.getDefaultDatabase().addConfig(conf);
        } catch (SQLException e) {
            e.printStackTrace();
        }


        return conf;
    }

    public Config downloadConfig(int type) throws JSchException, IOException {
        JSch jSch = new JSch();
        Session session = jSch.getSession(this.getUsername(), this.getIpAddress(), this.getPort());
        //session.getHostKeyRepository().add(new HostKey("localhost", HostKey.SSHRSA, ));

        session.setConfig("StrictHostKeyChecking", "no");
        session.setPassword(this.getPassword());
        session.connect();
        // System.out.println("connected");
        ChannelShell shell = (ChannelShell) session.openChannel("shell");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(shell.getOutputStream()));
        BufferedReader br = new BufferedReader(new InputStreamReader(shell.getInputStream()));
        shell.connect();
        bw.write("terminal length 0");
        bw.newLine();
        //System.out.println("term len odesl");
        if(type == Config.RUNNING_CONFIG) {
            bw.write("show running-config");
        } else {
            bw.write("show startup-config");
        }
        bw.newLine();
        //System.out.println("show run odesl");
        bw.flush();
        //System.out.println("flush");
        br.readLine(); // empty line
        br.readLine(); // term len
        br.readLine(); // show run
        br.readLine(); // build conf
        br.readLine(); // build space
        br.readLine(); // build config length

        String line = "";
        StringBuilder builder = new StringBuilder();

        while(!(line = br.readLine()).equals("end")) {
            System.out.println(line);
            builder.append(line);
            builder.append("\n");
        }

        session.disconnect();
        Config conf = new Config(this, builder.toString(), type);
        return conf;
    }

    public void uploadConfig(String config) {
        JSch jSch = new JSch();
        try {
            Session session = jSch.getSession(this.getUsername(), this.getIpAddress(), this.getPort());
            //session.getHostKeyRepository().add(new HostKey("localhost", HostKey.SSHRSA, ));

            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(this.getPassword());
            session.connect();
            // System.out.println("connected");
            ChannelShell shell = (ChannelShell) session.openChannel("shell");
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(shell.getOutputStream()));
            BufferedReader br = new BufferedReader(new InputStreamReader(shell.getInputStream()));
            shell.connect();
            bw.write("terminal length 0");
            bw.newLine();
            //System.out.println("show run odesl");
            bw.write("configure terminal");
            bw.newLine();
            bw.write(config);
            bw.flush();
            //System.out.println("flush");
            br.readLine(); // empty line
            br.readLine(); // term len
            // a lot of br.readLine() every command sent below has own line

            session.disconnect();

        } catch (JSchException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
