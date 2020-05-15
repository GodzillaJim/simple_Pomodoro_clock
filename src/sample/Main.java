package sample;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {
    private static Stage stage;

    public static Stage getStage() {
        return Main.stage;
    }

    public void setStage(Stage stage) {
        Main.stage = stage;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        setStage(primaryStage);
        Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
        primaryStage.setTitle("Time-ree");
        Scene scene = new Scene(root, 640, 420);
        primaryStage.setScene(scene);
        primaryStage.setMaxWidth(scene.getWidth());
        primaryStage.setMaxHeight(scene.getHeight());
        primaryStage.getIcons().add(new Image("resources/clock.jpeg"));
        primaryStage.setResizable(true);
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                primaryStage.setIconified(true);
            }
        });
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
