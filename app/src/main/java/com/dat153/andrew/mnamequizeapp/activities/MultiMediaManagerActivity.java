package com.dat153.andrew.mnamequizeapp.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dat153.andrew.mnamequizeapp.R;
import com.dat153.andrew.mnamequizeapp.utils.Upload;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import android.support.design.widget.FloatingActionButton;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MultiMediaManagerActivity extends AppCompatActivity {


    private static final int REQUEST_IMAGE_CAPTURE = 1888;
    private Button btnCapturePhoto;
    private ImageView imagePreview;
    private static final int CHOOSE_IMAGE = 1;
    private Button chooseImage, btnUploadImage;
    private TextView viewGallery;
    private EditText imgDescription;
    private ProgressBar uploadProgress;
    private Uri imgUrl;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private StorageTask mUploadTask;


    /************************************************************************************************/

    Uri picUri;
    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();

    private final static int ALL_PERMISSIONS_RESULT = 101;
    private final static int IMAGE_RESULT = 202;


    /************************************************************************************************/


    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_media_manager);

        /************************************************************************************************/

        /**
         *
         */
        btnCapturePhoto = (Button) findViewById(R.id.btnCapturePhoto);

        btnCapturePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(getPickImageChooserIntent(), IMAGE_RESULT);
            }
        });


        permissions.add(CAMERA);
        permissions.add(WRITE_EXTERNAL_STORAGE);
        permissions.add(READ_EXTERNAL_STORAGE);
        permissionsToRequest = findUnAskedPermissions(permissions);

        /************************************************************************************************/


        imagePreview = (ImageView) findViewById(R.id.imgPreview);
        uploadProgress = findViewById(R.id.uploadProgress);
        chooseImage = findViewById(R.id.btnChooseImg);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        viewGallery = findViewById(R.id.viewGallery);
        imgDescription = findViewById(R.id.imgDescription);

        /* Firebase Ref*/
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");

        /**
         * Implenment viewGallery
         */
        viewGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MultiMediaManagerActivity.this, ViewImageActivity.class);
                startActivity(intent);
            }
        });

        /**
         * Implenment Upload_image Button fuk
         */
        btnUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mUploadTask != null && mUploadTask.isInProgress()) {
                    Toast.makeText(MultiMediaManagerActivity.this, "Upload in progress", Toast.LENGTH_LONG).show();
                } else {
                    uploadImage();
                }
            }
        });

        /**
         * Implenment local(mobile) image storage
         */
        chooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFileChoose();
            }
        });


    }//onCreate


    /***********************************************************************************************
     *                          Implement methods
     **********************************************************************************************/


    /**
     * View and pick image from Gallery
     */
    private void showFileChoose() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, CHOOSE_IMAGE);
    }

    /**
     * onActivityResult
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


//        if (requestCode == CHOOSE_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
//            Log.d("onActivityResult", "CHOOSE IMAGE");
//            imgUrl = data.getData();
//            Picasso.with(this).load(imgUrl).into(imagePreview);
//            }




        if (resultCode == Activity.RESULT_OK) {

            /*
             * If select img from img gallery
             *   Get uri path
             *   Preview selected image from mobil gallery
             * */
            if (requestCode == CHOOSE_IMAGE && data != null && data.getData() != null) {
                Log.d("onActivityResult", "CHOOSE IMAGE");
                imgUrl = data.getData();
                Picasso.with(this).load(imgUrl).into(imagePreview);

             /*
             * If get img with Camera
             *   Get img path
             *   Get preview
             * */
            } else if (requestCode == IMAGE_RESULT) {

                String filePath = getImageFilePath(data); // get image path

                if (filePath != null) {

                    imgUrl = getCaptureImageOutputUri();
                    Glide.with(MultiMediaManagerActivity.this).load(imgUrl).into(imagePreview);

                    // Bitmap selectedImage = BitmapFactory.decodeFile(filePath);
                    //imagePreview.setImageBitmap(selectedImage);


                }// filePath != null

            } // using Camera

        } // resultCode == Activity.RESULT_OK

    } //onActivityResult



