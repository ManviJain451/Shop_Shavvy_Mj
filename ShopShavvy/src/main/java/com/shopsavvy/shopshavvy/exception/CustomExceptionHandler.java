package com.shopsavvy.shopshavvy.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ErrorDetails> handleAllExceptions(Exception exception, WebRequest request) throws Exception {
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(),
                exception.getMessage(), request.getDescription(false));

        return new ResponseEntity<ErrorDetails>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @ExceptionHandler(LockedException.class)
    public final ResponseEntity<ErrorDetails> handleLockedExceptions(Exception exception, WebRequest request) throws Exception {
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(),
                exception.getMessage(), request.getDescription(false));

        return new ResponseEntity<ErrorDetails>(errorDetails, HttpStatus.LOCKED);

    }

    @ExceptionHandler(UserNotFoundException.class)
    public final ResponseEntity<ErrorDetails> handleUserNotFoundException(Exception exception, WebRequest request) throws Exception {
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(),
                exception.getMessage(), request.getDescription(false));

        return new ResponseEntity<ErrorDetails>(errorDetails, HttpStatus.NOT_FOUND);

    }

    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleTokenNotFoundException(TokenNotFoundException exception, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(),
                exception.getMessage(), request.getDescription(false));
        return new ResponseEntity<ErrorDetails>(errorDetails, HttpStatus.GONE);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ErrorDetails> handleTokenExpiredException(TokenExpiredException exception, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(),
                exception.getMessage(), request.getDescription(false));
        return new ResponseEntity<ErrorDetails>(errorDetails, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AlreadyActivatedException.class)
    public ResponseEntity<ErrorDetails> handleAlreadyActivatedException(AlreadyActivatedException exception, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(),
                exception.getMessage(), request.getDescription(false));
        return new ResponseEntity<ErrorDetails>(errorDetails, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorDetails> handleInvalidTokenException(InvalidTokenException exception, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(),
                exception.getMessage(), request.getDescription(false));
        return new ResponseEntity<ErrorDetails>(errorDetails, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleResourceNotFoundException(ResourceNotFoundException exception, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(),
                exception.getMessage(), request.getDescription(false));
        return new ResponseEntity<ErrorDetails>(errorDetails, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidEmailException.class)
    public ResponseEntity<ErrorDetails> handleInvalidEmailException(InvalidEmailException exception, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(),
                exception.getMessage(), request.getDescription(false));
        return new ResponseEntity<ErrorDetails>(errorDetails, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AlreadyDeactivatedException.class)
    public ResponseEntity<ErrorDetails> handleAlreadyDeactivatedException(AlreadyDeactivatedException exception, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(),
                exception.getMessage(), request.getDescription(false));
        return new ResponseEntity<ErrorDetails>(errorDetails, HttpStatus.CONFLICT);
    }


    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorDetails> handleEmailAlreadyExistsException(EmailAlreadyExistsException exception, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(),
                exception.getMessage(), request.getDescription(false));
        return new ResponseEntity<ErrorDetails>(errorDetails, HttpStatus.CONFLICT);
    }


    @ExceptionHandler(DuplicateEntryExistsException.class)
    public final ResponseEntity<ErrorDetails> handleDuplicateEntryExistsException(DuplicateEntryExistsException exception, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), exception.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MaximumUploadSizeExceededException.class)
    public ResponseEntity<String> handleMaximumUploadSizeExceededException(MaximumUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body("File size exceeds the maximum allowed limit. Please upload a smaller file.");
    }

    @ExceptionHandler(PasswordMismatchException.class)
    public final ResponseEntity<ErrorDetails> handlePasswordMismatchException(PasswordMismatchException exception, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), exception.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            org.springframework.http.HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

}



