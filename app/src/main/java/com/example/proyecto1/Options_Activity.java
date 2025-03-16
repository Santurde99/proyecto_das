package com.example.proyecto1;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import java.util.Locale;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Options_Activity extends AppCompatActivity {

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


        ImageButton reset_button = findViewById(R.id.reset_button);
        ImageButton back_button = findViewById(R.id.back_button);
        ImageButton instagram_button = findViewById(R.id.instagram_button);
        ImageButton twitter_button = findViewById(R.id.twitter_button);
        ImageButton youtube_button = findViewById(R.id.youtube_button);
        ImageButton spanish_button = findViewById(R.id.spanish_button);
        ImageButton english_button = findViewById(R.id.english_button);

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

        reset_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteDialog();
            }
        });

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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

    private boolean deleteSave() {
        boolean result = false;
        try {
            // Elimina la base de datos
            result = deleteDatabase("database.db");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_diag_title))
                .setMessage(getString(R.string.delete_diag_text))
                .setPositiveButton(getString(R.string.delete_diag_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean isDeleted = deleteSave();
                        // Muestra un mensaje al usuario
                        if (isDeleted) {
                            Toast.makeText(Options_Activity.this, getString(R.string.delete_toast), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Options_Activity.this, getString(R.string.err_delete_toast), Toast.LENGTH_SHORT).show();
                        }
                        finishAffinity();
                    }
                })
                .setNegativeButton(getString(R.string.delete_diag_no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialog.dismiss();
                    }
                })
                .show();
    }
}