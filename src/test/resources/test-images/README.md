# Test Images Directory

This directory contains test images for the real image upload tests.

## Required Test Images

Place your test images here:

- `test-image.webp` - Primary test image (required)
- `test-image-2.webp` - Secondary test image (optional)

## Supported Formats

The tests support various image formats:
- `.webp` - WebP format (recommended for testing)
- `.jpg` - JPEG format
- `.png` - PNG format
- `.gif` - GIF format

## Test Image Requirements

- **Size**: Any size (tests will verify size consistency)
- **Format**: WebP recommended, but any image format works
- **Content**: Any valid image content
- **Naming**: Use descriptive names like `test-image.webp`

## How to Add Test Images

1. Place your `.webp` file in this directory
2. Name it `test-image.webp`
3. Optionally add `test-image-2.webp` for multiple image tests
4. Run the `RealImageUploadTest` to test upload functionality

## Test Coverage

The `RealImageUploadTest` will test:
- ✅ File upload to local storage
- ✅ File upload to S3 storage (if configured)
- ✅ URL generation for both storage types
- ✅ File size verification
- ✅ File deletion
- ✅ Multiple image uploads
- ✅ Different image formats

## Example Test Images

You can use any small image files for testing. For example:
- Screenshots
- Simple graphics
- Photos
- Icons

The tests will verify that the uploaded file matches the original in size and can be properly retrieved.
