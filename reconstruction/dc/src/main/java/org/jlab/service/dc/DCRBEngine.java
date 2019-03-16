package org.jlab.service.dc;

import java.io.File;
import java.util.Arrays;
import org.jlab.clas.swimtools.MagFieldsEngine;
import org.jlab.clas.swimtools.Swim;
import org.jlab.clas.swimtools.Swimmer;
import org.jlab.geom.prim.Point3D;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.io.hipo.HipoDataSync;
import org.jlab.jnp.hipo.schema.SchemaFactory;
import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.cross.Cross;

/**
 * This class is intended to be used to reconstruct the interaction vertex from
 * previously calculated HB and CVT data
 * 
 * @author friant
 */
public class DCRBEngine extends DCEngine {
    //Global Variables
    static double solVal = 1.0;
    static double torVal = -1.0;
    static int iterations = 10;
    static int samples = 10;
    static double zMinGlobal = -10.0;
    static double zMaxGlobal = 10.0;
    
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
        if(!solenoid.isEmpty()){
            solVal = Double.parseDouble(solenoid);
        }
        if(!toroid.isEmpty()){
            torVal = Double.parseDouble(toroid);
        }
        
        if(solVal == Double.NaN || torVal == Double.NaN){
            System.out.println("Invalid input for either toroid or solenoid value.");
            help = true;
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
            //processDataEventBoth(event);//Just use HB for now
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
        //Pull info out of TB/HB Banks
        String sourceTracks;
        String sourceCrosses;
        if(event.hasBank("TimeBasedTrkg::TBTracks")){
            event.appendBank(copyBank(event, "TimeBasedTrkg::TBHits", "RasterBasedTrkg::RBHits"));
            event.appendBank(copyBank(event, "TimeBasedTrkg::TBClusters", "RasterBasedTrkg::RBClusters"));
            event.appendBank(copyBank(event, "TimeBasedTrkg::TBSegments", "RasterBasedTrkg::RBSegments"));
            event.appendBank(copyBank(event, "TimeBasedTrkg::TBCrosses", "RasterBasedTrkg::RBCrosses"));
            sourceCrosses = "TimeBasedTrkg::TBCrosses";
            sourceTracks = "TimeBasedTrkg::TBTracks";
        }
        else{
            event.appendBank(copyBank(event, "HitBasedTrkg::HBHits", "RasterBasedTrkg::RBHits"));
            event.appendBank(copyBank(event, "HitBasedTrkg::HBClusters", "RasterBasedTrkg::RBClusters"));
            event.appendBank(copyBank(event, "HitBasedTrkg::HBSegments", "RasterBasedTrkg::RBSegments"));
            event.appendBank(copyBank(event, "HitBasedTrkg::HBCrosses", "RasterBasedTrkg::RBCrosses"));
            sourceCrosses = "HitBasedTrkg::HBCrosses";
            sourceTracks = "HitBasedTrkg::HBTracks";
        }
        
        //Create the RBTracks Bank
        DataBank rbBank = copyBank(event, sourceTracks, "RasterBasedTrkg::RBTracks");
        
        //Raster variables
        double rasterX = event.getBank("MC::Particle").getFloat("vx", 0);
        double rasterY = event.getBank("MC::Particle").getFloat("vy", 0);
        
        //Create Swimmer to use and reuse
        Swim swim = new Swim();
        
        //Create a cross to use the coordinate system tranformations
        Cross cross = new Cross(1, 1, 1);
        
        //Calculate the interaction vertex for each for each track in the event
        for(int i = 0; i < rbBank.rows(); i++){
            //Need to get momentum and cross position/unit-momentum
            int crossIndex = -1;
            float p = -1.0f;
            
            //Get the associated cross at the entrance to the drift chamber
            short crossID = event.getBank(sourceTracks).getShort("Cross1_ID", i);
            for(int j = 0; j < event.getBank(sourceCrosses).rows(); j++){
                if(event.getBank(sourceCrosses).getShort("id", j) == crossID){
                    crossIndex = j;
                    break;
                }
            }
            
            //Get the total momentum
            float px = event.getBank(sourceTracks).getFloat("p0_x", i);
            float py = event.getBank(sourceTracks).getFloat("p0_y", i);
            float pz = event.getBank(sourceTracks).getFloat("p0_z", i);
            p = (float)Math.sqrt(px * px + py * py + pz * pz);
            
            
            //Skip event if no corresponding cross id was found
            if(crossIndex == -1){
                continue;
            }
            
            //Get cross info
            cross.set_Sector(event.getBank(sourceCrosses).getByte("sector", crossIndex));
            Point3D crossPos = new Point3D(event.getBank(sourceCrosses).getFloat("x", crossIndex),
                                           event.getBank(sourceCrosses).getFloat("y", crossIndex),
                                           event.getBank(sourceCrosses).getFloat("z", crossIndex));
            Point3D crossMom = new Point3D(event.getBank(sourceCrosses).getFloat("ux", crossIndex) * p,
                                           event.getBank(sourceCrosses).getFloat("uy", crossIndex) * p,
                                           event.getBank(sourceCrosses).getFloat("uz", crossIndex) * p);
            
            //Transform cross info from tilted sector coord system to lab system
            crossPos = cross.getCoordsInLab(crossPos.x(), crossPos.y(), crossPos.z());
            crossMom = cross.getCoordsInLab(crossMom.x(), crossMom.y(), crossMom.z());
            
            //Calculate doca info
            swim.SetSwimParameters(crossPos.x(),
                                   crossPos.y(),
                                   crossPos.z(),
                                   crossMom.x(),
                                   crossMom.y(),
                                   crossMom.z(),
                                   event.getBank(sourceTracks).getByte("q", i));
            double[] output = new double[8];
            double doca = findInteractionVertex(iterations, samples, zMinGlobal, zMaxGlobal, rasterX, rasterY, swim, output);
            
            //Make sure that the momentum is pointing in the right direction
            if(output[5] < 0.0){
                output[3] = output[3] * -1.0;
                output[4] = output[4] * -1.0;
                output[5] = output[5] * -1.0;
            }
            
            //Set Values
            rbBank.setFloat("Vtx0_x", i, (float)output[0]);
            rbBank.setFloat("Vtx0_y", i, (float)output[1]);
            rbBank.setFloat("Vtx0_z", i, (float)output[2]);
            rbBank.setFloat("p0_x", i, (float)output[3]);
            rbBank.setFloat("p0_y", i, (float)output[4]);
            rbBank.setFloat("p0_z", i, (float)output[5]);
            rbBank.setFloat("doca", i, (float)doca);
        }
        System.out.println("");
        //event.getBank("MC::Particle").show();
        //rbBank.show();
        
        //Put Tracks in Event
        event.appendBank(rbBank);
        
        return true;
    }
    
