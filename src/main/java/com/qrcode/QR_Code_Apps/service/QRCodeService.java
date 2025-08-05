package com.qrcode.QR_Code_Apps.service;

import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
        String filePath = "C:\\QRCode\\" + mobile + ".png";

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

    public boolean isValidQRCode(String filePath, String mobile) {
        try {
            log.info("Validating QR Code from file: " + filePath);
            log.info("Expected email: " + mobile);
            String qr1 = getQR(filePath);
            log.info("Decoded QR Code text: " + qr1);
            String qr2 = getQR("c:\\QRCode\\"+mobile+".png");
            log.info("Decoded QR Code text: " + qr2);
            return qr1.equals(qr2);
        } catch (Exception e) {
            log.error("Error validating QR Code", e);
            return false;
        }
    }

    private static String getQR(String filePath) throws IOException, NotFoundException, ChecksumException, FormatException {
        java.awt.image.BufferedImage bufferedImage = javax.imageio.ImageIO.read(new java.io.File(filePath));
        LuminanceSource source = new com.google.zxing.client.j2se.BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap bitmap = new BinaryBitmap(new com.google.zxing.common.HybridBinarizer(source));
        Result result = new com.google.zxing.qrcode.QRCodeReader().decode(bitmap);
        return result.getText();
    }

}
