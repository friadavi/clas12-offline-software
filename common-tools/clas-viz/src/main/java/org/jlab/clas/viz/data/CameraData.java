package org.jlab.clas.viz.data;

import com.jogamp.opengl.math.FloatUtil;
import java.nio.FloatBuffer;

/**
 *
 * @author friant
 */
public class CameraData {
    private static float aspectRatio;
    private static float xRange;
    
    private static float[] cameraPos;
    private static float[] cameraLAt;
    private static float[] cameraLUp;
    
    private static float[] lookAtMatrix;
    private static float[] projectionMatrix;
    private static float[] MVPMatrix;
    
    private static boolean MVPMatrixChanged;
    
    /**
     * 
     */
    public static void intitialize(){
        aspectRatio = 1.0f;
        xRange = 400.0f;
        
        cameraPos = new float[]{-1000.0f, 000.0f, xRange, 1.0f};
        cameraLAt = new float[]{ 0000.0f, 000.0f, xRange, 1.0f};
        cameraLUp = new float[]{ 0000.0f, 001.0f, 000.0f, 0.0f};
        
        lookAtMatrix = new float[16];
        projectionMatrix = new float[16];
        MVPMatrix = new float[16];
        
        updateLookAtMatrix();
        updateProjectionMatrix();
    }
    
    /**
     * 
     */
    public static void updateAspectRatio(float _aspectRatio){
        aspectRatio = _aspectRatio;
        updateProjectionMatrix();
    }
    
    /**
     * 
     */
    public static void updateXRange(float _xRange){
        xRange = _xRange;
        updateProjectionMatrix();
    }
    
    /**
     * 
     */
    private static void updateLookAtMatrix(){
        float[] dummyMatrix = new float[16];
        FloatUtil.makeLookAt(lookAtMatrix, 0, cameraPos, 0, cameraLAt, 0, cameraLUp, 0, dummyMatrix);
        updateMVPMatrix();
    }
    
    /**
     * 
     */
    private static void updateProjectionMatrix(){
        FloatUtil.makeOrtho(projectionMatrix, 0, true, -1.0f*xRange - 20.0f, xRange + 20.0f, -1.0f*xRange*aspectRatio - 20.0f, xRange*aspectRatio + 20.0f, 0.0f, 2000.0f);
        updateMVPMatrix();
    }
    
    /**
     * 
     */
    private static void updateMVPMatrix(){
        FloatUtil.multMatrix(projectionMatrix, lookAtMatrix, MVPMatrix);
        MVPMatrixChanged = true;
    }
    
    /**
     * 
     * @return 
     */
    public static FloatBuffer getMVPMatrix(){
        return FloatBuffer.wrap(MVPMatrix);
    }
    
    /**
     * 
     * @return 
     */
    public static boolean getMVPMatrixChanged(){
        return MVPMatrixChanged;
    }
    
    /**
     * 
     * @param _MVPMatrixChanged
     */
    public static void setMVPMatrixChanged(boolean _MVPMatrixChanged){
        MVPMatrixChanged = _MVPMatrixChanged;
    }
}
