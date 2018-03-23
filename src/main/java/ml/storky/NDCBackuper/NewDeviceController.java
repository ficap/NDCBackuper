package ml.storky.NDCBackuper;

import com.jcraft.jsch.JSchException;
import com.sun.javafx.css.Stylesheet;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.StyleOrigin;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * Created by storky on 30.4.16.
 */
public class NewDeviceController implements Initializable {
    public Button okButton;
    public Button cancelButton;
    public Button testSettingsButton;

    public TextField ipAddressTextField;
    public TextField usernameTextField;
    public PasswordField passwordPasswordField;
    public TextField descriptionTextField;
    public CheckBox autoConfigBackupCheckBox;

    public Label backupPeriodLabel;
    public TextField backupPeriodTextField;
    public ComboBox<String> backupPeriodUnitsComboBox;

    public Circle ipAddressIndicator;
    public Circle backupPeriodIndicator;
    public Label idLabel;

    private Database db;

    public void initialize(URL location, ResourceBundle resources) {
        try {
            this.db = Database.getDefaultDatabase();
            System.out.println(this.db.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            // TODO: notify user the db is not available
        }
        ObservableList<String> backupUnits = FXCollections.observableArrayList("mins", "hours", "days", "months");
        this.backupPeriodUnitsComboBox.setItems(backupUnits);
        this.backupPeriodUnitsComboBox.getSelectionModel().select(0);

    }


    public void handleAutoConfigBackupCheckBox(ActionEvent actionEvent) {
        if(((CheckBox) actionEvent.getSource()).isSelected()) {
            this.backupPeriodLabel.setVisible(true);
            this.backupPeriodTextField.setVisible(true);
            this.backupPeriodUnitsComboBox.setVisible(true);
        } else {
            this.backupPeriodLabel.setVisible(false);
            this.backupPeriodTextField.setVisible(false);
            this.backupPeriodUnitsComboBox.setVisible(false);

        }
    }

    public void handleCancelAction(ActionEvent actionEvent) {
        ((Stage)this.cancelButton.getScene().getWindow()).close();
    }

    public void handleOKAction(ActionEvent actionEvent) {
        this.ipAddressIndicator.setVisible(false);
        this.backupPeriodIndicator.setVisible(false);

        String ipAddress = this.ipAddressTextField.getText();
        String username = this.usernameTextField.getText();
        String password = this.passwordPasswordField.getText();
        String description = this.descriptionTextField.getText();
        int backup_period = 0;

        boolean fail = false;

        if(this.autoConfigBackupCheckBox.isSelected()) {
            try {
                backup_period = Integer.parseInt(this.backupPeriodTextField.getText());
                String selected = this.backupPeriodUnitsComboBox.getSelectionModel().getSelectedItem();
                if(selected.equals("mins")) {
                    backup_period *= 60;
                } else if(selected.equals("hours")) {
                    backup_period *= 3600;
                } else if(selected.equals("days")) {
                    backup_period *= 86400;
                } else if(selected.equals("months")) {
                    backup_period *= 2592000; // 1 month has 30 days
                }
            } catch (NumberFormatException e) {
                fail = true;
                this.backupPeriodIndicator.setVisible(true);
                this.backupPeriodTextField.selectAll();
            }
        }

        if(ipAddress == null || !ipAddress.matches("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")) {
            this.ipAddressIndicator.setVisible(true);
            return;
        }

        if(this.idLabel.getText() != null && !this.idLabel.getText().equals("")) {
            try {
                this.db.modifyDevice(new Router(Integer.parseInt(this.idLabel.getText()), ipAddress, 22, username, password, description, backup_period)); // not optimal you know why :D
                ((Stage)this.okButton.getScene().getWindow()).close();
            } catch (SQLException e) {
                // TODO: notify user
                e.printStackTrace();
            }
            return;
        }

        try {
            this.db.addDevice(new Router(ipAddress, description, 22, username, password, backup_period));
            ((Stage)this.okButton.getScene().getWindow()).close();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
            //TODO: notify user
        }


    }

    public void handleTestSettingsAction(ActionEvent actionEvent) {
        String ipAddress = this.ipAddressTextField.getText();
        String username = this.usernameTextField.getText();
        String password = this.passwordPasswordField.getText();
        String description = this.descriptionTextField.getText();
        int backup_period = 0;

        boolean fail = false;

        if (this.autoConfigBackupCheckBox.isSelected()) {
            try {
                backup_period = Integer.parseInt(this.backupPeriodTextField.getText());
                String selected = this.backupPeriodUnitsComboBox.getSelectionModel().getSelectedItem();
                if (selected.equals("mins")) {
                    backup_period *= 60;
                } else if (selected.equals("hours")) {
                    backup_period *= 3600;
                } else if (selected.equals("days")) {
                    backup_period *= 86400;
                } else if (selected.equals("months")) {
                    backup_period *= 2592000; // 1 month has 30 days
                }
            } catch (NumberFormatException e) {
                fail = true;
                this.backupPeriodIndicator.setVisible(true);
                this.backupPeriodTextField.selectAll();
            }
        }

        if (ipAddress == null || !ipAddress.matches("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")) {
            this.ipAddressIndicator.setVisible(true);
            return;
        }

        if (this.idLabel.getText() != null && !this.idLabel.getText().equals("")) {
            try {
                new Router(Integer.parseInt(this.idLabel.getText()), ipAddress, 22, username, password, description, backup_period).tryConnection();
                this.testSettingsButton.setText("OK");
            } catch (JSchException e) {
                this.testSettingsButton.setText("FAIL");
            } catch (IOException e) {
                this.testSettingsButton.setText("FAIL");
            }
            return;
        }

        try {
            new Router(ipAddress, description, 22, username, password, backup_period).tryConnection();
            this.testSettingsButton.setText("OK");
        } catch (JSchException e) {
            this.testSettingsButton.setText("FAIL");
        } catch (IOException e) {
            this.testSettingsButton.setText("FAIL");
        }
    }

        private boolean checkInput() {
        //TODO: checking
        return true;
    }
}
