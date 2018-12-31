package org.jlab.clas.viz.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import javax.swing.tree.TreeNode;

/**
 *
 * @author friant
 */
public class DisplayTreeNode implements TreeNode{
    private TreeNode parent;
    private final ArrayList<TreeNode> children;
    
    private final int displayIndex;
    private final String displayString;
    
    
    /**
     * 
     */
    public DisplayTreeNode(){
        parent = null;
        children = new ArrayList();
        displayIndex = -1;
        displayString = "";
    }
    
    /**
     * 
     * @param _displayIndex 
     */
    public DisplayTreeNode(int _displayIndex){
        parent = null;
        children = new ArrayList();
        displayIndex = _displayIndex;
        displayString = "";
    }
    
    /**
     * 
     * @param _displayString 
     */
    public DisplayTreeNode(String _displayString){
        parent = null;
        children = new ArrayList();
        displayIndex = -1;
        displayString = _displayString;
    }
    
    /**
     * 
     * @param _displayIndex
     * @param _displayString 
     */
    public DisplayTreeNode(int _displayIndex, String _displayString){
        parent = null;
        children = new ArrayList();
        displayIndex = _displayIndex;
        displayString = _displayString;
    }
    
    /**
     * 
     * @param _parent 
     */
    public void setParent(DisplayTreeNode _parent){
        parent = _parent;
    }
    
    /**
     * 
     * @param child
     */
    public void addChild(DisplayTreeNode child){
        child.setParent(this);
        children.add(child);
    }
    
    /**
     * 
     */
    public void removeChild(int index){
        children.remove(index);
    }
    
    /**
     * 
     * @param node
     */
    public void removeChild(DisplayTreeNode node){
        children.remove(node);
    }
    
    /**
     * 
     * @return 
     */
    public int getDisplayIndex(){
        return displayIndex;
    }
    
    /**
     * 
     * @return 
     */
    @Override
    public String toString(){
        return displayString;
    }
    
    /**
     * 
     * @param childIndex
     * @return 
     */
    @Override
    public TreeNode getChildAt(int childIndex){
        return children.get(childIndex);
    }
    /**
     * 
     * @return 
     */
    @Override
    public int getChildCount(){
        return children.size();
    }
    
    /**
     * 
     * @return 
     */
    @Override
    public TreeNode getParent(){
        return parent;
    }
    
    /**
     * 
     * @param node
     * @return 
     */
    @Override
    public int getIndex(TreeNode node){
        return children.indexOf(node);
    }
    
    /**
     * 
     * @return 
     */
    @Override
    public boolean getAllowsChildren(){
        return true;
    }
    
    /**
     * 
     * @return 
     */
    @Override
    public boolean isLeaf(){
        return children.isEmpty();
    }
    
    /**
     * 
     * @return 
     */
    @Override
    public Enumeration children(){
        return Collections.enumeration(children);
    }
    
}