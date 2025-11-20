#!/bin/bash

# Script to push test images to Android emulator for Lineup app testing
# Usage: ./push_test_images.sh [path_to_image_directory]

echo "📱 Lineup App - Test Image Pusher"
echo "=================================="

# Check if adb is available
if ! command -v adb &> /dev/null; then
    echo "❌ Error: adb not found. Please install Android SDK Platform Tools."
    exit 1
fi

# Check if emulator is running
if ! adb devices | grep -q "emulator"; then
    echo "❌ Error: No emulator detected. Please start your Android emulator first."
    exit 1
fi

# Get the image directory from argument or use current directory
IMAGE_DIR="${1:-.}"

if [ ! -d "$IMAGE_DIR" ]; then
    echo "❌ Error: Directory '$IMAGE_DIR' not found."
    exit 1
fi

echo ""
echo "📂 Looking for images in: $IMAGE_DIR"
echo ""

# Create Pictures directory on emulator if it doesn't exist
adb shell mkdir -p /sdcard/Pictures/LineupTestImages

# Counter for pushed images
count=0

# Push all image files (jpg, jpeg, png)
shopt -s nullglob
for img in "$IMAGE_DIR"/*.jpg "$IMAGE_DIR"/*.jpeg "$IMAGE_DIR"/*.png "$IMAGE_DIR"/*.JPG "$IMAGE_DIR"/*.JPEG "$IMAGE_DIR"/*.PNG; do
    if [ -f "$img" ]; then
        filename=$(basename "$img")
        echo "⬆️  Pushing: $filename"
        adb push "$img" /sdcard/Pictures/LineupTestImages/
        count=$((count + 1))
    fi
done
shopt -u nullglob

if [ $count -eq 0 ]; then
    echo "⚠️  No image files found in $IMAGE_DIR"
    echo ""
    echo "💡 Tip: Place .jpg, .jpeg, or .png files in a directory and run:"
    echo "   ./push_test_images.sh /path/to/your/images"
else
    echo ""
    echo "✅ Successfully pushed $count image(s) to emulator!"
    echo ""
    echo "📍 Images location: /sdcard/Pictures/LineupTestImages/"
    echo ""
    echo "🎯 Next steps:"
    echo "   1. Open the Lineup app on your emulator"
    echo "   2. Navigate to any restaurant detail screen"
    echo "   3. Click 'Or choose from gallery'"
    echo "   4. Look in 'Pictures/LineupTestImages' folder"
    echo "   5. Select a test image to analyze"
fi

echo ""
