/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bzh.terrevirtuelle.navisu.charts.vector.s57.databases.impl.controller;

import bzh.terrevirtuelle.navisu.app.drivers.instrumentdriver.InstrumentDriverManagerServices;
import bzh.terrevirtuelle.navisu.app.guiagent.GuiAgentServices;
import bzh.terrevirtuelle.navisu.app.guiagent.layers.LayersManagerServices;
import bzh.terrevirtuelle.navisu.app.guiagent.layertree.LayerTreeServices;
import bzh.terrevirtuelle.navisu.charts.vector.s57.charts.S57ChartComponentServices;
import bzh.terrevirtuelle.navisu.charts.vector.s57.charts.impl.controller.navigation.S57Controller;
import bzh.terrevirtuelle.navisu.charts.vector.s57.databases.impl.S57DBComponentImpl;
import bzh.terrevirtuelle.navisu.charts.vector.s57.databases.impl.controller.loader.TOPMAR_DbLoader;
import bzh.terrevirtuelle.navisu.core.view.geoview.worldwind.impl.GeoWorldWindViewImpl;
import bzh.terrevirtuelle.navisu.database.relational.DatabaseServices;
import bzh.terrevirtuelle.navisu.domain.charts.vector.s57.model.geo.Buoyage;
import bzh.terrevirtuelle.navisu.util.Pair;
import gov.nasa.worldwind.WorldWindow;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import bzh.terrevirtuelle.navisu.widgets.impl.Widget2DController;
import gov.nasa.worldwind.layers.RenderableLayer;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import org.postgis.PGgeometry;

/**
 * @author Serge Morvan
 * @date 14/02/2018 12:49
 */
