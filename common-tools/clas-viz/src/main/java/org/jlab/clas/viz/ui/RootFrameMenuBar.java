package org.jlab.clas.viz.ui;

import java.awt.event.ActionEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.jlab.clas.viz.data.DataReader;
import org.jlab.clas.viz.reco.ReconstructionCalls;

/**
 *
 * @author friant
 */
public final class RootFrameMenuBar extends JMenuBar{
    DataReader reader;
    DisplayPanel display;
    
    JMenu fileMenu;
    JMenuItem openItem;
    JMenuItem writeItem;
    JMenuItem closeItem;
    JMenu viewMenu;
    JMenuItem lateralItem;
    JMenuItem longitudinalItem;
    JMenuItem resetItem;
    JMenu recoMenu;
    JMenuItem cvtItem;
    JMenuItem dchbItem;
    JMenuItem dctbItem;
    JMenuItem dcrbItem;
    
    /*
     * 
     */
    public RootFrameMenuBar(DataReader _reader, DisplayPanel _display){
        reader = _reader;
        display = _display;
        
        fileMenu = new JMenu();
        fileMenu.setText("File");
        
        openItem = new JMenuItem();
        openItem.setText("Open");
        
        writeItem = new JMenuItem();
        writeItem.setText("Write");
        
        closeItem = new JMenuItem();
        closeItem.setText("Close");
        
        viewMenu = new JMenu();
        viewMenu.setText("View");
        
        lateralItem = new JMenuItem();
        lateralItem.setText("Lateral");
        
        longitudinalItem = new JMenuItem();
        longitudinalItem.setText("Longitudinal");
        
        resetItem = new JMenuItem();
        resetItem.setText("Reset");
        
        recoMenu = new JMenu();
        recoMenu.setText("Reco");
        
        cvtItem = new JMenuItem();
        cvtItem.setText("CVT");
        
        dchbItem = new JMenuItem();
        dchbItem.setText("DCHB");
        
        dctbItem = new JMenuItem();
        dctbItem.setText("DCTB");
        
        dcrbItem = new JMenuItem();
        dcrbItem.setText("DCRB");
        
        build();
        addListeners();
    }
    
    /**
     * 
     */
    private void build(){
        fileMenu.add(openItem);
        fileMenu.add(writeItem);
        fileMenu.add(closeItem);
        this.add(fileMenu);
        
        viewMenu.add(lateralItem);
        viewMenu.add(longitudinalItem);
        viewMenu.add(resetItem);
        this.add(viewMenu);
        
        recoMenu.add(cvtItem);
        recoMenu.add(dchbItem);
        recoMenu.add(dctbItem);
        recoMenu.add(dcrbItem);
        this.add(recoMenu);
    }
    
