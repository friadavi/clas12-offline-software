package org.jlab.service.dc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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

/**
 * This class is intended to be used to reconstruct the interaction vertex from
 * previously calculated HB/TB and CVT data
 * 
 * @author friant
 */
public class DCRBEngine extends DCEngine {
    //Global Variables
    static String engine            =  "";
    static float  solVal            =  1.0f;
    static float  torVal            = -1.0f;
    static float  zMinGlobal        = -10.0f;
    static float  zMaxGlobal        =  10.0f;
    static float  percentGridSearch =  0.10f;
    static int    samplesGridSearch =  5;
    static int    iterationsVertex  =  2;
    static int    samplesVertex     =  5;
    static int    maxThreads        =  1;
    
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
        
        //Required
        String input = "";
        String output = "";
        
        //Optional
        String gridSearchSamples = "";
        String gridSearchPercent = "";
        String solenoid = "";
        String toroid = "";
        String vertexSamples = "";
        String vertexIterations = "";
        String threads = "";
        String lowerBound = "";
        String upperBound = "";
        
        //Parse
        for(int i = 0; i < args.length; i++){
            switch(args[i]){
                case "-i":
                    if(i + 1 < args.length){
                        input = args[i + 1];
                    }
                    else{
                        System.out.println("Missing Parameter For Option -i");
                        return;
                    }
                    break;
                case "-o":
                    if(i + 1 < args.length){
                        output = args[i + 1];
                    }
                    else{
                        System.out.println("Missing Parameter For Option -o");
                        return;
                    }
                    break;
                case "-e":
                    if(i + 1 < args.length){
                        engine = args[i + 1];
                    }
                    else{
                        System.out.println("Missing Parameter For Option -e");
                        return;
                    }
                    break;
                case "-g":
                    if(i + 2 < args.length){
                        gridSearchSamples = args[i + 1];
                        gridSearchPercent = args[i + 2];
                    }
                    else{
                        System.out.println("Missing Parameter For Option -g");
                        return;
                    }
                    break;
                case "-h":
                    help = true;
                    break;
                case "-s":
                    if(i + 1 < args.length){
                        solenoid = args[i + 1];
                    }
                    else{
                        System.out.println("Missing Parameter For Option -s");
                        return;
                    }
                    break;
                case "-t":
                    if(i + 1 < args.length){
                        toroid = args[i + 1];
                    }
                    else{
                        System.out.println("Missing Parameter For Option -t");
                        return;
                    }
                    break;
                case "-v":
                    if(i + 2 < args.length){
                        vertexSamples = args[i + 1];
                        vertexIterations = args[i + 2];
                    }
                    else{
                        System.out.println("Missing Parameter For Option -v");
                        return;
                    }
                    break;
                case "-x":
                    if(i + 1 < args.length){
                        threads = args[i + 1];
                    }
                    else{
                        System.out.println("Missing Parameter For Option -x");
                        return;
                    }
                    break;
                case "-z":
                    if(i + 2 < args.length){
                        lowerBound = args[i + 1];
                        upperBound = args[i + 2];
                    }
                    else{
                        System.out.println("Missing Parameter For Option -z");
                        return;
                    }
                    break;
            }
        }
        
