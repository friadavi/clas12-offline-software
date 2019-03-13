package org.jlab.rec.dc.track;

import java.util.Arrays;
import org.jlab.clas.swimtools.Swim;
import org.jlab.clas.swimtools.Swimmer;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.cross.Cross;

/**
 * A class which is used to find the raster based tracks
 *
 * @author friant
 */

public class TrackFinderRB{
    double solStopZ = 75.0;
    double solField = 5.0;
    
    /**
     * Dummy Constructor for future use if necessary
     */
    public TrackFinderRB(){
    }
    
    /**
     * Reconstruct using only information available from the HB Banks
     * 
     * @param event   DataEvent which holds the information to reconstruct from
     * @param rasterX       x position of the raster beam
     * @param rasterUX      uncertainty of the x position of the raster beam
     * @param rasterY       y position of the raster beam
     * @param rasterUY      uncertainty of the y position of the raster beam
     * @return       
     */
    public DataBank getTracksHB(DataEvent event, double rasterX, double rasterUX, double rasterY, double rasterUY){
        //Get RECHB banks
        DataBank rechbParticleBank = event.getBank("RECHB::Particle");
        DataBank rechbTrackBank = event.getBank("RECHB::Track");
        
        //Get HB Banks
        DataBank hbTrackBank = event.getBank("HitBasedTrkg::HBTracks");
        DataBank hbCrossBank = event.getBank("HitBasedTrkg::HBCrosses");
        
        //Create the bank to return
        DataBank rbBank = event.createBank("RasterBasedTrkg::RBTracks", hbTrackBank.rows());
        
        event.getBank("MC::Particle").show();
        //event.getBank("HitBasedTrkg::HBHits").show();
        //event.getBank("HitBasedTrkg::HBSegments").show();
        //event.getBank("TimeBasedTrkg::TBHits").show();
        //event.getBank("HitBasedTrkg::HBCrosses").show();
        //event.getBank("RECHB::Particle").show();
        //event.getBank("RECHB::Track").show();
        //event.getBank("HitBasedTrkg::HBTracks").show();
        
        //Useful Things
        Cross crs = new Cross(0, 0, 0);//For converting coordinate systems
        solField = solField * Swimmer.getSolScale();
        
        //For each hb reconstructed track, refit using the same crosses, beta, momentum, and charge
        for(int i = 0; i < rechbTrackBank.rows(); i++){
            int trackIndex = rechbTrackBank.getShort("index", i);
            int particleIndex = rechbTrackBank.getShort("pindex", i);
            
            //Get cross positions and momentum unit vectors
            Point3D[] crossPos = new Point3D[3];
            Vector3D[] crossMom = new Vector3D[3];
            short[] crossIdArr = new short[3];
            for(int j = 0; j < 3; j++){
                short crossId = hbTrackBank.getShort("Cross" + (j + 1) + "_ID", trackIndex);
                crossIdArr[j] = crossId;
                for(int k = 0; k < hbCrossBank.rows(); k++){
                    if(hbCrossBank.getShort("id", k) == crossId){
                        crossPos[j] = new Point3D(hbCrossBank.getFloat("x", k), hbCrossBank.getFloat("y", k), hbCrossBank.getFloat("z", k));
                        crossMom[j] = new Vector3D(hbCrossBank.getFloat("ux", k), hbCrossBank.getFloat("uy", k), hbCrossBank.getFloat("uz", k));
                        break;
                    }
                }
            }
            
            //Get particle beta, and charge
            double beta = rechbParticleBank.getFloat("beta", particleIndex);
            double gamma = Math.sqrt(1.0 / (1 - beta * beta));
            double charge = rechbParticleBank.getByte("charge", particleIndex);
          //System.out.println("Beta: " + beta);
            
            //Calculate momentum
            Vector3D mom = calculateMomentum(crossPos);
            if(mom == null){
                continue;
            }
            System.out.print("Momentum: ");
            System.out.println(mom.mag() * 3.0 / 1000.0);
            
            //Convert into sector coordinate system
            mom = crs.getCoordsInSector(mom.x(), mom.y(), mom.z()).toVector3D();
            Point3D torEntrance = crs.getCoordsInSector(crossPos[0].x(), crossPos[0].y(), crossPos[0].z());
            
            //Assume straight line from first cross to entrance of solenoid
            double t = (solStopZ - torEntrance.z()) / mom.z();
            Point3D solEntrance = new Point3D(torEntrance.x() + mom.x() * t, torEntrance.y() + mom.y() * t, solStopZ);
            System.out.print("Solenoid Entrance: ");
            System.out.println(solEntrance);
            
            //Calculate radius of helix
            double radiusHelix = Math.sqrt(mom.x() * mom.x() + mom.y() * mom.y()) / 5.0;
            System.out.println("Helix Rad: " + radiusHelix);
            
            //Calculate the central axis of the helix in x,y
            Vector3D momPlane = new Vector3D(mom.x(), mom.y(), 0.0);
            Vector3D radiusVec = momPlane.cross(new Vector3D(0.0, 0.0, 1.0)).asUnit();
            radiusVec.scale(-1.0 * radiusHelix * charge);
          //System.out.print("RadVec: ");
          //System.out.println(radiusVec);
            Point3D centralAxis = solEntrance.toVector3D().add(radiusVec).toPoint3D();
            centralAxis.setZ(0.0);
            System.out.print("Central Axis: ");
            System.out.println(centralAxis);
            
            //Calculate the doca between the raster position and the cylinder defined by the helix. Also calculate the doca position in x,y
            Point3D rasterPos = new Point3D(rasterX, rasterY, 0.0);
            Vector3D docaVec = centralAxis.vectorTo(rasterPos).asUnit();
            docaVec.scale(radiusHelix);
            Point3D docaPos = centralAxis.toVector3D().add(docaVec).toPoint3D();
            double doca = docaPos.vectorTo(rasterPos).mag();
            
            //Calculate the doca z coordinate
            //Vector3D initVec = centralAxis.vectorTo(solEntrance);
            //double orbitTime = (2.0 * Math.PI * radiusHelix) / (beta * Constants.SPEEDLIGHT * Math.pow(10.0, 9.0));
            //t = (Math.acos((docaPos.x() - centralAxis.x()) / radiusHelix) - Math.atan(initVec.y() / initVec.x())) * orbitTime;
            //System.out.println("tx = " + t);
            //t = (Math.asin((docaPos.y() - centralAxis.y()) / radiusHelix) - Math.atan(initVec.y() / initVec.x())) * orbitTime;
            //System.out.println("ty = " + t);
            //double vZ = -1.0 * beta * Constants.SPEEDLIGHT * Math.pow(10.0, 9.0) * mom.asUnit().z();
            //docaPos.setZ(vZ * t + solStopZ);
            //System.out.print("DocaPos: ");
            //System.out.println(docaPos);
            
            //Write to bank
            rbBank.setShort("id", i, hbTrackBank.getShort("id", i));
            rbBank.setShort("status", i, (short)0);
            rbBank.setByte("sector", i, hbTrackBank.getByte("sector", i));
            rbBank.setFloat("c1_x", i, (float)-1.0);
            rbBank.setFloat("c1_y", i, (float)-1.0);
            rbBank.setFloat("c1_z", i, (float)-1.0);
            rbBank.setFloat("c1_ux", i,(float)-1.0);
            rbBank.setFloat("c1_uy", i, (float)-1.0);
            rbBank.setFloat("c1_uz", i, (float)-1.0);
            rbBank.setFloat("c1_x", i, (float)-1.0);
            rbBank.setFloat("c3_y", i, (float)-1.0);
            rbBank.setFloat("c3_z", i, (float)-1.0);
            rbBank.setFloat("c3_ux", i, (float)-1.0);
            rbBank.setFloat("c3_uy", i, (float)-1.0);
            rbBank.setFloat("c3_uz", i, (float)-1.0);
            rbBank.setFloat("t1_x", i, (float)-1.0);
            rbBank.setFloat("t1_y", i, (float)-1.0);
            rbBank.setFloat("t1_z", i, (float)-1.0);
            rbBank.setFloat("t1_px", i, (float)-1.0);
            rbBank.setFloat("t1_py", i, (float)-1.0);
            rbBank.setFloat("t1_pz", i, (float)-1.0);
            rbBank.setFloat("Vtx0_x", i, (float)docaPos.x());
            rbBank.setFloat("Vtx0_y", i, (float)docaPos.y());
            rbBank.setFloat("Vtx0_z", i, (float)docaPos.z());
            rbBank.setFloat("p0_x", i, (float)-1.0);
            rbBank.setFloat("p0_y", i, (float)-1.0);
            rbBank.setFloat("p0_z", i, (float)-1.0);
            rbBank.setShort("Cross1_ID", i, crossIdArr[0]);
            rbBank.setShort("Cross2_ID", i, crossIdArr[1]);
            rbBank.setShort("Cross3_ID", i, crossIdArr[2]);
            rbBank.setByte("q", i, (byte)charge);
            rbBank.setFloat("pathlength", i, (float)-1.0);
            rbBank.setFloat("chi2", i, (float)-1.0);
            rbBank.setShort("ndf", i, (short)0);
            rbBank.setFloat("doca", i, (float)doca);
        }
        return rbBank;
    }
    
