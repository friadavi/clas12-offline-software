package org.jlab.clas.viz;

import javax.swing.ProgressMonitor;
import org.jlab.clas.viz.data.DataReader;
import org.jlab.clas.viz.reco.PathSimulation;
import org.jlab.clas.viz.reco.ReconstructionCalls;
import org.jlab.clas.viz.ui.RootFrame;

/**
 * 
 * 
 * @author friant
 */
public class DCVisualizer {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int n = 0;
        ProgressMonitor monitor = new ProgressMonitor(null, "Initialization Underway", "DataReader", n++, 8);
        monitor.setMillisToPopup(0);
        monitor.setMillisToDecideToPopup(0);
        
        DataReader reader = new DataReader();
        monitor.setNote("MagFields");
        monitor.setProgress(n++);
        PathSimulation.init();
        if(monitor.isCanceled()){
            return;
        }
        
        monitor.setNote("Reco Engines:");
        monitor.setProgress(n++);
        ReconstructionCalls.initReader(reader);
        if(monitor.isCanceled()){
            return;
        }
        
        monitor.setNote("Reco Engines: CVT");
        monitor.setProgress(n++);
        ReconstructionCalls.initCVT();
        if(monitor.isCanceled()){
            return;
        }
        
        monitor.setNote("Reco Engines: DCHB");
        monitor.setProgress(n++);
        ReconstructionCalls.initDCHB();
        if(monitor.isCanceled()){
            return;
        }
        
        monitor.setNote("Reco Engines: DCTB");
        monitor.setProgress(n++);
        ReconstructionCalls.initDCTB();
        if(monitor.isCanceled()){
            return;
        }
        
        monitor.setNote("Reco Engines: DCRB");
        monitor.setProgress(n++);
        ReconstructionCalls.initDCRB();
        if(monitor.isCanceled()){
            return;
        }
        
        monitor.setNote("GUI");
        monitor.setProgress(n++);
        RootFrame frame = new RootFrame(reader);
        if(monitor.isCanceled()){
            return;
        }
        
        monitor.setNote("Finished");
        monitor.setProgress(n++);
        if(monitor.isCanceled()){
            return;
        }
        
        if(args.length > 0){
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run(){
                frame.setVisible(true);
                reader.open(args[0]);
                }
            });
        }
        else{
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run(){
                frame.setVisible(true);
                }
            });
        }
    }
}
