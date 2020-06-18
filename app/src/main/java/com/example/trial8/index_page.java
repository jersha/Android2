package com.example.trial8;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class index_page extends AppCompatActivity {
    static {
        System.loadLibrary("native-lib");
    }

    public native Bitmap BlackWhite(Bitmap bitmapIn);
    public native Bitmap solve(Bitmap src, Bitmap overlay, int start_x, int start_y, int end_x, int end_y);
    public native int solution_present();
    public native Bitmap mazefordisplay(Bitmap src);

    ImageView imageView;
    int enable = 0;
    int points_selected = 0;
    Button button_cap;
    Button button_browse, button_solve;
    File photoFile = null;
    static final int CAPTURE_IMAGE_REQUEST = 1;
    String mCurrentPhotoPath = "";
    int[] viewCoords = new int[2];
    int bt_height = 0, bt_width = 0;
    Bitmap outBitmap, display_image, dispImageCpy;
    int x_start, y_start, x_end, y_end;
    int touch_count = 0;
    Bitmap Overlay;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.TYPE_STATUS_BAR);

        setContentView(R.layout.activity_index_page);
        imageView = findViewById(R.id.imageView);
        button_cap = findViewById(R.id.btnCaptureImage);
        button_browse = findViewById(R.id.btnBrowseImage);
        button_solve = findViewById(R.id.btnSolve);

        Bitmap default_image = BitmapFactory.decodeResource(getResources(),R.drawable.page2);
        Bitmap default_image_resized = padBitmap(default_image);
        imageView.setImageBitmap(default_image_resized);

        button_cap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(index_page.this);
            }
        });
        button_browse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(index_page.this);
            }
        });
        button_solve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(enable == 1 && points_selected == 1){
                    int return_value;
                    Bitmap solution;
                    solution = solve(outBitmap, Overlay, x_start, y_start, x_end, y_end);
                    return_value = solution_present();
                    if(return_value == 0){
                        imageView.setImageBitmap(solution);
                        displayMessage(getBaseContext(),"Follow my Paw Prints");
                    }else{
                        imageView.setImageBitmap(dispImageCpy);
                        display_image = dispImageCpy.copy(dispImageCpy.getConfig(), true);;
                        displayMessage(getBaseContext(),"Sorry! No Path Found");
                        touch_count = 0;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                displayMessage(getBaseContext(),"Select the Start Point");
                            }
                        },2000);
                    }
                    enable = 1;
                    points_selected = 0;
                }
            }
        });

        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(enable == 1) {
                    int width = display_image.getWidth() - 2;
                    int height = display_image.getHeight() - 2;
                    if (touch_count == 0) {
                        x_start = (int) event.getX();
                        y_start = (int) event.getY();
                        if(x_start < width && y_start < height
                        && x_start > 0 && y_start > 0) {
                            int w_olay = Overlay.getWidth();
                            int h_olay = Overlay.getHeight();
                            for (int x_olay = 0; x_olay < w_olay; x_olay++) {
                                for (int y_olay = 0; y_olay < h_olay; y_olay++) {
                                    int coloring = Overlay.getPixel(x_olay, y_olay);
                                    if (( coloring > -10000000
                                            && (x_start + x_olay) < width
                                            && (y_start + y_olay) < height)) {
                                        display_image.setPixel((x_start + x_olay), (y_start + y_olay), coloring);
                                    }
                                }
                            }
                            imageView.setImageBitmap(display_image);
                            touch_count = touch_count + 1;
                            displayMessage(getBaseContext(),"Select the End Point");
                        }
                    } else if (touch_count == 1) {
                        x_end = (int) event.getX();
                        y_end = (int) event.getY();
                        if(x_end < width && y_end < height
                                && x_end > 0 && y_end > 0) {
                            int w_olay = Overlay.getWidth();
                            int h_olay = Overlay.getHeight();
                            for (int x_olay = 0; x_olay < w_olay; x_olay++) {
                                for (int y_olay = 0; y_olay < h_olay; y_olay++) {
                                    int coloring = Overlay.getPixel(x_olay, y_olay);
                                    if (coloring > -10000000
                                            && (x_end + x_olay) < width
                                            && (y_end + y_olay) < height) {
                                        display_image.setPixel((x_end + x_olay), (y_end + y_olay), coloring);
                                    }
                                }
                            }
                            imageView.setImageBitmap(display_image);
                            touch_count = touch_count + 1;
                            points_selected = 1;
                        }
                    }
                    return false;
                }
                return false;
            }
        });
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
                    displayMessage(getBaseContext(), ex.getMessage());
                }


            }else
            {
                displayMessage(getBaseContext(),"Nullll");
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
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

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri selectedImageUri = result.getUri();
                try {
                    Bitmap selImage, dummy;
                    selImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    Overlay = BitmapFactory.decodeResource(getResources(),R.drawable.dogpaw3d2);
                    bt_height = selImage.getHeight();
                    bt_width = selImage.getWidth();
                    if(bt_width < bt_height){
                        selImage = BlackWhite(selImage);
                        dummy = selImage.copy(selImage.getConfig(), true);
                        outBitmap = padBitmap(selImage);
                    }else{
                        Matrix mat = new Matrix();
                        mat.postRotate(90);
                        Bitmap rotImage = Bitmap.createBitmap(selImage, 0, 0,bt_width,bt_height, mat, true);
                        rotImage = BlackWhite(rotImage);
                        dummy = rotImage.copy(rotImage.getConfig(), true);
                        outBitmap = padBitmap(rotImage);
                    }
                    display_image = mazefordisplay(dummy);
                    display_image = padBitmap(display_image);
                    dispImageCpy = display_image.copy(display_image.getConfig(), true);
                    imageView.setImageBitmap(display_image);
                    displayMessage(getBaseContext(),"Select the Start Point");
                    enable = 1;
                    points_selected = 0;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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

    public Bitmap padBitmap(Bitmap bitmap)
    {
        int paddingY;
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float device_height = metrics.heightPixels;
        float device_width = metrics.widthPixels;
        float origWidth = bitmap.getWidth();
        float origHeight = bitmap.getHeight();
        int destWidth = (int) device_width;
        float divisor = origWidth / destWidth;
        int destHeight = (int) (origHeight / divisor);
        // we create an scaled bitmap so it reduces the image, not just trim it
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, destWidth, destHeight, false);

        //padding
        int newdestHeight = (int)(device_height * 0.8);
        paddingY = newdestHeight - destHeight;

        Bitmap paddedBitmap = Bitmap.createBitmap(
                destWidth,
                destHeight + paddingY,
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(paddedBitmap);
        canvas.drawARGB(0xFF, 0xFF, 0x95, 0x01); // this represents white color
        canvas.drawBitmap(
                resized,0,
                paddingY / 2,
                new Paint(Paint.FILTER_BITMAP_FLAG));

        return paddedBitmap;
    }
}