package com.example.photogallery;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.io.InputStream;
import java.util.ArrayList;

public class PhotoGallery extends AppCompatActivity {

    private PhotoAdapter photoAdapter;
    private final ArrayList<Uri> imageUris = new ArrayList<>();
    private GridView gridView;
    private ImageView fullImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Here we initialize the main UI by setting up the GridView which displays the thumbnail and the ImageView
        // which will display any chosen photo in fullscreen mode. The PhotoAdapter manages how the images are loaded and displayed.
        // Also a listener is created so that we know when a user chooses/taps an image to be displayed in fullscreen.
        setContentView(R.layout.photo_gallery);
        gridView = findViewById(R.id.gridView);
        fullImageView = findViewById(R.id.fullImageView);
        photoAdapter = new PhotoAdapter(this, imageUris);
        gridView.setAdapter(photoAdapter);
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            Uri imageUri = imageUris.get(position);
            openFullScreen(imageUri);
        });

        checkPermissionAndLoadImages();

        // Here we override the back button so that while in the application, if the user has chosen an image
        // and it is displayed in fullscreen mode, when the back button is pressed the user returns to the GridView
        // containing the thumbnails instead of exiting the application.
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (fullImageView.getVisibility() == View.VISIBLE) {
                    fullImageView.setVisibility(View.GONE);
                    gridView.setVisibility(View.VISIBLE);
                } else {
                    finish();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    // This method is used to open an image to fullscreen mode if the user has chosen.
    // We achieve this by hiding the GridView when a photo is chosen and the ImageView is shown displaying fullscreen mode.
    // Using background thread to avoid congestion on the UI thread, once loaded it can be displayed on the UI thread.
    private void openFullScreen(Uri imageUri) {
        gridView.setVisibility(View.GONE);
        new Thread(() -> {
            try {
                InputStream is = getContentResolver().openInputStream(imageUri);
                final Bitmap bitmap = BitmapFactory.decodeStream(is);
                runOnUiThread(() -> {
                    fullImageView.setImageBitmap(bitmap);
                    fullImageView.setVisibility(View.VISIBLE);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // This method ensures that the app has permission to read images stored on the device. It first checks
    // This method is used to check if the application has been granted permission to read images on the device.
    // If the READ_MEDIA_IMAGES is granted, then it will load images from the MediaStore, if not it will ask user for permission.
    private void checkPermissionAndLoadImages() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                == PackageManager.PERMISSION_GRANTED) {
            loadImagesFromMediaStore();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
        }
    }

    // Permissions granted = load photos, Permissions denied = toast informing user permissions are necessary.
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    loadImagesFromMediaStore();
                } else {
                    Toast.makeText(this, "Permission is necessary for this application to proceed.", Toast.LENGTH_SHORT).show();
                }
            });

    // This method gets the images stored on the device via the MediaStore content provider, obtaining the URI's and then
    // storing them in the imageUris list. Images are loaded from the descending order of the date they were added which
    // ensures most recent images appear at the top or first. When images are all loaded, the adapter will update the GridView.
    private void loadImagesFromMediaStore() {
        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        String[] projection = {
                MediaStore.Images.Media._ID
        };
        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";
        try (Cursor cursor = contentResolver.query(uri, projection, null, null, sortOrder)) {
            if (cursor != null) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idColumn);
                    Uri imageUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
                    imageUris.add(imageUri);
                }
                photoAdapter.notifyDataSetChanged();
            }
        }
    }
}
