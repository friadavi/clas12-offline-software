package org.jlab.clas.viz.data;

import java.util.Arrays;
import javax.swing.JOptionPane;
import org.jlab.clas.viz.reco.PathSimulation;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.io.hipo.HipoDataEvent;
import org.jlab.clas.pdg.PDGDatabase;
import org.jlab.clas.viz.ui.DisplayTreeModel;
import org.jlab.clas.viz.ui.DisplayTreeNode;
import org.jlab.io.base.DataBank;
import org.jlab.io.hipo.HipoDataDictionary;
import org.jlab.jnp.hipo.schema.Schema;
import org.jlab.jnp.hipo.schema.SchemaFactory;

/**
 * This class controls reading from Hipo files.
 * 
 * @author friant
 */
public class DataReader {
    private final HipoDataSource reader;
    private final SchemaFactory fact;
    private DisplayTreeModel model;
    private int eventCount;
    private HipoDataEvent currentEvent;
    private int currentEventIndex;
    private boolean isOpen;
    
    /**
     * Constructor, note that the tree model is not set here. It must be set by the setTreeModel method.
     */
    public DataReader(){
        reader = new HipoDataSource();
        fact = new SchemaFactory();
        fact.initFromDirectory("CLAS12DIR", "etc/bankdefs/hipo");
        currentEvent = null;
        currentEventIndex = -1;
        isOpen = false;
    }
    
    /**
     * Attempt to open a hipo file. Alerts the user if something fails.
     * Feeds the first event's information into the rest of the program.
     *  
     * @param file
     */
    public void open(String file){
        try{
            if(isOpen){
                close();
            }
            reader.open(file);
            eventCount = reader.getSize();
            isOpen = true;
            getEvent(0);
        }
        catch(Exception e){
            JOptionPane.showMessageDialog(null, e, "IO Error", JOptionPane.WARNING_MESSAGE);
            close();
        }
    }
    
    /**
     * Close the hipo file and  clear the tree.
     */
    public void close(){
        if(isOpen){
            reader.close();
            currentEvent = null;
            currentEventIndex = -1;
            isOpen = false;
            model.setRoot(new DisplayTreeNode());
            model.reload();
        }
    }
    
    /**
     * Returns true if there is a hipo file open, false otherwise.
     * 
     * @return 
     */
    public boolean isOpen(){
        return isOpen;
    }
    
    /**
     * Feed information from the next event into the system.
     */
    public void getNextEvent(){
        getEvent(currentEventIndex + 1);
    }
    
    /**
     * Feed information from the previous event into the system.
     */
    public void getPrevEvent(){
        getEvent(currentEventIndex - 1);
    }
    
    /**
     * Feed information from the nth event into the system.
     * 
     * @param n 
     */
    public void getEvent(int n){
        if(!isOpen){
            return;
        }
        if(n != currentEventIndex){
            currentEventIndex = n;
            currentEvent = (HipoDataEvent)(reader.gotoEvent(n));
        }
        if(n > -1 && n < reader.getSize()){
            if(!currentEvent.hasBank("RasterBasedTrkg::RBHits")){
                currentEvent.initDictionary(fact);
            }
            model.setRoot(new DisplayTreeNode("Event " + Integer.toString(currentEventIndex)));
            fillDisplayData(currentEvent);
            fillTree(currentEvent);
            model.reload();
        }
    }
    
    /**
     * 
     * @return 
     */
    public HipoDataEvent getCurrentEvent(){
        return currentEvent;
    }
    
    /**
     * 
     * @return 
     */
    public int getEventCount(){
        return eventCount;
    }
    
    /**
     * 
     * @return 
     */
    public int getCurrentEventIndex(){
        return currentEventIndex;
    }
    
    /**
     * Sets the treeModel that this reader feeds info to.
     * 
     * @param _model
     */
    public void setTreeModel(DisplayTreeModel _model){
        model = _model;
    }
    
