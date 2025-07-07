# Android Gallery App

## 🖼️ Overview
This project is a simple Android gallery application that displays all photos stored on a device. The app presents a scrollable grid of images and opens individual photos in full-screen view upon selection.

---

## ✅ Features
- Displays a **scrollable grid** of thumbnails using `GridView` or `RecyclerView`
- Opens **full-size image view** in a separate activity
- Retrieves photos using the **MediaStore content provider**
- Loads photos in the background using a custom Adapter
- Handles metadata such as **id**, **orientation**, **width**, and **height**
- Targets **API level 33+** and requests runtime permission for `READ_MEDIA_IMAGES`
- Supports **large images** (up to 12MP)
- App handles **device rotation** and **photo deletion** while active

---

## 🛠️ Known Issues / Improvement Suggestions

- **Image flickering**:
  - Occurs while loading thumbnails.
  - Suggested fix: Clear the ImageView before loading and ensure view position consistency using tags.

- **Full-size image crash with large files**:
  - Happens when opening high-resolution images without downsampling.
  - Suggested fix: Downsample large images before loading into memory.

- **Incorrect image orientation**:
  - Some images display rotated incorrectly.
  - Suggested fix: Adjust display using orientation data from MediaStore or Exif metadata.

---

## ⚙️ Build & Run Instructions

1. Open the project in **Android Studio**
2. Run on a device with photo content and **Android 13+ (API 33)**
3. Grant the `READ_MEDIA_IMAGES` permission at runtime
4. Browse the photo grid, tap a photo to view full-screen

