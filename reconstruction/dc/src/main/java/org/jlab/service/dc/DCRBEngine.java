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
    static float solVal            =  1.0f;
    static float torVal            = -1.0f;
    static float zMinGlobal        = -5.0f;
    static float zMaxGlobal        =  5.0f;
    static int   iterationsVertex  =  3;
    static int   samplesVertex     =  10;
    static int   samplesGridSearch =  4;
    static int   maxThreads        =  1;
    
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
        String threads = "";
        
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
                case "-x":
                    if(i + 1 < args.length){
                        threads = args[i + 1];
                    }
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
            catch(IOException e){
                System.out.println("Could Not Create Output File");
                return;
            }
        }
        if(!solenoid.isEmpty()){
            solVal = Float.parseFloat(solenoid);
        }
        if(!toroid.isEmpty()){
            torVal = Float.parseFloat(toroid);
        }
        if(!threads.isEmpty()){
            maxThreads = Integer.parseInt(threads);
        }
        
        if(solVal == Float.NaN || torVal == Float.NaN){
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
            + "            -h      Print This Message\r\n"
            + "            -s      Solonoid Field Scale Factor\r\n"
            + "                      Default Value =  1.0\r\n"
            + "            -t      Toroid Field Scale Factor\r\n"
            + "                      Default Value = -1.0\r\n"
            + "            -x      Maximum Number of Threads to Use\r\n");
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
        if(event.hasBank("TimeBasedTrkg::TBTracks")){
            event.appendBank(copyBank(event, "TimeBasedTrkg::TBHits", "RasterBasedTrkg::RBHits"));
            event.appendBank(copyBank(event, "TimeBasedTrkg::TBClusters", "RasterBasedTrkg::RBClusters"));
            event.appendBank(copyBank(event, "TimeBasedTrkg::TBSegments", "RasterBasedTrkg::RBSegments"));
            event.appendBank(copyBank(event, "TimeBasedTrkg::TBCrosses", "RasterBasedTrkg::RBCrosses"));
            sourceTracks = "TimeBasedTrkg::TBTracks";
        }
        else{
            event.appendBank(copyBank(event, "HitBasedTrkg::HBHits", "RasterBasedTrkg::RBHits"));
            event.appendBank(copyBank(event, "HitBasedTrkg::HBClusters", "RasterBasedTrkg::RBClusters"));
            event.appendBank(copyBank(event, "HitBasedTrkg::HBSegments", "RasterBasedTrkg::RBSegments"));
            event.appendBank(copyBank(event, "HitBasedTrkg::HBCrosses", "RasterBasedTrkg::RBCrosses"));
            sourceTracks = "HitBasedTrkg::HBTracks";
        }
        
        //Create the RBTracks Bank
        DataBank rbBank = copyBank(event, sourceTracks, "RasterBasedTrkg::RBTracks");
        
        //Raster variables
        float rasterX = event.getBank("MC::Particle").getFloat("vx", 0);
        float rasterY = event.getBank("MC::Particle").getFloat("vy", 0);
        float rasterUX = 0.05f;//0.5mm in either direction
        float rasterUY = 0.05f;//0.5mm in either direction
        float[] rasterInfo = new float[]{rasterX, rasterUX, rasterY, rasterUY};
        
        //Calculate the interaction vertex for each for each track in the event
        for(int i = 0; i < rbBank.rows(); i++){
            
            //Get track info
            float[] trackInfo = new float[7];
            trackInfo[0] = event.getBank(sourceTracks).getFloat("t1_x", i);
            trackInfo[1] = event.getBank(sourceTracks).getFloat("t1_y", i);
            trackInfo[2] = event.getBank(sourceTracks).getFloat("t1_z", i);
            trackInfo[3] = event.getBank(sourceTracks).getFloat("t1_px", i);
            trackInfo[4] = event.getBank(sourceTracks).getFloat("t1_py", i);
            trackInfo[5] = event.getBank(sourceTracks).getFloat("t1_pz", i);
            trackInfo[6] = (float)(event.getBank(sourceTracks).getByte("q", i));
            
            //Set assumed errors in track components
            float posErr = 0.25f;//+ or - one quarter cm
            float momErr = 0.1f;//+ or - 10%
            float[] uncertainty = new float[]{posErr, momErr};
            
            //Calculate
            float[] output = new float[8];
            Arrays.fill(output, Float.NaN);
            float doca = -1.0f;
            try {
                doca = interactionVertexGridSearch(samplesGridSearch, rasterInfo, trackInfo, uncertainty, output);
            } catch (InterruptedException | ExecutionException exception) {
                Logger.getLogger(DCRBEngine.class.getName()).log(Level.SEVERE, null, exception);
            }
            
            //Make sure that the momentum is pointing in the right direction
            if(output[5] < 0.0){
                output[3] = output[3] * -1.0f;
                output[4] = output[4] * -1.0f;
                output[5] = output[5] * -1.0f;
            }
            
            //Set Values
            rbBank.setFloat("Vtx0_x", i, output[0]);
            rbBank.setFloat("Vtx0_y", i, output[1]);
            rbBank.setFloat("Vtx0_z", i, output[2]);
            rbBank.setFloat("p0_x", i, output[3]);
            rbBank.setFloat("p0_y", i, output[4]);
            rbBank.setFloat("p0_z", i, output[5]);
            rbBank.setFloat("doca", i, doca);
        }
        
        //Put Tracks in Event
        event.appendBank(rbBank);
        
        return true;
    }
    
    /**
     * This method performs a six-dimensional grid search within the uncertainty
     * in the track position-momentum space.
     * 
     * @param samples       the number of smaples to ake in each dimension
     * @param rasterInfo    the array {x, uncert x, y, uncert y}
     * @param trackInfo     the array{vx, vy, vz, px, py, pz}
     * @param uncertainty   the array{+ or - uncert in track vertex, + or - percent uncert in track momentum}
     * @param out           the array to fill with the ultimate swim output result
     * @return              the doca of the ultimate swim output result
     */
    private float interactionVertexGridSearch(int samples, float[] rasterInfo, float[] trackInfo, float[] uncertainty, float[] out) throws InterruptedException, ExecutionException{
        //define bounds
        float upperBoundVX = trackInfo[0] + uncertainty[0];
        float lowerBoundVX = trackInfo[0] - uncertainty[0];
        float upperBoundVY = trackInfo[1] + uncertainty[0];
        float lowerBoundVY = trackInfo[1] - uncertainty[0];
        float upperBoundVZ = trackInfo[2] + uncertainty[0];
        float lowerBoundVZ = trackInfo[2] - uncertainty[0];
        float upperBoundPX = trackInfo[3] * (1.0f + uncertainty[1]);
        float lowerBoundPX = trackInfo[3] * (1.0f - uncertainty[1]);
        float upperBoundPY = trackInfo[4] * (1.0f + uncertainty[1]);
        float lowerBoundPY = trackInfo[4] * (1.0f - uncertainty[1]);
        float upperBoundPZ = trackInfo[5] * (1.0f + uncertainty[1]);
        float lowerBoundPZ = trackInfo[5] * (1.0f - uncertainty[1]);
        
        //choose the min radius of uncertainty to check doca against
        float rasterErr = Math.min(rasterInfo[1], rasterInfo[3]);
        
        //define useful arrays
        float[][][][][][][] output = new float[samples][samples][samples][samples][samples][samples][8];
        ArrayList<ArrayList<ArrayList<ArrayList<ArrayList<ArrayList<Future<Float>>>>>>> doca = new ArrayList();
        ArrayList<int[]>     localMinIndices = new ArrayList<>();
        
        //Define thread service
        ExecutorService ex = Executors.newFixedThreadPool(maxThreads);
        
        //Find the interaction vertices and docas to sift through
        for(int i = 0; i < samples; i++){
            doca.add(new ArrayList<>());
            for(int j = 0; j < samples; j++){
                doca.get(i).add(new ArrayList<>());
                for(int k = 0; k < samples; k++){
                    doca.get(i).get(j).add(new ArrayList<>());
                    for(int l = 0; l < samples; l++){
                        doca.get(i).get(j).get(k).add(new ArrayList<>());
                        for(int m = 0; m < samples; m++){
                            doca.get(i).get(j).get(k).get(l).add(new ArrayList<>());
                            for(int n = 0; n < samples; n++){
                                //Set Swimmer params
                                float[] swimParams = new float[]{lowerBoundVX + i * (upperBoundVX - lowerBoundVX) / (samples - 1),
                                                                 lowerBoundVY + j * (upperBoundVY - lowerBoundVY) / (samples - 1),
                                                                 lowerBoundVZ + k * (upperBoundVZ - lowerBoundVZ) / (samples - 1),
                                                                 lowerBoundPX + l * (upperBoundPX - lowerBoundPX) / (samples - 1),
                                                                 lowerBoundPY + m * (upperBoundPY - lowerBoundPY) / (samples - 1),
                                                                 lowerBoundPZ + n * (upperBoundPZ - lowerBoundPZ) / (samples - 1),
                                                                 trackInfo[6]};
                                
                                //Get interaction vertices
                                doca.get(i).get(j).get(k).get(l).get(m).add(ex.submit(new ThreadedVertexFinder(iterationsVertex,
                                                                                                               samplesVertex,
                                                                                                               zMinGlobal,
                                                                                                               zMaxGlobal,
                                                                                                               rasterInfo[0],
                                                                                                               rasterInfo[2],
                                                                                                               swimParams,
                                                                                                               output[i][j][k][l][m][n])));
                                
                            }
                        }
                    }
                }
            }
        }
        
        //Wait for all tasks to finish
        ex.shutdown();
        ex.awaitTermination(10, TimeUnit.DAYS);
            
        //Find local mins
        for(int i = 0; i < samples; i++){
            for(int j = 0; j < samples; j++){
                for(int k = 0; k < samples; k++){
                    for(int l = 0; l < samples; l++){
                        for(int m = 0; m < samples; m++){
                            for(int n = 0; n < samples; n++){
                                //check if within rasterErr
                                if(doca.get(i).get(j).get(k).get(l).get(m).get(n).get() < rasterErr){
                                    localMinIndices.add(new int[]{i, j, k, l, m, n});
                                    continue;
                                }
                                //check vx component
                                if(i == 0 && doca.get(i).get(j).get(k).get(l).get(m).get(n).get() > doca.get(i + 1).get(j).get(k).get(l).get(m).get(n).get()){
                                    continue;
                                }
                                else if (i > 0 && i < samples - 1 && (doca.get(i).get(j).get(k).get(l).get(m).get(n).get() > doca.get(i + 1).get(j).get(k).get(l).get(m).get(n).get() || doca.get(i).get(j).get(k).get(l).get(m).get(n).get() > doca.get(i - 1).get(j).get(k).get(l).get(m).get(n).get())){
                                    continue;
                                }
                                else if(i == samples - 1 && doca.get(i).get(j).get(k).get(l).get(m).get(n).get() > doca.get(i - 1).get(j).get(k).get(l).get(m).get(n).get()){
                                    continue;
                                }
                                //check vy component
                                if(j == 0 && doca.get(i).get(j).get(k).get(l).get(m).get(n).get() > doca.get(i).get(j + 1).get(k).get(l).get(m).get(n).get()){
                                    continue;
                                }
                                else if (j > 0 && j < samples - 1 && (doca.get(i).get(j).get(k).get(l).get(m).get(n).get() > doca.get(i).get(j + 1).get(k).get(l).get(m).get(n).get() || doca.get(i).get(j).get(k).get(l).get(m).get(n).get() > doca.get(i).get(j - 1).get(k).get(l).get(m).get(n).get())){
                                    continue;
                                }
                                else if(j == samples - 1 && doca.get(i).get(j).get(k).get(l).get(m).get(n).get() > doca.get(i).get(j - 1).get(k).get(l).get(m).get(n).get()){
                                    continue;
                                }
                                //check vz component
                                if(k == 0 && doca.get(i).get(j).get(k).get(l).get(m).get(n).get() > doca.get(i).get(j).get(k + 1).get(l).get(m).get(n).get()){
                                    continue;
                                }
                                else if (k > 0 && k < samples - 1 && (doca.get(i).get(j).get(k).get(l).get(m).get(n).get() > doca.get(i).get(j).get(k + 1).get(l).get(m).get(n).get() || doca.get(i).get(j).get(k).get(l).get(m).get(n).get() > doca.get(i).get(j).get(k - 1).get(l).get(m).get(n).get())){
                                    continue;
                                }
                                else if(k == samples - 1 && doca.get(i).get(j).get(k).get(l).get(m).get(n).get() > doca.get(i).get(j).get(k - 1).get(l).get(m).get(n).get()){
                                    continue;
                                }
                                //check px component
                                if(l == 0 && doca.get(i).get(j).get(k).get(l).get(m).get(n).get() > doca.get(i).get(j).get(k).get(l + 1).get(m).get(n).get()){
                                    continue;
                                }
                                else if (l > 0 && l < samples - 1 && (doca.get(i).get(j).get(k).get(l).get(m).get(n).get() > doca.get(i).get(j).get(k).get(l + 1).get(m).get(n).get() || doca.get(i).get(j).get(k).get(l).get(m).get(n).get() > doca.get(i).get(j).get(k).get(l - 1).get(m).get(n).get())){
                                    continue;
                                }
                                else if(l == samples - 1 && doca.get(i).get(j).get(k).get(l).get(m).get(n).get() > doca.get(i).get(j).get(k).get(l - 1).get(m).get(n).get()){
                                    continue;
                                }
                                //check py component
                                if(m == 0 && doca.get(i).get(j).get(k).get(l).get(m).get(n).get() > doca.get(i).get(j).get(k).get(l).get(m + 1).get(n).get()){
                                    continue;
                                }
                                else if (m > 0 && m < samples - 1 && (doca.get(i).get(j).get(k).get(l).get(m).get(n).get() > doca.get(i).get(j).get(k).get(l).get(m + 1).get(n).get() || doca.get(i).get(j).get(k).get(l).get(m).get(n).get() > doca.get(i).get(j).get(k).get(l).get(m - 1).get(n).get())){
                                    continue;
                                }
                                else if(m == samples - 1 && doca.get(i).get(j).get(k).get(l).get(m).get(n).get() > doca.get(i).get(j).get(k).get(l).get(m - 1).get(n).get()){
                                    continue;
                                }
                                //check pz component
                                if(n == 0 && doca.get(i).get(j).get(k).get(l).get(m).get(n).get() > doca.get(i).get(j).get(k).get(l).get(m).get(n + 1).get()){
                                    continue;
                                }
                                else if (n > 0 && n < samples - 1 && (doca.get(i).get(j).get(k).get(l).get(m).get(n).get() > doca.get(i).get(j).get(k).get(l).get(m).get(n + 1).get() || doca.get(i).get(j).get(k).get(l).get(m).get(n).get() > doca.get(i).get(j).get(k).get(l).get(m).get(n - 1).get())){
                                    continue;
                                }
                                else if(n == samples - 1 && doca.get(i).get(j).get(k).get(l).get(m).get(n).get() > doca.get(i).get(j).get(k).get(l).get(m).get(n - 1).get()){
                                    continue;
                                }
                                //add to local min list
                                localMinIndices.add(new int[]{i, j, k, l, m, n});
                            }
                        }
                    }
                }
            }
        }
        
        //Cull values that fail zMinGlobal < z < zMaxGlobal
        for(int i = 0; i < localMinIndices.size(); i++){
            int[] idx = localMinIndices.get(i);
            if(output[idx[0]][idx[1]][idx[2]][idx[3]][idx[4]][idx[5]][2] < zMinGlobal || output[idx[0]][idx[1]][idx[2]][idx[3]][idx[4]][idx[5]][2] > zMaxGlobal){
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
                System.arraycopy(output[i[0]][i[1]][i[2]][i[3]][i[4]][i[5]], 0, out, 0, 8);
                return doca.get(i[0]).get(i[1]).get(i[2]).get(i[3]).get(i[4]).get(i[5]).get();
            default:
                //Find value which minimizes change from nominal track
                float deltaPosMin = Float.MAX_VALUE;
                float deltaMomMin = Float.MAX_VALUE;
                float minDoca = Float.MAX_VALUE;
                boolean inRasterErr = false;
                int minIndex = 0;
                for(int[] idx : localMinIndices){
                    float swimDoca = doca.get(idx[0]).get(idx[1]).get(idx[2]).get(idx[3]).get(idx[4]).get(idx[5]).get();
                    if(swimDoca < rasterErr){
                        inRasterErr = true;
                        float deltaPos = (float)Math.sqrt(Math.pow(trackInfo[0] - (lowerBoundVX + idx[0] * (upperBoundVX - lowerBoundVX) / (samples - 1)), 2.0f) + 
                                                          Math.pow(trackInfo[1] - (lowerBoundVY + idx[1] * (upperBoundVY - lowerBoundVY) / (samples - 1)), 2.0f) + 
                                                          Math.pow(trackInfo[2] - (lowerBoundVZ + idx[2] * (upperBoundVZ - lowerBoundVZ) / (samples - 1)), 2.0f));
                        float deltaMom = (float)Math.sqrt(Math.pow(trackInfo[3] - (lowerBoundPX + idx[3] * (upperBoundPX - lowerBoundPX) / (samples - 1)), 2.0f) + 
                                                          Math.pow(trackInfo[4] - (lowerBoundPY + idx[4] * (upperBoundPY - lowerBoundPY) / (samples - 1)), 2.0f) + 
                                                          Math.pow(trackInfo[5] - (lowerBoundPZ + idx[5] * (upperBoundPZ - lowerBoundPZ) / (samples - 1)), 2.0f));
                        if(deltaPos < deltaPosMin && deltaMom < deltaMomMin){
                            minIndex = localMinIndices.indexOf(idx);
                        }
                    }
                    else if(inRasterErr == false){
                        if(swimDoca < minDoca){
                            minIndex = localMinIndices.indexOf(idx);
                        }
                    }
                    
                }
                i = localMinIndices.get(minIndex);
                System.arraycopy(output[i[0]][i[1]][i[2]][i[3]][i[4]][i[5]], 0, out, 0, 8);
                return doca.get(i[0]).get(i[1]).get(i[2]).get(i[3]).get(i[4]).get(i[5]).get();
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
                minDoca[i] = findInteractionVertex(iterations, samples, zMin - (zMax - zMin) / 2, zMax - (zMax - zMin) / 2, rasterX, rasterY, swim, minOut[i]);
            }
            else if(docaIndex[localMin[i]] == samples - 1){
                minDoca[i] = findInteractionVertex(iterations, samples, zMin + (zMax - zMin) / 2, zMax + (zMax - zMin) / 2, rasterX, rasterY, swim, minOut[i]);
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