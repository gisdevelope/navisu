/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bzh.terrevirtuelle.navisu.stl.vector.s57.charts.impl.controller;

import bzh.terrevirtuelle.navisu.charts.vector.s57.charts.impl.controller.S57ChartComponentController;
import bzh.terrevirtuelle.navisu.charts.vector.s57.charts.impl.controller.loader.M_NSYS_ShapefileLoader;
import bzh.terrevirtuelle.navisu.charts.vector.s57.charts.impl.controller.loader.TOPMAR_ShapefileLoader;
import bzh.terrevirtuelle.navisu.stl.vector.s57.charts.impl.controller.loader.BUOYAGE_Stl_ShapefileLoader;
import bzh.terrevirtuelle.navisu.stl.vector.s57.charts.impl.controller.loader.PONTON_Stl_ShapefileLoader;
import bzh.terrevirtuelle.navisu.stl.vector.s57.charts.impl.controller.loader.SLCONS_Stl_ShapefileLoader;
import com.vividsolutions.jts.geom.Geometry;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.render.Polygon;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author serge
 * @date Apr 24, 2017
 */
public class StlChartComponentController
        extends S57ChartComponentController {

    protected static boolean created = false;
    protected String outDirname;
    protected String outFilename;
    protected String outPathname;
    protected String chartName;
    protected int tilesCount;
    protected int index;
    protected double tileSideX;
    protected double tileSideY;
    protected int ptsCountsX;
    protected int ptsCountsY;
    protected double bottom;
    protected double magnification;
    protected double scaleLatFactor;
    protected double scaleLonFactor;
    protected double buoyageScale;
    protected Polygon polyEnveloppe;
    protected List<? extends Position> positions;
    protected Geometry geometryEnveloppe;
    protected boolean base;
    protected Charset charset = Charset.forName("UTF-8");
    protected ArrayList<String> lines;
    protected double spaceX;
    protected double spaceY;

    public StlChartComponentController() {

    }

    public void init(String path, String chartPath) {
        init(path);
        chartName = new File(chartPath).getName();
    }

    public void compute(String outDirname, String outFilename,//2 compute plutot
            ElevationModel model,
            int tilesCount,
            int index,
            double magnification,
            double tileSideX, double tileSideY,
            int ptsCountsX, int ptsCountsY,
            double bottom,
            boolean base,
            Polygon polyEnveloppe,
            Geometry geometryEnveloppe) {
        this.index = index;
        this.tilesCount = tilesCount;
        this.outDirname = outDirname;
        this.outFilename = outFilename;
        this.outPathname = outDirname + outFilename;
        this.magnification = magnification;
        this.tileSideX = tileSideX;
        this.tileSideY = tileSideY;
        this.ptsCountsX = ptsCountsX;
        this.ptsCountsY = ptsCountsY;
        this.bottom = bottom;
        this.base = base;
        this.polyEnveloppe = polyEnveloppe;
        this.geometryEnveloppe = geometryEnveloppe;
        positions = polyEnveloppe.getBoundaries().get(0);

        //writeInitOutFile(outPathname, chartName);
        //writeRef(outPathname, polyEnveloppe);// repere XYZ
       // writePositionOrientation(outPathname);
       // writeTexture(outDirname, index, polyEnveloppe);
       // writeElevation(outPathname, index, polyEnveloppe, model);
        writeS57Charts(polyEnveloppe, geometryEnveloppe, buoyageScale);
       // writeBase(outPathname, base);
        //writeEndOutFile(outPathname);
    }


    private void write(String outFilename, String str) {
        lines = new ArrayList<>();
        lines.add(str);
        try {
            Files.write(Paths.get(outFilename), lines, charset, StandardOpenOption.APPEND);
        } catch (IOException ex) {
            Logger.getLogger(StlChartComponentController.class.getName()).log(Level.SEVERE, ex.toString(), ex);
        }
    }

 

    protected void writeS57Charts(Polygon polyEnveloppe, Geometry geometryEnveloppe, double buoyageScale) {
        geos = new HashMap<>();
        File[] listOfFiles;
        if (file != null && file.isDirectory()) {
            listOfFiles = file.listFiles();
            // Context variables
            for (File f : listOfFiles) {
                String s = f.getName();
                if (s.equals("M_NSYS.shp")) {
                    M_NSYS_ShapefileLoader nsys = new M_NSYS_ShapefileLoader();
                    nsys.createLayersFromSource(new File(path + "/M_NSYS.shp"));
                    marsys = nsys.getMarsys();
                }
                if (s.equals("TOPMAR.shp")) {
                    load(new TOPMAR_ShapefileLoader(topMarks), "BUOYAGE", "TOPMAR", "/");
                }
            }
            // DEPARE in background
            for (File f : listOfFiles) {
                String s = f.getName();
                switch (s) {
                    case "DEPARE.shp":
                        //   load(new DEPARE_Stl_ShapefileLoader(OUT_FILE, polyEnveloppe), "DEPARE", "DEPARE", "/");
                        break;
                    case "PONTON.shp":
                        PONTON_Stl_ShapefileLoader ponton_Stl_ShapefileLoader
                                = new PONTON_Stl_ShapefileLoader(outPathname, polyEnveloppe,
                                        scaleLatFactor, scaleLonFactor, tileSideX);
                        load(ponton_Stl_ShapefileLoader, "HARBOUR", "PONTON", "/");
                        String resultPonton = ponton_Stl_ShapefileLoader.compute();
                        if (resultPonton != null) {
                            write(outPathname, resultPonton);
                        }
                        break;
                    case "SLCONS.shp":
                        SLCONS_Stl_ShapefileLoader slConsStlShapefileLoader
                                = new SLCONS_Stl_ShapefileLoader(outPathname, polyEnveloppe,
                                        scaleLatFactor, scaleLonFactor, tileSideX);
                        load(slConsStlShapefileLoader, "HARBOUR", "SLCONS", "/");
                        String resultSl = slConsStlShapefileLoader.compute();
                        if (resultSl != null) {
                            write(outPathname, resultSl);
                        }
                        break;
                    case "BCNCAR.shp":
                        BUOYAGE_Stl_ShapefileLoader buoyageStlShapefileLoader
                                = new BUOYAGE_Stl_ShapefileLoader(geometryEnveloppe, polyEnveloppe,
                                        scaleLatFactor, scaleLonFactor,
                                        buoyageScale,
                                        tileSideX, tileSideY,
                                        DEV, BUOYAGE_PATH, topMarks, marsys, "BCNCAR", null);
                        load(buoyageStlShapefileLoader, "BUOYAGE", "BCNCAR", "/");
                        String resultCar = buoyageStlShapefileLoader.compute();
                        if (resultCar != null) {
                            write(outPathname, resultCar);
                        }
                        break;
                    case "BOYCAR.shp":
                        buoyageStlShapefileLoader
                                = new BUOYAGE_Stl_ShapefileLoader(geometryEnveloppe, polyEnveloppe,
                                        scaleLatFactor, scaleLonFactor,
                                        buoyageScale,
                                        tileSideX, tileSideY,
                                        DEV, BUOYAGE_PATH, topMarks, marsys, "BOYCAR", null);
                        load(buoyageStlShapefileLoader, "BUOYAGE", "BOYCAR", "/");
                        resultCar = buoyageStlShapefileLoader.compute();
                        if (resultCar != null) {
                            write(outPathname, resultCar);
                        }
                        break;
                    case "BCNLAT.shp":
                        buoyageStlShapefileLoader
                                = new BUOYAGE_Stl_ShapefileLoader(geometryEnveloppe, polyEnveloppe,
                                        scaleLatFactor, scaleLonFactor,
                                        buoyageScale,
                                        tileSideX, tileSideY,
                                        DEV, BUOYAGE_PATH, topMarks, marsys, "BCNLAT", null);
                        load(buoyageStlShapefileLoader, "BUOYAGE", "BCNLAT", "/");
                        String resultLat = buoyageStlShapefileLoader.compute();
                        if (resultLat != null) {
                            write(outPathname, resultLat);
                        }
                        break;
                    case "BOYLAT.shp":
                        buoyageStlShapefileLoader
                                = new BUOYAGE_Stl_ShapefileLoader(geometryEnveloppe, polyEnveloppe,
                                        scaleLatFactor, scaleLonFactor,
                                        buoyageScale,
                                        tileSideX, tileSideY,
                                        DEV, BUOYAGE_PATH, topMarks, marsys, "BOYLAT", null);
                        load(buoyageStlShapefileLoader, "BUOYAGE", "BOYLAT", "/");
                        resultLat = buoyageStlShapefileLoader.compute();
                        if (resultLat != null) {
                            write(outPathname, resultLat);
                        }
                        break;
                    case "BCNSPP.shp":
                        buoyageStlShapefileLoader
                                = new BUOYAGE_Stl_ShapefileLoader(geometryEnveloppe, polyEnveloppe,
                                        scaleLatFactor, scaleLonFactor,
                                        buoyageScale,
                                        tileSideX, tileSideY,
                                        DEV, BUOYAGE_PATH, topMarks, marsys, "BCNSPP", null);
                        load(buoyageStlShapefileLoader, "BUOYAGE", "BCNSPP", "/");
                        resultLat = buoyageStlShapefileLoader.compute();
                        if (resultLat != null) {
                            write(outPathname, resultLat);
                        }
                        break;
                    case "BOYSPP.shp":
                        buoyageStlShapefileLoader
                                = new BUOYAGE_Stl_ShapefileLoader(geometryEnveloppe, polyEnveloppe,
                                        scaleLatFactor, scaleLonFactor,
                                        buoyageScale,
                                        tileSideX, tileSideY,
                                        DEV, BUOYAGE_PATH, topMarks, marsys, "BOYSPP", null);
                        load(buoyageStlShapefileLoader, "BUOYAGE", "BOYSPP", "/");
                        resultLat = buoyageStlShapefileLoader.compute();
                        if (resultLat != null) {
                            write(outPathname, resultLat);
                        }
                        break;
                    case "BCNISD.shp":
                        buoyageStlShapefileLoader
                                = new BUOYAGE_Stl_ShapefileLoader(geometryEnveloppe, polyEnveloppe,
                                        scaleLatFactor, scaleLonFactor,
                                        buoyageScale,
                                        tileSideX, tileSideY,
                                        DEV, BUOYAGE_PATH, topMarks, marsys, "BCNISD", null);
                        load(buoyageStlShapefileLoader, "BUOYAGE", "BCNISD", "/");
                        resultLat = buoyageStlShapefileLoader.compute();
                        if (resultLat != null) {
                            write(outPathname, resultLat);
                        }
                        break;
                    case "BOYISD.shp":
                        buoyageStlShapefileLoader
                                = new BUOYAGE_Stl_ShapefileLoader(geometryEnveloppe, polyEnveloppe,
                                        scaleLatFactor, scaleLonFactor,
                                        buoyageScale,
                                        tileSideX, tileSideY,
                                        DEV, BUOYAGE_PATH, topMarks, marsys, "BOYISD", null);
                        load(buoyageStlShapefileLoader, "BUOYAGE", "BOYISD", "/");
                        resultLat = buoyageStlShapefileLoader.compute();
                        if (resultLat != null) {
                            write(outPathname, resultLat);
                        }
                        break;
                    case "MORFAC.shp":
                        buoyageStlShapefileLoader
                                = new BUOYAGE_Stl_ShapefileLoader(geometryEnveloppe, polyEnveloppe,
                                        scaleLatFactor, scaleLonFactor,
                                        buoyageScale,
                                        tileSideX, tileSideY,
                                        DEV, BUOYAGE_PATH, topMarks, marsys, "MORFAC", null);
                        load(buoyageStlShapefileLoader, "BUOYAGE", "MORFAC", "/");
                        resultLat = buoyageStlShapefileLoader.compute();
                        if (resultLat != null) {
                            write(outPathname, resultLat);
                        }
                        break;
                    default:
                }
            }
            /*
            for (File f : listOfFiles) {
                String s = f.getName();
                switch (s) {
                    case "RESARE.shp":
                        load(new AREA_ShapefileLoader("RESARE", new Color(197, 69, 195), 0.2, false), "AREA", "RESARE", "/");
                        break;
                    case "UNSARE.shp":
                        load(new UNSARE_ShapefileLoader(), "AREA", "UNSARE", "/");
                        break;
                    default:
                }
            }
         
            try {
                for (File f : listOfFiles) {
                    String s = f.getName();
                    switch (s) {
                        case "ACHARE.shp":
                            load(new ACHARE_ShapefileLoader("ACHARE", new Color(2, 200, 184), 0.4, true), "AREA", "ACHARE", "/");
                            break;
                        case "BCNSAW.shp":
                            load(new BUOYAGE_ShapefileLoader(DEV, BUOYAGE_PATH, topMarks, marsys, "BCNSAW", s57Controllers), "BUOYAGE", "BCNSAW", "/");
                            break;
                        case "BRIDGE.shp":
                            load(new BRIDGE_ShapefileLoader(), "BUILDING", "BRIDGE", "/");
                            break;
                        case "BOYSAW.shp":
                            load(new BUOYAGE_ShapefileLoader(DEV, BUOYAGE_PATH, topMarks, marsys, "BOYSAW", s57Controllers), "BUOYAGE", "BOYSAW", "/");
                            break;
                        case "COALNE.shp":
                            load(new COALNE_ShapefileLoader(), "COALNE", "/");
                            break;
                        case "CBLSUB.shp":
                            load(new CBLSUB_ShapefileLoader(), "CBLSUB", "CBLSUB", "/");
                            break;
                        case "DAYMAR.shp":
                            load(new DAYMAR_ShapefileLoader(marsys), "BUOYAGE", "DAYMAR", "/");
                            break;
                        case "DEPCNT.shp":
                            load(new DEPCNT_ShapefileLoader(), "BATHYMETRY", "DEPCNT", "/");
                            break;
                        case "DOCARE.shp":
                            load(new DOCARE_ShapefileLoader(), "AREA", "DOCARE", "/");
                            break;
                        case "DGRARE.shp":
                            load(new AREA_ShapefileLoader("DGRARE", new Color(7, 149, 24), 0.0, false), "AREA", "DGRARE", "/");
                            break;
                        case "FAIRWY.shp":
                            load(new AREA_ShapefileLoader("FAIRWY", new Color(7, 141, 29), 0.0, false), "NAVIGATION", "FAIRWY", "/");
                            break;
                        case "LAKARE.shp":
                            load(new LAKE_ShapefileLoader("LAKARE", new Color(9, 13, 33), 1.0), "EARTH", "LAKARE", "/");
                            break;
                        case "LNDMRK.shp":
                            load(new LANDMARK_ShapefileLoader(DEV, marsys, "LNDMRK"), "BUILDING", "LNDMRK", "/");
                            break;
                        case "MIPARE.shp":
                            load(new AREA_ShapefileLoader("MIPARE", new Color(1, 5, 105), 0.0, false), "AREA", "MIPARE", "/");
                            break;
                        case "M_SREL.shp":
                            load(new AREA_ShapefileLoader("M_SREL", new Color(0, 255, 0), 0.0, false), "AREA", "M_SREL", "/");
                            break;
                        case "NAVLNE.shp":
                            load(new NAVLNE_ShapefileLoader(), "NAVIGATION", "NAVLNE", "/");
                            break;
                        case "OBSTRN.shp":
                            load(new OBSTRN_CNT_ShapefileLoader(), "DANGERS", "OBSTRN", "/");
                            load(new OBSTRN_ShapefileLoader(), "DANGERS", "OBSTRN", "/");
                            break;
                        case "SEAARE.shp":
                            load(new AREA_ShapefileLoader("SEAARE", new Color(0, 246, 232), 0.0, false), "AREA", "SEAARE", "/");
                            break;
                        case "SLCONS.shp":
                            load(new SLCONS_ShapefileLoader(), "HARBOUR", "SLCONS", "/");
                            break;
                        case "SOUNDG.shp":
                            load(new SOUNDG_ShapefileLoader(), "BATHYMETRY", "SOUNDG", "/soundg/");
                            break;
                        case "TSSBND.shp":
                            load(new TSSBND_ShapefileLoader(), "NAVIGATION", "TSSBND", "/");
                            break;
                        case "UWTROC.shp":
                            load(new UWTROC_ShapefileLoader(), "DANGERS", "UWTROC", "/");
                            break;
                        case "WRECKS.shp":
                            load(new WRECKS_CNT_ShapefileLoader(), "DANGERS", "WRECKS", "/");
                            load(new WRECKS_ShapefileLoader(), "DANGERS", "WRECKS", "/");
                            break;
                        case "LIGHTS.shp":
                            loadLights();
                            break;
                        default:
                    }

                    if (s.contains(".shp")) {
                        geos.put(s.replace(".shp", ""), new HashMap<>());
                    }
                }

            } catch (Exception e) {
                System.out.println("eee : " + e);
            }
             */
        }
    }

}