    /**
     * 
     * @param event
     */
    private void fillTree(HipoDataEvent event){
        //event.show();
        
        DisplayTreeNode root = model.getRoot();
        int count = 0;
        
        //Simulated Particles
        if(event.hasBank("MC::Particle")){
            DataBank bank = event.getBank("MC::Particle");
            DisplayTreeNode node = new DisplayTreeNode("Simulated Particles");
            for(int i = 0; i < bank.rows(); i++){
                DisplayTreeNode particle = new DisplayTreeNode(count, PDGDatabase.getParticleById(bank.getInt("pid", i)).name());
                particle.addChild(new DisplayTreeNode(count, "vx: " + bank.getFloat("vx", i)));
                particle.addChild(new DisplayTreeNode(count, "vy: " + bank.getFloat("vy", i)));
                particle.addChild(new DisplayTreeNode(count, "vz: " + bank.getFloat("vz", i)));
                particle.addChild(new DisplayTreeNode(count, "px: " + bank.getFloat("px", i)));
                particle.addChild(new DisplayTreeNode(count, "py: " + bank.getFloat("py", i)));
                particle.addChild(new DisplayTreeNode(count, "pz: " + bank.getFloat("pz", i)));
                node.addChild(particle);
                count++;
            }
            root.addChild(node);
        }
        
        //Central Vertex Tracker Based Reconstruction
            //TODO
        
        //Drift Chamber Hit Based Reconstruction
        if(event.hasBank("HitBasedTrkg::HBTracks")){
            DataBank bank = event.getBank("HitBasedTrkg::HBTracks");
            DisplayTreeNode node = new DisplayTreeNode("DCHB Reconstruction");
            for(int i = 0; i < bank.rows(); i++){
                DisplayTreeNode track = new DisplayTreeNode(count, "Track " + i);
                track.addChild(new DisplayTreeNode(count, "vx: " + bank.getFloat("Vtx0_x", i)));
                track.addChild(new DisplayTreeNode(count, "vy: " + bank.getFloat("Vtx0_y", i)));
                track.addChild(new DisplayTreeNode(count, "vz: " + bank.getFloat("Vtx0_z", i)));
                track.addChild(new DisplayTreeNode(count, "px: " + bank.getFloat("p0_x", i)));
                track.addChild(new DisplayTreeNode(count, "py: " + bank.getFloat("p0_y", i)));
                track.addChild(new DisplayTreeNode(count, "pz: " + bank.getFloat("p0_z", i)));
                track.addChild(new DisplayTreeNode(count, "q: " + bank.getInt("q", i)));
                track.addChild(new DisplayTreeNode(count, "chi2: " + bank.getFloat("chi2", i)));
                node.addChild(track);
                count++;
            }
            root.addChild(node);
        }
        
        //Drift Chamber Time Based Reconstruction
        if(event.hasBank("TimeBasedTrkg::TBTracks")){
            DataBank bank = event.getBank("TimeBasedTrkg::TBTracks");
            DisplayTreeNode node = new DisplayTreeNode("DCTB Reconstruction");
            for(int i = 0; i < bank.rows(); i++){
                DisplayTreeNode track = new DisplayTreeNode(count, "Track " + i);
                track.addChild(new DisplayTreeNode(count, "vx: " + bank.getFloat("Vtx0_x", i)));
                track.addChild(new DisplayTreeNode(count, "vy: " + bank.getFloat("Vtx0_y", i)));
                track.addChild(new DisplayTreeNode(count, "vz: " + bank.getFloat("Vtx0_z", i)));
                track.addChild(new DisplayTreeNode(count, "px: " + bank.getFloat("p0_x", i)));
                track.addChild(new DisplayTreeNode(count, "py: " + bank.getFloat("p0_y", i)));
                track.addChild(new DisplayTreeNode(count, "pz: " + bank.getFloat("p0_z", i)));
                track.addChild(new DisplayTreeNode(count, "q: " + bank.getInt("q", i)));
                track.addChild(new DisplayTreeNode(count, "chi2: " + bank.getFloat("chi2", i)));
                node.addChild(track);
                count++;
            }
            root.addChild(node);
        }
        
        //Drift Chamber Raster Based Reconstruction
        if(event.hasBank("RasterBasedTrkg::RBTracks")){
            DataBank bank = event.getBank("RasterBasedTrkg::RBTracks");
            DisplayTreeNode node = new DisplayTreeNode("DCRB Reconstruction");
            for(int i = 0; i < bank.rows(); i++){
                DisplayTreeNode track = new DisplayTreeNode(count, "Track " + i);
                track.addChild(new DisplayTreeNode(count, "vx: " + bank.getFloat("Vtx0_x", i)));
                track.addChild(new DisplayTreeNode(count, "vy: " + bank.getFloat("Vtx0_y", i)));
                track.addChild(new DisplayTreeNode(count, "vz: " + bank.getFloat("Vtx0_z", i)));
                track.addChild(new DisplayTreeNode(count, "px: " + bank.getFloat("p0_x", i)));
                track.addChild(new DisplayTreeNode(count, "py: " + bank.getFloat("p0_y", i)));
                track.addChild(new DisplayTreeNode(count, "pz: " + bank.getFloat("p0_z", i)));
                track.addChild(new DisplayTreeNode(count, "q: " + bank.getInt("q", i)));
                track.addChild(new DisplayTreeNode(count, "chi2: " + bank.getFloat("chi2", i)));
                track.addChild(new DisplayTreeNode(count, "doca: " + bank.getFloat("doca", i)));
                node.addChild(track);
                count++;
            }
            root.addChild(node);
        }
        model.reload();
    }
    
