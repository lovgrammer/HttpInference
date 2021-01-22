package kr.ac.snu.imageshare.tflite;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;
import android.util.Pair;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.task.vision.segmenter.ColoredLabel;
import org.tensorflow.lite.task.vision.segmenter.ImageSegmenter;
import org.tensorflow.lite.task.vision.segmenter.Segmentation;

public class ImageSegmentationModelExecutor {

    private final String modelFilename0 = "lite-model_deeplabv3_1_metadata_2.tflite";
    private final int ALPHA_VALUE = 128;
    
    private ImageSegmenter imageSegmenter;
    private long fullTimeExecutionTime, imageSegmentationTime, maskFlatteningTime;

    public ImageSegmentationModelExecutor(Context context) {
	try {
	    imageSegmenter = ImageSegmenter.createFromFile(context, modelFilename0);
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public ModelExecutionResult execute(Bitmap inputImage) {
	fullTimeExecutionTime = System.currentTimeMillis();
	imageSegmentationTime = System.currentTimeMillis();
	TensorImage tensorImage = TensorImage.fromBitmap(inputImage);
	List<Segmentation> results = imageSegmenter.segment(tensorImage);
	imageSegmentationTime = System.currentTimeMillis() - imageSegmentationTime;
	
	maskFlatteningTime = System.currentTimeMillis();
	Pair<Bitmap, Map<String, Integer>> mresult = createMaskBitmapAndLabels(results.get(0), inputImage.getWidth(), inputImage.getHeight());
	Bitmap maskBitmap = mresult.first;
	Map<String, Integer> itemsFound = mresult.second;
	maskFlatteningTime = System.currentTimeMillis() - maskFlatteningTime;
	fullTimeExecutionTime = System.currentTimeMillis() - fullTimeExecutionTime;
	return new ModelExecutionResult(stackTwoBitmaps(maskBitmap, inputImage),
					inputImage, maskBitmap,
					formatExecutionLog(inputImage.getWidth(), inputImage.getHeight()),
					itemsFound);
    }

    private Pair<Bitmap, Map<String, Integer>>
	createMaskBitmapAndLabels(Segmentation result, int inputWidth, int inputHeight) {
	List<ColoredLabel> coloredLabels = result.getColoredLabels();
	int[] colors = new int[coloredLabels.size()];
	int cnt = 0;
	for (ColoredLabel coloredLabel : coloredLabels) {
	    int rgb = coloredLabel.getColor().toArgb();
	    colors[cnt++] = Color.argb(ALPHA_VALUE, Color.red(rgb), Color.green(rgb), Color.blue(rgb));
	}
	colors[0] = Color.TRANSPARENT;
	TensorImage maskTensor = result.getMasks().get(0);
	byte[] maskArray = maskTensor.getBuffer().array();
	int[] pixels = new int[maskArray.length];
	HashMap<String, Integer> itemsFound = new HashMap<String, Integer>();
	for (int i=0; i<maskArray.length; i++) {
	    int color = colors[new Byte(maskArray[i]).intValue()];
	    pixels[i] = color;
	    itemsFound.put(coloredLabels.get(new Byte(maskArray[i]).intValue()).getlabel(), color);
	}
	Bitmap maskBitmap
	    = Bitmap.createBitmap(pixels, maskTensor.getWidth(), maskTensor.getHeight(),
				  Bitmap.Config.ARGB_8888);
	return new Pair(Bitmap.createScaledBitmap(maskBitmap, inputWidth, inputHeight, true), itemsFound);
    }

    private Bitmap stackTwoBitmaps(Bitmap foregrand, Bitmap background) {
	Bitmap mergedBitmap =
	    Bitmap.createBitmap(foregrand.getWidth(), foregrand.getHeight(), foregrand.getConfig());
	Canvas canvas = new Canvas(mergedBitmap);
	canvas.drawBitmap(background, 0.0f, 0.0f, null);
	canvas.drawBitmap(foregrand, 0.0f, 0.0f, null);
	return mergedBitmap;
    }

    private String formatExecutionLog(int imageWidth, int imageHeight) {
	StringBuilder sb = new StringBuilder();
	sb.append("Input Image Size: " + imageWidth + " x " + imageHeight + "\n");
	sb.append("ImageSegmenter execution time: " + imageSegmentationTime + " ms\n");
	sb.append("Mask creation time: " + maskFlatteningTime + " ms\n");
	sb.append("Full execution time: " + fullTimeExecutionTime + "ms\n");
	return sb.toString();
    }

    public void close() {
	imageSegmenter.close();
    }
}