//        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
//
//
//            Bundle extras = data.getExtras();
//            Bitmap imageBitmap = (Bitmap) extras.get("data");
//
//            imagePreview.setImageBitmap(imageBitmap);
//
//            //Glide.with(MultiMediaManagerActivity.this).load(imageFilePath).into(imagePreview);
//
//
//
//        }

    /**
     * getFileExtension from uri
     *
     * @param uri
     * @return
     */
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }


    /**
     * Upload image to Firebase
     */
    private void uploadImage() {

        if (imgUrl != null) {
           // Log.d("uploadImage", "METHOD CALLED");
            final StorageReference fileReference = mStorageRef.child(System.currentTimeMillis() + "." + getFileExtension(imgUrl));

            //Log.d(getFileExtension(imgUrl), "imgurl");

            /*
            Upload data with a reference to the root of your Google Storage bucket. the reference must point to a child URL.
             */


            mUploadTask = fileReference.putFile(imgUrl)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    uploadProgress.setProgress(0);
                                }
                            }, 500);
                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Upload upload = new Upload(imgDescription.getText().toString().trim(), uri.toString());
                                    String uploadID = mDatabaseRef.push().getKey();
                                    mDatabaseRef.child(uploadID).setValue(upload);
                                    Toast.makeText(MultiMediaManagerActivity.this, "Upload successfully", Toast.LENGTH_LONG).show();
                                    imagePreview.setImageResource(R.drawable.imagepreview);
                                    imgDescription.setText("");
                                }
                            });


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MultiMediaManagerActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            uploadProgress.setProgress((int) progress);
                        }
                    });
        } else {
            Toast.makeText(MultiMediaManagerActivity.this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }

//    private void dispatchTakePictureIntent() {
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//
//            // create a file to store the img
//            File photoFile = null;
//            try{
//                photoFile = createImageFile();
//
//            }catch (IOException ex) {
//                // Error occurred while creating the File
//
//            }
//
//            if (photoFile != null){
//                Uri photoUri = FileProvider.getUriForFile(this,"com.example.android.provider", photoFile);
//                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
//            }
//            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//        }
//    }


//private File createImageFile() throws IOException{
//    String timeStamp;
//    timeStamp =
//            new SimpleDateFormat("yyyyMMdd_HHmmss",
//                    Locale.getDefault()).format(new Date());
//    String imageFileName = "IMG_" + timeStamp + "_";
//    File storageDir =
//            getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//
//    File image = File.createTempFile(
//            imageFileName,  /* prefix */
//            ".jpg",         /* suffix */
//            storageDir      /* directory */
//    );
//
//    imageFilePath = image.getAbsolutePath();
//    return image;
//
//}





    /************************************************************************************************/
    /**
     *
     * getPickImageChooserIntent
     *
     * @return
     */
    public Intent getPickImageChooserIntent() {

        Uri outputFileUri = getCaptureImageOutputUri();

        List<Intent> allIntents = new ArrayList<>();
        PackageManager packageManager = getPackageManager();

        Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            if (outputFileUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            }
            allIntents.add(intent);
        }

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        List<ResolveInfo> listGallery = packageManager.queryIntentActivities(galleryIntent, 0);
        for (ResolveInfo res : listGallery) {
            Intent intent = new Intent(galleryIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            allIntents.add(intent);
        }

        Intent mainIntent = allIntents.get(allIntents.size() - 1);
        for (Intent intent : allIntents) {
            if (intent.getComponent().getClassName().equals("com.android.documentsui.DocumentsActivity")) {
                mainIntent = intent;
                break;
            }
        }
        allIntents.remove(mainIntent);

        Intent chooserIntent = Intent.createChooser(mainIntent, "Select source");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toArray(new Parcelable[allIntents.size()]));

        return chooserIntent;
    } //


    /**
     * getCaptureImageOutputUri
     *
     * @return
     */
    private Uri getCaptureImageOutputUri() {
        Uri outputFileUri = null;
        File getImage = getExternalFilesDir("");
        if (getImage != null) {
            outputFileUri = Uri.fromFile(new File(getImage.getPath(), "profile.png"));
        }
        return outputFileUri;
    }


    /**
     *
     * getImageFilePath
     *
     * @param data
     * @return
     */
    public String getImageFilePath(Intent data) {
        return getImageFromFilePath(data);
    }

    /**
     * getImageFromFilePath
     *
     * @param data
     * @return
     */
    private String getImageFromFilePath(Intent data) {
        boolean isCamera = data == null || data.getData() == null;

        if (isCamera) return getCaptureImageOutputUri().getPath();
        else return getPathFromURI(data.getData());

    }

    /**
     *
     * getPathFromURI
     *
     * @param contentUri
     * @return
     */
    private String getPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Audio.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    /**
     *
     * @param wanted
     * @return
     */
    private ArrayList<String> findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<String>();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    /**
     * boolean hasPermission
     *
     * @param permission
     * @return
     */
    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }


    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        picUri = savedInstanceState.getParcelable("pic_uri");
    }


    /**
     *
     * showMessageOKCancel
     *
     * @param message
     * @param okListener
     */
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    /**
     *
     * canMakeSmores
     *
     * @return
     */
    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }


    /**
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {

            case ALL_PERMISSIONS_RESULT:
                for (String perms : permissionsToRequest) {
                    if (!hasPermission(perms)) {
                        permissionsRejected.add(perms);
                    }
                }

                if (permissionsRejected.size() > 0) {


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {

                            showMessageOKCancel("Permissions are mandatory for the application.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                                                requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    });
                            return;
                        }
                    }

                }

                break;

        }//  switch (requestCode)

    } // onRequestPermissionsResult



} // MultiMediaManagerActivity Class



