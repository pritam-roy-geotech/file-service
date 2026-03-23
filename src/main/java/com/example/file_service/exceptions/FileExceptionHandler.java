//package com.example.file_service.exceptions;
//
//
//import org.springframework.http.*;
//import org.springframework.http.ProblemDetail;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MaxUploadSizeExceededException;
//
//import java.nio.file.NoSuchFileException;
//
//@RestControllerAdvice
//public class FileExceptionHandler {
//
//    @ExceptionHandler(IllegalArgumentException.class)
//    public ProblemDetail handleBadRequest(IllegalArgumentException ex) {
//        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
//        pd.setTitle("Invalid File");
//        return pd;
//    }
//
//    @ExceptionHandler(NoSuchFileException.class)
//    public ProblemDetail handleNotFound(NoSuchFileException ex) {
//        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "File not found");
//        pd.setTitle("Not Found");
//        pd.setProperty("key", ex.getFile());
//        return pd;
//    }
//
//    @ExceptionHandler(MaxUploadSizeExceededException.class)
//    public ProblemDetail handleTooLarge(MaxUploadSizeExceededException ex) {
//        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.PAYLOAD_TOO_LARGE, "Upload exceeds maximum size");
//        pd.setTitle("Payload Too Large");
//        return pd;
//    }
//
//    @ExceptionHandler(Exception.class)
//    public ProblemDetail handleGeneric(Exception ex) {
//        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
//        pd.setTitle("Internal Error");
//        return pd;
//    }
//}