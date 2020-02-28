package com.irrigation.wifilocation.adapter;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.irrigation.wifilocation.R;
import com.irrigation.wifilocation.databinding.ItemWifiLocationBinding;
import com.irrigation.wifilocation.db.schema.Wifi;

import java.util.List;


/**
 * Created by dalvendrakumar on 29/11/18.
 */

public class WifiLocationAdapter extends RecyclerView.Adapter<WifiLocationAdapter.ViewHolder> {
    private static final String TAG = WifiLocationAdapter.class.getSimpleName();
    private List<Wifi> wifis;
    public WifiLocationListner listner;

    public interface WifiLocationListner {
        void setCurrentLocation(Wifi wifi);
        void removeWifiLocation(Wifi wifi);
    }

    public WifiLocationAdapter(List<Wifi> wifis, WifiLocationListner listner) {
        this.wifis = wifis;
        this.listner=listner;
    }


    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWifiLocationBinding binding= DataBindingUtil.inflate( LayoutInflater.from(parent.getContext()), R.layout.item_wifi_location, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public int getItemViewType(int position){
        return position;
    }
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Wifi wifi=wifis.get(position);
        holder.binding.setWifi(wifi);


        holder.binding.imgCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listner.setCurrentLocation(wifi);
            }
        });
        holder.binding.imgRemoveWifiLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listner.removeWifiLocation(wifi);
            }
        });

        holder.binding.edtLatitude.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                wifi.setLatitude(holder.binding.edtLatitude.getText().toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        holder.binding.edtLogtitude.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                wifi.setLongtitude(holder.binding.edtLogtitude.getText().toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        holder.binding.edtSSID.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                wifi.setSsid(holder.binding.edtSSID.getText().toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        holder.binding.edtpassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                wifi.setPassword(holder.binding.edtpassword.getText().toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        String ssidHint="";
        switch (position){
            case 0:
                ssidHint="Home";
//                holder.binding.edtSSID.setFocusable(false);
//                holder.binding.imgRemoveWifiLocation.setVisibility(View.GONE);
                break;
            case 1:
                ssidHint="Office1";
//                holder.binding.edtSSID.setFocusable(false);
//                holder.binding.imgRemoveWifiLocation.setVisibility(View.GONE);
                break;
            case 2:
                ssidHint="Office2";
//                holder.binding.edtSSID.setFocusable(false);
//                holder.binding.imgRemoveWifiLocation.setVisibility(View.GONE);
                break;
            default:
                ssidHint="SSID";
        }

        wifi.setTag(ssidHint);
        holder.binding.edtSSID.setHint(ssidHint);
    }


    @Override
    public int getItemCount() {
        return wifis==null?0:wifis.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private ItemWifiLocationBinding binding;

        public ViewHolder(ItemWifiLocationBinding binding) {
            super(binding.getRoot());
            this.binding=binding;
        }
    }
}
