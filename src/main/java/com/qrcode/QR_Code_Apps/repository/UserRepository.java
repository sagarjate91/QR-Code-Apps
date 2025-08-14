package com.qrcode.QR_Code_Apps.repository;

import com.qrcode.QR_Code_Apps.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    Optional<User> findByMobile(String mobile);

    boolean existsByEmail(String email);

    Optional<User> findByEmailAndPassword(String email, String password);
}
