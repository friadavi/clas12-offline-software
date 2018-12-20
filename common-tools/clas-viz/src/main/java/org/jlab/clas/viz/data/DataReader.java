package org.jlab.clas.viz.data;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.jlab.clas.viz.sim.PathSim;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.io.hipo.HipoDataEvent;
import org.jlab.clas.pdg.PDGDatabase;
import org.jlab.io.base.DataBank;

/**
 * This class controls reading from Hipo files.
 * 
 * @author friant
 */
public class DataReader {
    private final HipoDataSource reader;
    private DefaultTreeModel model;
    private int eventCount;
    private int currentEvent;
    private boolean isOpen;
    
    /**
     * Constructor, note that the tree model is not set here. It must be set by the setTreeModel method.
     */
    public DataReader(){
        reader = new HipoDataSource();
        currentEvent = -1;
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
            isOpen = false;
            model.setRoot(null);
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
        getEvent(currentEvent + 1);
    }
    
    /**
     * Feed information from the previous event into the system.
     */
    public void getPrevEvent(){
        getEvent(currentEvent - 1);
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
        if(n == currentEvent){
            return;
        }
        if(n > -1 && n < reader.getSize()){
            currentEvent = n;
            HipoDataEvent event = (HipoDataEvent)(reader.gotoEvent(n));
            model.setRoot(new DefaultMutableTreeNode("Event " + Integer.toString(currentEvent)));
            fillTree(event);
            fillDisplayData(event);
        }
    }
    
    /**
     * 
     * @param n
     * @return 
     */
    public HipoDataEvent getHipoEvent(int n){
        return (HipoDataEvent)reader.gotoEvent(n);
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
    public int getCurrentEvent(){
        return currentEvent;
    }
    
    /**
     * Sets the treeModel that this reader feeds info to.
     * 
     * @param _model
     */
    public void setTreeModel(DefaultTreeModel _model){
        model = _model;
    }
    
    /**
     * 
     * @param event
     */
    public void fillTree(HipoDataEvent event){
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
        DataBank bank = event.getBank("MC::Particle");
        for(int i = 0; i < bank.rows(); i++){
            DefaultMutableTreeNode particle = new DefaultMutableTreeNode("Simulated: " + PDGDatabase.getParticleById(bank.getInt("pid", i)).name());
            particle.add(new DefaultMutableTreeNode("px: " + bank.getFloat("px", i)));
            particle.add(new DefaultMutableTreeNode("py: " + bank.getFloat("py", i)));
            particle.add(new DefaultMutableTreeNode("pz: " + bank.getFloat("pz", i)));
            particle.add(new DefaultMutableTreeNode("vx: " + bank.getFloat("vx", i)));
            particle.add(new DefaultMutableTreeNode("vy: " + bank.getFloat("vy", i)));
            particle.add(new DefaultMutableTreeNode("vz: " + bank.getFloat("vz", i)));
            particle.add(new DefaultMutableTreeNode("vt: " + bank.getFloat("vt", i)));
            root.add(particle);
        }
        bank = event.getBank("HitBasedTrkg::HBTracks");
        for(int i = 0; i < bank.rows(); i++){
            DefaultMutableTreeNode track = new DefaultMutableTreeNode("Reconstructed: Track " + i);
            track.add(new DefaultMutableTreeNode("px: " + bank.getFloat("p0_x", i)));
            track.add(new DefaultMutableTreeNode("py: " + bank.getFloat("p0_y", i)));
            track.add(new DefaultMutableTreeNode("pz: " + bank.getFloat("p0_z", i)));
            track.add(new DefaultMutableTreeNode("vx: " + bank.getFloat("Vtx0_x", i)));
            track.add(new DefaultMutableTreeNode("vy: " + bank.getFloat("Vtx0_y", i)));
            track.add(new DefaultMutableTreeNode("vz: " + bank.getFloat("Vtx0_z", i)));
            track.add(new DefaultMutableTreeNode("q: " + bank.getInt("q", i)));
            track.add(new DefaultMutableTreeNode("chi2: " + bank.getFloat("chi2", i)));
            root.add(track);
        }
        //model.reload();
    }
    
    /**
     * 
     * @param event
     */
    public void fillDisplayData(HipoDataEvent event){
        //event.show();
        //event.getBank("HitBasedTrkg::HBTracks").show();
        //event.getBank("TimeBasedTrkg::Trajectory").show();
        
        int numParticles = event.getBank("MC::Particle").rows();
        int numTracks = event.getBank("HitBasedTrkg::HBTracks").rows();
        DisplayData.initialize(numParticles + numTracks);
        
        for(int i = 0; i < numParticles; i++){
            DisplayData.setReal(i, false);
            DisplayData.setCharge(i, PDGDatabase.getParticleById(event.getBank("MC::Particle").getInt("pid", i)).charge());
            
            float[] posVec = {event.getBank("MC::Particle").getFloat("vx", i),
                              event.getBank("MC::Particle").getFloat("vy", i),
                              event.getBank("MC::Particle").getFloat("vz", i)};
            float[] momVec = {event.getBank("MC::Particle").getFloat("px", i),
                              event.getBank("MC::Particle").getFloat("py", i),
                              event.getBank("MC::Particle").getFloat("pz", i)};
            DisplayData.addTrack(PathSim.simulate(event.getBank("MC::Particle").getInt("pid", i), posVec, momVec));
        }
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
                    
                    PathSim.sectorToClas(sector, dataArray, count * 4);
                    count++;
                }
                DisplayData.addTrack(dataArray);
            }
        }
        DisplayData.updateColors();
    }
}