package ml.storky.NDCBackuper;

import java.sql.*;
import java.sql.Date;
import java.util.*;

/**
 * Created by storky on 25.5.16.
 */
public class Database {
    private static final char TYPE_ROUTER = 'r', TYPE_RUNNING = '1';
    private static final char TYPE_SWITCH = 's', TYPE_STARTUP = '2';

    private static Database database;
    private Connection connection;

    private Database() throws SQLException {
        // this.connection = DriverManager.getConnection("jdbc:h2:file:~/.NDC-Backuper/data;MODE=MySQL");
        this.connection = DriverManager.getConnection("jdbc:h2:file:~/.NDC-Backuper/data;MODE=MySQL");
        Statement statement = connection.createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS `Device` (" +
                "  `id` int(11) NOT NULL AUTO_INCREMENT," +
                "  `type` char(1) NOT NULL," +
                "  `ip` varchar(15) NOT NULL," +
                "  `port` int(11) NOT NULL," +
                "  `hostname` varchar(255) NOT NULL," +
                "  `login` varchar(255) NOT NULL," +
                "  `password` varchar(255) NOT NULL," +
                "  `backup_period` int(11) NOT NULL," +
                "  `description` varchar(255) DEFAULT NULL," +
                "  PRIMARY KEY (`id`)" +
                ") DEFAULT CHARSET=utf8;");

        statement.execute("CREATE TABLE IF NOT EXISTS `Config` (" +
                "  `id` int(11) NOT NULL AUTO_INCREMENT," +
                "  `device_id` int(11) DEFAULT NULL," +
                "  `type` char(1) NOT NULL," +
                "  `content` longtext NOT NULL," +
                "  `date` datetime NOT NULL," +
                "  PRIMARY KEY (`id`)," +
                ") DEFAULT CHARSET=utf8;");
        statement.close();
        connection.commit();
    }

    public static Database getDefaultDatabase() throws SQLException {
        if(database == null) {
            database = new Database();
        }

        return database;
    }

