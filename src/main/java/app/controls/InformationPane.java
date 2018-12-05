package app.controls;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import weblearning.Information;

import java.io.IOException;
import java.net.URL;

public class InformationPane extends Pane {
    private static final URL fxml = InformationPane.class.getResource("/app/controls/InformationPane.fxml");
    private Information information;

    public InformationPane() {
        FXMLLoader fxmlLoader = new FXMLLoader(fxml);
        fxmlLoader.setRoot(this);
        fxmlLoader.setControllerFactory(ignored -> this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void setInformation(Information information) {
        this.information = information;
        Platform.runLater(this::updateInformation);
    }

    private void updateInformation() {

    }
}
