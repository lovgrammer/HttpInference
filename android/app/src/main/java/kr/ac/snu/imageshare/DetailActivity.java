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

public class DetailActivity extends AppCompatActivity {
    
    public static TestProcessResult processResult;
	
    private ImageView mLocalImage;
    private ImageView mRemoteImage1;
    private ImageView mRemoteImage2;
    private TextView mResultView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
	
	mLocalImage = (ImageView) findViewById(R.id.img_local);
	mRemoteImage1 = (ImageView) findViewById(R.id.img_remote1);
	mRemoteImage2 = (ImageView) findViewById(R.id.img_remote2);

	displayImage(mLocalImage, processResult.resultFilePathLocal);
	displayImage(mRemoteImage1, processResult.resultFilePathRemote1);
	displayImage(mRemoteImage2, processResult.resultFilePathRemote2);
	
	mResultView = (TextView) findViewById(R.id.txt_result);
	mResultView.setText("inferenceTimeLocal: " + processResult.inferenceTimeLocal + "\n" +
			    "numberOfSegmentsLocal: " + processResult.numberOfSegmentsLocal + "\n" +
    
			    "inferenceTimeRemote1: " + processResult.inferenceTimeRemote1 + "\n" +
			    "totalExecutionTimeRemote1: " + processResult.totalExecutionTimeRemote1 + "\n" +
			    "numberOfSegmentsRemote1: " + processResult.numberOfSegmentsRemote1 + "\n" +

			    "inferenceTimeRemote2: " + processResult.inferenceTimeRemote2 + "\n" +
			    "totalExecutionTimeRemote2: " + processResult.totalExecutionTimeRemote2 + "\n" +
			    "numberOfSegmentsRemote2: " + processResult.numberOfSegmentsRemote2 + "\n");
    }

    public void displayImage(ImageView view, String filepath) {
	File imgFile = new  File(filepath);
	if(imgFile.exists()){
	    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
	    view.setImageBitmap(myBitmap);
	}	
    }
    
}
