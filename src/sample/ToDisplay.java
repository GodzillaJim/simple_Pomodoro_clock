package sample;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.Observable;
import java.util.Observer;

public class ToDisplay implements Observer {
    @FXML
    Label topDisplay;

    @Override
    public void update(Observable o, Object arg) {
        topDisplay.setText(arg.toString());
    }
}
