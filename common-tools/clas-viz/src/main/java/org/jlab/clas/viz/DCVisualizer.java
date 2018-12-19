package org.jlab.clas.viz;

import cnuphys.magfield.MagneticFields;
import javax.swing.JOptionPane;
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
        try{
        MagneticFields.getInstance().initializeMagneticFieldsFromPath(System.getenv("CLAS12DIR") + "/etc/data/magfield/" + System.getenv("TORUSMAP"),
                System.getenv("CLAS12DIR") + "/etc/data/magfield/" + System.getenv("SOLENOIDMAP"));
        }
        catch(Exception e){
            JOptionPane.showMessageDialog(null, e, "Magnetic Field Initialization Error!", JOptionPane.WARNING_MESSAGE);
        }
        PathSim.init();
        RootFrame frame = new RootFrame(reader);
        
        if(args.length > 0){
            frame.setVisible(true);
            reader.open(args[0]);
        }
        else{
            frame.setVisible(true);
        }
    }
}
