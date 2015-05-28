/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bzh.terrevirtuelle.navisu.instruments.common.view;

import bzh.terrevirtuelle.navisu.app.guiagent.GuiAgentServices;
import bzh.terrevirtuelle.navisu.domain.ship.model.Ship;
import bzh.terrevirtuelle.navisu.widgets.impl.Widget2DController;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

/**
 *
 * @author Serge
 */
public class TrackPanel
        extends Widget2DController
        implements Initializable {

    @FXML
    public Group trackPanel;
    @FXML
    TextArea newNames;
    @FXML
    public Text timeStamp;
    @FXML
    public Text shipsInSight;
    @FXML
    public Text countOfAisShipsReceived;
    @FXML
    public ImageView quit;
    @FXML
    public Slider slider;
    //  @FXML
    // public Button photo;
    NumberFormat nf = new DecimalFormat("0.###");
    SimpleDateFormat dt = new SimpleDateFormat("hh:mm dd-MM");
    protected GuiAgentServices guiAgentServices;

    public TrackPanel(GuiAgentServices guiAgentServices, KeyCode keyCode, KeyCombination.Modifier keyCombination) {
        super(keyCode, keyCombination);
        this.guiAgentServices = guiAgentServices;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("aisTracks.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        trackPanel.setOpacity(0.8);
        quit.setOnMouseClicked((MouseEvent event) -> {
            trackPanel.setVisible(false);
        });
        slider.valueProperty().addListener((ObservableValue<? extends Number> ov, Number old_val, Number new_val) -> {
            Platform.runLater(() -> {
                trackPanel.setOpacity(slider.getValue());
            });
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TODO 

    }

    public void updateAisPanel(Ship ship) {
        updateAisPanel(ship, null);
    }

    public void updateAisPanel(Ship ship,
            Map<Integer, Calendar> timestamps) {
        setVisible(true);
        
    }
}
