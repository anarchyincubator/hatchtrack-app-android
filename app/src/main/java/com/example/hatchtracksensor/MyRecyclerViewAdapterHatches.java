package com.example.hatchtracksensor;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

public class MyRecyclerViewAdapterHatches extends RecyclerView.Adapter<MyRecyclerViewAdapterHatches.ViewHolder> {

    private JSONArray mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    MyRecyclerViewAdapterHatches(Context context, JSONArray data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_row_hatches, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        JSONObject hatchData = new JSONObject();
        try{
            hatchData = mData.getJSONObject(position);
            Log.i("hatchData",hatchData.toString());
            Date dateStart = new java.util.Date(hatchData.getInt("start_unix_timestamp")*1000L);
            SimpleDateFormat hourTime = new SimpleDateFormat("M/d/Y", Locale.ENGLISH);
            String dateString = hourTime.format(dateStart);

            String mEggType = "Egg type not set.";
            if(hatchData.getString("species_uuid").equals("90df88e3-5ed5-4a1f-a689-97dfc097ebf7"))mEggType = "Chicken Eggs";
            if(hatchData.getString("species_uuid").equals("c0999080-7749-4c9b-ada1-947ec383a845"))mEggType = "Duck Eggs";


            if(!hatchData.getString("hatch_name").equals("null")) {
                holder.mTextViewStartTitle.setText(hatchData.getString("hatch_name"));
            }else{
                holder.mTextViewStartTitle.setText("Hatch "+position);
            }
            Date dateEnd = new java.util.Date(hatchData.getInt("end_unix_timestamp")*1000L);
            dateString = hourTime.format(dateEnd);
            Date today = new Date();

            int int_egg_count = 0;
            int_egg_count = hatchData.optInt("egg_count",0);
            int int_hatch_count = 0;
            int_hatch_count = hatchData.optInt("hatched_count",0);

            float mPercentHatched = 0;
            if(dateEnd.compareTo(today)>0){
                dateString = "In Progress";
                holder.mTextViewEndTitle.setTextColor(0xff2ebf0d);
            }else{ // dateString

                mPercentHatched = 0;
                if(int_egg_count + int_hatch_count > 0){
                    mPercentHatched = (float)int_hatch_count/(float)int_egg_count;
                    mPercentHatched = mPercentHatched * 100;
                }

                //dateString = Math.round(mPercentHatched) + "% Hatched ("+ int_hatch_count + " of " + int_egg_count + ")";
                dateString = int_hatch_count + " Hatched ("+ Math.round(mPercentHatched) + "%)";
                holder.mTextViewEndTitle.setTextColor(0xff000000);
            }
            if(mPercentHatched == 0 && !dateString.equals("In Progress"))dateString = "---";
            if(int_egg_count>0) {
                holder.mTextViewSpeciesTitle.setText(int_egg_count + " " + mEggType);
            }else{
                holder.mTextViewSpeciesTitle.setText(mEggType);
            }

            holder.mTextViewEndTitle.setText(dateString);
        }catch(Exception e){
            Log.i("BROKE",e.toString());
        }
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.length();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView mTextViewStartTitle;
        TextView mTextViewEndTitle;
        TextView mTextViewSpeciesTitle;

        ViewHolder(View itemView) {
            super(itemView);
            mTextViewStartTitle = itemView.findViewById(R.id.textViewPeepEntry);
            mTextViewSpeciesTitle = itemView.findViewById(R.id.textViewPeepSpecies);
            mTextViewEndTitle = itemView.findViewById(R.id.textViewPeepLastHatch);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) {
                mClickListener.onItemClick(view, getAdapterPosition());
            }
        }
    }


    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}