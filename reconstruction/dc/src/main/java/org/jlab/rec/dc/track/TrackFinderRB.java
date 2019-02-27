package org.jlab.rec.dc.track;

import java.util.Arrays;
import org.jlab.clas.swimtools.Swim;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

/**
 * A class which is used to find the raster based tracks
 *
 * @author friant
 */

public class TrackFinderRB{
    
    /**
     * Dummy Constructor for future use if necessary
     */
    public TrackFinderRB(){
    }
    
    /**
     * This method currently uses the reconstructed vertex and momentum from the
     * HB algorithm to find the DOCA position on the linearized track between it
     * and the beam line.
     * 
     * @param event   DataEvent which holds the information to reconstruct from
     * @param x       x position of the raster beam
     * @param uX      uncertainty of the x position of the raster beam
     * @param y       y position of the raster beam
     * @param uY      uncertainty of the y position of the raster beam
     * @param swim    swimmer to be used in reconstruction
     */
    public DataBank getTracksHB(DataEvent event, double x, double uX, double y, double uY, Swim swim){
        //Get HB Bank
        DataBank hbBank = event.getBank("HitBasedTrkg::HBTracks");
        
        //Create the bank to return
        DataBank rbBank = event.createBank("RasterBasedTrkg::RBTracks", hbBank.rows());
        
        //Cycle through all tracks
        for(int i = 0; i < hbBank.rows(); i++){
            //Linear estimation to find a z postion for the interaction vertex
            // - Paramterize both the beam and the track at the boundary as: position + momentum * variable
            //   * Beam
            Point3D p1  =  new Point3D(x, y, 0);
            Vector3D v1 = new Vector3D(0, 0, 1);
            //   * Track
            Point3D p2  =  new Point3D(hbBank.getFloat("Vtx0_x", i), hbBank.getFloat("Vtx0_y", i), hbBank.getFloat("Vtx0_z", i));
            Vector3D v2 = new Vector3D(hbBank.getFloat("p0_x", i), hbBank.getFloat("p0_y", i), hbBank.getFloat("p0_z", i));
            // - Calculate the doca
            Vector3D n = v1.cross(v2);
            n.unit();
            double d = Math.abs(n.dot(p1.vectorFrom(p2)));
            // - Calculate the point on the track nearest to the beam
            //   * Find the normal-normal vector
            Vector3D n1 = v1.cross(v2.cross(v1));
            //   * Compute
            v2.scale( ((p2.vectorFrom(p1)).dot(n1)) / (v2.dot(n1)) );
            p2.set(p2, v2);
            Point3D c2 = p2;
            
            //Add all info to rbBank
            rbBank.setShort("id", i, hbBank.getShort("id", i));
            rbBank.setShort("status", i, hbBank.getShort("status", i));
            rbBank.setByte("sector", i, hbBank.getByte("sector", i));
            rbBank.setFloat("c1_x", i, hbBank.getFloat("c1_x", i));
            rbBank.setFloat("c1_y", i, hbBank.getFloat("c1_y", i));
            rbBank.setFloat("c1_z", i, hbBank.getFloat("c1_z", i));
            rbBank.setFloat("c1_ux", i, hbBank.getFloat("c1_ux", i));
            rbBank.setFloat("c1_uy", i, hbBank.getFloat("c1_uy", i));
            rbBank.setFloat("c1_uz", i, hbBank.getFloat("c1_uz", i));
            rbBank.setFloat("c3_x", i, hbBank.getFloat("c3_x", i));
            rbBank.setFloat("c3_y", i, hbBank.getFloat("c3_y", i));
            rbBank.setFloat("c3_z", i, hbBank.getFloat("c3_z", i));
            rbBank.setFloat("c3_ux", i, hbBank.getFloat("c3_ux", i));
            rbBank.setFloat("c3_uy", i, hbBank.getFloat("c3_uy", i));
            rbBank.setFloat("c3_uz", i, hbBank.getFloat("c3_uz", i));
            rbBank.setFloat("t1_x", i, hbBank.getFloat("t1_x", i));
            rbBank.setFloat("t1_y", i, hbBank.getFloat("t1_y", i));
            rbBank.setFloat("t1_z", i, hbBank.getFloat("t1_z", i));
            rbBank.setFloat("t1_px", i, hbBank.getFloat("t1_px", i));
            rbBank.setFloat("t1_py", i, hbBank.getFloat("t1_py", i));
            rbBank.setFloat("t1_pz", i, hbBank.getFloat("t1_pz", i));
            rbBank.setFloat("Vtx0_x", i, (float)c2.x());
            rbBank.setFloat("Vtx0_y", i, (float)c2.y());
            rbBank.setFloat("Vtx0_z", i, (float)c2.z());
            rbBank.setFloat("p0_x", i, hbBank.getFloat("p0_x", i));
            rbBank.setFloat("p0_y", i, hbBank.getFloat("p0_y", i));
            rbBank.setFloat("p0_z", i, hbBank.getFloat("p0_z", i));
            rbBank.setShort("Cross1_ID", i, hbBank.getShort("Cross1_ID", i));
            rbBank.setShort("Cross2_ID", i, hbBank.getShort("Cross2_ID", i));
            rbBank.setShort("Cross3_ID", i, hbBank.getShort("Cross3_ID", i));
            rbBank.setByte("q", i, hbBank.getByte("q", i));
            rbBank.setFloat("pathlength", i, hbBank.getFloat("pathlength", i));
            rbBank.setFloat("chi2", i, hbBank.getFloat("chi2", i));
            rbBank.setShort("ndf", i, hbBank.getShort("ndf", i));
            rbBank.setFloat("doca", i, (float)d);
        }
        //hbBank.show();
        //rbBank.show();
        return rbBank;
    }
}