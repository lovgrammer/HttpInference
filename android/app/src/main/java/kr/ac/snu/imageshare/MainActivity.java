package kr.ac.snu.imageshare;

import android.content.Intent;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
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
    
    private Button mSendButton;
    private EditText mRemoteEdit1;
    private EditText mRemoteEdit2;

    private String[] rootList;
    private int currentIndex = 0;
    
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private MainAdapter mAdapter;

    private ArrayList<TestProcessResult> results;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
	
	mSendButton = (Button) findViewById(R.id.btn_send);
	mRemoteEdit1 = (EditText) findViewById(R.id.edit_remote1);
	mRemoteEdit2 = (EditText) findViewById(R.id.edit_remote2);
	
	mSendButton.setOnClickListener(onButtonsClick);

	mRecyclerView = (RecyclerView) findViewById(R.id.recycler_test);
	mLinearLayoutManager = new GridLayoutManager(this, 1);	
	mRecyclerView.setLayoutManager(mLinearLayoutManager);	
	mAdapter = new MainAdapter(MainActivity.this);
	mAdapter.setOnMainItemListener(new MainAdapter.OnMainItemListener() {
		@Override
		public void onItemClick(TestProcessResult result) {
		    Intent intent = new Intent(MainActivity.this,
					       DetailActivity.class);
		    DetailActivity.processResult = result;
		    startActivity(intent);
		}
	    });
	mRecyclerView.setAdapter(mAdapter);

	results = new ArrayList<TestProcessResult>();
	
	if (!TextUtils.isEmpty(ConfigUtil.getRemote1(this))) {
	    mRemoteEdit1.setText(ConfigUtil.getRemote1(this));
	}
	if (!TextUtils.isEmpty(ConfigUtil.getRemote2(this))) {
	    mRemoteEdit2.setText(ConfigUtil.getRemote2(this));
	}
	
	try {
	    AssetManager assetMgr = getAssets();
	    rootList = assetMgr.list("archive");
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
    
    OnClickListener onButtonsClick = new OnClickListener() {
	    @Override
	    public void onClick(View v) {
		switch(v.getId()) {
		case R.id.btn_send:
		    results.clear();
		    currentIndex = 0;
		    String filename = rootList[currentIndex];
		    TestProcess t = new TestProcess(MainActivity.this,
						    filename);
		    t.setTestProcessListener(testListener);
		    t.run(mRemoteEdit1.getText().toString(),
			  mRemoteEdit2.getText().toString());
		    break;
		}
	    }
	};
    
    TestProcess.TestProcessListener testListener
	= new TestProcess.TestProcessListener() {
		@Override
		public void onTestFinished(TestProcessResult result) {
		    currentIndex++;
		    if (currentIndex < rootList.length) {
			String filename = rootList[currentIndex];
			TestProcess t = new TestProcess(MainActivity.this,
							filename);
			t.setTestProcessListener(testListener);
			t.run(mRemoteEdit1.getText().toString(),
			      mRemoteEdit2.getText().toString());
		    }
		    results.add(result);
		    mAdapter.setData(results);
		    mAdapter.notifyDataSetChanged();
		}
	    };
}
