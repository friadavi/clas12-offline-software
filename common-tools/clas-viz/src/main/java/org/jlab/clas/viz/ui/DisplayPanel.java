package org.jlab.clas.viz.ui;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.nio.IntBuffer;
import org.jlab.clas.viz.data.DisplayData;


/**
 *
 * @author friant
 */
public final class DisplayPanel extends GLJPanel{
    //Cameras
    private final Camera[] cameras;
    private int activeCamera;
    
    //CameraControls
    boolean clicked;
    Point clickPoint;
    
    //OpenGL
    private IntBuffer vao;
    private IntBuffer vbo;
    private int clrVector;
    private int mvpMatrix;
    private ShaderProgram program;
    
    /**
     * 
     * @param capabilities
     */
    public DisplayPanel(GLCapabilities capabilities){
        super(capabilities);
        
        cameras = new Camera[]{
            new Camera(1.0f, 550.0f, 2000.0f, new float[]{-1.0f, 0.0f, 500.0f, 1.0f}, new float[]{0.0f, 0.0f, 500.0f, 1.0f}, new float[]{0.0f, 1.0f, 0.0f, 0.0f}),
            new Camera(1.0f, 600.0f, 2000.0f, new float[]{ 0.0f, 0.0f,  -1.0f, 1.0f}, new float[]{0.0f, 0.0f,   0.0f, 1.0f}, new float[]{0.0f, 1.0f, 0.0f, 0.0f})};
        activeCamera = 0;
        
        this.addListeners();
    }
    
    /**
     * 
     * @return 
     */
    public int getActiveCamera(){
       return activeCamera; 
    }
    
    /**
     * 
     * @param _activeCamera 
     */
    public void setActiveCamera(int _activeCamera){
        activeCamera = _activeCamera;
        cameras[activeCamera].setMVPMatrixChanged(true);
    }
    
    /**
     * 
     */
    public void resetCamera(){
       cameras[activeCamera].reset();
    }
            
    /**
     * 
     */ 
    private void addListeners(){
        //OpenGL Specific Events
        this.addGLEventListener(new GLEventListener(){
            @Override
            public void init(GLAutoDrawable drawable) {
                GL3 gl = drawable.getGL().getGL3();
                gl.glEnable(GL3.GL_DEPTH_TEST);
                gl.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
                program = new ShaderProgram();
                program.init(gl);
                
                //Vertex Array Object
                vao = IntBuffer.allocate(1);
                gl.glGenVertexArrays(1, vao);
                gl.glBindVertexArray(vao.get(0));
                
                //Dummy Vertex Buffer Object
                vbo = IntBuffer.allocate(1);
                gl.glGenBuffers(1, vbo);
                gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vbo.get(0));
                gl.glBufferData(GL3.GL_ARRAY_BUFFER, 3 * 4 * Float.BYTES, Buffers.newDirectFloatBuffer(new float[]{-1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f}), GL3.GL_STATIC_DRAW);
                
                //Shaders
                program.add(gl, ShaderCode.create(gl, GL3.GL_VERTEX_SHADER, this.getClass(), "shaders", null, "vert", "glsl", null, false), System.out);
                program.add(gl, ShaderCode.create(gl, GL3.GL_FRAGMENT_SHADER, this.getClass(), "shaders", null, "frag", "glsl", null, false), System.out);
                program.link(gl, System.out);
                program.validateProgram(gl, System.out);
                gl.glUseProgram(program.program());
                
                //Uniforms
                clrVector = gl.glGetUniformLocation(program.program(), "clrVec");
                mvpMatrix = gl.glGetUniformLocation(program.program(), "mvpMat");
            }

            @Override
            public void dispose(GLAutoDrawable drawable) {
                GL3 gl = drawable.getGL().getGL3();
                gl.glDeleteBuffers(vbo.array().length, vbo.array(), 0);
                gl.glDeleteVertexArrays(vao.array().length, vao);
            }

            @Override
            public void display(GLAutoDrawable drawable) {
                GL3 gl = drawable.getGL().getGL3();
                gl.glUseProgram(program.program());
                gl.glClear(GL3.GL_DEPTH_BUFFER_BIT | GL3.GL_COLOR_BUFFER_BIT);
                
                //Update MVP Matrix in the GPU if necessary
                if(cameras[activeCamera].getMVPMatrixChanged()){
                    gl.glUniformMatrix4fv(mvpMatrix, 1, false, cameras[activeCamera].getMVPMatrix());
                    cameras[activeCamera].setMVPMatrixChanged(false);
                }
                
                //Update DataBuffers in the GPU if necessary
                if(DisplayData.getTracksChanged()){
                    gl.glDeleteBuffers(vbo.array().length, vbo.array(), 0);
                    vbo = IntBuffer.allocate(DisplayData.getCount());
                    gl.glGenBuffers(DisplayData.getCount(), vbo);
                    for(int i = 0; i < DisplayData.getCount(); i++){
                        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vbo.get(i));
                        gl.glBufferData(GL3.GL_ARRAY_BUFFER, DisplayData.getTrack(i).array().length * Float.BYTES, DisplayData.getTrack(i), GL3.GL_STATIC_DRAW);
                    }
                    DisplayData.setTracksChanged(false);
                }
                
                //Draw Calls
                for(int i = 0; i < DisplayData.getCount(); i++){
                    if(DisplayData.getDrawable(i)){
                        gl.glUniform4fv(clrVector, 1, DisplayData.getColor(i));
                        gl.glBindVertexArray(vao.get(0));
                        gl.glEnableVertexAttribArray(0);
                        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vbo.get(i));
                        gl.glVertexAttribPointer(0, 4, GL3.GL_FLOAT, false, 4 * Float.BYTES, 0);
                        gl.glDrawArrays(GL3.GL_LINE_STRIP, 0, DisplayData.getTrack(i).array().length);
                        gl.glDisableVertexAttribArray(0);
                    }
                }
                
                //Print errors if necessary
                if(gl.glGetError() != 0){
                    System.out.println("GL Error: " + Integer.toHexString(gl.glGetError()));
                }
            }

            @Override
            public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h){
                for(int i = 0; i < cameras.length; i++){
                    cameras[i].setAspectRatio(((float)h)/((float)w));
                    cameras[i].updateProjectionMatrix();
                    cameras[i].updateMVPMatrix();
                    cameras[i].setMVPMatrixChanged(true);
                }
            }
        });
        
        //Mouse Click, Release, Etc
        this.addMouseListener(new MouseListener(){
            @Override
            public void mouseClicked(MouseEvent e) {
                //Possibly open extra information about clicked item
            }

            @Override
            public void mousePressed(MouseEvent e) {
                clicked = true;
                clickPoint = e.getPoint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                clicked = false;
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                //Do Nothing
            }

            @Override
            public void mouseExited(MouseEvent e) {
                //Do Nothing
            }
            
        });
        
        //Mouse Moved and Dragged
        this.addMouseMotionListener(new MouseMotionListener(){
            @Override
            public void mouseDragged(MouseEvent e) {
                //handle panning here
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                //Do Nothing
            }
        });
        
        //Mouse Wheel
        this.addMouseWheelListener(new MouseWheelListener(){
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                float newVal = cameras[activeCamera].getXRange();
                if(newVal > 10.0f){
                    cameras[activeCamera].setXRange(newVal + e.getWheelRotation() * 5.0f);
                    cameras[activeCamera].updateProjectionMatrix();
                    cameras[activeCamera].updateMVPMatrix();
                    repaint();
                }
            }
        });
    }
}
