package org.jlab.clas.viz;

import org.jlab.clas.viz.data.DataReader;
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
