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
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
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
        
        tree = new JTree(new DisplayTreeModel());
        
        reader.setTreeModel((DisplayTreeModel)tree.getModel());
        
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
        
        //((DisplayTreeModel)tree.getModel()).reload();
        
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
                DisplayData.clearDrawable();
                if(tree.getSelectionPaths() == null){
                    graphicsPanel.repaint();
                    return;
                }
                for(int i = 0; i < tree.getSelectionPaths().length; i++){
                    switch(tree.getSelectionPaths()[i].getPathCount()){
                        case 1:
                            for(int j = 0; j < DisplayData.getCount(); j++){
                                DisplayData.setDrawable(j, true);
                            }
                            break;
                        case 2:
                            for(int j = 0; j < ((DisplayTreeNode)(tree.getSelectionPaths()[i].getPath()[1])).getChildCount(); j++){
                                DisplayData.setDrawable(((DisplayTreeNode)(((DisplayTreeNode)(tree.getSelectionPaths()[i].getPath()[1])).getChildAt(j))).getDisplayIndex(), true);
                            }
                            break;
                        case 3:
                            DisplayData.setDrawable(((DisplayTreeNode)(tree.getSelectionPaths()[i].getPath()[2])).getDisplayIndex(), true);
                            break;
                        case 4:
                            DisplayData.setDrawable(((DisplayTreeNode)(tree.getSelectionPaths()[i].getPath()[2])).getDisplayIndex(), true);
                            break;
                        default:
                            //Do Nothing
                            break;
                    }
                }
                graphicsPanel.repaint();
            }
        });
        //Listener for DisplayTreeModel of tree
        tree.getModel().addTreeModelListener(new TreeModelListener(){
            @Override
            public void treeNodesChanged(TreeModelEvent e) {
                
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
                //Set default tree expansion
                tree.expandPath(e.getTreePath());
                for(int i = tree.getModel().getChildCount(tree.getModel().getRoot()); i > 0; i--){
                    tree.expandRow(i);
                }
                //Set default selected path(s)
                tree.setSelectionPath(e.getTreePath());
            }
        });
    }
}