    /**
     * 
     */
    private void addListeners(){
        //File > Open
        openItem.addActionListener((ActionEvent e) -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setFileFilter(new FileNameExtensionFilter("Hipo Files", "hipo"));
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.showDialog(null, "Open");
            if(chooser.getSelectedFile() != null){
                reader.open(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        
        //File > Write
        writeItem.addActionListener((ActionEvent e) -> {
            JOptionPane.showMessageDialog(null, "Not Yet Implemented", "Info", JOptionPane.INFORMATION_MESSAGE);
        });
        
        //File > Close
        closeItem.addActionListener((ActionEvent e) -> {
            reader.close();
        });
        
        //Reco > DCHB
        dchbItem.addActionListener((ActionEvent e) -> {
            int current = reader.getCurrentEventIndex();
            
            //Simple class which only exists in this scope.
            class Task extends SwingWorker<Void, Void>{
                /**
                 * 
                 * @return
                 * @throws Exception
                 */
                @Override
                protected Void doInBackground() throws Exception {
                    ReconstructionCalls.recoEventDCHB();
                    return null;
                }
                
                /**
                 *
                 */
                @Override
                protected void done(){
                    fileMenu.setEnabled(true);
                    viewMenu.setEnabled(true);
                    recoMenu.setEnabled(true);
                    reader.getEvent(current);
                }
            }
            
            //Method Body
            try{
                fileMenu.setEnabled(false);
                viewMenu.setEnabled(false);
                recoMenu.setEnabled(false);
                Task task = new Task();
                task.execute();
            }
            catch(Exception ex){
                JOptionPane.showMessageDialog(null, ex, "Reconstruction Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        //Reco > DCTB
        dctbItem.addActionListener((ActionEvent e) -> {
            int current = reader.getCurrentEventIndex();
            
            //Simple class which only exists in this scope.
            class Task extends SwingWorker<Void, Void>{
                /**
                 * 
                 * @return
                 * @throws Exception
                 */
                @Override
                protected Void doInBackground() throws Exception {
                    ReconstructionCalls.recoEventDCTB();
                    return null;
                }
                
                /**
                 *
                 */
                @Override
                protected void done(){
                    fileMenu.setEnabled(true);
                    viewMenu.setEnabled(true);
                    recoMenu.setEnabled(true);
                    reader.getEvent(current);
                }
            }
            
            //Method Body
            try{
                fileMenu.setEnabled(false);
                viewMenu.setEnabled(false);
                recoMenu.setEnabled(false);
                Task task = new Task();
                task.execute();
            }
            catch(Exception ex){
                JOptionPane.showMessageDialog(null, ex, "Reconstruction Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        //Reco > DCRB
        dcrbItem.addActionListener((ActionEvent e) -> {
            int current = reader.getCurrentEventIndex();
            
            //Simple class which only exists in this scope.
            class Task extends SwingWorker<Void, Void>{
                /**
                 * 
                 * @return
                 * @throws Exception
                 */
                @Override
                protected Void doInBackground() throws Exception {
                    ReconstructionCalls.recoEventDCRB();
                    return null;
                }
                
                /**
                 *
                 */
                @Override
                protected void done(){
                    fileMenu.setEnabled(true);
                    viewMenu.setEnabled(true);
                    recoMenu.setEnabled(true);
                    reader.getEvent(current);
                }
            }
            
            //Method Body
            try{
                fileMenu.setEnabled(false);
                viewMenu.setEnabled(false);
                recoMenu.setEnabled(false);
                Task task = new Task();
                task.execute();
            }
            catch(Exception ex){
                JOptionPane.showMessageDialog(null, ex, "Reconstruction Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        //Reco > CVT
        cvtItem.addActionListener((ActionEvent e) -> {
            int current = reader.getCurrentEventIndex();
            
            //Simple worker class which only exists in this scope.
            class Task extends SwingWorker<Void, Void>{
                /**
                 * 
                 * @return
                 * @throws Exception
                 */
                @Override
                protected Void doInBackground() throws Exception {
                    ReconstructionCalls.recoEventCVT();
                    return null;
                }
                
                /**
                 *
                 */
                @Override
                protected void done(){
                    fileMenu.setEnabled(true);
                    recoMenu.setEnabled(true);
                }
            }
            
            //Method Body
            try{
                fileMenu.setEnabled(false);
                recoMenu.setEnabled(false);
                Task task = new Task();
                task.execute();
            }
            catch(Exception ex){
                JOptionPane.showMessageDialog(null, ex, "Reconstruction Error", JOptionPane.ERROR_MESSAGE);
            }
            reader.getEvent(current);
        });
        
        //View > Lateral
        lateralItem.addActionListener((ActionEvent e) -> {
            display.setActiveCamera(0);
            display.repaint();
        });
        
        //View > Longitudinal
        longitudinalItem.addActionListener((ActionEvent e) -> {
            display.setActiveCamera(1);
            display.repaint();
        });
        
        //View > Reset
        resetItem.addActionListener((ActionEvent e) -> {
            display.resetCamera();
            display.repaint();
        });
    }
}
