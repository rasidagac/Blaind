package com.app.rasitagac.btp.blaind;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.app.rasitagac.btp.blaind.databinding.ActivityWelcomeBinding;

import java.util.Locale;

public class WelcomeActivity extends AppCompatActivity {
    private ActivityWelcomeBinding binding;
    public TextToSpeech textToSpeech;
    private final Handler handler = new Handler();
    public String s = "";
    private Runnable myRunnable, myRunnable1, myRunnable2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWelcomeBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i == TextToSpeech.SUCCESS) {
                    int lang = textToSpeech.setLanguage(new Locale("tr", "TR"));
                    textToSpeech.setSpeechRate(0.8f);
                    checkInternet();
                }
            }
        }, "com.google.android.tts");

        binding.yoloV3Mb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getConnectivityStatusString(WelcomeActivity.this) == null) {
                    checkInternet();
                } else {
                    Intent intent = new Intent(WelcomeActivity.this, YoloV3Activity.class);
                    if (textToSpeech != null) {
                        textToSpeech.stop();
                    }
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        checkInternet();
    }

    void checkInternet() {
        String status = getConnectivityStatusString(this);
        if (status != null) {
            String s = "Sanal Asistanınıza Hoş Geldiniz, Giriş için , Butona Basınız";
            int speech = textToSpeech.speak(s, TextToSpeech.QUEUE_FLUSH, null, null);
            Toast.makeText(WelcomeActivity.this, status, Toast.LENGTH_SHORT).show();

        } else {
            final AlertDialog dialog = new AlertDialog.Builder(WelcomeActivity.this)
                    .setTitle("Uyarı").setMessage("İnternet bağlantınızı kontrol ediniz")
                    .setPositiveButton("Tekrar", null)
                    .create();

            String s = "İnternet bağlantınızı kontrol ediniz";
            int speech = textToSpeech.speak(s, TextToSpeech.QUEUE_FLUSH, null, null);

            dialog.setCanceledOnTouchOutside(false);

            dialog.setOnShowListener(dialogInterface -> {

                Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        String net = getConnectivityStatusString(WelcomeActivity.this);
                        if (net != null) {
                            dialog.dismiss();
                            Toast.makeText(WelcomeActivity.this, net, Toast.LENGTH_SHORT).show();
                            int speech = textToSpeech.speak(net, TextToSpeech.QUEUE_FLUSH, null, null);
                        } else {
                            Toast.makeText(WelcomeActivity.this, "Lütfen tekrardan kontrol ediniz!!", Toast.LENGTH_SHORT).show();
                            String s = "Lütfen tekrardan kontrol ediniz!!";
                            int speech = textToSpeech.speak(s, TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    }
                });
            });
            dialog.show();
        }
    }

    public static String getConnectivityStatusString(Context context) {
        String status = null;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                status = "Wifi etkinleştirildi";
                return status;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                status = "Mobil veri etkinleştirildi";
                return status;
            }
        }
        return status;
    }


    public void Speak() {
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}