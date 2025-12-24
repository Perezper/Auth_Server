package com.perezper.authserver.exception;

import com.perezper.authserver.dto.ErrorResponse;
import com.perezper.authserver.dto.StandardResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardResponse<Object>> handleAll(Exception ex) {
        StandardResponse<Object> resp = new StandardResponse<>(false, null, new ErrorResponse(ex.getMessage()));
        return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
    }
}
