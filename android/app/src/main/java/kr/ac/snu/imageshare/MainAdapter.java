package kr.ac.snu.imageshare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainViewHolder> {
    
    private List<TestProcessResult> mData;
    private static int csize = 0;
    private static int msize = 0;
    private Context mContext;
    private OnMainItemListener mListener;
    
    public interface OnMainItemListener {
	void onItemClick(TestProcessResult result);
    }
    
    public void setOnMainItemListener(OnMainItemListener listener) {
	mListener = listener;
    }    
    
    public class MainViewHolder extends RecyclerView.ViewHolder {
	
	protected TextView title;
	
	public MainViewHolder(View view) {
	    super(view);
	    this.title = (TextView) view.findViewById(R.id.txt_title);
	    view.setOnClickListener(new OnClickListener() {
		    @Override
		    public void onClick(View v) {
			int position = getAdapterPosition();
			if (mListener != null)
			    mListener.onItemClick(mData.get(position));
		    }
		});	    
	}
    }
    
    public MainAdapter(Context context) {
	mContext = context;
    }
    
    public void setData(List<TestProcessResult> data) {
	mData = data;
    }
    
    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
	View view = LayoutInflater.from(viewGroup.getContext())
	    .inflate(R.layout.item_main, viewGroup, false);
	MainViewHolder viewHolder = new MainViewHolder(view);
	return viewHolder;
    }
    
    @Override
    public void onBindViewHolder(@NonNull MainViewHolder viewholder, int position) {
	TestProcessResult result = mData.get(position);
	viewholder.title.setText("Result" + position + " > ");
    }

    @Override
    public int getItemCount() {
	if (mData == null)
	    return 0;
	return mData.size();
    }
}
