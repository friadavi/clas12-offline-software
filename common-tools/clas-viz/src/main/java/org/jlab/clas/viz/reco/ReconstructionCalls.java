package org.jlab.clas.viz.reco;

import org.jlab.clas.viz.data.DataReader;
import org.jlab.rec.cvt.services.CVTReconstruction;
import org.jlab.service.dc.DCHBEngine;
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
    
    /**
     * 
     * @param _reader 
     */
    public static void initReader(DataReader _reader){
        reader = _reader;
    }
    /**
     * 
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
     */
    public static void recoEventCVT(){
        cvt.processDataEvent(reader.getCurrentEvent());
    }
    
    /**
     * 
     */
    public static void recoEventDC(){
        dchb.processDataEvent(reader.getCurrentEvent());
        dctb.processDataEvent(reader.getCurrentEvent());
    }
}
