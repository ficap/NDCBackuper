package ml.storky.NDCBackuper;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ResourceBundle;

/**
 * Created by storky on 24.4.16.
 */
public class MainController implements Initializable {
    public ListView devicesListView;
    public TableView configsTableView;
    public TableColumn idConfigsTableColumn;
    public TableColumn dateConfigsTableColumn;
    public TableColumn commentConfigsTableColumn;
    public ImageView deviceTypeImageView;
    public Label hostnameLabel;
    public Label ipLabel;
    public Label lastBackupLabel;
    public Label backupPeriodLabel;
    public Label descriptionLabel;
    public Label leftStatusLabel;
    public MenuItem newRouterMenu;
    public TextArea configTextArea;

    private static final String ROUTER_IMG = "file:resources/router.eps.png";
    private static final String SWITCH_IMG = "file:resources/switch.eps.png";


    private Database db;
    private NetworkDevice selectedDevice;
    private Config.ConfRow selectedConfig;
    private ObservableList<NetworkDevice> devicesList;
    private ObservableList<Config.ConfRow> configsList;

    public void initialize(URL location, ResourceBundle resources) {
        try {
            this.db = Database.getDefaultDatabase();
            System.out.println(this.db.toString());
        } catch (SQLException e) {
            this.leftStatusLabel.setText("Database Error!!");
            return;
        }
        this.devicesList = FXCollections.observableArrayList();

        //for (int i = 0; i < 2; i++) {
            //observe.add(new Router("10.200.200.2", "cisco", "cisco"));
            //observe.add(new Switch("192.168.100.5", "blah", "quo"));
        //}

        this.devicesListView.setCellFactory(new Callback<ListView, ListCell>() {
            public ListCell call(ListView param) {
                return new NetworkDeviceListCell();
            }
        });
        this.devicesListView.setItems(this.devicesList);
        this.devicesListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if(newValue == null) {
                    configsList.clear();
                    return;
                }

                selectedDevice = (NetworkDevice)newValue;
                hostnameLabel.setText(selectedDevice.getHostname());
                ipLabel.setText(selectedDevice.getIpAddress());
                int period = selectedDevice.getBackupPeriod();
                if(period >= 2592000) {
                    backupPeriodLabel.setText(Integer.toString(period / 2592000) + " months");
                } else if(period >= 86400) {
                    backupPeriodLabel.setText(Integer.toString(period / 86400) + " days");
                } else if (period >= 3600) {
                    backupPeriodLabel.setText(Integer.toString(period / 3600) + " hours");
                } else if (period >= 60) {
                    backupPeriodLabel.setText(Integer.toString(period / 60) + " mins");
                } else if (period == 0) {
                    backupPeriodLabel.setText("none");
                }


                descriptionLabel.setText(selectedDevice.getDescription());
                try {
                    lastBackupLabel.setText(DateFormat.getDateInstance().format(db.getLastConfigByDevice(selectedDevice).getTimestamp()));
                } catch (SQLException e) {
                    lastBackupLabel.setText("never");
                }

                if(selectedDevice instanceof Router)
                    deviceTypeImageView.setImage(new Image(ROUTER_IMG));

                if(selectedDevice instanceof Switch)
                    deviceTypeImageView.setImage(new Image(SWITCH_IMG));
                loadConfigs((NetworkDevice) newValue);
                //TODO: change all datas
            }
        });

        this.configsTableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if(newValue == null) {
                    configTextArea.clear();
                    selectedConfig = null;
                    return;
                }
                configTextArea.setText(((Config.ConfRow) newValue).getContent());
                selectedConfig = (Config.ConfRow) newValue;
            }
        });

        this.configsList = FXCollections.observableArrayList();
        //this.configsList.add(new Config(1, Config.RUNNING_CONFIG, "Blah", new Date(System.currentTimeMillis()), null).getConfRow());

        this.idConfigsTableColumn.setCellValueFactory(new PropertyValueFactory<Config.ConfRow, String>("id"));
        this.dateConfigsTableColumn.setCellValueFactory(new PropertyValueFactory<Config.ConfRow, String>("date"));
        this.commentConfigsTableColumn.setCellValueFactory(new PropertyValueFactory<Config.ConfRow, String>("comment"));

        this.configsTableView.setItems(this.configsList);
        this.reloadAll();

    }

    public void handleNewRouterAction(ActionEvent actionEvent) {
        try {
            final FXMLLoader loader = new FXMLLoader(getClass().getResource("/layouts/NewDeviceLayout.fxml"));
            loader.getNamespace().put("selected", "false");
            Parent parent = loader.load();
            Stage stage = new Stage();
            //stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add new network device");
            stage.setScene(new Scene(parent, 500, 250));
            stage.showAndWait();
            this.reloadAll();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void reloadAll() {
        this.devicesList.clear();
        this.configsList.clear();
        this.selectedConfig = null; // because .clear() removes focus
        this.selectedDevice = null; // because .clear() removes focus

        this.hostnameLabel.setText("Hostname");
        this.ipLabel.setText("IP");
        this.lastBackupLabel.setText("never");
        this.backupPeriodLabel.setText("none");
        this.descriptionLabel.setText("");
        try {
            NetworkDevice[] devices = this.db.getNetworkDevices();
            for(NetworkDevice nd : devices) {
                if(nd.getBackupPeriod() == 0) {
                    continue;
                }
                try {
                    if(System.currentTimeMillis() + nd.getBackupPeriod()*1000 > this.db.getLastConfigByDevice(nd).getTimestamp().getTime()) {
                        nd.downloadConfigAndSave(Config.RUNNING_CONFIG);
                    }
                } catch (SQLException e) {
                    nd.downloadConfigAndSave(Config.RUNNING_CONFIG);
                }

            }
            this.devicesList.addAll(this.db.getNetworkDevices());
        } catch (SQLException e) {
            // TODO: notify user
            e.printStackTrace();
        }
    }

    private void reloadConfigs() {
        if(this.selectedDevice == null) {
            return;
        }
        loadConfigs(this.selectedDevice);
    }

    public void backupButtonOnAction(ActionEvent actionEvent) {
        if(this.selectedDevice == null) {
            // TODO: notify user or disable button
            return;
        }
        this.selectedDevice.downloadConfigAndSave(Config.RUNNING_CONFIG);
        this.reloadConfigs();
    }

    public void restoreButtonOnAction(ActionEvent actionEvent) {
        if(this.selectedDevice == null || this.selectedConfig == null) {
            // TODO: notify user or disable button
            return;
        }
        this.selectedDevice.uploadConfig(selectedConfig.getContent());
    }

    public void loadConfigs(NetworkDevice device) {
        this.configsList.clear();
        try {
            Config[] configs = this.db.getConfigsByDevice(device);
            for(Config conf : configs) {
                this.configsList.add(conf.getConfRow());
            }
        } catch (SQLException e) {
            // TODO: notify user
            e.printStackTrace();
        }
    }

    public void handleModifyDeviceAction(ActionEvent actionEvent) {
        if(this.selectedDevice == null) {
            // TODO: notify user or button is disabled
            return;
        }

        try {
            final FXMLLoader loader = new FXMLLoader(getClass().getResource("/layouts/NewDeviceLayout.fxml"));
            loader.getNamespace().put("ipAddress", this.selectedDevice.getIpAddress());
            loader.getNamespace().put("username", this.selectedDevice.getUsername());
            loader.getNamespace().put("description", this.selectedDevice.getDescription());
            loader.getNamespace().put("selected", "false");
            loader.getNamespace().put("id", Integer.toString(this.selectedDevice.getDeviceID()));
            Parent parent = loader.load();
            Stage stage = new Stage();
            //stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Modify network device");
            stage.setScene(new Scene(parent, 500, 250));
            stage.showAndWait();
            this.reloadAll();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void handleDeleteDeviceAction(ActionEvent actionEvent) {
        if(this.selectedDevice == null) {
            // TODO: notify user
            return;
        }
        try {
            this.db.removeDevice(this.selectedDevice, true);
        } catch (SQLException e) {
            // TODO: notify user
            e.printStackTrace();
        }
        this.reloadAll();
    }
}
