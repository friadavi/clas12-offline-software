package org.jlab.clas.viz.reco;

import cnuphys.magfield.MagneticFields;
import org.jlab.clas.pdg.PDGDatabase;
import org.jlab.clas.swimtools.Swim;
import java.util.ArrayList;
import javax.swing.JOptionPane;

/**
 *
 * @author friant
 */
public class PathSimulation {
    
    /**
     * 
     */
    public static final void init(){
        try{
        MagneticFields.getInstance().initializeMagneticFieldsFromPath(System.getenv("CLAS12DIR") + "/etc/data/magfield/" + System.getenv("TORUSMAP"),
                System.getenv("CLAS12DIR") + "/etc/data/magfield/" + System.getenv("SOLENOIDMAP"));
        }
        catch(Exception e){
            JOptionPane.showMessageDialog(null, e, "Magnetic Field Initialization Error!", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    /**
     * 
     * @param pid
     * @param posVec
     * @param momVec
     * @return 
     */
    public static final float[] simulate(int pid, float[] posVec, float[] momVec){
        Swim swim = new Swim();
        ArrayList<Float> trackPos = new ArrayList<>();
        int charge = PDGDatabase.getParticleById(pid).charge();
        
        swim.SetSwimParameters(posVec[0], posVec[1], posVec[2], momVec[0], momVec[1], momVec[2], charge);
        
        trackPos.add(posVec[0]);
        trackPos.add(posVec[1]);
        trackPos.add(posVec[2]);
        trackPos.add(1.0f);
        
        double[] newInfo = new double[]{posVec[0], posVec[1], posVec[2], momVec[0], momVec[1], momVec[2], 0.0, 0.0};
        for(int i = 0; i < 100; i++){
            swim.SetSwimParameters(newInfo[0], newInfo[1], newInfo[2], newInfo[3], newInfo[4], newInfo[5], charge);
            newInfo = swim.SwimToPlaneLab(newInfo[2] + 10.0);
            
            if(newInfo == null){
                break;
            }
            
            trackPos.add((float)newInfo[0]);
            trackPos.add((float)newInfo[1] * -1.0f);//One of the coordinate systems is left handed, this is a temporary fix
            trackPos.add((float)newInfo[2]);
            trackPos.add(1.0f);
            
            for(int j = 0; j < 3; j++){
                if(newInfo[j] < -1000.0 || newInfo[j] > 1000.0){
                    break;
                }
            }
        }
        
        float[] out = new float[trackPos.size()];
        for(int i = 0; i < out.length; i++){
            out[i] = trackPos.get(i);
        }
        return out;
    }
    
    /**
     * Converts the sector 3D coordinates to clas (lab) 3D coordinates.
     * 
     * Code stolen from cnuphys.ced.geometry.GeometryManager and modified.
     * 
     * @param sector the 1-based sector [1..6]
     * @param data
     * @param offset
     */
    public static void sectorToClas(int sector, float[] data, int offset) {
        float x = data[offset + 0];
        float y = data[offset + 1];
        float midPlanePhi = (float)Math.toRadians(60 * (sector - 1));
        float cosPhi = (float)Math.cos(midPlanePhi);
        float sinPhi = (float)Math.sin(midPlanePhi);
        data[offset + 0] = cosPhi * x - sinPhi * y;
        data[offset + 1] = sinPhi * x + cosPhi * y;
    }
}
