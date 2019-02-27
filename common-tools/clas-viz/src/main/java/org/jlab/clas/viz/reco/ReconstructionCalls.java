package org.jlab.clas.viz.reco;

import org.jlab.clas.viz.data.DataReader;
import org.jlab.rec.cvt.services.CVTReconstruction;
import org.jlab.service.dc.DCHBEngine;
import org.jlab.service.dc.DCRBEngine;
import org.jlab.service.dc.DCTBEngine; 

/**
 *
 * @author friant
 */
public class ReconstructionCalls {
    private static DataReader reader;
    private static CVTReconstruction cvt;
    private static DCHBEngine dchb;
    private static DCTBEngine dctb;
    private static DCRBEngine dcrb;
    
    /**
     * 
     * @param _reader 
     */
    public static void initReader(DataReader _reader){
        reader = _reader;
    }
    /**
     * 
     * @return 
     */
    public static boolean initCVT(){
        cvt = new CVTReconstruction();
        return cvt.init();
    }
    
    /**
     * 
     * @return 
     */
    public static boolean initDCHB(){
        dchb = new DCHBEngine();
        return dchb.init();
    }
    
    /**
     * 
     * @return 
     */
    public static boolean initDCTB(){
        dctb = new DCTBEngine();
        return dctb.init();
    }
    
    /**
     * 
     * @return 
     */
    public static boolean initDCRB(){
        dcrb = new DCRBEngine();
        return dcrb.init();
    }
    
    /**
     * 
     */
    public static void recoEventCVT(){
        cvt.processDataEvent(reader.getCurrentEvent());
    }
    
    /**
     * 
     */
    public static void recoEventDCHB(){
        dchb.processDataEvent(reader.getCurrentEvent());
    }
    
    /**
     * 
     */
    public static void recoEventDCTB(){
        dctb.processDataEvent(reader.getCurrentEvent());
    }
    
    /**
     * 
     */
    public static void recoEventDCRB(){
        dcrb.processDataEvent(reader.getCurrentEvent());
    }
}
