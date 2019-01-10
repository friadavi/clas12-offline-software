package org.jlab.clas.viz.ui;

import com.jogamp.opengl.math.FloatUtil;
import java.nio.FloatBuffer;
import java.util.Arrays;
import org.jlab.clas.viz.data.DisplayData;

/**
 *
 * @author friant
 */
public final class Camera{
    private final float initAspectRatio;
    private final float initXRange;
    private final float initDepth;
    
    private final float[] initCameraPos;
    private final float[] initCameraLAt;
    private final float[] initCameraLUp;
    
    private float aspectRatio;
    private float xRange;
    private float depth;
    
    private float[] cameraPos;
    private float[] cameraLAt;
    private float[] cameraLUp;
    
    private final float[] lookAtMatrix;
    private final float[] projectionMatrix;
    private final float[] MVPMatrix;
    
    private boolean MVPMatrixChanged;
    
    /**
     * 
     * @param _aspectRatio
     * @param _xRange
     * @param _depth
     * @param _cameraPos
     * @param _cameraLAt
     * @param _cameraLUp
     */
    public Camera(float _aspectRatio, float _xRange, float _depth, float[] _cameraPos, float[] _cameraLAt, float[] _cameraLUp){
        initAspectRatio = _aspectRatio;
        initXRange = _xRange;
        initDepth = _depth;
        
        aspectRatio = initAspectRatio;
        xRange = initXRange;
        depth = initDepth;
        
        initCameraPos = _cameraPos;
        initCameraLAt = _cameraLAt;
        initCameraLUp = _cameraLUp;
        
        cameraPos = Arrays.copyOf(initCameraPos, 4);
        cameraLAt = Arrays.copyOf(initCameraLAt, 4);
        cameraLUp = Arrays.copyOf(initCameraLUp, 4);
        
        cameraPos = Camera.rotateZ(cameraPos, DisplayData.getTheta());
        cameraLUp = Camera.rotateZ(cameraLUp, DisplayData.getTheta());
        
        lookAtMatrix = new float[16];
        projectionMatrix = new float[16];
        MVPMatrix = new float[16];
        
        updateLookAtMatrix();
        updateProjectionMatrix();
        updateMVPMatrix();
    }
    
    /**
     * 
     * @return 
     */
    public float getAspectRatio(){
        return aspectRatio;
    }
    
    /**
     * 
     * @param _aspectRatio
     */
    public void setAspectRatio(float _aspectRatio){
        aspectRatio = _aspectRatio;
        updateProjectionMatrix();
    }
    
    /**
     * 
     * @return 
     */
    public float getXRange(){
        return xRange;
    }
    
    /**
     * 
     * @param _xRange
     */
    public void setXRange(float _xRange){
        xRange = _xRange;
        updateProjectionMatrix();
    }
    
    /**
     * 
     * @return 
     */
    public float getDepth(){
        return depth;
    }
    
    /**
     * 
     * @param _depth 
     */
    public void setDepth(float _depth){
        depth = _depth;
    }
    
    /**
     * 
     * @return 
     */
    public float[] getCameraPos(){
        return cameraPos;
    }
    
    /**
     * 
     * @param _cameraPos 
     */
    public void setCameraPos(float[] _cameraPos){
        cameraPos = _cameraPos;
    }
    
    /**
     * 
     * @return 
     */
    public float[] getCameraLAt(){
        return cameraLAt;
    }
    
    /**
     * 
     * @param _cameraLAt 
     */
    public void setCameraLAt(float[] _cameraLAt){
        cameraLAt = _cameraLAt;
    }
    
    /**
     * 
     * @return 
     */
    public float[] getCameraLUp(){
        return cameraLUp;
    }
    
    /**
     * 
     * @param _cameraLUp 
     */
    public void setCameraLUp(float[] _cameraLUp){
        cameraLUp = _cameraLUp;
    }
    
    /**
     * 
     * @return 
     */
    public FloatBuffer getMVPMatrix(){
        return FloatBuffer.wrap(MVPMatrix);
    }
    
    /**
     * 
     * @return 
     */
    public boolean getMVPMatrixChanged(){
        return MVPMatrixChanged;
    }
    
    /**
     * 
     * @param _MVPMatrixChanged
     */
    public void setMVPMatrixChanged(boolean _MVPMatrixChanged){
        MVPMatrixChanged = _MVPMatrixChanged;
    }
    
    /**
     * Call after any of cameraPos, cameraLAt, or cameraLUp is/are changed.
     */
    public void updateLookAtMatrix(){
        float[] dummyMatrix = new float[16];
        FloatUtil.makeLookAt(lookAtMatrix, 0, cameraPos, 0, cameraLAt, 0, cameraLUp, 0, dummyMatrix);
    }
    
    /**
     * Call after any of xRange, aspectRatio, or depth is/are changed.
     */
    public void updateProjectionMatrix(){
        FloatUtil.makeOrtho(projectionMatrix, 0, true, -1.0f*xRange, xRange, -1.0f*xRange*aspectRatio, xRange*aspectRatio, 0.0f, depth);
    }
    
    /**
     * Call after any of updateLookAtMatrix or updateProjectionMatrix is/are called.
     */
    public void updateMVPMatrix(){
        FloatUtil.multMatrix(projectionMatrix, lookAtMatrix, MVPMatrix);
        MVPMatrixChanged = true;
    }
    
    /**
     * Resets all values to their state when the class was constructed.
     */
    public void reset(){
        xRange = initXRange;
        
        cameraPos = Arrays.copyOf(initCameraPos, 4);
        cameraLAt = Arrays.copyOf(initCameraLAt, 4);
        cameraLUp = Arrays.copyOf(initCameraLUp, 4);
        
        cameraPos = Camera.rotateZ(cameraPos, DisplayData.getTheta());
        cameraLUp = Camera.rotateZ(cameraLUp, DisplayData.getTheta());
        
        updateLookAtMatrix();
        updateProjectionMatrix();
        updateMVPMatrix();
        MVPMatrixChanged = true;
    }
    
    /**
     * 
     * @param point
     * @param theta
     * @return 
     */
    public static float[] rotateX(float[] point, float theta){
        return rotateArbitrary(point, new float[]{1.0f, 0.0f, 0.0f}, theta);
    }
    
    /**
     * 
     * @param point
     * @param theta
     * @return 
     */
    public static float[] rotateY(float[] point, float theta){
        return rotateArbitrary(point, new float[]{0.0f, 1.0f, 0.0f}, theta);
    }
    
    /**
     * 
     * @param point
     * @param theta
     * @return 
     */
    public static float[] rotateZ(float[] point, float theta){
        return rotateArbitrary(point, new float[]{0.0f, 0.0f, 1.0f}, theta);
    }
    
    /**
     * 
     * @param point
     * @param axis
     * @param theta
     * @return 
     */
    public static float[] rotateArbitrary(float[] point, float[] axis, float theta){
        float[] rotMat = new float[16];
        float[] dummy = new float[4];
        rotMat = FloatUtil.makeRotationAxis(rotMat, 0, theta, axis[0], axis[1], axis[2], dummy);
        FloatUtil.multMatrixVec(rotMat, point, dummy);
        return dummy;
    }
}
