package com.example.proyecto1;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.proyecto1.workers.SaveProfilePic_Worker;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Camera_Activity extends AppCompatActivity {

    private ImageView imageViewProfile;
    private Button btnTakePhoto, btnChooseFromGallery, btnSaveProfile;
    private Uri photoUri;
    private ActivityResultLauncher<Intent> takePictureLauncher;
    private ActivityResultLauncher<String> pickImageLauncher;
    private ImageButton back_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);


        imageViewProfile = findViewById(R.id.imageViewProfile);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnChooseFromGallery = findViewById(R.id.btnChooseFromGallery);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        back_button = findViewById(R.id.backButton);

        // Inicializar los launchers
        initializeLaunchers();

        btnTakePhoto.setOnClickListener(v -> takePhoto());
        btnChooseFromGallery.setOnClickListener(v -> chooseFromGallery());
        btnSaveProfile.setOnClickListener(v -> saveProfilePicture());

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Aplicamos la imagen actual
        if (Data_Load.getDL().getProfPic() != null) {
            byte[] imageBytes = Base64.decode(Data_Load.getDL().getProfPic(), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            imageViewProfile.setImageBitmap(bitmap);
        }else{
            imageViewProfile.setImageResource(R.drawable.default_profile_pic);
        }

    }



    private void initializeLaunchers() {
        // Launcher para tomar foto
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Si la foto se guardó en un archivo, cargarla desde el URI
                        if (photoUri != null) {
                            try {
                                Bitmap bitmap = BitmapFactory.decodeStream(
                                        getContentResolver().openInputStream(photoUri));
                                imageViewProfile.setImageBitmap(bitmap);
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Si no se especificó URI, obtener la miniatura
                            Bundle extras = result.getData().getExtras();
                            Bitmap imageBitmap = (Bitmap) extras.get("data");
                            imageViewProfile.setImageBitmap(imageBitmap);
                        }
                    }
                });

        // Launcher para seleccionar imagen de la galería
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        photoUri = uri;
                        imageViewProfile.setImageURI(uri);
                    }
                });
    }

    private void takePhoto() {
        // Verificar permisos antes de continuar
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, 100);
            return;
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Crear un archivo para guardar la foto
        File photoFile = createImageFile();
        if (photoFile != null) {
            photoUri = FileProvider.getUriForFile(this,
                    "com.example.yourapp.fileprovider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        }

        takePictureLauncher.launch(takePictureIntent);
    }

    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        try {
            File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
            return image;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void chooseFromGallery() {
        // Verificar permisos antes de continuar
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
            return;
        }

        pickImageLauncher.launch("image/*");
    }

    private void saveProfilePicture() {
        if (photoUri == null) {
            Toast.makeText(this, "No hay imagen para guardar", Toast.LENGTH_SHORT).show();
            return;
        }


        // Crear los datos de entrada para el Worker
        Data inputData = new Data.Builder()
                .putString(SaveProfilePic_Worker.KEY_USERNAME, Data_Load.getDL().getUsername())
                .putString(SaveProfilePic_Worker.KEY_IMAGE_URI, photoUri.toString())
                .build();

        // Crear y encolar el trabajo
        OneTimeWorkRequest saveWorkRequest =
                new OneTimeWorkRequest.Builder(SaveProfilePic_Worker.class)
                        .setInputData(inputData)
                        .build();

        WorkManager.getInstance(this).enqueue(saveWorkRequest);

        // Observar el resultado del Worker
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(saveWorkRequest.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null) {
                        if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                            Toast.makeText(Camera_Activity.this,
                                    "Foto de perfil guardada con éxito",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        } else if (workInfo.getState() == WorkInfo.State.FAILED) {
                            Toast.makeText(Camera_Activity.this,
                                    "Error al guardar la foto de perfil",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            takePhoto();
        } else if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            chooseFromGallery();
        } else {
            Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show();
        }
    }
}