    /**
     * This method uses a swimmer to calculate the beam's doca position relative to the raster beam axis.
     * 
     * @param iterations    The maximum number of recursive steps
     * @param samples       The number of sample points to take between zMin and zMax for each step. DO NOT USE LESS THAN 5
     * @param zMin          The lower bound of the area of interest. Should be below the lower bound of the target
     * @param zMax          The upper bound of the area of interest. Should be above the upper bound of the target
     * @param rasterX       The rasterized beam x position
     * @param rasterY       The rasterized beam y postiion
     * @param swim          A Swim class which has been set to the position and momentum of the first cross
     * @param out           A pointer to a double array which will be filled by this method. Will be set to the Swim output or NaN
     * @return the doca to the rasterized beam coords; -1.0 if no mimimum was found
     */
    private double findInteractionVertex(int iterations, int samples, double zMin, double zMax, double rasterX, double rasterY, Swim swim, double[] out){
        //Define useful arrays
        double[][] swimOutput = new double[samples][];
        double[] doca = new double[samples];
        int docaLength = 0;
        int[] localMin = new int[samples - 3];
        int localMinLength = 0;
        
        //Get swim outputs
        for(int i = 0; i < samples; i++){
            swimOutput[i] = swim.SwimToPlaneLab(zMin + i * (zMax - zMin) / samples);
        }
        
        //Calculate the doca for each sample point
        for(int i = 0; i < samples; i++){
            if(swimOutput[i] == null){
                continue;
            }
            doca[docaLength] = Math.sqrt(Math.pow(rasterX - swimOutput[i][0], 2.0) + Math.pow(rasterY - swimOutput[i][1], 2.0));
            docaLength++;
        }
        
        //Handle variable docaLengths
        switch(docaLength){
            case 0:
                //Fail gracefully
                localMinLength = 0;
                break;
            case 1:
                //Set only point as local minimum
                localMinLength = 1;
                localMin[0] = 0;
                break;
            default:
                //Find local minima
                if(doca[0] < doca[1]){
                    localMin[localMinLength] = 0;
                    localMinLength++;
                }   for(int i = 1; i < docaLength - 1; i++){
                    if(doca[i] < doca[i - 1] && doca[i] < doca[i + 1]){
                        localMin[localMinLength] = i;
                        localMinLength++;
                    }
                }   if(doca[docaLength - 1] < doca[docaLength - 2]){
                    localMin[localMinLength] = docaLength - 1;
                    localMinLength++;
                }   break;
        }
        
        //Exit?
        if(iterations == 1){
            int index = -1;
            double smallest = Double.MAX_VALUE;
            
            //Find a minimum?
            if(localMinLength == 0){
                Arrays.fill(out, Double.NaN);
                return -1.0;
            }
            
            //Find the smallest doca
            for(int i = 0; i < localMinLength; i++){
                if(doca[localMin[i]] < smallest){
                    index = i;
                }
            }
            copyArrayTo(swimOutput[localMin[index]], out);
            return doca[localMin[index]];
        }
        
        //Recursively call this method on each of the local minima
        double[][] minOut = new double[localMinLength][8];
        double[] minDoca = new double[localMinLength];
        for(int i = 0; i < localMinLength; i++){
            minDoca[i] = findInteractionVertex(iterations - 1, samples, swimOutput[localMin[i]][2] - (zMax - zMin) / samples, swimOutput[localMin[i]][2] + (zMax - zMin) / samples, rasterX, rasterY, swim, minOut[i]);
        }
        
        //Find the smallest doca
        int index = -1;
        double smallest = Double.MAX_VALUE;
        for(int i = 0; i < localMinLength; i++){
            if(minOut[i] != null && minDoca[i] < smallest)
            {
                index = i;
            }
        }
        
        //Exit
        if(index == -1){
            Arrays.fill(out, Double.NaN);
            return -1.0;
        }
        copyArrayTo(minOut[index], out);
        return minDoca[index];
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
     * Utility method to copy the values of one array into the other
     * 
     * @param source
     * @param destination 
     */
    private void copyArrayTo(double[] source, double[] destination){
        for(int i = 0; i < source.length; i++){
            destination[i] = source[i];
        }
    }
}