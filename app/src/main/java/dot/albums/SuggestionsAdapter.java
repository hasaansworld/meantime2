package dot.albums;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SuggestionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_INFO = 0;
    private static final int TYPE_HEADER = 1;
    private static final int TYPE_SUGGESTION = 2;
    private static final int TYPE_INVITE = 3;

    Context context;
    boolean showInfo = false;
    int infoCount = 0, contactCount = 4, suggestionsCount = 4;

    public SuggestionsAdapter(Context context, boolean showInfo){
        this.context = context;
        this.showInfo = showInfo;
        if(showInfo)
            infoCount = 1;
    }

    public class ViewHolderInfo extends RecyclerView.ViewHolder{
        public ViewHolderInfo(View v){
            super(v);
        }
    }

    public class ViewHolderHeader extends RecyclerView.ViewHolder{
        TextView header;
        LinearLayout inviteLayout;
        public ViewHolderHeader(View v){
            super(v);
            header = v.findViewById(R.id.header);
            inviteLayout = v.findViewById(R.id.inviteLayout);
        }
    }

    public class ViewHolderSuggestion extends RecyclerView.ViewHolder{
        ImageView profilePicture;
        TextView username, follow;
        public ViewHolderSuggestion(View v){
            super(v);
            profilePicture = v.findViewById(R.id.profilePicture);
            username = v.findViewById(R.id.username);
            follow = v.findViewById(R.id.follow);
        }
    }

    public class ViewHolderInvite extends RecyclerView.ViewHolder{
        public ViewHolderInvite(View v){
            super(v);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        if(viewType == TYPE_INFO) {
            v = LayoutInflater.from(context).inflate(R.layout.item_suggestion_info, parent, false);
            return new ViewHolderInfo(v);
        }
        else if(viewType == TYPE_HEADER){
            v = LayoutInflater.from(context).inflate(R.layout.item_suggestion_header, parent, false);
            return new ViewHolderHeader(v);
        }
        else if(viewType == TYPE_INVITE){
            v = LayoutInflater.from(context).inflate(R.layout.item_suggestion_invite, parent, false);
            return new ViewHolderInvite(v);
        }
        else{
            v = LayoutInflater.from(context).inflate(R.layout.item_suggestion, parent, false);
            return new ViewHolderSuggestion(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof ViewHolderHeader){
            ViewHolderHeader holderHeader = (ViewHolderHeader)holder;
            if(position == infoCount){
                if(contactCount == 0)
                    holderHeader.inviteLayout.setVisibility(View.VISIBLE);
                else
                    holderHeader.inviteLayout.setVisibility(View.GONE);
                holderHeader.header.setText("Your Contacts");
            }
            else{
                holderHeader.inviteLayout.setVisibility(View.GONE);
                holderHeader.header.setText("Suggestions");
            }
        }
        else if(holder instanceof ViewHolderSuggestion){
            ViewHolderSuggestion holderSuggestion = (ViewHolderSuggestion)holder;
            holderSuggestion.username.setText("@dotUser_"+position+position);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0 && showInfo)
            return TYPE_INFO;
        else if(position == infoCount || position == infoCount+contactCount+2)
            return TYPE_HEADER;
        else if(position == infoCount+contactCount+1)
            return TYPE_INVITE;
        else
            return TYPE_SUGGESTION;
    }

    @Override
    public int getItemCount() {
        return infoCount + 2 + contactCount + suggestionsCount + 1;
    }
}