    public void addDevice(NetworkDevice device) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO `Device` (`type`, `ip`, `port`, `hostname`, `login`, `password`, `backup_period`, `description`) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?);");
        if(device instanceof Router) {
            preparedStatement.setString(1, "r");
        } else if(device instanceof Switch) {
            preparedStatement.setString(1, "s");
        } else {
            throw new SQLException("Unknown device type");
        }

        preparedStatement.setString(2, device.getIpAddress());
        preparedStatement.setInt(3, device.getPort());
        preparedStatement.setString(4, device.getHostname());
        preparedStatement.setString(5, device.getUsername());
        preparedStatement.setString(6, device.getPassword());
        preparedStatement.setInt(7, device.getBackupPeriod());
        preparedStatement.setString(8, device.getDescription());

        preparedStatement.execute();
        preparedStatement.close();
        this.connection.commit();
    }

    public void removeDevice(NetworkDevice device, boolean keepConfigs) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM `Device`" +
                "WHERE ((`id` = ?));");
        preparedStatement.setInt(1, device.getDeviceID());
        preparedStatement.execute();
        preparedStatement.close();
        this.connection.commit();
    }

    public void modifyDevice(NetworkDevice device) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "UPDATE `Device` SET `type` = ?, `ip` = ?, `port` = ?, `hostname` = ?, `login` = ?, `password` = ?, " +
                        "`backup_period` = ?, `description` = ? WHERE `id` = ?;");

        if(device instanceof Router) {
            preparedStatement.setString(1, "r");
        } else if(device instanceof Switch) {
            preparedStatement.setString(1, "s");
        } else {
            throw new SQLException("Unknown device type");
        }

        preparedStatement.setString(2, device.getIpAddress());
        preparedStatement.setInt(3, device.getPort());
        preparedStatement.setString(4, device.getHostname()); //NOTE:
        preparedStatement.setString(5, device.getUsername());
        preparedStatement.setString(6, device.getPassword());
        preparedStatement.setInt(7, device.getBackupPeriod());
        preparedStatement.setString(8, device.getDescription());
        preparedStatement.setInt(9, device.getDeviceID());

        preparedStatement.execute();
        preparedStatement.close();
        this.connection.commit();
    }

    public NetworkDevice getDeviceById(int id) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT * FROM `Device` WHERE `id` = ?;");
        preparedStatement.setInt(1, id);
        if(preparedStatement.execute()) {
            ResultSet resultSet = preparedStatement.getResultSet();
            resultSet.next();

            String type = resultSet.getString("type");
            String ip = resultSet.getString("ip");
            int port = resultSet.getInt("port");
            String hostname = resultSet.getString("hostname");
            String login = resultSet.getString("login");
            String password = resultSet.getString("password");
            int backupPeriod = resultSet.getInt("backup_period");
            String description = resultSet.getString("description");
            resultSet.close();

            if(type.equals("r")) {
                return new Router(id, ip, port, login, password, description, backupPeriod);
            } else if(type.equals("s")) {
                return new Switch(id, ip, port, login, password);
            } else {
                throw new SQLException("Unknown device type");
            }
        }
        preparedStatement.close();
        throw new SQLException("No device found");
    }

    public NetworkDevice[] getNetworkDevices() throws SQLException {
        //TODO: improve and use all available fields
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT * FROM `Device`;");
        if(!preparedStatement.execute()) {
            return new NetworkDevice[]{};
        }

        ResultSet resultSet = preparedStatement.getResultSet();
        ArrayList<NetworkDevice> devices = new ArrayList<NetworkDevice>();

        while(resultSet.next()) {
            int id = resultSet.getInt("id");
            String type = resultSet.getString("type");
            String ip = resultSet.getString("ip");
            int port = resultSet.getInt("port");
            String hostname = resultSet.getString("hostname");
            String login = resultSet.getString("login");
            String password = resultSet.getString("password");
            int backupPeriod = resultSet.getInt("backup_period");
            String description = resultSet.getString("description");
            System.out.println(Integer.toString(backupPeriod));

            NetworkDevice device;
            if(type.equals("r")) {
                device = new Router(id, ip, port, login, password, description, backupPeriod);
            } else if(type.equals("s")) {
                device = new Switch(id, ip, port, login, password);
            } else {
                throw new SQLException("Unknown device type");
            }

            devices.add(device);
        }
        resultSet.close();
        NetworkDevice[] devices1 = new NetworkDevice[devices.size()];
        devices.toArray(devices1);
        return devices1;
    }

    public NetworkDevice getDeviceFromConfig(Config config) throws SQLException {
        return this.getDeviceById(config.getParent().getDeviceID());
    }

    public void addConfig(Config config) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO `Config` (`device_id`, `type`, `content`, `date`) VALUES (?, ?, ?, ?);");

        preparedStatement.setInt(1, config.getParent().getDeviceID());

        //System.out.println(Integer.toString(config.getParent().getDeviceID()));

        preparedStatement.setInt(2, config.getType());
        preparedStatement.setString(3, config.getRawContent());
        preparedStatement.setDate(4, new java.sql.Date(config.getTimestamp().getTime()));

        preparedStatement.execute();
        preparedStatement.close();
        this.connection.commit();
    }

    public void removeConfig(Config config) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM `Config`" +
                "WHERE ((`id` = ?));");
        preparedStatement.setInt(1, config.getConfigID());

        preparedStatement.execute();
        preparedStatement.close();
        this.connection.commit();
    }

    public Config[] getConfigsByDevice(NetworkDevice device) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT * FROM `Config` WHERE `device_id` = ?;");
        preparedStatement.setInt(1, device.getDeviceID());

        if(preparedStatement.execute()) {
            ResultSet resultSet = preparedStatement.getResultSet();
            ArrayList<Config> configs = new ArrayList<Config>();

            while(resultSet.next()) {
                int id = resultSet.getInt("id");
                int type = resultSet.getInt("type");
                String content = resultSet.getString("content");
                Date date = resultSet.getDate("date");

                configs.add(new Config(id, type, content, new java.util.Date(date.getTime()), device));
            }
            resultSet.close();
            Config[] configs1 = new Config[configs.size()];
            configs.toArray(configs1);
            return configs1;
        }
        throw new SQLException("No device found");
    }

    public Config getLastConfigByDevice(NetworkDevice device) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT * FROM `Config` WHERE `device_id` = ? ORDER BY `id` DESC LIMIT 1;");
        preparedStatement.setInt(1, device.getDeviceID());

        if(preparedStatement.execute()) {
            ResultSet resultSet = preparedStatement.getResultSet();

            if(resultSet.next()) {
                int id = resultSet.getInt("id");
                int type = resultSet.getInt("type");
                String content = resultSet.getString("content");
                Date date = resultSet.getDate("date");

                return new Config(id, type, content, new java.util.Date(date.getTime()), device);
            }
            resultSet.close();

        }
        throw new SQLException("No such config found");
    }

    public Config getConfigById(int id) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT * FROM `Config` WHERE `id` = ?;");
        preparedStatement.setInt(1, id);
        if(preparedStatement.execute()) {
            ResultSet resultSet = preparedStatement.getResultSet();
            ArrayList<Config> configs = new ArrayList<Config>();

            resultSet.next();

            int type = resultSet.getInt("type");
            String content = resultSet.getString("content");
            Date date = resultSet.getDate("date");

            resultSet.close();
            return new Config(id, type, content, new java.util.Date(date.getTime()), null); // TODO: bad device
        }
        throw new SQLException("No device found");
    }


}
