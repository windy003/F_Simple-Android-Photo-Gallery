package com.example.photogallery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PhotoAdapter extends BaseAdapter {

    private final Context context;
    private final List<Uri> imageUris;
    private final ExecutorService executorService;
    private final LruCache<String, Bitmap> memoryCache;

    // This is our constructor, which initializes the context and the list of image URI's. It also sets up a thread pool
    // containing four threads for loading images in the background. LruCache is initialized so we can allocate some memory
    // to cache the images.
    public PhotoAdapter(Context context, List<Uri> imageUris) {
        this.context = context;
        this.imageUris = imageUris;
        this.executorService = Executors.newFixedThreadPool(4);

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        this.memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    // This method checks if a bitmap for the given key is already cached. If it isn't then it adds the bitmap to the cache.
    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemoryCache(key) == null) {
            memoryCache.put(key, bitmap);
        }
    }

    // This is a getter for checking if the image has already been loaded and cached before attempting to reload it.
    private Bitmap getBitmapFromMemoryCache(String key) {
        return memoryCache.get(key);
    }

    // This is a getter for the number of images in the adapter. This helps the adapter display them in the GridView.
    @Override
    public int getCount() {
        return imageUris.size();
    }

    // This is a getter for the URI of the image, used by the adapter to get the correct image for each item in the GridView.
    @Override
    public Object getItem(int position) {
        return imageUris.get(position);
    }

    // This is a getter for the position of the item. The position itself will be used as the ID.
    @Override
    public long getItemId(int position) {
        return position;
    }

    // This method is used for creating and returning the view for each item in the GridView. Checking for recycled views with convertView,
    // if found it will be reused. If the images position is already cached in the memory, the cached image is used.
    // If not, it will load the image on the background thread, store it in the cache and update the view once the image is ready.
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.grid_item_image, parent, false);
            imageView = convertView.findViewById(R.id.imageView);
            convertView.setTag(imageView);
        } else {
            imageView = (ImageView) convertView.getTag();
        }
        Uri imageUri = imageUris.get(position);
        String imageKey = String.valueOf(imageUri);
        Bitmap cachedBitmap = getBitmapFromMemoryCache(imageKey);
        if (cachedBitmap != null) {
            imageView.setImageBitmap(cachedBitmap);
        } else {
            executorService.execute(() -> {
                try (InputStream is = context.getContentResolver().openInputStream(imageUri)) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 4;
                    Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
                    if (bitmap != null) {
                        addBitmapToMemoryCache(imageKey, bitmap);
                        imageView.post(() -> imageView.setImageBitmap(bitmap));
                    } else {
                        imageView.post(() -> imageView.setImageDrawable(null));
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return convertView;
    }
}
