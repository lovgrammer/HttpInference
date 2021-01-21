package kr.ac.snu.imageshare;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.Button;
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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    
    private ApiClient mApiClient;
	
    private TextView mFileNameView;
    private Button mSendButton;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
	
	mApiClient = new ApiClient(MainActivity.this);
	
	mFileNameView = (TextView) findViewById(R.id.txt_filenames);
	mSendButton = (Button) findViewById(R.id.btn_send);


	mSendButton.setOnClickListener(onButtonsClick);
    }

    OnClickListener onButtonsClick = new OnClickListener() {
	    @Override
	    public void onClick(View v) {
		switch(v.getId()) {
		case R.id.btn_send:
		    try {
			AssetManager assetMgr = getAssets();
			String[] rootList = assetMgr.list("archive");
			// String fileNames = "";
			// for(String element : rootList) {
			//     fileNames = fileNames + "\n" + element;
			// }
			if (rootList.length > 0) {
			    mFileNameView.setText(rootList[0]);
			    File inputFile = new File(getFilesDir(), rootList[0]);
			    copyInputStreamToFile(assetMgr.open("archive/" + rootList[0]), inputFile);
			    Call<UploadResponse> call
				= mApiClient.uploadFile(inputFile,
							rootList[0]);
			    call.enqueue(new Callback<UploadResponse>() {
				    @Override
				    public void onResponse(Call call, Response response) {
					if (response.isSuccessful()) {
					    UploadResponse res = (UploadResponse) response.body();
					    if (res.result == 200) {
						Toast.makeText(MainActivity.this, "success", Toast.LENGTH_SHORT).show();
					    }
					}
				    }
				    @Override
				    public void onFailure(Call call, Throwable t) {
					Toast.makeText(MainActivity.this, "fail", Toast.LENGTH_SHORT).show();
				    }

					
				});
			    
			}
		    } catch (IOException e) {
			mFileNameView.setText("exception occurs");
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
}