    /**
     * 
     * @param event
     */
    private void fillDisplayData(HipoDataEvent event){
        //event.show();
        int numParticles = event.getBank("MC::Particle").rows();
        int numTracks = event.getBank("HitBasedTrkg::HBTracks").rows();
        DisplayData.initialize(numParticles + numTracks);
        
        event.getBank("MC::Particle").show();
        for(int i = 0; i < numParticles; i++){
            DisplayData.setReal(i, false);
            DisplayData.setCharge(i, PDGDatabase.getParticleById(event.getBank("MC::Particle").getInt("pid", i)).charge());
            
            float[] posVec = {event.getBank("MC::Particle").getFloat("vx", i),
                              event.getBank("MC::Particle").getFloat("vy", i),
                              event.getBank("MC::Particle").getFloat("vz", i)};
            float[] momVec = {event.getBank("MC::Particle").getFloat("px", i),
                              event.getBank("MC::Particle").getFloat("py", i),
                              event.getBank("MC::Particle").getFloat("pz", i)};
            DisplayData.addTrack(PathSimulation.simulate(event.getBank("MC::Particle").getInt("pid", i), posVec, momVec));
        }
        event.getBank("HitBasedTrkg::HBTracks").show();
        event.getBank("TimeBasedTrkg::TBCrosses").show();
        event.getBank("RasterBasedTrkg::RBTracks").show();
        for(int i = numParticles; i < numParticles + numTracks; i++){
            DisplayData.setReal(i, true);
            DisplayData.setCharge(i, event.getBank("HitBasedTrkg::HBTracks").getInt("q", i - numParticles));
            int tid = event.getBank("HitBasedTrkg::HBTracks").getShort("id", i - numParticles);
            int sector = event.getBank("HitBasedTrkg::HBTracks").getByte("sector", i - numParticles);
            int count = 0;
            for(int j = 0; j < event.getBank("TimeBasedTrkg::Trajectory").rows(); j++){
                if(tid == event.getBank("TimeBasedTrkg::Trajectory").getShort("tid", j)){
                    count++;
                }
            }
            
            float[] dataArray = new float[(1 + count) * 4];
            count = 0;
            dataArray[count + 0] = event.getBank("HitBasedTrkg::HBTracks").getFloat("Vtx0_x", i - numParticles);
            dataArray[count + 1] = event.getBank("HitBasedTrkg::HBTracks").getFloat("Vtx0_y", i - numParticles);
            dataArray[count + 2] = event.getBank("HitBasedTrkg::HBTracks").getFloat("Vtx0_z", i - numParticles);
            dataArray[count + 3] = 1.0f;
            count++;
            for(int j = 0; j < event.getBank("TimeBasedTrkg::Trajectory").rows(); j++){
                if(tid == event.getBank("TimeBasedTrkg::Trajectory").getShort("tid", j)){
                    dataArray[count * 4 + 0] = event.getBank("TimeBasedTrkg::Trajectory").getFloat("x", j);
                    dataArray[count * 4 + 1] = event.getBank("TimeBasedTrkg::Trajectory").getFloat("y", j);
                    dataArray[count * 4 + 2] = event.getBank("TimeBasedTrkg::Trajectory").getFloat("z", j);
                    dataArray[count * 4 + 3] = 1.0f;
                    
                    PathSimulation.sectorToClas(sector, dataArray, count * 4);
                    count++;
                }
            }
            DisplayData.addTrack(dataArray);
        }
        DisplayData.updateColors();
    }
}