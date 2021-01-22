package kr.ac.snu.imageshare.tflite;

import android.graphics.Bitmap;
import java.util.Map;

public class ModelExecutionResult {
    public Bitmap bitmapResult;
    public Bitmap bitmapOriginal;
    public Bitmap bitmapMaskOnly;
    public String executionLog;
    public Map<String, Integer> itemsFound;
    
    public ModelExecutionResult(Bitmap bitmapResult, Bitmap bitmapOriginal,
				Bitmap bitmapMaskOnly, String executionLog,
				Map<String, Integer> itemsFound) {
	this.bitmapResult = bitmapResult;
	this.bitmapOriginal = bitmapOriginal;
	this.bitmapMaskOnly = bitmapMaskOnly;
	this.executionLog = executionLog;
	this.itemsFound = itemsFound;
    }
}
