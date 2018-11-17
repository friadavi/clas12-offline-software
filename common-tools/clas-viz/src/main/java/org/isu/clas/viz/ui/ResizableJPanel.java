package org.isu.clas.viz.ui;

import java.awt.Cursor;
import java.awt.LayoutManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author friant
 */
public class ResizableJPanel extends JPanel{
    //Constants
    public static final int TOP    = 1;
    public static final int LEFT  = 2;
    public static final int BOTTOM  = 4;
    public static final int RIGHT = 8;
    
    //Variables
    private boolean drag = false;
    private int dragBorders = 0;
    private int dragInitX = 0;
    private int dragInitY = 0;
    private int panlInitX = 0;
    private int panlInitY = 0;
    private int panlInitW = 0;
    private int panlInitH = 0;
    private int resizableDirections = 0;
    private int borderWidth = 5;
    
    /**
     * 
     */
    public ResizableJPanel() {
        super();
        addListeners();
    }
    
    /**
     * 
     * @param layout 
     */
    public ResizableJPanel(LayoutManager layout){
        super(layout);
        addListeners();
    }
    
    /**
     * 
     * @param isDoubleBuffered
     */
    public ResizableJPanel(boolean isDoubleBuffered){
        super(isDoubleBuffered);
        addListeners();
    }
    
    
    
    /**
     * 
     * @param _resizableDirections 
     */
    public ResizableJPanel(int _resizableDirections){
        super();
        addListeners();
        setResizableDirections(_resizableDirections);
    }
    
    /**
     * 
     * @param layout
     * @param isDoubleBuffered
     */
    public ResizableJPanel(LayoutManager layout, boolean isDoubleBuffered){
        super(layout, isDoubleBuffered);
        addListeners();
    }
    
    /**
     * 
     * @param layout
     * @param _resizableDirections
     */
    public ResizableJPanel(LayoutManager layout, int _resizableDirections){
        super(layout);
        addListeners();
        setResizableDirections(_resizableDirections);
    }
    
    /**
     * 
     * @param isDoubleBuffered
     * @param _resizableDirections
     */
    public ResizableJPanel(boolean isDoubleBuffered, int _resizableDirections){
        super(isDoubleBuffered);
        addListeners();
        setResizableDirections(_resizableDirections);
    }
    
    /**
     * 
     * @param layout
     * @param isDoubleBuffered
     * @param _resizableDirections 
     */
    public ResizableJPanel(LayoutManager layout, boolean isDoubleBuffered, int _resizableDirections){
        super(layout, isDoubleBuffered);
        addListeners();
        setResizableDirections(_resizableDirections);
    }
    
