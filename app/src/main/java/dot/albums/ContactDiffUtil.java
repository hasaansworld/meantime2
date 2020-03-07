package dot.albums;

import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

public class ContactDiffUtil extends DiffUtil.Callback {
    List<DataContact> oldContacts;
    List<DataContact> newContacts;

    public ContactDiffUtil(List<DataContact> oldContacts, List<DataContact> newContacts){
        this.oldContacts = oldContacts;
        this.newContacts = newContacts;
    }

    @Override
    public int getOldListSize() {
        return oldContacts.size();
    }

    @Override
    public int getNewListSize() {
        return newContacts.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldContacts.get(oldItemPosition).getPhoneNumber().equals(newContacts.get(newItemPosition).getPhoneNumber());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldContacts.get(oldItemPosition).equals(newContacts.get(newItemPosition));
    }

}
