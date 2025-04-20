package com.example.proyecto1;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Locale;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.example.proyecto1.workers.LoadProfilePic_Worker;

public class Options_Activity extends AppCompatActivity {

    private ImageButton profile_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.options_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        ImageButton back_button = findViewById(R.id.back_button);
        ImageButton instagram_button = findViewById(R.id.instagram_button);
        ImageButton twitter_button = findViewById(R.id.twitter_button);
        ImageButton youtube_button = findViewById(R.id.youtube_button);
        ImageButton spanish_button = findViewById(R.id.spanish_button);
        ImageButton english_button = findViewById(R.id.english_button);
        profile_button = findViewById(R.id.profile_button);
        TextView username_text = findViewById(R.id.user_text);

        // Cargar la foto de perfil al iniciar la actividad
        loadProfilePicture();
        username_text.setText(Data_Load.getDL().getUsername());

        String currentLanguage = Locale.getDefault().getLanguage();
        if (currentLanguage.equals("en")){
            english_button.setEnabled(false);
            english_button.setAlpha(0.5f);
            spanish_button.setEnabled(true);
            spanish_button.setAlpha(1.0f);
        } else{
            english_button.setEnabled(true);
            english_button.setAlpha(1.0f);
            spanish_button.setEnabled(false);
            spanish_button.setAlpha(0.5f);
        }

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        profile_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Options_Activity.this, Camera_Activity.class);
                startActivity(intent);
            }
        });

        instagram_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com"));
                startActivity(intent);
            }
        });

        twitter_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com"));
                startActivity(intent);
            }
        });

        youtube_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://youtube.com"));
                startActivity(intent);
            }
        });

        spanish_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Options_Activity.this,getString(R.string.language_toast),Toast.LENGTH_SHORT).show();
                english_button.setEnabled(true);
                english_button.setAlpha(1.0f);
                spanish_button.setEnabled(false);
                spanish_button.setAlpha(0.5f);
                Language_Helper.setLocale(Options_Activity.this,Locale.getDefault().getLanguage());
            }
        });

        english_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Language_Helper.setLocale(Options_Activity.this, "en");
                Toast.makeText(Options_Activity.this,getString(R.string.language_toast),Toast.LENGTH_SHORT).show();
                english_button.setEnabled(false);
                english_button.setAlpha(0.5f);
                spanish_button.setEnabled(true);
                spanish_button.setAlpha(1.0f);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Volver a cargar la foto de perfil cuando la actividad se reanuda (por ejemplo, al volver de Camera_Activity)
        loadProfilePicture();
    }

    private void loadProfilePicture() {

        Data inputData = new Data.Builder()
                .putString(LoadProfilePic_Worker.KEY_USERNAME, Data_Load.getDL().getUsername())
                .build();

        OneTimeWorkRequest loadWorkRequest =
                new OneTimeWorkRequest.Builder(LoadProfilePic_Worker.class)
                        .setInputData(inputData)
                        .build();

        WorkManager.getInstance(this).enqueue(loadWorkRequest);

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(loadWorkRequest.getId())
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        if (workInfo != null && workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                            if (Data_Load.getDL().getProfPic() != null){
                                try {
                                    //Actualizamos imagen de perfil tomandola de DataLoad
                                    byte[] imageBytes = Base64.decode(Data_Load.getDL().getProfPic(), Base64.DEFAULT);
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                                    if (bitmap != null) {
                                        profile_button.setImageBitmap(bitmap);
                                    }
                                } catch (IllegalArgumentException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });
    }
}