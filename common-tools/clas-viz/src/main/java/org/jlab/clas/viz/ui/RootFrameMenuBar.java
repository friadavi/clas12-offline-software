package org.jlab.clas.viz.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;
import org.jlab.clas.viz.data.DataReader;
import org.jlab.rec.cvt.services.CVTReconstruction;
import org.jlab.service.dc.DCHBEngine;
import org.jlab.service.dc.DCTBEngine;

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
    JMenuItem dcItem;
    JMenuItem cvtItem;
    
    
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
        
        dcItem = new JMenuItem();
        dcItem.setText("DC");
        
        cvtItem = new JMenuItem();
        cvtItem.setText("CVT");
        
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
        
        recoMenu.add(dcItem);
        recoMenu.add(cvtItem);
        
        this.add(recoMenu);
    }
    
    /**
     * 
     */
    private void addListeners(){
        //File > Open
        openItem.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setAcceptAllFileFilterUsed(false);
                chooser.setFileFilter(new FileNameExtensionFilter("Hipo Files", "hipo"));
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.showDialog(null, "Open");
                if(chooser.getSelectedFile() != null){
                    reader.open(chooser.getSelectedFile().getAbsolutePath());
                }
            }
        });
        
        //File > Write
        writeItem.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Not Yet Implemented", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        //File > Close
        closeItem.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                reader.close();
            }
        });
        
        //Reco > DC
        dcItem.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                DCHBEngine dchb = new DCHBEngine();
                DCTBEngine dctb = new DCTBEngine();
                
                int current = reader.getCurrentEvent();
                int events = reader.getEventCount();
                
                ProgressMonitor monitor = new ProgressMonitor(null, "Event Reconstruction Underway", "Progress: Initializing Reconstruction Engine", 0, events);
                monitor.setMillisToDecideToPopup(0);
                
                /**
                 * 
                 */
                class Task extends SwingWorker<Void, Void>{
                    /**
                     * 
                     * @return
                     * @throws Exception 
                     */
                    @Override
                    protected Void doInBackground() throws Exception {
                        monitor.setProgress(0);
                        
                        boolean success;
                        success = dchb.init();
                        if(!success){
                            JOptionPane.showMessageDialog(null, "DCHBEngine failed to initialize.", "Error", JOptionPane.ERROR_MESSAGE);
                            return null;
                        }
                        success = dctb.init();
                        if(!success){
                            JOptionPane.showMessageDialog(null, "DCTBEngine failed to initialize.", "Error", JOptionPane.ERROR_MESSAGE);
                            return null;
                        }

                        for(int i = 0; i < events; i++){
                            dchb.processDataEvent(reader.getHipoEvent(i));
                            dctb.processDataEvent(reader.getHipoEvent(i));
                            monitor.setNote("Progress: " + String.valueOf(i + 1) + " / " + String.valueOf(events));
                            monitor.setProgress(i + 1);
                            if(monitor.isCanceled()){
                                break;
                            }
                        }
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
                monitor.close();
                reader.getEvent(current);
            }
        });
        
        //Reco > CVT
        cvtItem.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Not Yet Implemented", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }
}
