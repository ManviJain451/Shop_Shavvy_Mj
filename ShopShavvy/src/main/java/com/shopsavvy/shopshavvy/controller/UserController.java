package com.shopsavvy.shopshavvy.controller;

import com.shopsavvy.shopshavvy.dto.passwordDto.PasswordDTO;
import com.shopsavvy.shopshavvy.security.configurations.UserDetailsImpl;
import com.shopsavvy.shopshavvy.service.FileStorageService;
import com.shopsavvy.shopshavvy.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/shop-shavvy/users")
@RequiredArgsConstructor
public class UserController {

    private final FileStorageService fileStorageService;
    private final UserService userService;

    @PostMapping("/{userId}/upload-photo")
    public ResponseEntity<String> uploadUserPhoto(@PathVariable String userId, @RequestParam("file") MultipartFile file) {
        try {
            String message = fileStorageService.saveOrUpdateUserPhoto(userId, file);
            return ResponseEntity.ok(message);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload photo.");
        }
    }

    @DeleteMapping("/{userId}/delete-photo")
    public ResponseEntity<String> deleteProfilePhoto(@PathVariable String userId) throws IOException {
        fileStorageService.deleteUserPhoto(userId);
        return ResponseEntity.ok("Profile photo deleted successfully.");

    }

    @PutMapping("/update-password")
    public ResponseEntity<?> updatePassword(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
                                            @Valid @RequestBody PasswordDTO passwordUpdateDTO) throws MessagingException {

        userService.updatePassword(userDetailsImpl, passwordUpdateDTO);
        return ResponseEntity.ok("Password updated successfully.");
    }
}
