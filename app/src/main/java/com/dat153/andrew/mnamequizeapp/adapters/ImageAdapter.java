package com.dat153.andrew.mnamequizeapp.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dat153.andrew.mnamequizeapp.R;
import com.dat153.andrew.mnamequizeapp.utils.Upload;
import com.squareup.picasso.Picasso;

import java.util.List;


public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private Context mContext;
    private List<Upload> mUploads;

    /**
     *
     * @param context
     * @param uploads
     */
    public ImageAdapter(Context context, List<Upload> uploads)
    {
        mContext=context;
        mUploads=uploads;
    }

    /**
     *
     * @param viewGroup
     * @param i
     * @return
     */
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v=LayoutInflater.from(mContext).inflate(R.layout.image_item, viewGroup,false);
        return  new ImageViewHolder(v);
    }

    /**
     *
     * @param imageViewHolder
     * @param i
     */
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder imageViewHolder, int i) {
        Upload uploadCur=mUploads.get(i);
        imageViewHolder.img_description.setText(uploadCur.getImgName());
        Picasso.with(mContext)
                .load(uploadCur.getImgUrl())
                .placeholder(R.drawable.imagepreview)
                .fit()
                .centerCrop()
                .into(imageViewHolder.image_view);

    }

    /**
     *
     * @return
     */
    @Override
    public int getItemCount() {
        return mUploads.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        public TextView img_description;
        public ImageView image_view;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            img_description=itemView.findViewById(R.id.img_description);
            image_view=itemView.findViewById(R.id.image_view);
        }
    }
}
