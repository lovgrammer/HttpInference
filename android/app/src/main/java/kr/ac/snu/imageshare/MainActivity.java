package kr.ac.snu.imageshare;

import android.content.res.AssetManager;
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

public class MainActivity extends AppCompatActivity {
    
    private ImageSegmentationModelExecutor segmentExecutor;
    private Drawable originalImageDrawable;
    private ApiClient mApiClient;
    private File inputFile;
	
    private TextView mFileNameView;
    private Button mSendButton;
    private ImageView mOriginalImage;
    private ImageView mLocalImage;
    private ImageView mRemoteImage1;
    private ImageView mRemoteImage2;
    private TextView mLocalResultView;
    private TextView mRemoteResultView1;
    private TextView mRemoteResultView2;
    private EditText mRemoteEdit1;
    private EditText mRemoteEdit2;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
	
	mFileNameView = (TextView) findViewById(R.id.txt_filenames);
	mSendButton = (Button) findViewById(R.id.btn_send);
	mOriginalImage = (ImageView) findViewById(R.id.img_original);
	mLocalImage = (ImageView) findViewById(R.id.img_local);
	mRemoteImage1 = (ImageView) findViewById(R.id.img_remote1);
	mRemoteImage2 = (ImageView) findViewById(R.id.img_remote2);
	mLocalResultView = (TextView) findViewById(R.id.txt_local);
	mRemoteResultView1 = (TextView) findViewById(R.id.txt_remote1);
	mRemoteResultView2 = (TextView) findViewById(R.id.txt_remote2);
	mRemoteEdit1 = (EditText) findViewById(R.id.edit_remote1);
	mRemoteEdit2 = (EditText) findViewById(R.id.edit_remote2);
	
	mSendButton.setOnClickListener(onButtonsClick);
	
	originalImageDrawable = ImageUtils.getDrawableFromAssets(this, "archive/object.png");
	mOriginalImage.setImageDrawable(originalImageDrawable);
	
	segmentExecutor = new ImageSegmentationModelExecutor(this);

	if (!TextUtils.isEmpty(ConfigUtil.getRemote1(this))) {
	    mRemoteEdit1.setText(ConfigUtil.getRemote1(this));
	}
	if (!TextUtils.isEmpty(ConfigUtil.getRemote2(this))) {
	    mRemoteEdit2.setText(ConfigUtil.getRemote2(this));
	}
	try {
	    AssetManager assetMgr = getAssets();
	    String[] rootList = assetMgr.list("archive");
	    // String fileNames = "";
	    // for(String element : rootList) {
	    //     fileNames = fileNames + "\n" + element;
	    // }
	    if (rootList.length > 0) {
		mFileNameView.setText(rootList[0]);
		inputFile = new File(getFilesDir(), rootList[0]);
		copyInputStreamToFile(assetMgr.open("archive/" + rootList[0]), inputFile);
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}	
    }

    private void runLocalProcessing() {
	// Bitmap bitmap = ImageUtils.drawableToBitmap(originalImageDrawable);
	ModelExecutionResult result = segmentExecutor.execute(ImageUtils.drawableToBitmap(originalImageDrawable));
	mLocalImage.setImageBitmap(result.bitmapResult);
	mLocalResultView.setText(result.executionLog);
	Log.i("JONGYUN", "itemsFound : " + result.itemsFound);
    }

    OnClickListener onButtonsClick = new OnClickListener() {
	    @Override
	    public void onClick(View v) {
		switch(v.getId()) {
		case R.id.btn_send:
		    runLocalProcessing();
		    if (!TextUtils.isEmpty(mRemoteEdit2.getText().toString())) {
			ConfigUtil.setRemote2(MainActivity.this, mRemoteEdit1.getText().toString());
		    }		    
		    if (!TextUtils.isEmpty(mRemoteEdit1.getText().toString())) {
			ConfigUtil.setRemote1(MainActivity.this, mRemoteEdit1.getText().toString());
			requestRemote1();
		    }

		    break;
		}
	    }
	};

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
	mApiClient = new ApiClient(MainActivity.this, ConfigUtil.getRemote1(this));
	Call<UploadResponse> call
	    = mApiClient.uploadFile(inputFile, "input.png");
	call.enqueue(new Callback<UploadResponse>() {
		@Override
		public void onResponse(Call call, Response response) {
		    if (response.isSuccessful()) {
			UploadResponse res = (UploadResponse) response.body();
			if (res.result == 200) {
			    Toast.makeText(MainActivity.this, "success", Toast.LENGTH_SHORT).show();

			    Call<ResponseBody> call2 = mApiClient.downloadFile(res.file_name);
			    call2.enqueue(new Callback<ResponseBody>() {
				    @Override
				    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
					if (response.isSuccessful()) {
					    if (response.body() != null) {
						// display the image data in a ImageView or save it
						Bitmap bmp = BitmapFactory.decodeStream(response.body().byteStream());
						mRemoteImage1.setImageBitmap(bmp);
						if (!TextUtils.isEmpty(ConfigUtil.getRemote2(MainActivity.this))) {
						    requestRemote2();
						}
					    }
					}
				    }
				    
				    @Override
				    public void onFailure(Call call, Throwable t) {
					Toast.makeText(MainActivity.this, "fail", Toast.LENGTH_SHORT).show();
				    }				    
				});
			}
		    }
		}
		@Override
		public void onFailure(Call call, Throwable t) {
		    Toast.makeText(MainActivity.this, "fail", Toast.LENGTH_SHORT).show();
		}
	    });	
    }
    
    private void requestRemote2() {
	mApiClient = new ApiClient(MainActivity.this, ConfigUtil.getRemote2(this));
	Call<UploadResponse> call
	    = mApiClient.uploadFile(inputFile, "input.png");
	call.enqueue(new Callback<UploadResponse>() {
		@Override
		public void onResponse(Call call, Response response) {
		    if (response.isSuccessful()) {
			UploadResponse res = (UploadResponse) response.body();
			if (res.result == 200) {
			    Toast.makeText(MainActivity.this, "success", Toast.LENGTH_SHORT).show();
			    // requestRemote2();
			    Call<ResponseBody> call2 = mApiClient.downloadFile(res.file_name);
			    call2.enqueue(new Callback<ResponseBody>() {
				    @Override
				    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
					if (response.isSuccessful()) {
					    if (response.body() != null) {
						// display the image data in a ImageView or save it
						Bitmap bmp = BitmapFactory.decodeStream(response.body().byteStream());
						mRemoteImage2.setImageBitmap(bmp);
					    }
					}
				    }
				    
				    @Override
				    public void onFailure(Call call, Throwable t) {
					Toast.makeText(MainActivity.this, "fail", Toast.LENGTH_SHORT).show();
				    }				    
				});
			}
		    }
		}
		@Override
		public void onFailure(Call call, Throwable t) {
		    Toast.makeText(MainActivity.this, "fail", Toast.LENGTH_SHORT).show();
		}
	    });	
    }
}
