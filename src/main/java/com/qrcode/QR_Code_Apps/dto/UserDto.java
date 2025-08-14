package com.qrcode.QR_Code_Apps.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    private Integer userId;
    private String firstName;
    private String lastName;
    private String email;
    private String mobile;
    private String password;
    private String address;
    private String qrCodeFileName;
}
