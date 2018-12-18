package org.jlab.clas.viz.ui;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import org.jlab.clas.viz.data.CameraData;
import org.jlab.clas.viz.data.DisplayData;


/**
 *
 * @author friant
 */
public class DisplayPanel extends GLJPanel{
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
        this.addListeners();
    }
    
    /**
     * 
     */ 
    private void addListeners(){
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
                if(CameraData.getMVPMatrixChanged()){
                    gl.glUniformMatrix4fv(mvpMatrix, 1, false, CameraData.getMVPMatrix());
                    CameraData.setMVPMatrixChanged(false);
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
            public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
                CameraData.updateAspectRatio(((float)h)/((float)w));
            }
        });
    }
}
