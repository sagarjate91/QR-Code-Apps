package com.qrcode.QR_Code_Apps.service;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

@Service
@Slf4j
public class QRCodeService {

    public void generateQRCode(String mobile) {
        if (mobile == null || mobile.isEmpty()) {
            log.error("mobile cannot be null or empty");
            return;
        }

        int width = 500;
        int height = 500;
        String filePath = mobile + ".png";

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = qrCodeWriter.encode(mobile, BarcodeFormat.QR_CODE, width, height);
            Path path = FileSystems.getDefault().getPath(filePath);
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
            log.info("QR Code generated successfully: " + filePath);
        } catch (WriterException | IOException e) {
            log.error("Error generating QR Code", e);
        }
    }

    /**
     * Generate QR code with custom content and filename
     */
    public void generateQRCodeWithContent(String filename, String content) {
        if (filename == null || filename.isEmpty() || content == null || content.isEmpty()) {
            log.error("Filename and content cannot be null or empty");
            return;
        }

        int width = 500;
        int height = 500;

        String filePath = filename + ".png";

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height);
            Path path = FileSystems.getDefault().getPath(filePath);
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
            log.info("QR Code generated successfully for user: " + filePath);
        } catch (WriterException | IOException e) {
            log.error("Error generating QR Code for user", e);
        }
    }



    /**
     * Get the file path of a user's QR code
     */
    public String getUserQRCodePath(String mobile) {
        return mobile + ".png";
    }

    /**
     * Check if QR code exists for a user
     */
    public boolean qrCodeExists(String mobile) {
        File file = new File(getUserQRCodePath(mobile));
        return file.exists();
    }

    /**
     * Decode QR code from uploaded file and extract user information
     */
    public String decodeQRCodeFromFile(MultipartFile file) {
        try {
            // Convert MultipartFile to BufferedImage
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());

            if (bufferedImage == null) {
                log.error("Could not read image from uploaded file");
                return null;
            }

            LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result result = new QRCodeReader().decode(bitmap);

            log.info("Successfully decoded QR code: " + result.getText());
            return result.getText();

        } catch (IOException | NotFoundException | ChecksumException | FormatException e) {
            log.error("Error decoding QR code from uploaded file", e);
            return null;
        }
    }

    /**
     * Extract mobile number from decoded QR code content
     */
    public String extractMobileFromQRContent(String qrContent) {
        if (qrContent == null || qrContent.isEmpty()) {
            return null;
        }

        // Parse the QR content to extract mobile number
        // Expected format: "Name: John Doe\nEmail: john@example.com\nMobile: 1234567890\nUser ID: 1"
        String[] lines = qrContent.split("\n");
        for (String line : lines) {
            if (line.trim().startsWith("Mobile:")) {
                return line.substring(line.indexOf(":") + 1).trim();
            }
        }
        return null;
    }

    /**
     * Extract user ID from decoded QR code content
     */
    public Integer extractUserIdFromQRContent(String qrContent) {
        if (qrContent == null || qrContent.isEmpty()) {
            return null;
        }

        String[] lines = qrContent.split("\n");
        for (String line : lines) {
            if (line.trim().startsWith("User ID:")) {
                try {
                    return Integer.parseInt(line.substring(line.indexOf(":") + 1).trim());
                } catch (NumberFormatException e) {
                    log.error("Invalid User ID format in QR code", e);
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * Read and decode QR code from file path (public method for UserService)
     */
    public String getQR(String filePath) throws IOException, NotFoundException, ChecksumException, FormatException {
        BufferedImage bufferedImage = ImageIO.read(new java.io.File(filePath));
        LuminanceSource source = new com.google.zxing.client.j2se.BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap bitmap = new BinaryBitmap(new com.google.zxing.common.HybridBinarizer(source));
        Result result = new com.google.zxing.qrcode.QRCodeReader().decode(bitmap);
        return result.getText();
    }

}
