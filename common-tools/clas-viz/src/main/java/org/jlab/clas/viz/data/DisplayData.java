package org.jlab.clas.viz.data;

import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 *
 * @author friant
 */
public class DisplayData {
    private static int count;
    private static boolean tracksChanged;
    private static boolean[] drawArray;
    private static boolean[] realArray;
    private static int[] chargeArray;
    private static ArrayList<FloatBuffer> colors;
    private static ArrayList<FloatBuffer> tracks;
    private static FloatBuffer hits;
    
    /**
     * 
     * @param _count 
     */
    public static void initialize(int _count){
        count = _count;
        drawArray = new boolean[count];
        realArray = new boolean[count];
        chargeArray = new int[count];
        colors = new ArrayList<>();
        tracks = new ArrayList<>();
    }
    
    /**
     * 
     * @return 
     */
    public static int getCount(){
        return count;
    }
    
    /**
     * 
     */
    public static boolean getTracksChanged(){
        return tracksChanged;
    }
    
    /**
     * 
     */
    public static void setTracksChanged(boolean _tracksChanged){
        tracksChanged = _tracksChanged;
    }
    
    /**
     * 
     * @param index
     * @return 
     */
    public static boolean getDrawable(int index){
        if(index < count){
            return drawArray[index];
        }
        return false;
    }
    
    /**
     * 
     * @param index
     * @param drawable 
     */
    public static void setDrawable(int index, boolean drawable){
        if(index < count){
            drawArray[index] = drawable;
        }
    }
    
    /**
     * 
     */
    public static void clearDrawable(){
        for(int i = 0; i < count; i++){
            drawArray[i] = false;
        }
    }
    
    /**
     * 
     * @param index
     * @return 
     */
    public static boolean getReal(int index){
        if(index < count){
            return realArray[index];
        }
        return false;
    }
    
    /**
     * 
     * @param index
     * @param real 
     */
    public static void setReal(int index, boolean real){
        if(index < count){
            realArray[index] = real;
        }
    }
    
    /**
     * 
     * @param index
     * @return 
     */
    public static int getCharge(int index){
        if(index < count){
            return chargeArray[index];
        }
        return 0;
    }
    
    /**
     * 
     * @param index
     * @param charge
     */
    public static void setCharge(int index, int charge){
        if(index < count){
            chargeArray[index] = charge;
        }
    }
    
    /**
     * 
     * @param index
     * @return 
     */
    public static FloatBuffer getColor(int index){
        if(index < count){
            return colors.get(index);
        }
        return null;
    }
    
    /**
     * 
     */
    public static void updateColors(){
        for(int i = 0; i < count; i++){
            float[] color = new float[4];
            switch(chargeArray[i]){
                case(-1):
                    color[0] = 1.0f;
                    if(!realArray[i]){
                        color[2] = 1.0f;
                    }
                    break;
                case(0):
                    color[1] = 1.0f;
                    if(!realArray[i]){
                        color[0] = 1.0f;
                    }
                    break;
                case(1):
                    color[2] = 1.0f;
                    if(!realArray[i]){
                        color[1] = 1.0f;
                    }
                    break;
                default:
                    //Do Nothing
            }
            color[3] = 1.0f;
            colors.add(FloatBuffer.wrap(color));
        }
    }
    
    /**
     * 
     * @param index
     * @return 
     */
    public static FloatBuffer getTrack(int index){
        if(index < count){
            return tracks.get(index);
        }
        return null;
    }
    
    /**
     * 
     * @param track
     */
    public static void addTrack(float[] track){
        tracks.add(FloatBuffer.wrap(track));
        tracksChanged = true;
    }
    
    /**
     * 
     */
    public static void clearTracks(){
        tracks.clear();
    }
    
    /**
     * 
     * @return 
     */
    public static FloatBuffer gethits(){
        return hits;
    }
    
    /**
     * 
     * @param _hits
     */
    public static void setHits(float[] _hits){
        hits = FloatBuffer.wrap(_hits);
    }
}