    /**
     * 
     * @param crossPos
     * @return the momentum vector at the first cross position
     */
    private Vector3D calculateMomentum(Point3D[] crossPos){
        Swim swim = new Swim();//For probing the B-Field
        
        //Ensure that there are 3 crosses
        if(crossPos[0] == null || crossPos[1] == null || crossPos[2] == null){
            return null;
        }
        
        //Define parabolas in x and y as functions of z
        double[] parabolaX = findParabolaParameters(crossPos[0].z(), crossPos[0].x(), crossPos[1].z(), crossPos[1].x(), crossPos[2].z(), crossPos[2].x());
        double[] parabolaY = findParabolaParameters(crossPos[0].z(), crossPos[0].y(), crossPos[1].z(), crossPos[1].y(), crossPos[2].z(), crossPos[2].y());
        
        //Calculate the momentum at the middle cross
        //Find radius of curvature
        double radiusX = findParabolaRadiusOfCurvature(parabolaX, crossPos[1]);
        double radiusY = findParabolaRadiusOfCurvature(parabolaY, crossPos[1]);
        double radMag = Math.sqrt(radiusX * radiusX + radiusY * radiusY);

        //Get B Field Magnitude at the ith cross
        float[] bField = new float[3];
        swim.Bfield(1, crossPos[1].x(), crossPos[1].y(), crossPos[1].z(), bField);
        double bFieldMag = Math.sqrt(bField[0] * bField[0] + bField[1] * bField[1] + bField[2] * bField[2]);

        //Calculate momentum magnitude (GeV)
        double momMag = radMag * bFieldMag;
        
        //Momentum vector of parabola at cross 1
        Vector3D mom = new Vector3D(2.0 * parabolaX[0] * crossPos[0].z() + parabolaX[1], 2.0 * parabolaY[0] * crossPos[0].z() + parabolaY[1], 1.0).asUnit();
        mom.scale(momMag);
        
        return mom;
    }
    
    /**
     * 
     * @return      The parameters [A, B, C] for y = Ax^2 + Bx + C
     */
    private double[] findParabolaParameters(double x1, double y1, double x2, double y2, double x3, double y3){
        double[] params = new double[3];
        double denominator = (x1 - x2) * (x1 - x3) * (x2 - x3);
        params[0] = (x1 * (y3 - y2) + x2 * (y1 - y3) + x3 * (y2 - y1)) / denominator;//A
        params[1] = (x1 * x1 * (y2 - y3) + x2 * x2 * (y3 - y1) + x3 * x3 * (y1 - y2)) / denominator;//B
        params[2] = (x1 * x2 * (x1 - x2) * y3 + x2 * x3 * (x2 - x3) * y1 + x3 * x1 * (x3 - x1) * y2) / denominator;//C
        return params;
    }
    
    /**
     * 
     * @param parabola
     * @param cross
     * @return 
     */
    private double findParabolaRadiusOfCurvature(double[] parabola, Point3D cross){
        return Math.abs(Math.pow(1.0 + Math.pow(2.0 * parabola[0] * cross.z() + parabola[1], 2.0), 1.5) / (2.0 * parabola[0]));
    }
}