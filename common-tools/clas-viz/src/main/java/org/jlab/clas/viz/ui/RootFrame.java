package org.jlab.clas.viz.ui;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.jlab.clas.viz.data.CameraData;
import org.jlab.clas.viz.data.DataReader;
import org.jlab.clas.viz.data.DisplayData;

/**
 *
 * @author friant
 */
public class RootFrame extends JFrame{
    DataReader reader;
    RootFrameMenuBar menuBar;
    JLayeredPane lPane;
    DisplayPanel graphicsPanel;
    ResizableJPanel controlsPanel;
    JPanel eventPanel;
    JButton prevButton;
    JTextField eventText;
    JButton nextButton;
    JScrollPane sPane;
    JTree tree;
    
    
    public RootFrame(DataReader _reader){
        CameraData.intitialize();
        reader = _reader;
        
        menuBar = new RootFrameMenuBar(reader);
        
        lPane = new JLayeredPane();
        
        graphicsPanel = new DisplayPanel(new GLCapabilities(GLProfile.get(GLProfile.GL3)));
        
        controlsPanel = new ResizableJPanel(ResizableJPanel.RIGHT);
        controlsPanel.setBounds(5, 5, 200, 549);
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));
        controlsPanel.setMinimumSize(new Dimension(200, 540));
        controlsPanel.setMaximumSize(new Dimension(800, 540));
        
        eventPanel = new JPanel();
        eventPanel.setLayout(new BoxLayout(eventPanel, BoxLayout.X_AXIS));
        
        prevButton = new JButton("<");
        prevButton.setMinimumSize(new Dimension(50, 50));
        prevButton.setMaximumSize(new Dimension(50, 50));
        
        eventText = new JTextField();
        eventText.setMinimumSize(new Dimension(100, 50));
        eventText.setMaximumSize(new Dimension(100, 50));
        
        nextButton = new JButton(">");
        nextButton.setMinimumSize(new Dimension(50, 50));
        nextButton.setMaximumSize(new Dimension(50, 50));
        
        sPane = new JScrollPane();
        sPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        tree = new JTree();
        
        reader.setTreeModel((DefaultTreeModel)tree.getModel());
        
        build();
        addListeners();
    }
    
    /**
     * 
     * 
     */
    private void build(){
        this.add(lPane);
        
        lPane.setLayer(graphicsPanel, 0);
        lPane.setLayer(controlsPanel, 1);
        lPane.add(graphicsPanel);
        lPane.add(controlsPanel);
        
        controlsPanel.add(eventPanel);
        
        eventPanel.add(prevButton);
        eventPanel.add(eventText);
        eventPanel.add(nextButton);
        
        controlsPanel.add(sPane);
        
        sPane.add(tree);
        sPane.setViewportView(tree);
        
        ((DefaultTreeModel)tree.getModel()).setRoot(null);
        ((DefaultTreeModel)tree.getModel()).reload();
        
        this.setJMenuBar(menuBar);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setMinimumSize(new Dimension(800,600));
        this.setTitle("DC Track Visualizer");
    }
    
    /**
     * 
     */
    private void addListeners(){
        
        //Listener for this RootFrame
        this.addComponentListener(new ComponentAdapter(){
            @Override
            public void componentResized(ComponentEvent e){
                graphicsPanel.setBounds(205, 5, e.getComponent().getWidth() - 210, e.getComponent().getHeight() - (((JFrame)(e.getSource())).getInsets().top + menuBar.getHeight() + 10));
                controlsPanel.setBounds(5, 5, controlsPanel.getWidth(), e.getComponent().getHeight() - 60);
                controlsPanel.setMinimumSize(new Dimension(200, e.getComponent().getHeight() - 60));
                controlsPanel.setMaximumSize(new Dimension(e.getComponent().getWidth() - 5, e.getComponent().getHeight() - 60));
                controlsPanel.revalidate();
            }
        });
        
        //Listener for prevButton
        prevButton.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                reader.getPrevEvent();
            }
            @Override
            public void mouseEntered(MouseEvent e){
                prevButton.setCursor(Cursor.getDefaultCursor());
            }
        });
        
        //Listener for nextButton
        nextButton.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                reader.getNextEvent();
            }
            @Override
            public void mouseEntered(MouseEvent e){
                nextButton.setCursor(Cursor.getDefaultCursor());
            }
        });
        
        //Listener for eventText
        eventText.addKeyListener(new KeyAdapter(){
            @Override
            public void keyTyped(KeyEvent e){
                if((int)(e.getKeyChar()) == 10){
                    if(!("".equals(eventText.getText()))){
                        reader.getEvent(Integer.parseInt(eventText.getText()));
                        eventText.setText("");
                    }
                }
                else if((int)(e.getKeyChar()) < 48 || (int)(e.getKeyChar()) > 57){
                    e.consume();
                }
            }
        });
        
        //Listeners for tree
        tree.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseEntered(MouseEvent e){
                tree.setCursor(Cursor.getDefaultCursor());
            }
        });
        tree.addTreeSelectionListener(new TreeSelectionListener(){
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                for(int i = 0; i < e.getPaths().length; i++) {
                    DefaultMutableTreeNode node = ((DefaultMutableTreeNode)(e.getPaths()[i].getLastPathComponent()));
                    if(node.getLevel() == 0){
                        if(e.isAddedPath(i)){
                            for(int j = 0; j < DisplayData.getCount(); j++){
                            }
                        }
                        else{
                            for(int j = 0; j < DisplayData.getCount(); j++){
                            }
                        }
                    }
                    else if(node.getLevel() == 1){
                        if(e.isAddedPath(i)){
                            DisplayData.setDrawable(node.getParent().getIndex(node), true);
                        }
                        else{
                            DisplayData.setDrawable(node.getParent().getIndex(node), false);
                        }
                    }
                }
                graphicsPanel.repaint();
            }
        });
        //Listener for TreeModel of tree
        tree.getModel().addTreeModelListener(new TreeModelListener(){
            @Override
            public void treeNodesChanged(TreeModelEvent e) {
                tree.expandPath(e.getTreePath());
                tree.setSelectionPath(e.getTreePath());
            }

            @Override
            public void treeNodesInserted(TreeModelEvent e) {
                //Do Nothing
            }

            @Override
            public void treeNodesRemoved(TreeModelEvent e) {
                //Do Nothing
            }

            @Override
            public void treeStructureChanged(TreeModelEvent e) {
                //Do Nothing
            }
        });
    }
}
