package ysrtp.party.app.profile;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.exifinterface.media.ExifInterface;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import ysrtp.party.app.PartyActivity;
import ysrtp.party.app.R;
import ysrtp.party.app.common.SessionManager;
import ysrtp.party.app.common.Utils;
import ysrtp.party.app.common.interfaces.ImageProcessingListener;
import ysrtp.party.app.databinding.ActivityProfileBinding;
import ysrtp.party.app.membersregister.MembersRegisterStatus;
import ysrtp.party.app.membersregister.UserDetailsModel;
import ysrtp.party.app.splash.SplashActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileActivity extends PartyActivity implements ImageProcessingListener {
    private ActivityProfileBinding activityProfileBinding;
    private String  selectFrom = "";
    private File cameraImageFile;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private ProfileViewModel profileViewModel;

    private static int RESULT_LOAD_IMAGE_GALLERY = 1, RESULT_LOAD_IMAGE_CAMERA = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        activityProfileBinding = DataBindingUtil.setContentView(ProfileActivity.this,R.layout.activity_profile);
        activityProfileBinding.layoutLoading.getRoot().setVisibility(View.GONE);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        profileViewModel = new ViewModelProvider(ProfileActivity.this).get(ProfileViewModel.class);
        activityProfileBinding.setProfile(UserDetailsModel.getInstance());

        activityProfileBinding.layoutPicHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        profileViewModel.getLogoutStatusLiveData().observe(ProfileActivity.this, new Observer<MembersRegisterStatus>() {
            @Override
            public void onChanged(@Nullable MembersRegisterStatus membersRegisterStatus) {

                if(membersRegisterStatus != null){
                    new Utils(ProfileActivity.this).showSnackBar(membersRegisterStatus.getMessage());
                    if(membersRegisterStatus.getResponseCode() == 1){
                        new SessionManager(ProfileActivity.this).clearSession();
                        Intent startIntent = new Intent(ProfileActivity.this, SplashActivity.class);
                        startActivity(startIntent);
                        ProfileActivity.this.finishAffinity();
                    }
                }else{
                    new Utils(ProfileActivity.this).showSnackBar(mFirebaseRemoteConfig.getString("error_occured"));
                }
                activityProfileBinding.layoutLoading.getRoot().setVisibility(View.GONE);

            }
        });

        profileViewModel.getUserDetailsLiveData().observe(ProfileActivity.this, new Observer<UserDetailsModel>() {
            @Override
            public void onChanged(@Nullable UserDetailsModel userDetailsModel) {
                if(userDetailsModel != null){
                    activityProfileBinding.setProfile(userDetailsModel);
                }
                activityProfileBinding.layoutLoading.getRoot().setVisibility(View.GONE);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_single, menu);
        MenuItem getItem = menu.findItem(R.id.action_single_item);
        if (getItem != null) {
            TextView tvLogout = (TextView) getItem.getActionView();
            tvLogout.setTextColor(getResources().getColor(R.color.colorPrimaryDark,null));
            tvLogout.setText(R.string.logout);
            tvLogout.setPadding(0,0,20,0);
            tvLogout.setTextSize(18f);
            tvLogout.setTypeface(Typeface.DEFAULT_BOLD);
            tvLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    logoutConfirmation();
                }
            });
        }
        return true;
    }

    public void updateProfile(View view) {
        if(TextUtils.isEmpty(activityProfileBinding.etName.getText())){
            activityProfileBinding.etName.setError(mFirebaseRemoteConfig.getString("name_required"));
            activityProfileBinding.etName.requestFocus();
            return;
        }else if(TextUtils.getTrimmedLength(activityProfileBinding.etName.getText()) < 3){
            activityProfileBinding.etName.setError(mFirebaseRemoteConfig.getString("name_min_char"));
            activityProfileBinding.etName.requestFocus();
            return;
        }
        activityProfileBinding.layoutLoading.getRoot().setVisibility(View.VISIBLE);
        profileViewModel.updateProfile(activityProfileBinding.etName.getText().toString());
    }

    private void logoutConfirmation() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        builder.setCancelable(false);
        builder.setTitle(FirebaseRemoteConfig.getInstance().getString("info"));
        builder.setMessage(FirebaseRemoteConfig.getInstance().getString("logout_confirmation"));
        builder.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                activityProfileBinding.layoutLoading.getRoot().setVisibility(View.VISIBLE);
                profileViewModel.sendLogout();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog ad = builder.create();
        ad.show();
        Button positiveButton = ad.getButton(DialogInterface.BUTTON_POSITIVE);
        positiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        positiveButton.setTextColor(Color.parseColor("#007567"));
        Button negativeButton = ad.getButton(DialogInterface.BUTTON_NEGATIVE);
        negativeButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        negativeButton.setTextColor(Color.parseColor("#007567"));
    }

    private void selectImage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        final CharSequence[] options = {"Click a Picture", "Choose from Gallery", "Cancel"};
        View customTitle = View.inflate(ProfileActivity.this, R.layout.update_profile_alert_title, null);
        builder.setCustomTitle(customTitle);
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Click a Picture")) {
                    selectFrom = "camera";
                    int permissionCheck = ContextCompat.checkSelfPermission(ProfileActivity.this,
                            android.Manifest.permission.CAMERA);
                    int permissionCheckStorage = ContextCompat.checkSelfPermission(ProfileActivity.this,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                        dialog.dismiss();
                        askCameraPermission();
                        return;
                    } else if (permissionCheckStorage != PackageManager.PERMISSION_GRANTED) {
                        dialog.dismiss();
                        askCameraPermission();
                        return;
                    }
                    imageFromCamera();

                } else if (options[item].equals("Choose from Gallery")) {
                    selectFrom = "gallery";
                    int permissionCheck = ContextCompat.checkSelfPermission(ProfileActivity.this,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                        dialog.dismiss();
                        askCameraPermission();
                        return;
                    }
                    imageFromGallery();
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();

                }
            }
        });
        AlertDialog alertDialogObject = builder.create();
        alertDialogObject.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        alertDialogObject.show();
    }

    private void imageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("image/*");
        startActivityForResult(intent, RESULT_LOAD_IMAGE_GALLERY);
    }

    private void imageFromCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            cameraImageFile = createImageFile();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(ProfileActivity.this, "ysrtp.party.app.fileprovider", cameraImageFile));
        startActivityForResult(takePictureIntent, RESULT_LOAD_IMAGE_CAMERA);
    }

    private File createImageFile() throws IOException {
        // Create an image cameraImageFile name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.ENGLISH).format(new Date());
        String imageFileName = "Profile_Pic_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    private void askCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    if (selectFrom.equalsIgnoreCase("camera")) {
                        imageFromCamera();
                    } else {
                        imageFromGallery();
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, "Allow Storage access permission to continue.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(ProfileActivity.this, "Allow Camera access permission to continue.", Toast.LENGTH_LONG).show();
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == RESULT_LOAD_IMAGE_GALLERY) && resultCode == RESULT_OK && data != null && data.getData() != null) {

                Uri uri = data.getData();
            String filePath = getFilePath(ProfileActivity.this,uri);
            File profilePicFile = new File(filePath);
//            new ImageCompressor(profilePicFile).execute();
            activityProfileBinding.layoutLoading.getRoot().setVisibility(View.VISIBLE);
            profileViewModel.updateProfilePicture(profilePicFile.getAbsolutePath());
        } else if (requestCode == RESULT_LOAD_IMAGE_CAMERA && resultCode == RESULT_OK) {
//            new ImageCompressor(cameraImageFile).execute();
            activityProfileBinding.layoutLoading.getRoot().setVisibility(View.VISIBLE);
            profileViewModel.updateProfilePicture(cameraImageFile.getAbsolutePath());
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

//
//
//
//    @SuppressLint("StaticFieldLeak")
//    private class ImageCompressor extends AsyncTask<Void, Void, Boolean> {
//        ProgressDialog ImageCompressProgress;
//        File fileToCompress;
//
//        ImageCompressor(File file) {
//            fileToCompress = file;
//        }
//
//        @Override
//        protected void onPreExecute() {
//            ImageCompressProgress = new ProgressDialog(ProfileActivity.this,R.style.MyAlertDialogStyle);
//            ImageCompressProgress.setCancelable(false);
//            ImageCompressProgress.setMessage("Processing Image");
//            ImageCompressProgress.show();
//            super.onPreExecute();
//
//        }
//
//        @Override
//        protected Boolean doInBackground(Void... voids) {
//            Bitmap scaledBitmap = null;
//
//            BitmapFactory.Options options = new BitmapFactory.Options();
//
////      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
////      you try the use the bitmap here, you will get null.
//            options.inJustDecodeBounds = true;
//            Bitmap bmp = BitmapFactory.decodeFile(fileToCompress.getAbsolutePath(), options);
//
//            int actualHeight = options.outHeight;
//            int actualWidth = options.outWidth;
//
////      max Height and width values of the compressed image is taken as 816x612
//
//            float maxHeight = 720.0f;
//            float maxWidth = 1280.0f;
//            float imgRatio = actualWidth / actualHeight;
//            float maxRatio = maxWidth / maxHeight;
//
////      width and height values are set maintaining the aspect ratio of the image
//
//            if (actualHeight > maxHeight || actualWidth > maxWidth) {
//                if (imgRatio < maxRatio) {
//                    imgRatio = maxHeight / actualHeight;
//                    actualWidth = (int) (imgRatio * actualWidth);
//                    actualHeight = (int) maxHeight;
//                } else if (imgRatio > maxRatio) {
//                    imgRatio = maxWidth / actualWidth;
//                    actualHeight = (int) (imgRatio * actualHeight);
//                    actualWidth = (int) maxWidth;
//                } else {
//                    actualHeight = (int) maxHeight;
//                    actualWidth = (int) maxWidth;
//
//                }
//            }
////      setting inSampleSize value allows to load a scaled down version of the original image
//
//            options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
//
////      inJustDecodeBounds set to false to load the actual bitmap
//            options.inJustDecodeBounds = false;
//
////      this options allow android to claim the bitmap memory if it runs low on memory
//            //noinspection deprecation
//            options.inPurgeable = true;
//            //noinspection deprecation
//            options.inInputShareable = true;
//            options.inTempStorage = new byte[16 * 1024];
//
//            try {
////          load the bitmap from its path
//                bmp = BitmapFactory.decodeFile(fileToCompress.getAbsolutePath(), options);
//            } catch (OutOfMemoryError exception) {
//                exception.printStackTrace();
//                return false;
//
//            }
//            try {
//                scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
//            } catch (OutOfMemoryError exception) {
//                exception.printStackTrace();
//                return false;
//            }
//
//            float ratioX = actualWidth / (float) options.outWidth;
//            float ratioY = actualHeight / (float) options.outHeight;
//            float middleX = actualWidth / 2.0f;
//            float middleY = actualHeight / 2.0f;
//
//            Matrix scaleMatrix = new Matrix();
//            scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);
//            System.out.println(ratioX + "===" + ratioY + "===" + middleX + "===" + middleY);
//            Canvas canvas = new Canvas(scaledBitmap);
//            canvas.setMatrix(scaleMatrix);
//            canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));
////      check the rotation of the image and display it properly
//            ExifInterface exif;
//            try {
//                exif = new ExifInterface(fileToCompress.getAbsolutePath());
//
//                int orientation = exif.getAttributeInt(
//                        ExifInterface.TAG_ORIENTATION, 0);
//                Matrix matrix = new Matrix();
//                if (orientation == 6) {
//                    matrix.postRotate(90);
//                } else if (orientation == 3) {
//                    matrix.postRotate(180);
//                } else if (orientation == 8) {
//                    matrix.postRotate(270);
//                }
//                scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
//                        scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
//                        true);
//            } catch (IOException e) {
//                e.printStackTrace();
//                return false;
//            }
//
//            FileOutputStream out = null;
//            try {
//                out = new FileOutputStream(fileToCompress);
////          write the compressed bitmap at the destination specified by filename.
//                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
//
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//                return false;
//            }
//            return true;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean compressed) {
//            super.onPostExecute(compressed);
//            ImageCompressProgress.dismiss();
//            if (compressed) {
//                activityProfileBinding.layoutLoading.getRoot().setVisibility(View.VISIBLE);
//                profileViewModel.updateProfilePicture(fileToCompress.getAbsolutePath());
//            }
//        }
//    }
//
//    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
//        // Raw height and width of image
//        final int height = options.outHeight;
//        final int width = options.outWidth;
//        int inSampleSize = 1;
//
//        if (height > reqHeight || width > reqWidth) {
//
//            // Calculate ratios of height and width to requested height and
//            // width
//            final int heightRatio = Math.round((float) height / (float) reqHeight);
//            final int widthRatio = Math.round((float) width / (float) reqWidth);
//            // Choose the smallest ratio as inSampleSize value, this will
//            // guarantee
//            // a final image with both dimensions larger than or equal to the
//            // requested height and width.
//            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
//        }
//
//        return inSampleSize;
//    }



    @Override
    public void OnFinishedProcessing(String compressedFile) {
        profileViewModel.updateProfilePicture(compressedFile);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case android.R.id.home:
                ProfileActivity.this.finish();
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }



    @SuppressLint("NewApi")
    private static String getFilePath(Context context, Uri uri) {
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        if (Build.VERSION.SDK_INT >= 19 && DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{
                        split[1]
                };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {
                    MediaStore.Images.Media.DATA
            };
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver()
                        .query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                assert cursor != null;
                cursor.close();
                e.printStackTrace();
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

}
