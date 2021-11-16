package ysrtp.party.app.notification;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import ysrtp.party.app.R;
import ysrtp.party.app.databinding.ItemNotificationsBinding;

import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationsHolder> {
    private List<PartyMessage> partyMessageList;

    NotificationsAdapter(List<PartyMessage> partyMessageList) {
        this.partyMessageList = partyMessageList;
    }

    @NonNull
    @Override
    public NotificationsHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        ItemNotificationsBinding binding =
                DataBindingUtil.inflate(layoutInflater, R.layout.item_notifications, viewGroup, false);
        return new NotificationsHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationsHolder viewHolder, int i) {
        PartyMessage partyMessage = partyMessageList.get(i);
        viewHolder.itemNotificationsBinding.setNotification(partyMessage);
    }

    @Override
    public int getItemCount() {
        return partyMessageList.size();
    }

    class NotificationsHolder extends RecyclerView.ViewHolder {

        ItemNotificationsBinding itemNotificationsBinding;
        NotificationsHolder(final ItemNotificationsBinding itemNotificationsBinding) {
            super(itemNotificationsBinding.getRoot());
            this.itemNotificationsBinding = itemNotificationsBinding;
        }
    }
}
