package kr.ac.snu.imageshare;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import kr.ac.snu.imageshare.net.ApiClient;
import kr.ac.snu.imageshare.net.UploadResponse;
import kr.ac.snu.imageshare.tflite.ImageSegmentationModelExecutor;
import kr.ac.snu.imageshare.tflite.ModelExecutionResult;
import kr.ac.snu.imageshare.util.ImageUtils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TestProcess {
    
    private ImageSegmentationModelExecutor segmentExecutor;
    private Drawable originalImageDrawable;
    private ApiClient mApiClient;
    private File inputFile;

    private Context context;
    private String localFilePath;
    private String remoteFilePath1;
    private String remoteFilePath2;
    private String filename;
    private TestProcessListener listener;
    public TestProcessResult processResult;

    public interface TestProcessListener {
	public void onTestFinished(TestProcessResult result);
    }

    public void setTestProcessListener(TestProcessListener listener) {
	this.listener = listener;
    }
    
    public TestProcess(Context context, String filename) {
	this.context = context;
	this.filename = filename;
	this.originalImageDrawable = ImageUtils.getDrawableFromAssets(context, "archive/" + filename);
	processResult = new TestProcessResult();
	segmentExecutor = new ImageSegmentationModelExecutor(context);
	try {
	    AssetManager assetMgr = context.getAssets();
	    inputFile = new File(context.getFilesDir(), filename);
	    copyInputStreamToFile(assetMgr.open("archive/" + filename), inputFile);
	} catch(IOException e) {
	    e.printStackTrace();	    
	}
    }
    
    public void run(String addr1, String addr2) {
	runLocalProcessing();
	if (!TextUtils.isEmpty(addr1)) {
	    ConfigUtil.setRemote2(context, addr1);
	}		    
	if (!TextUtils.isEmpty(addr2)) {
	    ConfigUtil.setRemote1(context, addr2);
	    requestRemote1();
	}
    }

    private void runLocalProcessing() {
	ModelExecutionResult result = segmentExecutor.execute(ImageUtils.drawableToBitmap(originalImageDrawable));
	localFilePath = saveBitmapToFileCache(result.bitmapResult, "L_" + this.filename.split("\\.")[0] + ".jpg");
	processResult.inferenceTimeLocal = result.fullTimeExecutionTime;
	processResult.numberOfSegmentsLocal = result.numberOfSegments;
	processResult.resultFilePathLocal = localFilePath;
    }
    
    private String saveBitmapToFileCache(Bitmap bitmap, String filename) {
        File fileCacheItem = new File(context.getFilesDir(), filename);
        OutputStream out = null;
        try {
	    fileCacheItem.createNewFile();
	    out = new FileOutputStream(fileCacheItem);
	    bitmap.compress(CompressFormat.JPEG, 100, out);
	} catch (Exception e) {
	    e.printStackTrace();
	} finally {
	    try {
		out.close();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
	return fileCacheItem.getPath();
    }

    private void copyInputStreamToFile(InputStream in, File file) {
	OutputStream out = null;

	try {
	    out = new FileOutputStream(file);
	    byte[] buf = new byte[1024];
	    int len;
	    while((len=in.read(buf))>0){
		out.write(buf,0,len);
	    }
	} 
	catch (Exception e) {
	    e.printStackTrace();
	} 
	finally {
	    // Ensure that the InputStreams are closed even if there's an exception.
	    try {
		if ( out != null ) {
		    out.close();
		}

		// If you want to close the "in" InputStream yourself then remove this
		// from here but ensure that you close it yourself eventually.
		in.close();  
	    }
	    catch ( IOException e ) {
		e.printStackTrace();
	    }
	}
    }

    private void requestRemote1() {
	processResult.totalExecutionTimeRemote1 = System.currentTimeMillis();
	mApiClient = new ApiClient(context, ConfigUtil.getRemote1(context));
	Call<UploadResponse> call
	    = mApiClient.uploadFile(inputFile, "input.png");
	call.enqueue(new Callback<UploadResponse>() {
		@Override
		public void onResponse(Call call, Response response) {
		    if (response.isSuccessful()) {
			UploadResponse res = (UploadResponse) response.body();
			if (res.result == 200) {
			    Toast.makeText(context, "success", Toast.LENGTH_SHORT).show();
			    
			    processResult.inferenceTimeRemote1 = res.inference_time;
			    processResult.numberOfSegmentsRemote1 = res.number_of_segments;

			    Call<ResponseBody> call2 = mApiClient.downloadFile(res.file_name);
			    call2.enqueue(new Callback<ResponseBody>() {
				    @Override
				    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
					if (response.isSuccessful()) {
					    if (response.body() != null) {
						processResult.totalExecutionTimeRemote1
						    = System.currentTimeMillis() - processResult.totalExecutionTimeRemote1;
						// display the image data in a ImageView or save it
						Bitmap bmp = BitmapFactory.decodeStream(response.body().byteStream());
						// mRemoteImage1.setImageBitmap(bmp);
						remoteFilePath1
						    = saveBitmapToFileCache(bmp,
									    "R1_" +
									    TestProcess.this.filename.split("\\.")[0]
									    + ".jpg");
						processResult.resultFilePathRemote1 = remoteFilePath1;
						if (bmp != null) {
						    bmp.recycle();
						    bmp = null;
						}
						if (!TextUtils.isEmpty(ConfigUtil.getRemote2(context))) {
						    requestRemote2();
						} else {
						    if (TestProcess.this.listener != null) {
							TestProcess.this.listener.onTestFinished(processResult);
						    }
						}
					    }
					}
				    }
				    
				    @Override
				    public void onFailure(Call call, Throwable t) {
					Toast.makeText(context, "fail", Toast.LENGTH_SHORT).show();
				    }				    
				});
			}
		    }
		}
		@Override
		public void onFailure(Call call, Throwable t) {
		    Toast.makeText(context, "fail", Toast.LENGTH_SHORT).show();
		}
	    });	
    }
    
    private void requestRemote2() {
	processResult.totalExecutionTimeRemote2 = System.currentTimeMillis();
	mApiClient = new ApiClient(context, ConfigUtil.getRemote2(context));
	Call<UploadResponse> call
	    = mApiClient.uploadFile(inputFile, "input.png");
	call.enqueue(new Callback<UploadResponse>() {
		@Override
		public void onResponse(Call call, Response response) {
		    if (response.isSuccessful()) {
			UploadResponse res = (UploadResponse) response.body();
			if (res.result == 200) {
			    Toast.makeText(context, "success", Toast.LENGTH_SHORT).show();
			    processResult.inferenceTimeRemote2 = res.inference_time;
			    processResult.numberOfSegmentsRemote2 = res.number_of_segments;
			    // requestRemote2();
			    Call<ResponseBody> call2 = mApiClient.downloadFile(res.file_name);
			    call2.enqueue(new Callback<ResponseBody>() {
				    @Override
				    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
					if (response.isSuccessful()) {
					    if (response.body() != null) {
						processResult.totalExecutionTimeRemote2
						    = System.currentTimeMillis() - processResult.totalExecutionTimeRemote2;
						// display the image data in a ImageView or save it
						Bitmap bmp = BitmapFactory.decodeStream(response.body().byteStream());
						remoteFilePath2
						    = saveBitmapToFileCache(bmp,
									    "R2_"
									    + TestProcess.this.filename.split("\\.")[0]
									    + ".jpg");
						
						processResult.resultFilePathRemote2 = remoteFilePath2;
						
						if (bmp != null) {
						    bmp.recycle();
						    bmp = null;
						}
						if (TestProcess.this.listener != null) {
						    TestProcess.this.listener.onTestFinished(processResult);
						}
					    }
					}
				    }
				    
				    @Override
				    public void onFailure(Call call, Throwable t) {
					Toast.makeText(context, "fail", Toast.LENGTH_SHORT).show();
				    }				    
				});
			}
		    }
		}
		@Override
		public void onFailure(Call call, Throwable t) {
		    Toast.makeText(context, "fail", Toast.LENGTH_SHORT).show();
		}
	    });	
    }    
}
