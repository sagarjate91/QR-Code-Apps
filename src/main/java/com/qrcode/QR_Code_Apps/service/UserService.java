package com.qrcode.QR_Code_Apps.service;

import com.qrcode.QR_Code_Apps.dto.UserDto;
import com.qrcode.QR_Code_Apps.entity.User;
import com.qrcode.QR_Code_Apps.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QRCodeService qrCodeService;

    public String registerUser(UserDto userDto) {
        try {
            // Check if user already exists
            if (userRepository.existsByEmail(userDto.getEmail())) {
                return "User with this email already exists!";
            }

            // Validate required fields
            if (userDto.getFirstName() == null || userDto.getFirstName().trim().isEmpty()) {
                return "First name is required!";
            }
            if (userDto.getLastName() == null || userDto.getLastName().trim().isEmpty()) {
                return "Last name is required!";
            }
            if (userDto.getEmail() == null || userDto.getEmail().trim().isEmpty()) {
                return "Email is required!";
            }
            if (userDto.getPassword() == null || userDto.getPassword().trim().isEmpty()) {
                return "Password is required!";
            }
            if (userDto.getMobile() == null || userDto.getMobile().trim().isEmpty()) {
                return "Mobile number is required!";
            }

            // Validate email format
            if (!isValidEmail(userDto.getEmail())) {
                return "Please enter a valid email address!";
            }

            // Validate password strength
            if (userDto.getPassword().length() < 6) {
                return "Password must be at least 6 characters long!";
            }
            String qrCodeFileName = userDto.getFirstName()+"_"+userDto.getLastName()+"_"+userDto.getMobile();

            // Create new user entity
            User user = new User();
            user.setFirstName(userDto.getFirstName().trim());
            user.setLastName(userDto.getLastName().trim());
            user.setEmail(userDto.getEmail().toLowerCase().trim());
            user.setMobile(userDto.getMobile().trim());
            user.setPassword(hashPassword(userDto.getPassword()));
            user.setQrCodeFileName(qrCodeFileName);
            user.setAddress(userDto.getAddress() != null ? userDto.getAddress().trim() : "");

            // Save user to database
            User savedUser = userRepository.save(user);

            // Generate QR code for the user after successful registration
            try {
                generateUserQRCode(savedUser);
            } catch (Exception e) {
                // Log the error but don't fail the registration
                System.err.println("Failed to generate QR code for user: " + e.getMessage());
            }

            return "SUCCESS";

        } catch (Exception e) {
            return "Registration failed: " + e.getMessage();
        }
    }

    public String authenticateUser(String email, String password) {
        try {
            if (email == null || email.trim().isEmpty()) {
                return "Email is required!";
            }
            if (password == null || password.trim().isEmpty()) {
                return "Password is required!";
            }

            Optional<User> userOptional = userRepository.findByEmail(email.toLowerCase().trim());

            if (userOptional.isEmpty()) {
                return "Invalid email or password!";
            }

            User user = userOptional.get();

            // Check if password matches
            if (verifyPassword(password, user.getPassword())) {
                return "SUCCESS";
            } else {
                return "Invalid email or password!";
            }

        } catch (Exception e) {
            return "Login failed: " + e.getMessage();
        }
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase().trim());
    }

    public UserDto convertToDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setUserId(user.getUserId());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setEmail(user.getEmail());
        userDto.setMobile(user.getMobile());
        userDto.setAddress(user.getAddress());
        // Note: We don't include password in DTO for security
        return userDto;
    }

    // Simple password hashing using SHA-256
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    // Verify password against hash
    private boolean verifyPassword(String password, String hashedPassword) {
        return hashPassword(password).equals(hashedPassword);
    }

    // Simple email validation
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[\\w\\.-]+@[\\w\\.-]+\\.[a-zA-Z]{2,}$");
    }

    /**
     * Generate QR code for user containing their profile information
     */
    private void generateUserQRCode(User user) {
        // Create user info string for QR code
        String userInfo = String.format(
            "Name: %s %s\nEmail: %s\nMobile: %s\nUser ID: %d",
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getMobile(),
            user.getUserId()
        );

        // Generate QR code using the mobile number as filename
        qrCodeService.generateQRCodeWithContent(user.getMobile(), userInfo);
    }

    /**
     * Authenticate user using uploaded QR code
     */
    public String authenticateUserWithQRCode(org.springframework.web.multipart.MultipartFile qrFile) {
        try {
            if (qrFile == null || qrFile.isEmpty()) {
                return "Please upload a QR code image!";
            }

            // Validate file type
            String contentType = qrFile.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return "Please upload a valid image file!";
            }

            // Decode the QR code
            String qrContent = qrCodeService.decodeQRCodeFromFile(qrFile);
            if (qrContent == null) {
                return "Could not read QR code from the uploaded image!";
            }

            // Extract user information from QR content
            String mobile = qrCodeService.extractMobileFromQRContent(qrContent);
            Integer userId = qrCodeService.extractUserIdFromQRContent(qrContent);

            if (mobile == null || userId == null) {
                return "Invalid QR code format! Please use a valid user QR code.";
            }

            // Find user by mobile number
            Optional<User> userOptional = userRepository.findByMobile(mobile);
            if (userOptional.isEmpty()) {
                return "User not found! Please check your QR code.";
            }

            User user = userOptional.get();

            // Verify that the user ID matches
            if (!user.getUserId().equals(userId)) {
                return "QR code validation failed! Invalid user information.";
            }

            // Additional verification: check if stored QR code matches
            if (qrCodeService.qrCodeExists(mobile)) {
                try {
                    String storedQRPath = qrCodeService.getUserQRCodePath(mobile);
                    String storedQRContent = qrCodeService.getQR(storedQRPath);

                    if (!qrContent.equals(storedQRContent)) {
                        return "QR code validation failed! The QR code doesn't match our records.";
                    }
                } catch (Exception e) {
                    System.err.println("Warning: Could not verify against stored QR code: " + e.getMessage());
                    // Continue with login even if stored QR verification fails
                }
            }

            return "SUCCESS";

        } catch (Exception e) {
            return "QR code authentication failed: " + e.getMessage();
        }
    }

    /**
     * Find user by mobile number
     */
    public Optional<User> findByMobile(String mobile) {
        return userRepository.findByMobile(mobile);
    }
}
