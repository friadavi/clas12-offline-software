package org.jlab.service.dc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.clas.swimtools.MagFieldsEngine;
import org.jlab.clas.swimtools.Swim;
import org.jlab.clas.swimtools.Swimmer;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.io.hipo.HipoDataSync;
import org.jlab.jnp.hipo.schema.SchemaFactory;
import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.track.TrackFinderRB;

/**
 * This class is intended to be used to reconstruct the interaction vertex from
 * previously calculated HB and CVT data
 * 
 * @author friant
 */
public class DCRBEngine extends DCEngine {
    //Global Variables
    static double solVal;
    static double torVal;
    
    /**
     * Constructor
     */
    public DCRBEngine() {
        super("DCRB");
    }
    
    /**
     * Main method
     * 
     * @param args 
     */
    public static void main(String[] args){
        //Ensure that RasterBased Data Banks are defined in the program
        SchemaFactory fact = new SchemaFactory();
        fact.initFromDirectory("CLAS12DIR", "etc/bankdefs/hipo");
        
        //Hipo Reader and Writer
        HipoDataSource reader = new HipoDataSource();
        HipoDataSync writer = new HipoDataSync(fact);
        
        //Files
        File inputFile;
        File outputFile;
        
        //Command Line Options
        boolean help = false;
        String input = "";
        String output = "";
        String solenoid = "";
        String toroid = "";
        
        //Parse
        for(int i = 0; i < args.length; i++){
            switch(args[i]){
                case "-i":
                    if(i + 1 < args.length){
                        input = args[i + 1];
                    }
                    break;
                case "-o":
                    if(i + 1 < args.length){
                        output = args[i + 1];
                    }
                    break;
                case "-s":
                    if(i + 1 < args.length){
                        solenoid = args[i + 1];
                    }
                    break;
                case "-t":
                    if(i + 1 < args.length){
                        toroid = args[i + 1];
                    }
                    break;
                case "-h":
                    help = true;
                    break;
            }
        }
        
        //Attempt to use command line parameters to set values
        if(input.isEmpty()){
            help = true;
        }
        else{
            inputFile = new File(input);
            if(inputFile.exists() && !inputFile.isDirectory()){
                reader.open(inputFile);
            }
            else{
                System.out.println("Input File Not Found");
                return;
            }
        }
        if(output.isEmpty()){
            help = true;
        }
        else{
            outputFile = new File(output);
            if(outputFile.exists() && !outputFile.isDirectory()){
                outputFile.delete();
            }
            try{
                outputFile.createNewFile();
                writer.open(outputFile.getAbsolutePath());
            }
            catch(Exception e){
                System.out.println("Could Not Create Output File");
                return;
            }
        }
        if(solenoid.isEmpty()){
            solVal = 1.0;
        }
        else{
            solVal = Double.parseDouble(solenoid);
        }
        if(toroid.isEmpty()){
            torVal = -1.0;
        }
        else{
            torVal = Double.parseDouble(solenoid);
        }
        
        //Print help message and exit
        if(help){
            printHelp();
            return;
        }
        
        //Init Engines
        MagFieldsEngine magField = new MagFieldsEngine();
        magField.init();
        DCRBEngine engine = new DCRBEngine();
        engine.init();
        
        //Apply magnetic field scaling
        Swimmer.setMagneticFieldsScales(solVal, torVal, 0.0);
        
        //Process data events
        int count = 0;
        while(reader.hasEvent()){
            DataEvent event = reader.getNextEvent();
            if(!event.hasBank("RasterBasedTrkg::RBHits")){
                ((HipoDataEvent)event).initDictionary(fact);
            }
            engine.processDataEvent(event);
            writer.writeEvent(event);
            System.out.println("EVENT " + count + " PROCESSED");
            count++;
        }
        reader.close();
        writer.close();
        
        //magFieldData();
    }
    
    /**
     * Print this help message to the terminal
     */
    private static void printHelp(){
        System.out.println(
              "FriTracking Command Line Options:\r\n"
            + " - Required:\r\n"
            + "            -i      Input Hipo File\r\n"
            + "            -o      Output Hipo File\r\n"
            + " - Optional:\r\n"
          //+ "            -e      Colon Delimited List of Engines To Use\r\n"
          //+ "                      e.g. CVT:DCHB:DCRB\r\n"
            + "            -s      Solonoid Field Scale Factor\r\n"
            + "                      Default Value =  1.0\r\n"
            + "            -t      Toroid Field Scale Factor\r\n"
            + "                      Default Value = -1.0\r\n"
            + "            -h      Print This Message\r\n");
    }
    
    /**
     * Initialize the engine
     * 
     * @return 
     */
    @Override
    public boolean init() {
        // Load cuts
        Constants.Load();
        super.setStartTimeOption();
        super.LoadTables();
        return true;
    }
    
