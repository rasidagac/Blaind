package com.app.rasitagac.btp.blaind;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.app.rasitagac.btp.blaind.customview.OverlayView;
import com.app.rasitagac.btp.blaind.databinding.ActivityCaptureBinding;
import com.app.rasitagac.btp.blaind.env.ImageUtils;
import com.app.rasitagac.btp.blaind.env.Logger;
import com.app.rasitagac.btp.blaind.env.Utils;
import com.app.rasitagac.btp.blaind.tflite.Detector;
import com.app.rasitagac.btp.blaind.tflite.TFLiteObjectDetectionAPIModel;
import com.app.rasitagac.btp.blaind.tracking.MultiBoxTracker;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CaptureActivity extends AppCompatActivity {

    public static final int TF_OD_API_INPUT_SIZE = 720;
    // Minimum detection confidence to track a detection.
    private static final Float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;
    private static final String TAG = "MyTag";
    private static final int CAMERA_PERMISSION_CODE = 101;
    private static final int READ_STORAGE_PERMISSION_CODE = 102;
    private static final int WRITE_STORAGE_PERMISSION_CODE = 103;
    private static final Logger LOGGER = new Logger();
    private static final boolean TF_OD_API_IS_QUANTIZED = false;
    private static final String TF_OD_API_MODEL_FILE = "detect.tflite";
    private static final String TF_OD_API_LABELS_FILE = "labelmap.txt";
    private static final boolean MAINTAIN_ASPECT = true;
    private final Integer sensorOrientation = 90;
    protected int previewWidth = 0;
    protected int previewHeight = 0;
    ActivityCaptureBinding binding;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private Bitmap sourceBitmap;
    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;
    private MultiBoxTracker tracker;
    private OverlayView trackingOverlay;
    private Bitmap croppedBitmap;
    private Detector detector;
    private TextToSpeech textToSpeech;
    private List<Detector.Recognition> currentRecognitions;

    private final HashMap<String, Integer> colorMap = new HashMap<String, Integer>() {{
        put("person", Color.RED);
        put("bicycle", Color.parseColor("#B0BF1A"));
        put("car", Color.YELLOW);
        put("motorcycle", Color.parseColor("#C0E8D5"));
        put("airplane", Color.parseColor("#B284BE"));
        put("bus", Color.parseColor("#72A0C1"));
        put("train", Color.parseColor("#EDEAE0"));
        put("truck", Color.parseColor("#C46210"));
        put("boat", Color.parseColor("#EFDECD"));
        put("traffic light", Color.parseColor("#E52B50"));
        put("fire hydrant", Color.parseColor("#9F2B68"));
        put("???", Color.parseColor("#F19CBB"));
        put("stop sign", Color.parseColor("#AB274F"));
        put("parking meter", Color.parseColor("#D3212D"));
        put("bench", Color.parseColor("#3B7A57"));
        put("bird", Color.parseColor("#3B7A57"));
        put("cat", Color.parseColor("#3B7A57"));
        put("dog", Color.parseColor("#3B7A57"));
        put("horse", Color.parseColor("#3B7A57"));

        put("sheep", Color.parseColor("#FFBF00"));
        put("cow", Color.parseColor("#FF7E00"));
        put("elephant", Color.parseColor("#9966CC"));
        put("bear", Color.parseColor("#3DDC84"));
        put("zebra", Color.parseColor("#CD9575"));
        put("giraffe", Color.parseColor("#665D1E"));
        put("backpack", Color.parseColor("#915C83"));
        put("umbrella", Color.parseColor("#841B2D"));
        put("handbag", Color.parseColor("#FAEBD7"));
        put("tie", Color.parseColor("#008000"));
        put("suitcase", Color.parseColor("#8DB600"));
        put("frisbee", Color.parseColor("#FBCEB1"));
        put("skis", Color.parseColor("#00FFFF"));
        put("snowboard", Color.parseColor("#7FFFD4"));
        put("sports ball", Color.parseColor("#D0FF14"));
        put("kite", Color.parseColor("#4B5320"));
        put("baseball bat", Color.parseColor("#8F9779"));
        put("baseball glove", Color.parseColor("#E9D66B"));
        put("skateboard", Color.parseColor("#B2BEB5"));
        put("surfboard", Color.parseColor("#87A96B"));
        put("tennis racket", Color.parseColor("#FF9966"));
        put("bottle", Color.parseColor("#A52A2A"));
        put("wine glass", Color.parseColor("#FDEE00"));
        put("cup", Color.parseColor("#568203"));
        put("fork", Color.parseColor("#007FFF"));
        put("knife", Color.parseColor("#F0FFFF"));
        put("spoon", Color.parseColor("#89CFF0"));
        put("bowl", Color.parseColor("#A1CAF1"));
        put("banana", Color.parseColor("#F4C2C2"));
        put("apple", Color.parseColor("#FEFEFA"));
        put("sandwich", Color.parseColor("#FF91AF"));
        put("orange", Color.parseColor("#FAE7B5"));
        put("carrot", Color.parseColor("#DA1884"));
        put("broccoli", Color.parseColor("#7C0A02"));
        put("hot dog", Color.parseColor("#848482"));
        put("pizza", Color.parseColor("#BCD4E6"));
        put("donut", Color.parseColor("#9F8170"));
        put("cake", Color.parseColor("#F5F5DC"));

        put("couch", Color.parseColor("#D70040"));
        put("potted plant", Color.parseColor("#FFA6C9"));
        put("bed", Color.parseColor("#00563F"));
        put("dining table", Color.parseColor("#C95A49"));
        put("toilet", Color.parseColor("#ACE1AF"));
        put("tv", Color.parseColor("#007BA7"));
        put("laptop", Color.parseColor("#2F847C"));
        put("mouse", Color.parseColor("#B2FFFF"));
        put("remote", Color.parseColor("#246BCE"));
        put("keyboard", Color.parseColor("#DE3163"));
        put("cell phone", Color.parseColor("#007BA7"));
        put("microwave", Color.parseColor("#2A52BE"));
        put("oven", Color.parseColor("#6D9BC3"));
        put("toaster", Color.parseColor("#1DACD6"));
        put("sink", Color.parseColor("#007AA5"));
        put("refrigerator", Color.parseColor("#E03C31"));
        put("book", Color.parseColor("#DE6FA1"));
        put("clock", Color.parseColor("#FFB200"));
        put("vase", Color.parseColor("#D2691E"));

        put("scissors", Color.parseColor("#9FA91F"));
        put("teddy bear", Color.parseColor("#0047AB"));
        put("hair drier", Color.parseColor("#FFD300"));
        put("toothbrush", Color.parseColor("#666699"));

    }};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = com.app.rasitagac.btp.blaind.databinding.ActivityCaptureBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            Intent data = result.getData();
            try {
                assert data != null;
                Bitmap picture = (Bitmap) data.getExtras().get("data");
                sourceBitmap = (Bitmap) data.getExtras().get("data");
                binding.inputImv.setImageBitmap(picture);
            } catch (Exception e) {
                Log.e(TAG, "cameraLauncher's onActivityResult : " + e.getMessage());
            }
        });

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            Intent data = result.getData();
            try {
                assert data != null;
                Bitmap picture = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                sourceBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                binding.inputImv.setImageBitmap(picture);
            } catch (Exception e) {
                Log.e(TAG, "cameraLauncher's onActivityResult : " + e.getMessage());
            }
        });


        binding.choosePictureMb.setOnClickListener(view -> {
            String[] options = {"Camera", "Gallery"};
            AlertDialog.Builder builder = new AlertDialog.Builder(CaptureActivity.this);
            builder.setTitle("Select an Option");
            builder.setItems(options, (dialogInterface, i) -> {
                if (i == 0) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    cameraLauncher.launch(cameraIntent);
                } else {
                    Intent storageIntent = new Intent();
                    storageIntent.setType("image/*");
                    storageIntent.setAction(Intent.ACTION_GET_CONTENT);
                    galleryLauncher.launch(storageIntent);
                }
            });
            builder.show();
        });


        binding.detectMb.setOnClickListener(view -> {
            if (sourceBitmap == null) {
                Toast.makeText(this, "Please select an Image First!!", Toast.LENGTH_SHORT).show();
            } else {
                croppedBitmap = Utils.processBitmap(sourceBitmap, TF_OD_API_INPUT_SIZE);
                createModel();
                Handler handler = new Handler();

                new Thread(() -> {
                    final List<Detector.Recognition> results = detector.recognizeImage(croppedBitmap);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            handleResult(croppedBitmap, results);
                        }
                    });
                }).start();
            }
        });

        this.textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech.setLanguage(new Locale("tr", "TR"));
                    textToSpeech.setSpeechRate(0.8f);
                    LOGGER.i("onCreate", "TextToSpeech is initialised");
                } else {
                    LOGGER.e("onCreate", "Cannot initialise text to speech!");
                }
            }
        });
    }


    private void handleResult(Bitmap croppedBitmap, List<Detector.Recognition> results) {
        final Canvas canvas = new Canvas(croppedBitmap);
        final Paint paint = new Paint();
        cropToFrameTransform = new Matrix();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.0f);

        String objects = "";

        final List<Detector.Recognition> mappedRecognitions =
                new LinkedList<Detector.Recognition>();

        for (final Detector.Recognition result : results) {
            final RectF location = result.getLocation();
            if (location != null && result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API) {
                paint.setTextSize(20);
                if (colorMap.containsKey(result.getTitle())) {
                    try {
                        paint.setColor(colorMap.get(result.getTitle()));
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                } else {
                    paint.setColor(Color.RED);
                }
                canvas.drawText(result.getTitle() + "  " + result.getConfidence(), location.left, location.top + 10, paint);
                canvas.drawRect(location, paint);
                objects += result.getTitle() + " " + result.getConfidence() + "\n";
                result.setLocation(location);
                mappedRecognitions.add(result);
            }

        }
        toSpeech(mappedRecognitions);
        binding.inputImv.setImageBitmap(croppedBitmap);
        binding.resultTv.setText(objects);
    }

    private void createModel() {
        previewHeight = TF_OD_API_INPUT_SIZE;
        previewWidth = TF_OD_API_INPUT_SIZE;
        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE,
                        sensorOrientation, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        tracker = new MultiBoxTracker(this);
        trackingOverlay = findViewById(R.id.tracking_overlay);
        trackingOverlay.addCallback(
                canvas -> tracker.draw(canvas));

        tracker.setFrameConfiguration(TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, sensorOrientation);

        int cropSize = TF_OD_API_INPUT_SIZE;

        try {
            detector =
                    TFLiteObjectDetectionAPIModel.create(
                            this,
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_INPUT_SIZE,
                            TF_OD_API_IS_QUANTIZED);
            cropSize = TF_OD_API_INPUT_SIZE;
        } catch (final IOException e) {
            e.printStackTrace();
            LOGGER.e(e, "Exception initializing Detector!");
            Toast toast =
                    Toast.makeText(
                            getApplicationContext(), "Detector could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        checkPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
    }

    private void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission Already Granted.", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, READ_STORAGE_PERMISSION_CODE);
            } else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_STORAGE_PERMISSION_CODE);
            } else {
                Toast.makeText(this, "Read Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == WRITE_STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "All permissions Granted.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Write Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected void toSpeech(List<Detector.Recognition> recognitions) {
        if (recognitions.isEmpty() || textToSpeech.isSpeaking()) {
            currentRecognitions = Collections.emptyList();
            return;
        }

        if (currentRecognitions != null) {

            // Ignore if current and new are same.
            if (currentRecognitions.equals(recognitions)) {
                return;
            }
            final Set<Detector.Recognition> intersection = new HashSet<>(recognitions);
            intersection.retainAll(currentRecognitions);

            // Ignore if new is sub set of the current
            if (intersection.equals(recognitions)) {
                return;
            }
        }

        currentRecognitions = recognitions;

        speak();
    }

    private void speak() {

        final double rightStart = previewWidth / 2 - 0.10 * previewWidth;
        final double rightFinish = previewWidth;
        final double letStart = 0;
        final double leftFinish = previewWidth / 2 + 0.10 * previewWidth;
        final double previewArea = previewWidth * previewHeight;

        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < currentRecognitions.size(); i++) {
            Detector.Recognition recognition = currentRecognitions.get(i);
            stringBuilder.append(recognition.getTitle());

            float start = recognition.getLocation().left;
            float end = recognition.getLocation().right;
            double objArea = recognition.getLocation().width() * recognition.getLocation().height();

            if (objArea > previewArea / 2) {
                stringBuilder.append(" in front of you, Alert!!! Alert!!! Alert!!! you may Collide");
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                // Vibrate for 500 milliseconds
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    //deprecated in API 26
                    v.vibrate(500);
                }
            } else {


                if (start > letStart && end < leftFinish) {
                    stringBuilder.append(" on the left ");
                } else if (start > rightStart && end < rightFinish) {
                    stringBuilder.append(" on the right ");
                } else {
                    stringBuilder.append(" in front of you ");
                }
            }

            if (i + 1 < currentRecognitions.size()) {
                stringBuilder.append(" and ");
            }
        }

        textToSpeech.speak(stringBuilder.toString(), TextToSpeech.QUEUE_FLUSH, null, null);
    }


    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}