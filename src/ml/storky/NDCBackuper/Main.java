package ml.storky.NDCBackuper;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


/**
 * Created by storky on 16.4.16.
 */
public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("layouts/MainLayout.fxml"));
        primaryStage.setTitle("NDC-Backuper");
        primaryStage.setScene(new Scene(root, 900, 600));
        primaryStage.show();
    }


    public void start() {
        launch();
    }

    public static void main(String[] args) {

    }
}
