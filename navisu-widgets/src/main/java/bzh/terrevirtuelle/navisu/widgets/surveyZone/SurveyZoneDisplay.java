/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bzh.terrevirtuelle.navisu.widgets.surveyZone;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Serge
 */
public class SurveyZoneDisplay extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        SurveyZoneDisplayController controller = new SurveyZoneDisplayController();
        Scene scene = new Scene(controller);
        
        stage.setScene(scene);
        stage.show();
        controller.schedule();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
