package com.app.rasitagac.btp.blaind;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;
import com.google.mlkit.vision.objects.defaults.PredefinedCategory;

import java.util.List;
import java.util.Locale;

public class OnDeviceActivity extends AppCompatActivity {

    public TextView showImageDetail;
    public ImageView showImage;
    //public MaterialButton chooseImageBtn;
    private static final String TAG = "MyTag";
    private static final int CAMERA_PERMISSION_CODE=101;
    private static final int READ_STORAGE_PERMISSION_CODE=102;
    private static final int WRITE_STORAGE_PERMISSION_CODE=103;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    public String name="",getConfig="";
    public Bitmap finalBitmap;
    public TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_device);

        showImage = findViewById(R.id.picImage);
       // chooseImageBtn = findViewById(R.id.choosePicBtn);
        showImageDetail = findViewById(R.id.result_tv);

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i == TextToSpeech.SUCCESS) {
                    int lang = textToSpeech.setLanguage(new Locale("tr", "TR"));
                }
            }
        });

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            Intent data = result.getData();
            try{
                assert data != null;
                Bitmap picture = (Bitmap)data.getExtras().get("data");
                finalBitmap=picture;
                showImage.setImageBitmap(picture);
                showImageDetail.setText("");
                detectObj(picture);
            }catch (Exception e){
                Log.e(TAG, "cameraLauncher's onActivityResult : " + e.getMessage());
            }
        });

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            Intent data = result.getData();
            try {
                assert data != null;
                Bitmap picture = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                showImage.setImageBitmap(picture);
                showImageDetail.setText("");
                detectObj(picture);
            } catch (Exception e) {
                Log.e(TAG, "cameraLauncher's onActivityResult : " + e.getMessage());
            }
        });

         // chooseImageBtn.setOnClickListener(view -> {
        {
            String[] options = {"Camera", "Gallery"};
            AlertDialog.Builder builder = new AlertDialog.Builder(OnDeviceActivity.this);
            builder.setTitle("Seçiniz");
            builder.setItems(options, (dialogInterface, i) -> {
                if (i == 0) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    cameraLauncher.launch(cameraIntent);
                }
                else{
                    Intent storageIntent = new Intent();
                    storageIntent.setType("image/*");
                    storageIntent.setAction(Intent.ACTION_GET_CONTENT);
                    galleryLauncher.launch(storageIntent);
                }
            });
            builder.show();
        };
    }

    public void detectObj(Bitmap pic){
        //showImageDetail.setText("name");
        ObjectDetectorOptions options = new ObjectDetectorOptions.Builder()
                .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                .enableMultipleObjects()
                .enableClassification()  // Optional
                .build();


        ObjectDetector objectDetector = ObjectDetection.getClient(options);
//        getRotationCompensation(null,YoloV3Activity.this,true);
        InputImage image = InputImage.fromBitmap(pic, 0);

        objectDetector.process(image)
                .addOnSuccessListener(
                        new OnSuccessListener<List<DetectedObject>>() {
                            @Override
                            public void onSuccess(List<DetectedObject> detectedObjects) {
                                // Task completed successfully
                                // ...
                                //showImageDetail.setText("name");
                                for (DetectedObject detectedObject : detectedObjects) {
                                    Rect boundingBox = detectedObject.getBoundingBox();
                                    Integer trackingId = detectedObject.getTrackingId();
                                    for (DetectedObject.Label label : detectedObject.getLabels()) {
                                        String text = label.getText();
                                        if (PredefinedCategory.FASHION_GOOD.equals(text)) {
                                            text= "FASHION GOODS";
                                            name=text;
                                        }
                                        if (PredefinedCategory.HOME_GOOD.equals(text)) {
                                            text= "HOME GOODS";
                                            name=text;
                                        }
                                        if (PredefinedCategory.FOOD.equals(text)) {
                                            text= "FOOD";
                                            name=text;
                                        }
                                        if (PredefinedCategory.PLACE.equals(text)) {
                                            text= "PLACE";
                                            name=text;
                                        }
                                        if (PredefinedCategory.PLANT.equals(text)) {
                                            text = "PLANT";
                                            name=text;
                                        }

                                        int index = label.getIndex();
                                        if (PredefinedCategory.FASHION_GOOD_INDEX == index) {
                                            //
                                        }
                                        if (PredefinedCategory.HOME_GOOD_INDEX == index) {
                                            //
                                        }
                                        if (PredefinedCategory.FOOD_INDEX == index) {
                                            //
                                        }
                                        if (PredefinedCategory.PLACE_INDEX == index) {
                                            //
                                        }
                                        if (PredefinedCategory.PLANT_INDEX == index) {
                                            //
                                        }
                                        float confidence = label.getConfidence();
                                        int confidentInt = (int) (confidence * 100);
                                        getConfig = confidentInt + "%";
                                        //Draw element = new Draw(YoloV3Activity.this, boundingBox, name, getConfi);
                                        showImageDetail.setText(name + " " + getConfig + "\n");

                                        Bitmap tempBitmap = Bitmap.createBitmap(pic.getWidth(), pic.getHeight(), Bitmap.Config.RGB_565);
                                        Canvas canvas = new Canvas(tempBitmap);
                                        canvas.drawBitmap(pic, 0, 0, null);

                                        Paint paint = new Paint();
                                        paint.setColor(Color.RED);
                                        paint.setStyle(Paint.Style.STROKE);
                                        paint.setStrokeWidth(2);

                                        //Paint paint2=new Paint();
                                        paint.setTextSize(10);
                                        canvas.drawText(name+"  "+getConfig, boundingBox.left, boundingBox.top+10, paint);


                                        canvas.drawRoundRect(new RectF(boundingBox), 2, 2, paint);
                                        showImage.setImageDrawable(new BitmapDrawable(getResources(),tempBitmap));
                                        String s = name;
                                        int speech = textToSpeech.speak(s, TextToSpeech.QUEUE_FLUSH,null);
                                    }

                                }

                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                // ...
                                Toast.makeText(OnDeviceActivity.this,"Nesne tanımlanamadı",Toast.LENGTH_SHORT).show();
                            }
                        });
    }

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private int getRotationCompensation(String cameraId, Activity activity, boolean isFrontFacing)
            throws CameraAccessException {
        // Get the device's current rotation relative to its "native" orientation.
        // Then, from the ORIENTATIONS table, look up the angle the image must be
        // rotated to compensate for the device's rotation.
        int deviceRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int rotationCompensation = ORIENTATIONS.get(deviceRotation);

        // Get the device's sensor orientation.
        CameraManager cameraManager = (CameraManager) activity.getSystemService(CAMERA_SERVICE);
        int sensorOrientation = cameraManager
                .getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.SENSOR_ORIENTATION);

        if (isFrontFacing) {
            rotationCompensation = (sensorOrientation + rotationCompensation) % 360;
        } else { // back-facing
            rotationCompensation = (sensorOrientation - rotationCompensation + 360) % 360;
        }
        return rotationCompensation;
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermission(Manifest.permission.CAMERA,CAMERA_PERMISSION_CODE);
    }

    private void checkPermission(String permission, int requestCode){
        if(ContextCompat.checkSelfPermission(OnDeviceActivity.this,permission)== PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "İzin verildi.", Toast.LENGTH_SHORT).show();
        }
        else{
            ActivityCompat.requestPermissions(OnDeviceActivity.this,new String[]{permission},requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode== CAMERA_PERMISSION_CODE){
            if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE,READ_STORAGE_PERMISSION_CODE);
            }
            else{
                Toast.makeText(this, "Kamera İzni Reddedildi", Toast.LENGTH_SHORT).show();
            }
        }
        else if(requestCode==READ_STORAGE_PERMISSION_CODE){
            if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,WRITE_STORAGE_PERMISSION_CODE);
            }
            else{
                Toast.makeText(this, "Depoyu Okuma İsteği Reddedildi", Toast.LENGTH_SHORT).show();
            }
        }
        else if(requestCode==WRITE_STORAGE_PERMISSION_CODE){
            if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Tüm İzinler Verildi", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "Depoya Yazma İsteği Reddedildi", Toast.LENGTH_SHORT).show();
            }
        }
    }
}