//@ControllerAdvice
//@RequiredArgsConstructor
//public class CustomExceptionHandler extends ResponseEntityExceptionHandler {
//
//    private final MessageSource messageSource;
//    private Locale locale;
//
//    @ModelAttribute
//    public void initialiseLocale() {
//        this.locale = LocaleContextHolder.getLocale();
//    }
//
//    private ErrorDetails createErrorDetails(String messageKey, WebRequest request) {
//        return new ErrorDetails(
//                LocalDateTime.now(),
//                messageSource.getMessage(messageKey, null, locale),
//                request.getDescription(false)
//        );
//    }
//
//    @ExceptionHandler(Exception.class)
//    public final ResponseEntity<ErrorDetails> handleAllExceptions(Exception ex, WebRequest request) {
//        return new ResponseEntity<>(
//                createErrorDetails("error.general", request),
//                HttpStatus.INTERNAL_SERVER_ERROR
//        );
//    }
//
//    @ExceptionHandler(LockedException.class)
//    public final ResponseEntity<ErrorDetails> handleLockedExceptions(LockedException ex, WebRequest request) {
//        return new ResponseEntity<>(
//                createErrorDetails("error.account.locked", request),
//                HttpStatus.LOCKED
//        );
//    }
//
//    @ExceptionHandler(UserNotFoundException.class)
//    public final ResponseEntity<ErrorDetails> handleUserNotFoundException(UserNotFoundException ex, WebRequest request) {
//        return new ResponseEntity<>(
//                createErrorDetails("error.user.not.found", request),
//                HttpStatus.NOT_FOUND
//        );
//    }
//
//    @ExceptionHandler(TokenNotFoundException.class)
//    public ResponseEntity<ErrorDetails> handleTokenNotFoundException(TokenNotFoundException ex, WebRequest request) {
//        return new ResponseEntity<>(
//                createErrorDetails("error.token.not.found", request),
//                HttpStatus.GONE
//        );
//    }
//
//    @ExceptionHandler(TokenExpiredException.class)
//    public ResponseEntity<ErrorDetails> handleTokenExpiredException(TokenExpiredException ex, WebRequest request) {
//        return new ResponseEntity<>(
//                createErrorDetails("error.token.expired", request),
//                HttpStatus.UNAUTHORIZED
//        );
//    }
//
//    @ExceptionHandler(AlreadyActivatedException.class)
//    public ResponseEntity<ErrorDetails> handleAlreadyActivatedException(AlreadyActivatedException ex, WebRequest request) {
//        return new ResponseEntity<>(
//                createErrorDetails("error.account.already.activated", request),
//                HttpStatus.CONFLICT
//        );
//    }
//
//    @ExceptionHandler(InvalidTokenException.class)
//    public ResponseEntity<ErrorDetails> handleInvalidTokenException(InvalidTokenException ex, WebRequest request) {
//        return new ResponseEntity<>(
//                createErrorDetails("error.token.invalid", request),
//                HttpStatus.UNAUTHORIZED
//        );
//    }
//
//    @ExceptionHandler(ResourceNotFoundException.class)
//    public ResponseEntity<ErrorDetails> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
//        return new ResponseEntity<>(
//                createErrorDetails("error.resource.not.found", request),
//                HttpStatus.NOT_FOUND
//        );
//    }
//
//    @ExceptionHandler(InvalidEmailException.class)
//    public ResponseEntity<ErrorDetails> handleInvalidEmailException(InvalidEmailException ex, WebRequest request) {
//        return new ResponseEntity<>(
//                createErrorDetails("error.email.invalid", request),
//                HttpStatus.UNAUTHORIZED
//        );
//    }
//
//    @ExceptionHandler(AlreadyDeactivatedException.class)
//    public ResponseEntity<ErrorDetails> handleAlreadyDeactivatedException(AlreadyDeactivatedException ex, WebRequest request) {
//        return new ResponseEntity<>(
//                createErrorDetails("error.account.already.deactivated", request),
//                HttpStatus.CONFLICT
//        );
//    }
//
//    @ExceptionHandler(EmailAlreadyExistsException.class)
//    public ResponseEntity<ErrorDetails> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex, WebRequest request) {
//        return new ResponseEntity<>(
//                createErrorDetails("error.email.exists", request),
//                HttpStatus.CONFLICT
//        );
//    }
//
//    @ExceptionHandler(DuplicateEntryExistsException.class)
//    public final ResponseEntity<ErrorDetails> handleDuplicateEntryExistsException(DuplicateEntryExistsException ex, WebRequest request) {
//        return new ResponseEntity<>(
//                createErrorDetails("error.duplicate.entry", request),
//                HttpStatus.CONFLICT
//        );
//    }
//
//    @ExceptionHandler(MaximumUploadSizeExceededException.class)
//    public ResponseEntity<ErrorDetails> handleMaximumUploadSizeExceededException(MaximumUploadSizeExceededException ex, WebRequest request) {
//        return new ResponseEntity<>(
//                createErrorDetails("error.file.size.exceeded", request),
//                HttpStatus.PAYLOAD_TOO_LARGE
//        );
//    }
//
//    @ExceptionHandler(PasswordMismatchException.class)
//    public final ResponseEntity<ErrorDetails> handlePasswordMismatchException(PasswordMismatchException ex, WebRequest request) {
//        return new ResponseEntity<>(
//                createErrorDetails("error.password.mismatch", request),
//                HttpStatus.UNPROCESSABLE_ENTITY
//        );
//    }
//
//    @Override
//    protected ResponseEntity<Object> handleMethodArgumentNotValid(
//            MethodArgumentNotValidException ex,
//            org.springframework.http.HttpHeaders headers,
//            HttpStatusCode status,
//            WebRequest request) {
//
//        Map<String, String> errors = new HashMap<>();
//        ex.getBindingResult().getFieldErrors().forEach(error ->
//                errors.put(error.getField(), messageSource.getMessage(error.getDefaultMessage(), null, locale))
//        );
//        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
//    }
//}
//
//
