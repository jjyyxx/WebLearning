package app.controls;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXHamburger;
import com.jfoenix.controls.JFXNodesList;
import javafx.animation.Transition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import java.io.IOException;

public class QuickButtonList extends JFXNodesList {
    @FXML private JFXButton nodesRoot;
    @FXML private JFXHamburger nodesRootBurger;
    @FXML private JFXButton refreshButton;
    @FXML private JFXButton profileButton;
    @FXML private JFXButton inboxButton;

    @FXML
    private void nodesRootClicked() {
        final Transition burgerAnimation = nodesRootBurger.getAnimation();
        burgerAnimation.setRate(burgerAnimation.getRate() * -1);
        burgerAnimation.play();
    }

    public QuickButtonList() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "/app/controls/QuickButtonList.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
