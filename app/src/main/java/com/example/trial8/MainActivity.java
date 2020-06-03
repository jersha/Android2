package com.example.trial8;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    ImageView imageView;
    TextView tv_x;
    TextView tv_y;
    Button button_cap;
    Button button_browse;
    File photoFile = null;
    static final int CAPTURE_IMAGE_REQUEST = 1;
    Uri photoURI = null;
    String mCurrentPhotoPath = "";
    int[] viewCoords = new int[2];
    int bt_height = 0, bt_width = 0;
    Integer REQUEST_CAMERA = 1, SELECT_FILE = 0;
    boolean buttonOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.TYPE_STATUS_BAR);

        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);
        button_cap = findViewById(R.id.btnCaptureImage);
        button_browse = findViewById(R.id.btnBrowseImage);
        tv_x = (TextView)findViewById(R.id.txt_x);
        tv_y = (TextView)findViewById(R.id.txt_y);
        button_cap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureImage();
            }
        });
        button_browse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectImage();
            }
        });
    }

    private void SelectImage(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    private void captureImage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
        }
        else{
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                try {

                    photoFile = createImageFile();
                    displayMessage(getBaseContext(),photoFile.getAbsolutePath());
                    Log.i("Mayank",photoFile.getAbsolutePath());

                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(this,
                                "com.example.trial8.fileprovider",
                                photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, CAPTURE_IMAGE_REQUEST);
                    }
                } catch (Exception ex) {
                    // Error occurred while creating the File
                    displayMessage(getBaseContext(),ex.getMessage().toString());
                }


            }else
            {
                displayMessage(getBaseContext(),"Nullll");
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Bundle extras = data.getExtras();
        //Bitmap imageBitmap = (Bitmap) extras.get("data");
        //imageView.setImageBitmap(imageBitmap);
        if(resultCode == Activity.RESULT_OK && requestCode == SELECT_FILE) {
            Uri selectedImageUri = data.getData();
            imageView.setImageURI(selectedImageUri);
        }
        if (requestCode == CAPTURE_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Bitmap myBitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
            bt_height = myBitmap.getHeight();
            bt_width = myBitmap.getWidth();
            if(bt_width < bt_height){
                imageView.setImageBitmap(myBitmap);
            }else{
                Matrix mat = new Matrix();
                mat.postRotate(90);
                Bitmap bMapRotate = Bitmap.createBitmap(myBitmap, 0, 0,bt_width,bt_height, mat, true);
                imageView.setImageBitmap(bMapRotate);
            }
            imageView.getLocationOnScreen(viewCoords);
        }
        else
        {
            displayMessage(getBaseContext(),"Request cancelled or something went wrong.");
        }
    }

    private void displayMessage(Context context, String message)
    {
        Toast.makeText(context,message,Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                captureImage();
            }
            else{
                displayMessage(getBaseContext(), "this app is not going to work without camera permission");
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        int x_flt = (int)event.getX();
        int y_flt = (int)event.getY();
        String vcx_str = Float.toString(viewCoords[0]);
        String vcy_str = Float.toString(viewCoords[1]);
        String x_str = Float.toString(x_flt);
        String y_str = Float.toString(y_flt);
        String w_str = Float.toString(bt_width);
        String h_str = Float.toString(bt_height);
        tv_x.setText(vcx_str + " ," + x_str + " ," +  w_str);
        tv_y.setText(vcy_str + " ," + y_str + " ," +  h_str);
        return false;
    }
}
