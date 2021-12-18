package com.app.rasitagac.btp.blaind;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.app.rasitagac.btp.blaind.databinding.ActivityYoloV3Binding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions;
import com.google.firebase.ml.modeldownloader.DownloadType;
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader;

import java.io.File;

public class YoloV3Activity extends AppCompatActivity {
    ActivityYoloV3Binding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        binding = ActivityYoloV3Binding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        binding.streamMb.setOnClickListener(view -> startActivity(new Intent(YoloV3Activity.this, DetectorActivity.class)));
        binding.captureMb.setOnClickListener(view -> startActivity(new Intent(YoloV3Activity.this, CaptureActivity.class)));
        CustomModelDownloadConditions conditions = new CustomModelDownloadConditions.Builder()
                .requireWifi()  // Also possible: .requireCharging() and .requireDeviceIdle()
                .build();
        FirebaseModelDownloader.getInstance()
                .getModel("Object-Detector", DownloadType.LOCAL_MODEL_UPDATE_IN_BACKGROUND, conditions)
                .addOnSuccessListener(model -> {

                    Toast.makeText(YoloV3Activity.this, "Model Yüklemesi Başarılı!", Toast.LENGTH_SHORT).show();

                    // Download complete. Depending on your app, you could enable the ML
                    // feature, or switch from the local model to the remote model, etc.

                    // The CustomModel object contains the local path of the model file,
                    // which you can use to instantiate a TensorFlow Lite interpreter.
                    File modelFile = model.getFile();
                    if (modelFile != null) {
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(YoloV3Activity.this, "Oops!!Model Yüklemesi Başarısız!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}