    /**
     * Generic process data event function
     * 
     * @param event
     * @return 
     */
    @Override
    public boolean processDataEvent(DataEvent event){
        boolean hasMCData = event.hasBank("MC::Particle");
        boolean hasCVTData = event.hasBank("CVTRec::Tracks");
        boolean hasHBData = event.hasBank("HitBasedTrkg::HBTracks");
        
        if(!hasMCData){
            return true;
        }
        
        if(hasCVTData && hasHBData){
            //processDataEventBoth(event);//Just run in HB mode for now
            processDataEventHB(event);
        }
        else if(hasCVTData){
            processDataEventCVT(event);
        }
        else if(hasHBData){
            processDataEventHB(event);
        }
        
        return true;
    }
    
    /**
     * Process data event for when HB and CVT data is available
     * 
     * @param event
     * @return 
     */
    public boolean processDataEventBoth(DataEvent event){
        System.out.println("Process for Both CVT and HB Not Yet Implemented");
        return true;
    }
    
    /**
     * Process data event for when only CVT data is available
     * 
     * @param event
     * @return 
     */
    public boolean processDataEventCVT(DataEvent event){
        System.out.println("Process for CVT Not Yet Implemented");
        return true;
    }
    
    /**
     * Process data event for when only HB data is available
     * 
     * @param event
     * @return 
     */
    public boolean processDataEventHB(DataEvent event) {
        //Pull info out of HB Banks
        event.appendBank(copyBank(event, "HitBasedTrkg::HBHits", "RasterBasedTrkg::RBHits"));
        event.appendBank(copyBank(event, "HitBasedTrkg::HBClusters", "RasterBasedTrkg::RBClusters"));
        event.appendBank(copyBank(event, "HitBasedTrkg::HBSegments", "RasterBasedTrkg::RBSegments"));
        event.appendBank(copyBank(event, "HitBasedTrkg::HBCrosses", "RasterBasedTrkg::RBCrosses"));
        
        //Raster variables
        double rasterX = event.getBank("MC::Particle").getFloat("vx", 0);//x position
        double rasterY = event.getBank("MC::Particle").getFloat("vy", 0);//y position
        double rasterUX = Math.sqrt(rasterX);//uncertainty in x position
        double rasterUY = Math.sqrt(rasterY);//uncertainty in y position
        
        //Find RB Tracks
        TrackFinderRB trkFinder = new TrackFinderRB();
        DataBank rbBank = trkFinder.getTracksHB(event, rasterX, rasterUX, rasterY, rasterUY);
        
        //Put Tracks in Event
        event.appendBank(rbBank);
        
        return true;
    }
    
    /**
     * Provides a copy mechanism from a pre-existing dataBank to a new one.
     * Note that this does not natively append the new bank to the old event.
     * 
     * @param event
     * @param oldBank
     * @param newBank 
     */
    private DataBank copyBank(DataEvent event, String oldBankName, String newBankName){
        DataBank oldBank = event.getBank(oldBankName);
        if(oldBank == null){
            return null;
        }
        
        DataBank newBank = event.createBank(newBankName, oldBank.rows());
        for(int i = 0; i < oldBank.rows(); i++){
            for(int j = 0; j < oldBank.columns(); j++){
                switch(oldBank.getDescriptor().getProperty("type", oldBank.getColumnList()[j])){
                    case 1://Byte
                        newBank.setByte(oldBank.getColumnList()[j], i, (byte)(oldBank.getByte(oldBank.getColumnList()[j], i)));
                        break;
                    case 2://Short
                        newBank.setShort(oldBank.getColumnList()[j], i, (short)(oldBank.getShort(oldBank.getColumnList()[j], i)));
                        break;
                    case 3://Int
                        newBank.setInt(oldBank.getColumnList()[j], i, (int)(oldBank.getInt(oldBank.getColumnList()[j], i)));
                        break;
                    case 4://Unused
                        break;
                    case 5://Float
                        newBank.setFloat(oldBank.getColumnList()[j], i, (float)(oldBank.getFloat(oldBank.getColumnList()[j], i)));
                        break;
                    case 6://Double
                        newBank.setDouble(oldBank.getColumnList()[j], i, (double)(oldBank.getDouble(oldBank.getColumnList()[j], i)));
                        break;
                }
            }
        }
        return newBank;
    }
    
    /**
     * Utility method to print out a .csv file for the magnetic field strength.
     */
    private static void magFieldData(){
        Swim dcSwim = new Swim();
        double y = 0.0;
        try {
            PrintWriter pw = new PrintWriter("magfield_y_" + y + ".csv");
            for(double x = 0.0; x < 400.0; x = x + 10.0){
                for(double z = -400.0; z < 400.0; z = z + 10.0){
                    float[] bField = new float[3];
                    //dcSwim.Bfield(1, x, y, z, bField);
                    dcSwim.BfieldLab(x, y, z, bField);
                    pw.print(Double.toString(x) + ",");
                    pw.print(Double.toString(y) + ",");
                    pw.print(Double.toString(z) + ",");
                    pw.println(Float.toString((float)Math.sqrt(bField[0]*bField[0]+bField[1]*bField[1]+bField[2]*bField[2])) + ",");
                }
            }
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DCRBEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}