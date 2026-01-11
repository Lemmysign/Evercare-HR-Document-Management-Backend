package com.hrplatform.service.impl;

import com.hrplatform.config.FileStorageConfig;
import com.hrplatform.exception.ImageProcessingException;
import com.hrplatform.service.ImageProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageProcessingServiceImpl implements ImageProcessingService {

    private final FileStorageConfig config;

    /**
     * Process image: strip metadata, resize if needed, re-encode
     * This removes EXIF data, potential exploits, and ensures clean image
     */
    @Override
    public void processImage(File imageFile) throws ImageProcessingException {
        if (!config.isImageProcessingEnabled()) {
            log.debug("⚠️ Image processing disabled - skipping");
            return;
        }

        if (!isImageFile(imageFile)) {
            log.debug("Not an image file, skipping processing: {}", imageFile.getName());
            return;
        }

        try {
            log.info("🖼️ Processing image: {}", imageFile.getName());

            // Read original image
            BufferedImage originalImage = ImageIO.read(imageFile);
            if (originalImage == null) {
                throw new ImageProcessingException("Could not read image file");
            }

            // Check and resize if needed
            BufferedImage processedImage = resizeIfNeeded(originalImage);

            // Strip metadata by re-encoding
            String format = getImageFormat(imageFile);

            // Create clean image without metadata
            boolean written = ImageIO.write(processedImage, format, imageFile);

            if (!written) {
                throw new ImageProcessingException("Failed to write processed image");
            }

            log.info("✅ Image processed successfully: {}", imageFile.getName());

        } catch (Exception e) {
            log.error("❌ Image processing failed for: {}", imageFile.getName(), e);
            throw new ImageProcessingException("Image processing failed: " + e.getMessage());
        }
    }

    private BufferedImage resizeIfNeeded(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int maxDimension = config.getMaxImageDimension();

        // Check if resize needed
        if (width <= maxDimension && height <= maxDimension) {
            return image;
        }

        log.info("🔄 Resizing image from {}x{}", width, height);

        // Calculate new dimensions maintaining aspect ratio
        double scaleFactor = Math.min(
                (double) maxDimension / width,
                (double) maxDimension / height
        );

        int newWidth = (int) (width * scaleFactor);
        int newHeight = (int) (height * scaleFactor);

        // Create resized image with high quality
        BufferedImage resized = new BufferedImage(newWidth, newHeight, image.getType());
        Graphics2D g2d = resized.createGraphics();

        // High quality rendering
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.drawImage(image, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        log.info("✅ Image resized to {}x{}", newWidth, newHeight);
        return resized;
    }

    private String getImageFormat(File file) {
        try (ImageInputStream iis = ImageIO.createImageInputStream(file)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                String format = reader.getFormatName().toLowerCase();
                reader.dispose();
                return format;
            }
        } catch (IOException e) {
            log.warn("Could not determine image format, defaulting to jpg");
        }
        return "jpg";
    }

    private boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png");
    }
}

/*
 * SECURITY NOTES:
 *
 * What this service does:
 * 1. Strips EXIF metadata (GPS, camera info, etc.)
 * 2. Re-encodes images to remove potential exploits
 * 3. Resizes oversized images
 * 4. Creates clean bitmap data
 *
 * Why this matters:
 * - EXIF data can contain sensitive information
 * - Malformed images can exploit image parsers
 * - Large images can cause DoS
 *
 * Additional tools to consider:
 * - ExifTool (external): More comprehensive metadata removal
 * - ImageMagick (external): Advanced processing
 *
 * This implementation uses Java AWT which is built-in and works on both Windows and Linux
 */