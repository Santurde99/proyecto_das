package com.example.proyecto1;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
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
    private ActivityResultLauncher<PickVisualMediaRequest> pickMediaLauncher;
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
        btnChooseFromGallery.setOnClickListener(v -> pickImage());
        btnSaveProfile.setOnClickListener(v -> saveProfilePicture());

        back_button.setOnClickListener(v -> finish());

        // Aplicamos la imagen actual
        if (Data_Load.getDL().getProfPic() != null) {
            byte[] imageBytes = Base64.decode(Data_Load.getDL().getProfPic(), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            imageViewProfile.setImageBitmap(bitmap);
        } else {
            imageViewProfile.setImageResource(R.drawable.default_profile_pic);
        }
    }

    private void initializeLaunchers() {
        // Launcher para tomar foto (como en tu versión original)
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        if (photoUri != null) {
                            try {
                                Bitmap bitmap = BitmapFactory.decodeStream(
                                        getContentResolver().openInputStream(photoUri));
                                imageViewProfile.setImageBitmap(bitmap);
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(this, getString(R.string.img_load_error), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Bundle extras = result.getData().getExtras();
                            Bitmap imageBitmap = (Bitmap) extras.get("data");
                            imageViewProfile.setImageBitmap(imageBitmap);
                        }
                    }
                });

        // Nuevo launcher para seleccionar imagen de la galería (según el PDF)
        pickMediaLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        photoUri = uri;
                        imageViewProfile.setImageURI(uri);
                    } else {
                        Toast.makeText(this, getString(R.string.no_image_toast), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void pickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launchImagePicker();
        } else {
            // Para versiones anteriores, verificamos permiso READ_EXTERNAL_STORAGE
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                launchImagePicker();
            } else {
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
            }
        }
    }

    private void launchImagePicker() {
        // Lanzamos el selector de imágenes como se muestra en el PDF (página 5)
        pickMediaLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void takePhoto() {
        // (Mantener tu implementación original)
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, 100);
            return;
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
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
        // (Mantener tu implementación original)
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        try {
            return File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveProfilePicture() {
        // (Mantener tu implementación original)
        if (photoUri == null) {
            Toast.makeText(this, getString(R.string.no_image_to_save), Toast.LENGTH_SHORT).show();
            return;
        }

        Data inputData = new Data.Builder()
                .putString(SaveProfilePic_Worker.KEY_USERNAME, Data_Load.getDL().getUsername())
                .putString(SaveProfilePic_Worker.KEY_IMAGE_URI, photoUri.toString())
                .build();

        OneTimeWorkRequest saveWorkRequest = new OneTimeWorkRequest.Builder(SaveProfilePic_Worker.class)
                .setInputData(inputData)
                .build();

        WorkManager.getInstance(this).enqueue(saveWorkRequest);

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(saveWorkRequest.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null && workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                        Toast.makeText(Camera_Activity.this,
                                getString(R.string.succes_change),
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            takePhoto();
        } else if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pickImage();
        } else {
            Toast.makeText(this, getString(R.string.perm_denyed), Toast.LENGTH_SHORT).show();
        }
    }
}