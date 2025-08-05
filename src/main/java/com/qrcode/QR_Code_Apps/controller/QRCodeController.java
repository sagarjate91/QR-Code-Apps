package com.qrcode.QR_Code_Apps.controller;

import com.qrcode.QR_Code_Apps.service.QRCodeService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class QRCodeController {

    @PostMapping("/generate")
    public String generateQRCode(@RequestParam("mobile") String mobile) {
        if (mobile == null || mobile.isEmpty()) {
            return "mobile cannot be null or empty";
        }
        QRCodeService qrCodeService = new QRCodeService();
        qrCodeService.generateQRCode(mobile);

        return "QR Code generated successfully for: " + mobile;
    }
    @PostMapping(value = "/validate", consumes = "multipart/form-data")
    public String validateQRCode(@RequestParam("file") MultipartFile file, @RequestParam("mobile") String mobile) {
        try {
            // Save the uploaded file to a temporary location
            java.io.File tempFile = java.io.File.createTempFile("uploaded_qr_", ".png");
            file.transferTo(tempFile);
            QRCodeService qrCodeService = new QRCodeService();
            boolean isValid = qrCodeService.isValidQRCode(tempFile.getAbsolutePath(),mobile);
            tempFile.delete(); // Clean up
            if (isValid) {
                return "QR Code is valid.";
            } else {
                return "QR Code is invalid or does not contain a valid email.";
            }
        } catch (Exception e) {
            return "Error processing file: " + e.getMessage();
        }
    }
}