        //Attempt to use command line parameters to set values
        ////Input File
        if(input.isEmpty()){
            System.out.println("Input File Required");
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
        ////output File
        if(output.isEmpty()){
            System.out.println("Output File Required");
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
            catch(IOException e){
                System.out.println("Could Not Create Output File");
                return;
            }
        }
        ////Engine
        if(!engine.isEmpty()){
            if(!(engine.equals("DCHB") || engine.equals("DCTB"))){
                System.out.println("Invalid Engine Specifified");
                help = true;
            }
        }
        ////Grid Search Parameters
        if(!(gridSearchSamples.isEmpty() || gridSearchPercent.isEmpty())){
            try{
                samplesGridSearch = Integer.parseInt(gridSearchSamples);
                percentGridSearch = Float.parseFloat(gridSearchPercent);
            }
            catch(NumberFormatException e){
                System.out.println("Invalid Number Format For Grid Search Parameters");
                help = true;
            }
        }
        ////Solenoid Scale
        if(!solenoid.isEmpty()){
            try{
                solVal = Float.parseFloat(solenoid);
            }
            catch(NumberFormatException e){
                System.out.println("Invalid Number Format For Solenoid");
                help = true;
            }
        }
        ////Toroid Scale
        if(!toroid.isEmpty()){
            try{
                torVal = Float.parseFloat(toroid);
            }
            catch(NumberFormatException e){
                System.out.println("Invalid Number Format For Toroid");
                help = true;
            }
        }
        ////Vertex Search Parameters
        if(!(vertexSamples.isEmpty() || vertexIterations.isEmpty())){
            try{
                samplesVertex = Integer.parseInt(vertexSamples);
                iterationsVertex = Integer.parseInt(vertexIterations);
            }
            catch(NumberFormatException e){
                System.out.println("Invalid Number Format For Vertex Search Parameters");
                help = true;
            }
        }
        ////Threads
        if(!threads.isEmpty()){
            try{
                maxThreads = Integer.parseInt(threads);
            }
            catch(NumberFormatException e){
                System.out.println("Invalid Number Format For Number Of Threads To Use");
                help = true;
            }
        }
        ////Target Bounds
        if(!(lowerBound.isEmpty() || upperBound.isEmpty())){
            try{
                zMinGlobal = Float.parseFloat(lowerBound);
                zMaxGlobal = Float.parseFloat(upperBound);
            }
            catch(NumberFormatException e){
                System.out.println("Invalid Number Format For Target Bounds");
                help = true;
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
        DCRBEngine thisEngine = new DCRBEngine();
        thisEngine.init();
        
        //Apply magnetic field scaling
        Swimmer.setMagneticFieldsScales(solVal, torVal, 0.0);
        
        //Process data events
        int count = 0;
        while(reader.hasEvent()){
            DataEvent event = reader.getNextEvent();
            if(!event.hasBank("RasterBasedTrkg::RBHits")){
                ((HipoDataEvent)event).initDictionary(fact);
            }
            thisEngine.processDataEvent(event);
            writer.writeEvent(event);
            System.out.println("EVENT " + count + " PROCESSED\r\n");
            count++;
        }
        
        reader.close();
        writer.close();
    }
    
    /**
     * Print help message to the terminal
     */
    private static void printHelp(){
        System.out.println(
            "FriTracking Command Line Options:                                  \r\n"
          + " -Required:                                                        \r\n"
          + "     -i    Input Hipo File                                         \r\n"
          + "           Requires 1 Parameter:                                   \r\n"
          + "               1 (String): Path to desired input file.             \r\n"
          + "           Ex: -i /path/to/my/input/file.hipo                      \r\n"
          + "                                                                   \r\n"
          + "     -o    Output Hipo File                                        \r\n"
          + "           Requires 1 Parameter:                                   \r\n"
          + "               1 (String): Path to the desried ouput file. Will    \r\n"
          + "                           overwrite file if pre-existing.         \r\n"
          + "           Ex: -o /path/to/my/output/file.hipo                     \r\n"
          + "                                                                   \r\n"
          + " -Optional:                                                        \r\n"
          + "     -e    Engine To Source Data From                              \r\n"
          + "           Requires 1 Parameter:                                   \r\n"
          + "               1 (String): Either \"DCHB\" or \"DCTB\"             \r\n"
          + "           Default Behavior:                                       \r\n"
          + "               Use DCTB if available and DCHB if not.              \r\n"
          + "           Ex: -e DCHB                                             \r\n"
          + "                                                                   \r\n"
          + "     -g    Grid Search Parameters                                  \r\n"
          + "           Requires 2 Parameters:                                  \r\n"
          + "               1 (Int   ): The number of samples to take per       \r\n"
          + "                           momentum dimension around the region-1  \r\n"
          + "                           cross.                                  \r\n"
          + "               2 (Float ): The percent of the nominal value to     \r\n"
          + "                           search within.                          \r\n"
          + "           Default Behavior:                                       \r\n"
          + "               5 Samples within 10% of the nominal value           \r\n"
          + "           Ex: -g 7 0.02                                           \r\n"
          + "                                                                   \r\n" 
          + "     -h    Print This Message                                      \r\n"
          + "                                                                   \r\n"
          + "     -s    Solonoid Field Scale Factor                             \r\n"
          + "           Requires 1 Parameter                                    \r\n"
          + "               1 (Float ): The number by which to scale the        \r\n"
          + "                           solenoid's magnetic field. Note: a value\r\n"
          + "                           of 0.0 will likely cause failure.       \r\n"
          + "           Default Behavior:                                       \r\n"
          + "               Scale by 1.0.                                       \r\n"
          + "           Ex: -s 0.001                                            \r\n"
          + "                                                                   \r\n"
          + "     -t    Toroid Field Scale Factor                               \r\n"
          + "           Requires 1 Parameter                                    \r\n"
          + "               1 (Float ): The number by which to scale the        \r\n"
          + "                           toroid's magnetic field. Note: a value  \r\n"
          + "                           of 0.0 will likely cause failure.       \r\n"
          + "           Default Behavior:                                       \r\n"
          + "               Scale by -1.0.                                      \r\n"
          + "           Ex: -s 0.1                                              \r\n"
          + "                                                                   \r\n"
          + "     -v    Vertex Search Parameters                                \r\n"
          + "           Requires 2 Parameters:                                  \r\n"
          + "               1 (Int   ): The number of samples to take within the\r\n"
          + "                           target range to find the best z value.  \r\n"
          + "               2 (Int   ): The number of recursive iterations to go\r\n"
          + "                           throught about each local minima.       \r\n"
          + "           Default Behavior:                                       \r\n"
          + "               5 samples with 2 iterations.                        \r\n"
          + "           Ex: -v 10 2                                             \r\n"
          + "                                                                   \r\n"
          + "     -x    Threads To Use                                          \r\n"
          + "           Requires 1 Parameter:                                   \r\n"
          + "               1 (Int   ): The maximum number of threads to use    \r\n"
          + "                           while searching for minima.             \r\n"
          + "           Default Behavior:                                       \r\n"
          + "               Only use 1 thread.                                  \r\n"
          + "           Ex: -x 4                                                \r\n"
          + "                                                                   \r\n"
          + "     -z    Target Bounds                                           \r\n"
          + "           Requires 2 Parameters:                                  \r\n"
          + "               1 (Float ): The lower bound in the z-direction in   \r\n"
          + "                           which to search for the interaction     \r\n"
          + "                           vertex. [Unit = cm]                     \r\n"
          + "               2 (Float ): The upper bound in the z-direction in   \r\n"
          + "                           which to search for the interaction     \r\n"
          + "                           vertex. [Unit = cm]                     \r\n"
          + "           Default Behavior:                                       \r\n"
          + "               Lower Bound = -10.0, Upper Bound = 10.0.            \r\n"
          + "           Ex: -z -2.5 5.0                                         \r\n"
          + "                                                                   \r\n");
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
        boolean hasCVTData = event.hasBank("CVTRec::Tracks");
        boolean hasHBData = event.hasBank("HitBasedTrkg::HBTracks");
        
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
        if(engine.isEmpty()){
            if(event.hasBank("TimeBasedTrkg::TBTracks")){
                event.appendBank(copyBank(event, "TimeBasedTrkg::TBHits", "RasterBasedTrkg::RBHits"));
                event.appendBank(copyBank(event, "TimeBasedTrkg::TBClusters", "RasterBasedTrkg::RBClusters"));
                event.appendBank(copyBank(event, "TimeBasedTrkg::TBSegments", "RasterBasedTrkg::RBSegments"));
                event.appendBank(copyBank(event, "TimeBasedTrkg::TBCrosses", "RasterBasedTrkg::RBCrosses"));
                sourceTracks = "TimeBasedTrkg::TBTracks";
                sourceCrosses = "TimeBasedTrkg::TBCrosses";
            }
            else{
                event.appendBank(copyBank(event, "HitBasedTrkg::HBHits", "RasterBasedTrkg::RBHits"));
                event.appendBank(copyBank(event, "HitBasedTrkg::HBClusters", "RasterBasedTrkg::RBClusters"));
                event.appendBank(copyBank(event, "HitBasedTrkg::HBSegments", "RasterBasedTrkg::RBSegments"));
                event.appendBank(copyBank(event, "HitBasedTrkg::HBCrosses", "RasterBasedTrkg::RBCrosses"));
                sourceTracks = "HitBasedTrkg::HBTracks";
                sourceCrosses = "HitBasedTrkg::HBCrosses";
            }
        }
        else if(engine.equals("DCHB")){
            if(event.hasBank("TimeBasedTrkg::TBTracks")){
                event.appendBank(copyBank(event, "HitBasedTrkg::HBHits", "RasterBasedTrkg::RBHits"));
                event.appendBank(copyBank(event, "HitBasedTrkg::HBClusters", "RasterBasedTrkg::RBClusters"));
                event.appendBank(copyBank(event, "HitBasedTrkg::HBSegments", "RasterBasedTrkg::RBSegments"));
                event.appendBank(copyBank(event, "HitBasedTrkg::HBCrosses", "RasterBasedTrkg::RBCrosses"));
                sourceTracks = "HitBasedTrkg::HBTracks";
                sourceCrosses = "HitBasedTrkg::HBCrosses";
            }
            else{
                return false;
            }
        }
        else if(engine.equals("DCTB")){
            if(event.hasBank("TimeBasedTrkg::TBTracks")){
                event.appendBank(copyBank(event, "TimeBasedTrkg::TBHits", "RasterBasedTrkg::RBHits"));
                event.appendBank(copyBank(event, "TimeBasedTrkg::TBClusters", "RasterBasedTrkg::RBClusters"));
                event.appendBank(copyBank(event, "TimeBasedTrkg::TBSegments", "RasterBasedTrkg::RBSegments"));
                event.appendBank(copyBank(event, "TimeBasedTrkg::TBCrosses", "RasterBasedTrkg::RBCrosses"));
                sourceTracks = "TimeBasedTrkg::TBTracks";
                sourceCrosses = "TimeBasedTrkg::TBCrosses";
            }
            else{
                return false;
            }
        }
        else{
            return false;
        }
        
        //Create the RBTracks Bank
        DataBank rbBank = copyBank(event, sourceTracks, "RasterBasedTrkg::RBTracks");
        
        //Raster variables
        float rasterX = 0.0f;
        float rasterY = 0.0f;
        float rasterUX = 0.05f;//0.5mm in either direction
        float rasterUY = 0.05f;//0.5mm in either direction
        if(event.hasBank("MC::Particle")){
            rasterX = event.getBank("MC::Particle").getFloat("vx", 0);
            rasterY = event.getBank("MC::Particle").getFloat("vy", 0);
        }
        float[] rasterInfo = new float[]{rasterX, rasterUX, rasterY, rasterUY};
        
        //Calculate the interaction vertex for each for each track in the event
        for(int i = 0; i < rbBank.rows(); i++){
            
//            //Get track info
//            float p = (float)Math.sqrt(Math.pow(event.getBank(sourceTracks).getFloat("t1_px", i), 2.0) +
//                                       Math.pow(event.getBank(sourceTracks).getFloat("t1_py", i), 2.0) +
//                                       Math.pow(event.getBank(sourceTracks).getFloat("t1_pz", i), 2.0));
//            if(p > 12.0f){
//                p = 12.0f;
//            }
            
            float[] trackInfo = new float[7];
            trackInfo[0] = event.getBank(sourceTracks).getFloat("t1_x" , i);
            trackInfo[1] = event.getBank(sourceTracks).getFloat("t1_y" , i);
            trackInfo[2] = event.getBank(sourceTracks).getFloat("t1_z" , i);
            trackInfo[3] = event.getBank(sourceTracks).getFloat("t1_px", i);
            trackInfo[4] = event.getBank(sourceTracks).getFloat("t1_py", i);
            trackInfo[5] = event.getBank(sourceTracks).getFloat("t1_pz", i);
            trackInfo[6] = (float)(event.getBank(sourceTracks).getByte("q", i));
            
            //Calculate
            float[] output = new float[8];
            Arrays.fill(output, Float.NaN);
            float doca = -1.0f;
            try {
                doca = interactionVertexGridSearch(samplesGridSearch, rasterInfo, trackInfo, percentGridSearch, output);
            } catch (InterruptedException | ExecutionException exception) {
                Logger.getLogger(DCRBEngine.class.getName()).log(Level.SEVERE, null, exception);
                System.out.println(exception);
            }
            
            //Make sure that the momentum is pointing in the right direction
            if(output[5] < 0.0){
                output[3] = output[3] * -1.0f;
                output[4] = output[4] * -1.0f;
                output[5] = output[5] * -1.0f;
            }
            
            //System.out.println("Doca: " + doca);
            //System.out.println(Arrays.toString(output));
            
            //Set Values
            rbBank.setFloat("Vtx0_x", i, output[0]);
            rbBank.setFloat("Vtx0_y", i, output[1]);
            rbBank.setFloat("Vtx0_z", i, output[2]);
            rbBank.setFloat(  "p0_x", i, output[3]);
            rbBank.setFloat(  "p0_y", i, output[4]);
            rbBank.setFloat(  "p0_z", i, output[5]);
            rbBank.setFloat(  "doca", i, doca);
        }
        
        //Put Tracks in Event
        event.appendBank(rbBank);
        
        return true;
    }
    
    /**
     * This method performs a three dimensional grid search within the uncertainty
     * in the track's momentum.
     * 
     * @param samples       the number of samples to ake in each dimension
     * @param rasterInfo    the array {x, uncert x, y, uncert y}
     * @param trackInfo     the array{vx, vy, vz, px, py, pz}
     * @param uncertainty   + or - percent uncertainty in track momentum
     * @param out           the array to fill with the ultimate swim output result
     * @return              the doca of the ultimate swim output result
     */
    private float interactionVertexGridSearch(int samples, float[] rasterInfo, float[] trackInfo, float uncertainty, float[] out) throws InterruptedException, ExecutionException{
        //define bounds
        float upperBoundPX = trackInfo[3] * (1.0f + uncertainty);
        float lowerBoundPX = trackInfo[3] * (1.0f - uncertainty);
        float upperBoundPY = trackInfo[4] * (1.0f + uncertainty);
        float lowerBoundPY = trackInfo[4] * (1.0f - uncertainty);
        float upperBoundPZ = trackInfo[5] * (1.0f + uncertainty);
        float lowerBoundPZ = trackInfo[5] * (1.0f - uncertainty);
        
        //choose the max radius of uncertainty to check doca against
        float rasterErr = Math.max(rasterInfo[1], rasterInfo[3]);
        
        //define useful arrays
        float[][][][] output = new float[samples][samples][samples][8];
        ArrayList<ArrayList<ArrayList<Future<Float>>>> doca = new ArrayList();
        ArrayList<int[]>     localMinIndices = new ArrayList<>();
        
        //Define thread service
        ExecutorService ex = Executors.newFixedThreadPool(maxThreads);
        
        //Find the interaction vertices and docas to sift through
        for(int i = 0; i < samples; i++){
            doca.add(new ArrayList<>());
            for(int j = 0; j < samples; j++){
                doca.get(i).add(new ArrayList<>());
                for(int k = 0; k < samples; k++){
                    //Set Swimmer params
                    float[] swimParams = new float[]{trackInfo[0],
                                                     trackInfo[1],
                                                     trackInfo[2],
                                                     lowerBoundPX + i * (upperBoundPX - lowerBoundPX) / (samples - 1),
                                                     lowerBoundPY + j * (upperBoundPY - lowerBoundPY) / (samples - 1),
                                                     lowerBoundPZ + k * (upperBoundPZ - lowerBoundPZ) / (samples - 1),
                                                     trackInfo[6]};

                    //Get interaction vertices
                    doca.get(i).get(j).add(ex.submit(new ThreadedVertexFinder(iterationsVertex,
                                                                              samplesVertex,
                                                                              zMinGlobal,
                                                                              zMaxGlobal,
                                                                              rasterInfo[0],
                                                                              rasterInfo[2],
                                                                              swimParams,
                                                                              output[i][j][k])));
                    }
                }
            }
        
        //Wait for all tasks to finish
        ex.shutdown();
        ex.awaitTermination(10, TimeUnit.DAYS);//Arbitrarily large
            
        //Find local mins
        for(int i = 0; i < samples; i++){
            for(int j = 0; j < samples; j++){
                for(int k = 0; k < samples; k++){
                                //check if within rasterErr
                                if(doca.get(i).get(j).get(k).get() < rasterErr){
                                    localMinIndices.add(new int[]{i, j, k});
                                    continue;
                                }
                                //check if unrealistically massive
                                if(doca.get(i).get(j).get(k).get() > Float.MAX_VALUE / 2.0){
                                    continue;
                                }
                                //check px component
                                if(i == 0 && doca.get(i).get(j).get(k).get() > doca.get(i + 1).get(j).get(k).get()){
                                    continue;
                                }
                                else if (i > 0 && i < samples - 1 && (doca.get(i).get(j).get(k).get() > doca.get(i + 1).get(j).get(k).get() || doca.get(i).get(j).get(k).get() > doca.get(i - 1).get(j).get(k).get())){
                                    continue;
                                }
                                else if(i == samples - 1 && doca.get(i).get(j).get(k).get() > doca.get(i - 1).get(j).get(k).get()){
                                    continue;
                                }
                                //check py component
                                if(j == 0 && doca.get(i).get(j).get(k).get() > doca.get(i).get(j + 1).get(k).get()){
                                    continue;
                                }
                                else if (j > 0 && j < samples - 1 && (doca.get(i).get(j).get(k).get() > doca.get(i).get(j + 1).get(k).get() || doca.get(i).get(j).get(k).get() > doca.get(i).get(j - 1).get(k).get())){
                                    continue;
                                }
                                else if(j == samples - 1 && doca.get(i).get(j).get(k).get() > doca.get(i).get(j - 1).get(k).get()){
                                    continue;
                                }
                                //check pz component
                                if(k == 0 && doca.get(i).get(j).get(k).get() > doca.get(i).get(j).get(k + 1).get()){
                                    continue;
                                }
                                else if (k > 0 && k < samples - 1 && (doca.get(i).get(j).get(k).get() > doca.get(i).get(j).get(k + 1).get() || doca.get(i).get(j).get(k).get() > doca.get(i).get(j).get(k - 1).get())){
                                    continue;
                                }
                                else if(k == samples - 1 && doca.get(i).get(j).get(k).get() > doca.get(i).get(j).get(k - 1).get()){
                                    continue;
                                }
                                //add to local min list
                                localMinIndices.add(new int[]{i, j, k});
                }
            }
        }
        
        //Cull values that fail zMinGlobal < z < zMaxGlobal
        for(int i = 0; i < localMinIndices.size(); i++){
            int[] idx = localMinIndices.get(i);
            if(output[idx[0]][idx[1]][idx[2]][2] < zMinGlobal || output[idx[0]][idx[1]][idx[2]][2] > zMaxGlobal){
                localMinIndices.remove(i);
                i--;
            }
        }
        
        //Exit
        int[] i;
        switch(localMinIndices.size()){
            case 0:
                //Fail gracefully
                Arrays.fill(out, Float.NaN);
                return Float.NaN;
            case 1:
                //Only one value
                i = localMinIndices.get(0);
                System.arraycopy(output[i[0]][i[1]][i[2]], 0, out, 0, 8);
                return doca.get(i[0]).get(i[1]).get(i[2]).get();
            default:
                //Find value which minimizes change from nominal track
                float minIdxDiff = Float.MAX_VALUE;
                float minDoca = Float.MAX_VALUE;
                boolean inRasterErr = false;
                int minIndex = 0;
                for(int[] idx : localMinIndices){
                    float swimDoca = doca.get(idx[0]).get(idx[1]).get(idx[2]).get();
                    //System.out.println("idx: " + Arrays.toString(idx) + ", doca: " + swimDoca + ", z: " + output[idx[0]][idx[1]][idx[2]][2]);
                    if(swimDoca < rasterErr){
                        inRasterErr = true;
                        float idxDiff = (float)Math.sqrt(Math.pow(idx[0] - (float)samples / 2, 2.0) +
                                                         Math.pow(idx[1] - (float)samples / 2, 2.0) +
                                                         Math.pow(idx[2] - (float)samples / 2, 2.0));
                        if(idxDiff < minIdxDiff){
                            minIdxDiff = idxDiff;
                            minIndex = localMinIndices.indexOf(idx);
                        }
                    }
                    else if(inRasterErr == false){
                        if(swimDoca < minDoca){
                            minDoca = swimDoca;
                            minIndex = localMinIndices.indexOf(idx);
                        }
                    }
                    
                }
                i = localMinIndices.get(minIndex);
                //System.out.println("Winner: " + Arrays.toString(i));
                System.arraycopy(output[i[0]][i[1]][i[2]], 0, out, 0, 8);
                return doca.get(i[0]).get(i[1]).get(i[2]).get();
        }
    }
    
    /**
     * This method uses a swimmer to calculate the beam's doca position relative to the raster beam axis.
     * 
     * @param iterations    The maximum number of recursive steps
     * @param samples       The number of sample points to take between zMin and zMax for each step.
     * @param zMin          The lower bound of the area of interest. Should be below the lower bound of the target
     * @param zMax          The upper bound of the area of interest. Should be above the upper bound of the target
     * @param rasterX       The rasterized beam x position
     * @param rasterY       The rasterized beam y postiion
     * @param swim          A Swim class which has been set to the position and momentum of interest
     * @param out           A pointer to a float array which will be filled by this method. Will be set to the Swim output or NaN
     * @return the doca to the rasterized beam coords; -1.0 if no mimimum was found
     */
    private float findInteractionVertex(int iterations, int samples, float zMin, float zMax, float rasterX, float rasterY, Swim swim, float[] out){
        //Define useful arrays
        float[][] swimOutput = new float[samples][8];
        float[] doca = new float[samples];
        int[] docaIndex = new int[samples];
        int docaLength = 0;
        int[] localMin = new int[samples];
        int localMinLength = 0;
        
        //Get swim outputs
        for(int i = 0; i < samples; i++){
            double[] temp = swim.SwimToPlaneLab(zMin + i * (zMax - zMin) / (samples - 1));
            if(temp == null){
                swimOutput[i] = null;
                continue;
            }
            for(int j = 0; j < 8; j++){
                swimOutput[i][j] = (float)temp[j];
            }
        }
        
        //Calculate the doca for each sample point
        for(int i = 0; i < samples; i++){
            if(swimOutput[i] == null){
                continue;
            }
            doca[docaLength] = (float)Math.sqrt(Math.pow(rasterX - swimOutput[i][0], 2.0f) + Math.pow(rasterY - swimOutput[i][1], 2.0f));
            docaIndex[docaLength] = i;
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
            float smallest = Float.MAX_VALUE;
            
            //Find a minimum?
            if(localMinLength == 0){
                Arrays.fill(out, Float.NaN);
                return Float.MAX_VALUE;
            }
            
            //Find the smallest doca
            for(int i = 0; i < localMinLength; i++){
                if(doca[localMin[i]] < smallest){
                    index = i;
                }
            }
            System.arraycopy(swimOutput[localMin[index]], 0, out, 0, 8);
            return doca[localMin[index]];
        }
        
        //Recursively call this method on each of the local minima
        float[][] minOut = new float[localMinLength][8];
        float[] minDoca = new float[localMinLength];
        for(int i = 0; i < localMinLength; i++){
            if(docaIndex[localMin[i]] == 0){
                float newZMin = zMin - (zMax - zMin) / 2;
                float newZMax = zMax - (zMax - zMin) / 2;
                if(newZMin < 2.0 * zMinGlobal - zMaxGlobal){
                    Arrays.fill(minOut[i], Float.NaN);
                    minDoca[i] = Float.MAX_VALUE;
                }
                else{
                    minDoca[i] = findInteractionVertex(iterations, samples, newZMin, newZMax, rasterX, rasterY, swim, minOut[i]);
                }
            }
            else if(docaIndex[localMin[i]] == samples - 1){
                float newZMin = zMin + (zMax - zMin) / 2;
                float newZMax = zMax + (zMax - zMin) / 2;
                if(newZMin > 2.0 * zMaxGlobal - zMinGlobal){
                    Arrays.fill(minOut[i], Float.NaN);
                    minDoca[i] = Float.MAX_VALUE;
                }
                else{
                    minDoca[i] = findInteractionVertex(iterations, samples, newZMin, newZMax, rasterX, rasterY, swim, minOut[i]);
                }
            }
            else{
                minDoca[i] = findInteractionVertex(iterations - 1, samples, swimOutput[localMin[i]][2] - (zMax - zMin) / (samples - 1), swimOutput[localMin[i]][2] + (zMax - zMin) / (samples - 1), rasterX, rasterY, swim, minOut[i]);
            }
        }
        
        //Find the smallest doca
        int index = -1;
        float smallest = Float.MAX_VALUE;
        for(int i = 0; i < localMinLength; i++){
            if(minOut[i] != null && minDoca[i] < smallest)
            {
                index = i;
            }
        }
        
        //Exit
        if(index == -1){
            Arrays.fill(out, Float.NaN);
            return Float.MAX_VALUE;
        }
        System.arraycopy(minOut[index], 0, out, 0, 8);
        return minDoca[index];
    }
    
    /**
     * This class is a passthrough class to allow the findInteractionVertex
     * method to be threaded
     */
    private class ThreadedVertexFinder implements Callable<Float>{
        //Copies of method parameters
        int iterations;
        int samples;
        float zMin;
        float zMax;
        float rasterX;
        float rasterY;
        Swim swim;
        float[] out;
        
        //Constructor
        public ThreadedVertexFinder(int _iterations, int _samples, float _zMin, float _zMax, float _rasterX, float _rasterY, float[] _swimParams, float[] _out){
            iterations = _iterations;
            samples = _samples;
            zMin = _zMin;
            zMax = _zMax;
            rasterX = _rasterX;
            rasterY = _rasterY;
            swim = new Swim();
            swim.SetSwimParameters(_swimParams[0], _swimParams[1], _swimParams[2], _swimParams[3], _swimParams[4], _swimParams[5], (int)_swimParams[6]);
            out = _out;
        }
        
        //Passthrough
        @Override
        public Float call(){
            return findInteractionVertex(iterations, samples, zMin, zMax, rasterX, rasterY, swim, out);
        }
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