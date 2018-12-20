package org.jlab.clas.viz;

import org.jlab.clas.viz.data.DataReader;
import org.jlab.clas.viz.sim.PathSim;
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
        DataReader reader = new DataReader();
        PathSim.init();
        RootFrame frame = new RootFrame(reader);
        
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
