# Testing Guide for Emulator

## Quick Start: Push Test Images to Emulator

I've created a helper script to make testing easier on the emulator.

### Step 1: Download Test Images

Download some restaurant/bar line images from Google Images:
- Search: "restaurant queue", "bar line", "coffee shop line"
- Save 2-3 images to the `test_images/` folder in your project

### Step 2: Push Images to Emulator

Run the script from your project directory:

```bash
# Make sure your emulator is running first!
./push_test_images.sh test_images
```

This will:
- ✅ Create a folder on the emulator at `/sdcard/Pictures/LineupTestImages/`
- ✅ Push all images from `test_images/` to the emulator
- ✅ Show you how many images were uploaded

### Step 3: Test in the App

1. Open the Lineup app on your emulator
2. Navigate to any restaurant detail screen
3. Click **"Or choose from gallery"**
4. Navigate to **Pictures → LineupTestImages**
5. Select a test image
6. Wait for the AI analysis (~3-5 seconds)
7. See the estimated wait time!

---

## Alternative: Manual adb Commands

If you prefer to push images manually:

```bash
# Push a single image
adb push /path/to/your/image.jpg /sdcard/Pictures/LineupTestImages/

# Example:
adb push ~/Downloads/restaurant-line.jpg /sdcard/Pictures/LineupTestImages/
```

---

## Troubleshooting

### "adb: command not found"
- Make sure Android SDK Platform Tools are installed
- Add to PATH: `export PATH=$PATH:~/Library/Android/sdk/platform-tools`

### "No emulator detected"
- Start your Android emulator first
- Check with: `adb devices`

### Images not showing in gallery
- Refresh media scanner: `adb shell am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d file:///sdcard/Pictures/LineupTestImages/`

---

## Recommended Test Images

Try these scenarios:

1. **Clear organized line** (15-20 people in a queue)
2. **Crowded venue** (30+ people, dense)
3. **Empty/sparse** (0-5 people)
4. **Indoor vs outdoor** lines
5. **Different venue types** (restaurant, bar, cafe)

The AI should detect different characteristics and adjust wait times accordingly!
