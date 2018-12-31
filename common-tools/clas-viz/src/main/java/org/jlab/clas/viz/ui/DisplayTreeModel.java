package org.jlab.clas.viz.ui;

import java.util.ArrayList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author friant
 */
public class DisplayTreeModel implements TreeModel{
    private final ArrayList<TreeModelListener> listenerList;
    private DisplayTreeNode root;
    
    /**
     * 
     */
    public DisplayTreeModel(){
        listenerList = new ArrayList();
        root = null;
    }
    
    /**
     * 
     * @param _root 
     */
    public DisplayTreeModel(DisplayTreeNode _root){
        listenerList = new ArrayList();
        root = _root;
    }
    
    /**
     * 
     * @return 
     */
    @Override
    public DisplayTreeNode getRoot() {
        return root;
    }
    
    /**
     * 
     * @param _root
     */
    public void setRoot(DisplayTreeNode _root){
        root = _root;
    }
    
    /**
     * 
     */
    public void reload(){
        TreeModelEvent e = new TreeModelEvent(this, new Object[]{root});
            for(int i = 0; i < listenerList.size(); i++){
                listenerList.get(i).treeStructureChanged(e);
        }
    }
    
    /**
     * 
     * @param parent
     * @param index
     * @return 
     */
    @Override
    public DisplayTreeNode getChild(Object parent, int index) {
        return (DisplayTreeNode)(((DisplayTreeNode)parent).getChildAt(index));
    }
    
    /**
     * 
     * @param parent
     * @return 
     */
    @Override
    public int getChildCount(Object parent) {
        return ((DisplayTreeNode)(parent)).getChildCount();
    }
    
    /**
     * 
     * @param node
     * @return 
     */
    @Override
    public boolean isLeaf(Object node) {
        return ((DisplayTreeNode)(node)).isLeaf();
    }
    
    /**
     * 
     * @param path
     * @param newValue 
     */
    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * 
     * @param parent
     * @param child
     * @return 
     */
    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return ((DisplayTreeNode)(parent)).getIndex(((DisplayTreeNode)(child)));
    }
    
    /**
     * 
     * @param l 
     */
    @Override
    public void addTreeModelListener(TreeModelListener l) {
        listenerList.add(l);
    }
    
    /**
     * 
     * @param l 
     */
    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        listenerList.remove(l);
    }
}
