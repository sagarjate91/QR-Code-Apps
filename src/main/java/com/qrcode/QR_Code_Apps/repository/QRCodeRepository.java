package com.qrcode.QR_Code_Apps.repository;

import com.qrcode.QR_Code_Apps.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QRCodeRepository extends JpaRepository<User,Integer> {
}
