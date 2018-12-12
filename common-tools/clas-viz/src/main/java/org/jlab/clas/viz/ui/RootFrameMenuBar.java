package org.jlab.clas.viz.ui;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import org.jlab.clas.viz.data.DataReader;

/**
 *
 * @author friant
 */
public class RootFrameMenuBar extends JMenuBar{
    DataReader reader;
    JMenu fileMenu;
    JMenuItem openItem;
    JMenuItem closeItem;
    
    /*
     * 
     */
    public RootFrameMenuBar(DataReader _reader){
        reader = _reader;
        
        fileMenu = new JMenu();
        fileMenu.setLabel("File");
        
        openItem = new JMenuItem(new AbstractAction("open"){
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
        openItem.setLabel("Open");
        
        closeItem = new JMenuItem(new AbstractAction("close"){
            @Override
            public void actionPerformed(ActionEvent e) {
                reader.close();
            }
            
        });
        closeItem.setLabel("Close");
        
        fileMenu.add(openItem);
        fileMenu.add(closeItem);
        
        this.add(fileMenu);
    }
}
