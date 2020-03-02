import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import javax.swing.text.TabExpander;
import java.io.IOException;

public class EKGApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("EKGPane.fxml"));
        try {
            AnchorPane anchorPane = loader.load();
            Scene scene = new Scene(anchorPane);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        primaryStage.setOnCloseRequest(windowEvent->System.exit(0));



    }
}
