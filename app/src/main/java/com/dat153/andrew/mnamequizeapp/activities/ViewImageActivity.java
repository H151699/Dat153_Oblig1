package com.dat153.andrew.mnamequizeapp.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dat153.andrew.mnamequizeapp.adapters.ImageAdapter;
import com.dat153.andrew.mnamequizeapp.R;
import com.dat153.andrew.mnamequizeapp.utils.Upload;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ViewImageActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private ImageAdapter mAdapter;
    private ProgressBar progressBar;
    private DatabaseReference mDatabaseRef;
    private List<Upload> mUploads;

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);


        progressBar=findViewById(R.id.progress_circular);
        mRecyclerView=findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        mUploads=new ArrayList<>();
        mDatabaseRef= FirebaseDatabase.getInstance().getReference("uploads"); // Firebase image root name;
        mDatabaseRef.addValueEventListener(new ValueEventListener() {

            /**
             *
             * @param dataSnapshot
             */
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot:dataSnapshot.getChildren())
                {
                    Upload upload=postSnapshot.getValue(Upload.class);
                    mUploads.add(upload);
                }
                mAdapter=new ImageAdapter(ViewImageActivity.this, mUploads);
                mRecyclerView.setAdapter(mAdapter);
                progressBar.setVisibility(View.INVISIBLE);
            }


            /**
             *
             * @param databaseError
             */
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ViewImageActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.INVISIBLE);
            }
        });








    }
}
