package org.jlab.clas.viz.sim;

import cnuphys.magfield.CompositeField;
import cnuphys.magfield.Solenoid;
import cnuphys.magfield.Torus;
import java.io.File;
import java.io.FileNotFoundException;
import org.jlab.clas.pdg.PDGDatabase;

/**
 *
 * @author friant
 */
public class PathSim {
    private static CompositeField field;
    
    /**
     * 
     * @param pid
     * @param posVec
     * @param momVec
     * @return 
     */
    public static final float[] simulate(int pid, float[] posVec, float[] momVec){
        float absMass = (float)PDGDatabase.getParticleById(pid).mass();
        float relMass = calculateRelativisticMass(absMass, momVec);
        float charge = (float)PDGDatabase.getParticleById(pid).charge();
        //System.out.println("PID: " + pid);
        //System.out.println("Absoulte Mass: " + absMass);
        //System.out.println("Relativistic Mass: " + relMass);
        //System.out.println("Charge: " + charge);
        //return new float[]{0.0f, -100.0f, 300.0f, 1.0f, 0.0f, 100f, 400.0f, 1.0f, 0.0f, -100.0f, 500.0f, 1.0f};
        return new float[]{0.0f, 0.0f, 100.0f, 1.0f, 0.0f, 0.0f, 700.0f, 1.0f};
    }
    
    /**
     * 
     */
    private static final float calculateRelativisticMass(float absMass, float[] momVec){
        return 0.0f;
    }
    
    /**
     * 
     */
    public static boolean setField(){
        boolean success = true;
        try{
            field = new CompositeField();
            File solFile = new File(System.getenv("CLAS12DIR") + "/etc/data/magfield/" + System.getenv("SOLENOIDMAP"));
            File torFile = new File(System.getenv("CLAS12DIR") + "/etc/data/magfield/" + System.getenv("TORUSMAP"));
            Solenoid solField = Solenoid.fromBinaryFile(solFile);
            Torus torField = Torus.fromBinaryFile(torFile);
            field.add(solField);
            field.add(torField);
        }
        catch(FileNotFoundException e){
            success = false;
        }
        return success;
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