    /**
     * 
     */
    private void addListeners(){
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(dragBorders != 0){
                    drag = true;
                    dragInitX = e.getX();
                    dragInitY = e.getY();
                    panlInitX = e.getComponent().getX();
                    panlInitY = e.getComponent().getY();
                    panlInitW = e.getComponent().getWidth();
                    panlInitH = e.getComponent().getHeight();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                drag = false;
                //e.getComponent().revalidate();
            }
        });
        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if(drag){
                    int x = e.getComponent().getX();
                    int y = e.getComponent().getY();
                    int w = e.getComponent().getWidth();
                    int h = e.getComponent().getHeight();
                    int deltaX = e.getX() - dragInitX;
                    int deltaY = e.getY() - dragInitY;
                    
                    switch(dragBorders){
                        case TOP:
                            x = panlInitX;
                            y = e.getComponent().getY() + deltaY;
                            w = panlInitW;
                            h = e.getComponent().getHeight() - deltaY;
                            break;
                        case TOP + LEFT:
                            x = e.getComponent().getX() + deltaX;
                            y = e.getComponent().getY() + deltaY;
                            w = e.getComponent().getWidth() - deltaX;
                            h = e.getComponent().getHeight() - deltaY;
                            break;
                        case LEFT:
                            x = e.getComponent().getX() + deltaX;
                            y = panlInitY;
                            w = e.getComponent().getWidth() - deltaX;
                            h = panlInitH;
                            break;
                        case LEFT + BOTTOM:
                            x = e.getComponent().getX() + deltaX;
                            y = panlInitY;
                            w = e.getComponent().getWidth() - deltaX;
                            h = panlInitH + deltaY;
                            break;
                        case BOTTOM:
                            x = panlInitX;
                            y = panlInitY;
                            w = panlInitW;
                            h = panlInitH + deltaY;
                            break;
                        case BOTTOM + RIGHT:
                            x = panlInitX;
                            y = panlInitY;
                            w = panlInitW + deltaX;
                            h = panlInitH + deltaY;
                            break;
                        case RIGHT:
                            x = panlInitX;
                            y = panlInitY;
                            w = panlInitW + deltaX;
                            h = panlInitH;
                            break;
                        case RIGHT + TOP:
                            x = panlInitX;
                            y = e.getComponent().getY() + deltaY;
                            w = panlInitW + deltaX;
                            h = e.getComponent().getHeight() - deltaY;
                            break;
                        default:
                            //Do Nothing    
                    }
                    if(w < e.getComponent().getMinimumSize().width || w > e.getComponent().getMaximumSize().width){
                        x = e.getComponent().getX();
                        w = e.getComponent().getWidth();
                    }
                    if(h < e.getComponent().getMinimumSize().height || h > e.getComponent().getMaximumSize().height){
                        y = e.getComponent().getY();
                        h = e.getComponent().getHeight();
                    }
                    e.getComponent().setBounds(x, y, w, h);
                    e.getComponent().revalidate();
                }
            }
            
            @Override
            public void mouseMoved(MouseEvent e){
                if(!drag){
                    int borders = cursorOnBorder(e);
                    dragBorders = borders;
                    setCursor(borders);
                }
            }
        });
    }
    
    /**
     * 
     * @return 
     */
    public int getResizableDirections(){
        return resizableDirections;
    }
    
    /**
     * 
     * @param _resizableDirections
     */
    public final void setResizableDirections(int _resizableDirections){
        resizableDirections = _resizableDirections;
        
        this.setBorder(new EmptyBorder(
                (TOP & resizableDirections) * borderWidth,
                ((LEFT & resizableDirections) >> 1) * borderWidth,
                ((BOTTOM & resizableDirections) >> 2) * borderWidth,
                ((RIGHT & resizableDirections) >> 3) * borderWidth));
    }
    
    /**
     * 
     * @return 
     */
    public int getBorderWidth(){
        return borderWidth;
    }
    
    /**
     * 
     * @param _borderWidth
     */
    public void setBorderWidth(int _borderWidth){
        borderWidth = _borderWidth;
        setResizableDirections(resizableDirections);
    }
    
    /**
     * 
     * @param borders 
     */
    private void setCursor(int borders){
        switch(borders){
            case TOP:
                this.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
                break;
            case TOP + LEFT:
                this.setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
                break;
            case LEFT:
                this.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
                break;
            case LEFT + BOTTOM:
                this.setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
                break;
            case BOTTOM:
                this.setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
                break;
            case BOTTOM + RIGHT:
                this.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
                break;
            case RIGHT:
                this.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                break;
            case RIGHT + TOP:
                this.setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
                break;
            default:
                this.setCursor(Cursor.getDefaultCursor());
        }
    }
    
    /**
     * 
     */
    private int cursorOnBorder(MouseEvent e){
        int borders = 0;
        if((TOP & resizableDirections) == TOP && e.getY() < borderWidth){
            borders += TOP;
        }
        else if((BOTTOM & resizableDirections) == BOTTOM && e.getY() > this.getHeight() - borderWidth){
            borders += BOTTOM;
        }
        if((LEFT & resizableDirections) == LEFT && e.getX() < borderWidth){
            borders += LEFT;
        }
        else if((RIGHT & resizableDirections) == RIGHT && e.getX() > this.getWidth() - borderWidth){
            borders += RIGHT;
        }
        return borders;
    }
}