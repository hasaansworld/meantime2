package dot.albums;

import android.content.Context;
import android.net.ConnectivityManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.siyamed.shapeimageview.RoundedImageView;

public class MainAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;

    public MainAdapter(Context context){
        this.context = context;
    }

    public class ViewHolderUpload extends RecyclerView.ViewHolder{
        public ViewHolderUpload(View v){
            super(v);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        RoundedImageView thumbnail;
        public ViewHolder(View v){
            super(v);
            thumbnail = v.findViewById(R.id.thumbnail);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == 0)
            return new ViewHolderUpload(LayoutInflater.from(context).inflate(R.layout.item_upload, parent, false));
        View v = LayoutInflater.from(context).inflate(R.layout.item_main, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(position != 0){
            ViewHolder holder1 = (ViewHolder)holder;
            Glide.with(context).asBitmap().load(R.drawable.sample).placeholder(R.drawable.imagepicker_image_placeholder).into(holder1.thumbnail);
        }
    }

    @Override
    public int getItemCount() {
        return 17;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}
