package org.jlab.service.dc;

import java.io.File;
import org.jlab.clas.swimtools.MagFieldsEngine;
import org.jlab.clas.swimtools.Swim;
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
        
        //Parse
        for(int i = 0; i < args.length; i++){
            switch(args[i]){
                case "-i":
                    if(i + 1 < args.length){
                        if(args[i+ 1].charAt(0) != '-'){
                            input = args[i + 1];
                        }
                    }
                    break;
                case "-o":
                    if(i + 1 < args.length){
                        if(args[i + 1].charAt(0) != '-'){
                            output = args[i + 1];
                        }
                    }
                    break;
                case "-h":
                    help = true;
                    break;
            }
        }
        
        //Figure out whether or not to run
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
        
        //Process Data Events
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
    }
    
    /**
     * Print this help message to the terminal
     */
    private static void printHelp(){
        System.out.println(
              "FriTracking Command Line Options:\r\n"
            + " - Required:\r\n"
            + "            -i      Input File\r\n"
            + "            -o      Output File\r\n"
            + " - Optional\r\n"
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
        
        //Set up swimmer
        Swim dcSwim = new Swim();
        
        //Find RB Tracks
        TrackFinderRB trkFinder = new TrackFinderRB();
        DataBank rbBank = trkFinder.getTracksHB(event, rasterX, rasterUX, rasterY, rasterUY, dcSwim);
        
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
}