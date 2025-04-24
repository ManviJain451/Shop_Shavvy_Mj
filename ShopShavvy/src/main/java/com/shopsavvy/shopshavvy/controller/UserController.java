package com.shopsavvy.shopshavvy.controller;

import com.shopsavvy.shopshavvy.dto.passwordDto.UpdatePasswordDTO;
import com.shopsavvy.shopshavvy.configuration.UserDetailsImpl;
import com.shopsavvy.shopshavvy.service.FileStorageService;
import com.shopsavvy.shopshavvy.service.UserService;
import com.shopsavvy.shopshavvy.utilities.SuccessMessageResponse;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
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
    public ResponseEntity<?> uploadUserPhoto(@PathVariable String userId, @RequestParam("file") MultipartFile file) {
        try {
            String message = fileStorageService.saveOrUpdateUserPhoto(userId, file);
            return ResponseEntity.ok(SuccessMessageResponse.success(message));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{userId}/delete-photo")
    public ResponseEntity<SuccessMessageResponse<String>> deleteProfilePhoto(@PathVariable String userId) throws IOException {
        String message = fileStorageService.deleteUserPhoto(userId);
        return ResponseEntity.ok(SuccessMessageResponse.success(message));

    }

    @PutMapping("/update-password")
    public ResponseEntity<SuccessMessageResponse<String>> updatePassword(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
                                                                         @Valid @RequestBody UpdatePasswordDTO updatePasswordDTO) throws MessagingException, BadRequestException {

        String message = userService.updatePassword(userDetailsImpl, updatePasswordDTO);
        return ResponseEntity.ok(SuccessMessageResponse.success(message));
    }
}