public class S57DBComponentController
        extends Widget2DController
        implements Initializable {

    protected S57DBComponentImpl component;
    private String componentKeyName;
    protected static final Logger LOGGER = Logger.getLogger(S57DBComponentController.class.getName());
    protected GuiAgentServices guiAgentServices;
    protected LayerTreeServices layerTreeServices;
    protected LayersManagerServices layersManagerServices;
    protected DatabaseServices databaseServices;
    protected InstrumentDriverManagerServices instrumentDriverManagerServices;

    private final String FXML = "s57DBController.fxml";

    protected String CONFIG_FILE_NAME = System.getProperty("user.home") + "/.navisu/config/config.properties";
    protected static final String ALARM_SOUND = "/data/sounds/pling.wav";
    protected static final String DATA_PATH = System.getProperty("user.dir").replace("\\", "/");
    private final String USER = "admin";
    private final String PASSWD = "admin";
    protected Properties properties;
    private static final String NAME = "S57DB";
    protected static final String GROUP = "S57 charts";

    private static final String CSS_STYLE_PATH = Paths.get(System.getProperty("user.dir") + "/css/").toUri().toString();
    protected String viewgroupstyle = "configuration.css";

    /* Common controls */
    @FXML
    public Group view;
    @FXML
    public Pane viewPane;
    @FXML
    public TabPane toolsTabPane;
    @FXML
    public Pane quit;
    @FXML
    public Button requestButton;
    @FXML
    public Button helpButton;
    @FXML
    public ChoiceBox<String> databasesCB;
    @FXML
    public ChoiceBox<String> objectsCB;
    @FXML
    public Button latLonButton;
    @FXML
    public Button interactiveButton;
    @FXML
    public TextField databaseTF;
    @FXML
    public TextField objectsTF;
    @FXML
    public Label latMinLabel;
    @FXML
    public Label lonMinLabel;
    @FXML
    public Label latMaxLabel;
    @FXML
    public Label lonMaxLabel;

    @FXML
    public TextField hostnameTF;
    @FXML
    public TextField encPortDBTF;

    protected Map<String, String> acronyms;
    protected WorldWindow wwd = GeoWorldWindViewImpl.getWW();
    protected String marsys;

    protected final Set<S57Controller> s57Controllers = new HashSet<>();
    protected boolean first = true;
    protected RenderableLayer layer;
    protected double lat0;
    protected double lon0;
    protected double lat1;
    protected double lon1;
    protected Connection connection;
    private Map<Pair<Double, Double>, String> topMarks = new HashMap<>();
    private ObservableList<String> dbCbData = FXCollections.observableArrayList("s57NP1DB", "s57NP2DB", "s57NP3DB", "s57NP4DB", "s57NP5DB", "s57NP6DB");
    /*
    private ObservableList<String> objectsCbData = FXCollections.observableArrayList("ALL : All objects",
            "BUOYAGE : All buoyage",
            "ACHARE : AnchorageArea",
            "BCNCAR : BeaconCardinal",
            "BCNISD : BeaconIsolatedDanger",
            "BCNLAT : BeaconLateral",
            "BCNSAW : BeaconSafeWater",
            "BCNSPP : BeaconSpecialPurposeGeneral",
            "BOYCAR : BuoyCardinal",
            "BOYINB : BuoyInstallation",
            "BOYISD : BuoyIsolatedDanger",
            "BOYLAT : BuoyLateral",
            "BOYSAW : BuoySafeWater",
            "BOYSPP : BuoySpecial",
            "DAYMAR : Daymark",
            "DEPARE : DepthArea",
            "DEPCNT : DepthContour",
            "DOCARE : DockArea",
            "DRGARE : DredgedArea",
            "FAIRWY : Fairway",
            "LAKARE : Lake",
            "LIGHTS : Light",
            "LNDMRK : Landmark",
            "MIPARE : MilitaryPracticeArea",
            "MORFAC : MooringWarpingFacility",
            "NAVLNE : NavigationLine",
            "OBSTRN : NavigationalSystemOfMarks",
            "PONTON : Pontoon",
            "RESARE : RestrictedArea",
            "SEAARE : SeaAreaNamedWaterArea",
            "SLCONS : ShorelineConstruction",
            "SOUNDG : Sounding",
            "TOPMAR : Topmark",
            "TSSBND : TrafficSeparationSchemeBoundary",
            "UNSARE : UnsurveyedArea",
            "UWTROC : UnderwaterAwashRock",
            "WRECKS : Wreck");
     */
    private ObservableList<String> objectsCbData = FXCollections.observableArrayList(
            "ALL : All objects",
            "BUOYAGE : All buoyage",
            "BCNCAR : BeaconCardinal",
            "BCNISD : BeaconIsolatedDanger",
            "BCNLAT : BeaconLateral",
            "BCNSAW : BeaconSafeWater",
            "BCNSPP : BeaconSpecialPurposeGeneral",
            "BOYCAR : BuoyCardinal",
            "BOYINB : BuoyInstallation",
            "BOYISD : BuoyIsolatedDanger",
            "BOYLAT : BuoyLateral",
            "BOYSAW : BuoySafeWater",
            "BOYSPP : BuoySpecial",
            "TOPMAR : Topmark"
    );
    public static final Map<String, String> S57_REQUEST_MAP = Collections.unmodifiableMap(new HashMap<String, String>() {
        {
            put("TOPMAR", "SELECT geom, topshp "
                    + " FROM topmar "
                    + " WHERE geom && ST_MakeEnvelope");
            put("BCNCAR", "SELECT objnam, geom, bcnshp, colour, colpat, marsys, rcid, height, catcam "
                    + " FROM bcncar "
                    + " WHERE geom && ST_MakeEnvelope");
            put("BCNLAT", "SELECT objnam, geom, bcnshp, colour, colpat, marsys, rcid, height, catlam "
                    + " FROM bcnlat "
                    + " WHERE geom && ST_MakeEnvelope");
            put("BCNISD", "SELECT objnam, geom, bcnshp, colour, colpat, marsys, rcid, height"
                    + " FROM bcnisd  "
                    + " WHERE geom && ST_MakeEnvelope");
            put("BCNSAW", "SELECT objnam, geom, bcnshp, colour, colpat, marsys, rcid, height"
                    + " FROM bcnsaw "
                    + " WHERE geom && ST_MakeEnvelope");
            put("BCNSPP", "SELECT objnam, geom, bcnshp, colour, colpat, marsys, rcid, height, catspm"
                    + " FROM bcnspp "
                    + " WHERE geom && ST_MakeEnvelope");
            put("BCNISD", "SELECT objnam, geom, bcnshp, colour, colpat, marsys, rcid, height"
                    + " FROM bcnisd "
                    + " WHERE geom && ST_MakeEnvelope");
            put("BOYCAR", "SELECT objnam, geom, boyshp, colour, colpat, marsys, rcid, catcam"
                    + " FROM boycar "
                    + " WHERE geom && ST_MakeEnvelope");
            put("BOYINB", "SELECT objnam, geom, boyshp, colour, colpat, marsys, rcid"
                    + " FROM boyinb "
                    + " WHERE geom && ST_MakeEnvelope");
            put("BOYISD", "SELECT objnam, geom, boyshp, colour, colpat, marsys, rcid"
                    + " FROM boyisd "
                    + " WHERE geom && ST_MakeEnvelope");
            put("BOYLAT", "SELECT objnam, geom, boyshp, colour, colpat, marsys, rcid, catlam"
                    + " FROM boylat"
                    + " WHERE geom && ST_MakeEnvelope");
            put("BOYSAW", "SELECT objnam, geom, boyshp, colour, colpat, marsys, rcid"
                    + " FROM boysaw "
                    + " WHERE geom && ST_MakeEnvelope");
            put("BOYSPP", "SELECT objnam, geom, boyshp, colour, colpat, marsys, rcid, catspm"
                    + " FROM boyspp"
                    + " WHERE geom && ST_MakeEnvelope");
        }
    ;

    });
    
    public S57DBComponentController(S57DBComponentImpl component, String componentKeyName,
            KeyCode keyCode, KeyCombination.Modifier keyCombination,
            GuiAgentServices guiAgentServices,
            LayersManagerServices layersManagerServices,
            S57ChartComponentServices s57ChartComponentServices,
            DatabaseServices databaseServices,
            InstrumentDriverManagerServices instrumentDriverManagerServices) {
        super(keyCode, keyCombination);
        this.componentKeyName = componentKeyName;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(FXML));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.toString(), ex);
        }
        String uri = CSS_STYLE_PATH + viewgroupstyle;
        view.getStylesheets().add(uri);
        this.component = component;
        this.guiAgentServices = guiAgentServices;
        // this.s57ChartComponentServices = s57ChartComponentServices;
        this.layersManagerServices = layersManagerServices;
        this.databaseServices = databaseServices;
        this.instrumentDriverManagerServices = instrumentDriverManagerServices;

        guiAgentServices.getScene().addEventFilter(KeyEvent.KEY_RELEASED, this);
        guiAgentServices.getRoot().getChildren().add(this);
        layer = layersManagerServices.getLayer(GROUP, NAME);
        // System.out.println("layer : " + layer.getName());
        initAcronymsMap();
    }

    private void initAcronymsMap() {
        acronyms = new HashMap<>();
        String tmp;
        String[] tmpTab;
        try {
            BufferedReader input = new BufferedReader(
                    new FileReader("properties/s57AcronymClasses.table"));
            while ((tmp = input.readLine()) != null) {
                tmpTab = tmp.split(",");
                acronyms.put(tmpTab[0], tmpTab[1]);
            }
        } catch (IOException ex) {
            Logger.getLogger(S57DBComponentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        databasesCB.setItems(dbCbData);
        databasesCB.getSelectionModel().select("s57NP5DB");
        databaseTF.setText("s57NP5DB");
        databasesCB.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (ObservableValue<? extends String> observable, String oldValue, String newValue)
                        -> databaseTF.setText(databasesCB.getValue())
                );
        objectsCB.setItems(objectsCbData);
        // initCheckBox( "properties/objects_1.csv", objectsCB);
        objectsCB.getSelectionModel().select("ALL : All objects");
        objectsTF.setText("ALL");
        objectsCB.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (ObservableValue<? extends String> observable, String oldValue, String newValue)
                        -> {
                    String[] v = newValue.split(":");
                    objectsTF.setText(v[0]);
                }
                );

        quit.setOnMouseClicked((MouseEvent event) -> {
            component.off();
        });
        helpButton.setOnMouseClicked((MouseEvent event) -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Options");
            alert.setHeaderText("Display S57 objects");
            Text s = new Text(
                    "    Choice database with scale :\n\n"
                    + "    s57NP1DB - Overview 	< 1 : 1 500 000\n"
                    + "    s57NP2DB - General 	1 : 350 000 à 1 : 1 500 000\n"
                    + "    s57NP3DB - Coastal 	1 : 90 000 à 1 : 350 000\n"
                    + "    s57NP4DB - Approach 	1 : 22 000 à 1 : 90 000\n"
                    + "    s57NP5DB - Harbour 	1 : 4 000 à 1 : 22 000\n"
                    + "    s57NP6DB - Berthing 	> 1 : 4 000\n\n"
                    + "    Selection of objects to display");
            s.setWrappingWidth(650);
            alert.getDialogPane().setContent(s);
            alert.show();
        });
        latLonButton.setOnMouseClicked((MouseEvent event) -> {
            initSelectedZone();
        });
        requestButton.setOnMouseClicked((MouseEvent event) -> {

            connection = databaseServices.connect(databaseTF.getText(),
                    "localhost", "jdbc:postgresql://", "5432", "org.postgresql.Driver", "admin", "admin");
            retrieveBuoyageIn(objectsTF.getText(), lat0, lon0, lat1, lon1);
        });

    }

    private void initSelectedZone() {

        Dialog dialog = new Dialog<>();
        dialog.setTitle("Create Area");
        dialog.setHeaderText("Please enter selected area coordinates.");
        dialog.setResizable(false);

        Label lat0Label = new Label("Northwest point latitude : ");
        Label lon0Label = new Label("Northwest point longitude : ");
        Label lat1Label = new Label("Southeast point latatitude : ");
        Label lon1Label = new Label("Southeast point longitude : ");

        TextField lat0TF = new TextField();
        TextField lat1TF = new TextField();
        TextField lon0TF = new TextField();
        TextField lon1TF = new TextField();

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 35, 20, 35));
        grid.add(lat0Label, 0, 0);
        grid.add(lat0TF, 1, 0);
        grid.add(lon0Label, 0, 1);
        grid.add(lon0TF, 1, 1);

        grid.add(lat1Label, 0, 2);
        grid.add(lat1TF, 1, 2);
        grid.add(lon1Label, 0, 3);
        grid.add(lon1TF, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                lat0 = Double.valueOf(lat0TF.getText().trim());
                lon0 = Double.valueOf(lon0TF.getText().trim());
                lat1 = Double.valueOf(lat1TF.getText().trim());
                lon1 = Double.valueOf(lon1TF.getText().trim());
            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Range latitude : -90° <= Latitude <= 90° \n"
                        + "Range longitude : -180° <= Longitude <= 180°");
                alert.show();
            }
            if (lat0 < -90.0 || lat0 > 90.0 || lat1 < -90.0 || lat1 > 90.0) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Range latitude : -90° <= Latitude <= 90°");
                alert.show();
            }
            if (lon0 < -180.0 || lon0 > 180.0 || lon1 < -180.0 || lon1 > 180.0) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Range longitude : -180° <= Lonitude <= 180°");
                alert.show();
            }
            event.consume();
            dialog.close();
            latMinLabel.setText(Double.toString(lat0));
            lonMinLabel.setText(Double.toString(lon0));
            latMaxLabel.setText(Double.toString(lat1));
            lonMaxLabel.setText(Double.toString(lon1));
        });
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        dialog.showAndWait();
    }

    private void initCheckBox(String filename, ChoiceBox<String> cb) {
        /* Pb a l'affichage */
        String content;
        String[] parts;
        try {
            content = new String(Files.readAllBytes(Paths.get(filename)));
            parts = content.split(",");
            ObservableList<String> cbData = FXCollections.observableArrayList();
            cbData.addAll(Arrays.asList(parts));
            cb.setItems(cbData);
            System.out.println("cbData : " + cbData);
        } catch (IOException ex) {
            Logger.getLogger(S57DBComponentController.class.getName()).log(Level.SEVERE, ex.toString(), ex);
        }
    }

    public List<Buoyage> retrieveBuoyageIn(String object, double latMin, double lonMin,
            double latMax, double lonMax) {
        
        TOPMAR_DbLoader topmarDbLoader = new TOPMAR_DbLoader(connection, topMarks); 
        
        topmarDbLoader.retrieveIn(latMin, lonMin, latMax, lonMax);
        
        
        List<Buoyage> tmp1 = new ArrayList<>();
        PGgeometry geom;
        ResultSet r;
        if (connection != null) {
            try {
                //  System.out.println("object : " + object);
                String request = S57_REQUEST_MAP.get(object.trim());
                request += "(" + lonMin + ", " + latMin + ", "
                        + lonMax + ", " + latMax + ", "
                        + "4326);";
                System.out.println("request : " + request);
                r = connection.createStatement().executeQuery(request);
                while (r.next()) {
                    geom = (PGgeometry) r.getObject(2);
                    /*
                    Point3D pt = new Point3D(geom.getGeometry().getFirstPoint().getX(),
                            geom.getGeometry().getFirstPoint().getY(),
                            0.0);
                    tmp1.add(pt);
                     */
                }
                System.out.println("tmp1 : " + tmp1);

            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, ex.toString(), ex);
            }
        } else {
            // alert();
        }
        return tmp1;
    }
}