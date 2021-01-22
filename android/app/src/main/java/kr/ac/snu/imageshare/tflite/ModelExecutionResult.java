package kr.ac.snu.imageshare.tflite;

import android.graphics.Bitmap;
import java.util.Map;

public class ModelExecutionResult {
    
    public Bitmap bitmapResult;
    public Bitmap bitmapOriginal;
    public Bitmap bitmapMaskOnly;
    public String executionLog;
    public Map<String, Integer> itemsFound;
    public long fullTimeExecutionTime;
    public int numberOfSegments;
    
    public ModelExecutionResult(Bitmap bitmapResult, Bitmap bitmapOriginal,
				Bitmap bitmapMaskOnly, String executionLog,
				Map<String, Integer> itemsFound,
				long fullTimeExecutionTime) {
	this.bitmapResult = bitmapResult;
	this.bitmapOriginal = bitmapOriginal;
	this.bitmapMaskOnly = bitmapMaskOnly;
	this.executionLog = executionLog;
	this.itemsFound = itemsFound;
	this.fullTimeExecutionTime = fullTimeExecutionTime;
	numberOfSegments = itemsFound.size();
    }

    public void recycle() {
	if (bitmapResult != null) {
	    bitmapResult.recycle();
	    bitmapResult = null;
	}
	if (bitmapOriginal != null) {
	    bitmapOriginal.recycle();
	    bitmapOriginal = null;
	}
	if (bitmapMaskOnly != null) {
	    bitmapMaskOnly.recycle();
	    bitmapMaskOnly = null;
	}		
    }
}
