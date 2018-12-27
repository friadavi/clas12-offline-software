package org.jlab.clas.viz.ui;

import java.awt.event.ActionEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import org.jlab.clas.viz.data.DataReader;
import org.jlab.clas.viz.reco.ReconstructionCalls;

/**
 *
 * @author friant
 */
public class RootFrameMenuBar extends JMenuBar{
    DataReader reader;
    
    JMenu fileMenu;
    JMenuItem openItem;
    JMenuItem writeItem;
    JMenuItem closeItem;
    JMenu recoMenu;
    JMenuItem cvtItem;
    JMenuItem dcItem;
    
    
    
    /*
     * 
     */
    public RootFrameMenuBar(DataReader _reader){
        reader = _reader;
        
        fileMenu = new JMenu();
        fileMenu.setText("File");
        
        openItem = new JMenuItem();
        openItem.setText("Open");
        
        writeItem = new JMenuItem();
        writeItem.setText("Write");
        
        closeItem = new JMenuItem();
        closeItem.setText("Close");
        
        recoMenu = new JMenu();
        recoMenu.setText("Reco");
        
        cvtItem = new JMenuItem();
        cvtItem.setText("CVT");
        
        dcItem = new JMenuItem();
        dcItem.setText("DC");
        
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
        
        recoMenu.add(cvtItem);
        recoMenu.add(dcItem);
        
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
        
        //Reco > DC
        dcItem.addActionListener((ActionEvent e) -> {
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
                    ReconstructionCalls.recoEventDC();
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
        
        //Reco > CVT
        cvtItem.addActionListener((ActionEvent e) -> {
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
    }
}
