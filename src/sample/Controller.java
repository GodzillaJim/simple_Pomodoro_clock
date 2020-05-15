package sample;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Controller extends Observable {
    static int sessionLengthValue = 25;
    Media audio;
    @FXML
    public Button set;
    Boolean isSession = true;

    public void setAudio(Media audio) {
        this.audio = audio;
    }

    ToDisplay display = new ToDisplay();
    @FXML
    Label topDisplay = new Label("00:00");
    @FXML
    Label indicatorLabel;
    static int breakLengthValue = 5;
    public VBox container;
    boolean session = true;
    @FXML
    Label sessionLength;
    @FXML
    Label breakLength;
    @FXML
    Label timerRunning;
    @FXML
    Button subtractSessionLength, playButton, pauseButton, restartButton;
    @FXML
    Button addBreakLength;
    @FXML
    Button subtractBreakLength;
    @FXML
    Button addSessionLength;
    int temporaryTime = sessionLengthValue * 60;
    boolean paused = false;
    boolean running = false;
    MediaPlayer mediaPlayer;
    Task counter = new Task<Void>() {
        @Override
        protected Void call() throws Exception {
            while (running) {
                if (session) {
                    temporaryTime = sessionLengthValue * 60;
                    session = false;
                } else {
                    temporaryTime = breakLengthValue * 60;
                    session = true;
                }
                while (temporaryTime > 0) {
                    float remainingPercentage;
                    int sessionSeconds = sessionLengthValue * 60;
                    int breakSeconds = breakLengthValue * 60;
                    if (!session) {
                        remainingPercentage = Float.parseFloat(String.valueOf(temporaryTime)) / Float.parseFloat(String.valueOf(sessionSeconds));
                    } else {
                        remainingPercentage = Float.parseFloat(String.valueOf(temporaryTime)) / Float.parseFloat(String.valueOf(breakSeconds));
                    }
                    Color color;
                    if (remainingPercentage >= 0.85) {
                        color = Color.GREEN;
                    } else if (remainingPercentage >= 0.05 && remainingPercentage <= 0.85) {
                        color = Color.WHITE;
                    } else {
                        color = Color.RED;
                    }
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            timerRunning.setTextFill(color);
                        }
                    });
                    updateTimeRunner();
                    Thread.sleep(1000);
                    if (!paused && running) {
                        temporaryTime--;
                        changeTime();
                    }
                    if (temporaryTime == 0) {
                        ringOnEndOfSession();
                        Thread.sleep((long) audio.getDuration().toMillis());
                    }
                }
                reachedZero();
            }
            return null;
        }
    };

    @FXML
    public void initialize() {
        set.onMouseClickedProperty().addListener(new ChangeListener<EventHandler<? super MouseEvent>>() {
            @Override
            public void changed(ObservableValue<? extends EventHandler<? super MouseEvent>> observable, EventHandler<? super MouseEvent> oldValue, EventHandler<? super MouseEvent> newValue) {

            }
        });
        Media audio = new Media(new File("D:\\projects\\Artificial Intelligence\\Java\\javafx\\sample_ui\\src\\sample\\alert1.mp3").toURI().toString());
        setAudio(audio);
        System.out.println(audio.durationProperty().toString());
        if (!SystemTray.isSupported()) {
            System.out.println("System tray not supported");
            return;
        }
        PopupMenu popupMenu = new PopupMenu();

        File file = new File("D:\\projects\\Artificial Intelligence\\Java\\javafx\\sample_ui\\src\\resources\\clock.jpeg");
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Image image = toolkit.createImage(file.getPath());
        TrayIcon trayIcon = new TrayIcon(image);
        SystemTray tray = SystemTray.getSystemTray();
        MenuItem aboutItem = new MenuItem("About");
        MenuItem exitItem = new MenuItem("Exit");

        exitItem.addActionListener(e -> {
            Platform.exit();
            tray.remove(trayIcon);
        });

        popupMenu.add(aboutItem);
        popupMenu.addSeparator();
        popupMenu.add(exitItem);

        trayIcon.setPopupMenu(popupMenu);
        try {
            tray.add(trayIcon);
        } catch (Exception e) {
            e.printStackTrace();
        }
        addObserver(display);

    }

    ExecutorService pool = Executors.newCachedThreadPool();
    Thread thread = new Thread(counter);

    public void changeTime() {
        String time = makeDate();
        hasChanged();
        notifyObservers(time);
    }

    @FXML
    void increaseSessionLength() {
        if (getSessionLengthValue() < 59) {
            sessionLengthValue++;
            sessionLength.setText(String.valueOf(sessionLengthValue));
            temporaryTime = sessionLengthValue * 60;
            updateTimeRunner();
        }
    }

    public static int getSessionLengthValue() {
        return sessionLengthValue;
    }
    @FXML
    void decreaseSessionLength(){
        if (sessionLengthValue > 0) {
            sessionLengthValue--;
            sessionLength.setText(String.valueOf(sessionLengthValue));
            temporaryTime = sessionLengthValue * 60;
            updateTimeRunner();
            //updateGUI();
        }
    }
    private String makeDate(){
        int minutes = temporaryTime / 60;
        int seconds = temporaryTime % 60;
        LocalTime time = LocalTime.of(0,minutes,seconds);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("mm:ss");
        return time.format(formatter);
    }
    @FXML
    void increaseBreakLength(){
        if(breakLengthValue < 59){
            breakLengthValue++;
            breakLength.setText(String.valueOf(breakLengthValue));
        }

        //updateGUI();
    }
    @FXML
    void decreaseBreakLength(){
        if (breakLengthValue > 0) {
            breakLengthValue--;
            breakLength.setText(String.valueOf(breakLengthValue));
        }
        //updateGUI();
    }
    @FXML
    void play() throws FileNotFoundException {
        running = true;
        if(thread.getState() == Thread.State.NEW){
            thread.start();
        }else {
            setPaused(false);
        }
    }
    void updateTimeRunner(){
        String time = makeDate();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (topDisplay != null) {
                    topDisplay.setText(time);
                }
                timerRunning.setText(time);
            }
        });
    }
    public void pauseCounter(ActionEvent actionEvent) throws InterruptedException {
        setPaused(true);
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    @FXML
    public void reset() {
        running = false;
        temporaryTime = sessionLengthValue * 60;
        session = true;
    }

    private void ringOnEndOfSession() {
        mediaPlayer = new MediaPlayer(audio);
        mediaPlayer.play();
    }

    public void onMinimizingProperty() {
    }

    public void onMaximizingProperty() {
    }

    public void reachedZero() {
        if (session) {
            indicatorLabel.setText("Break");
            Popup popup = new Popup();
            popup.centerOnScreen();
            popup.getContent().add(new Label("It's break time"));
            popup.setHeight(300);
            popup.setWidth(60);
            popup.show(Main.getStage());
        } else {
            indicatorLabel.setText("Session");
            Popup popup = new Popup();
            popup.centerOnScreen();
            popup.getContent().add(new Label("It's session"));
            popup.show(Main.getStage());
        }
    }

    @FXML
    public void manageScene() throws IOException {
        if (session) {
            set.setText("Session");
        } else {
            set.setText("Break");
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                topDisplay.setFont(new Font(45));
                topDisplay.setStyle("-fx-font-style: bold");
                topDisplay.setAlignment(Pos.CENTER);
                topDisplay.setTextFill(Color.RED);
                StackPane pane = new StackPane();
                pane.setPrefSize(150, 60);
                pane.getChildren().add(topDisplay);
                StackPane.setAlignment(pane, Pos.CENTER);
                pane.setStyle("-fx-background-color: #" + 364895);


                Scene sceneTwo = new Scene(pane);
                Stage stage = new Stage();
                stage.setScene(sceneTwo);
                stage.setWidth(208);
                stage.setHeight(60);
                stage.initStyle(StageStyle.UNDECORATED);
                Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
                stage.setX((bounds.getWidth() - stage.getWidth()) / 2);
                stage.setY(0);
                stage.setAlwaysOnTop(true);
                stage.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                Main.getStage().setIconified(false);
                                Main.getStage().show();
                            }
                        });
                    }
                });
                stage.show();
            }
        });
    }
}
