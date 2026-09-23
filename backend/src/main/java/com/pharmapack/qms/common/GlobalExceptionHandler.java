package com.pharmapack.qms.common; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import org.springframework.web.server.ResponseStatusException; import java.time.*; import java.util.*;
@RestControllerAdvice public class GlobalExceptionHandler { public record ApiError(String timestamp,int status,String error,String message){}
 @ExceptionHandler(IllegalArgumentException.class) ResponseEntity<ApiError> bad(IllegalArgumentException e){return ResponseEntity.badRequest().body(new ApiError(Instant.now().toString(),400,"BAD_REQUEST",e.getMessage()));}
 // Respects the status/reason a controller (e.g. AuthController's login) deliberately chose, instead of
 // falling through to the generic 500 handler below - without this, "throw new ResponseStatusException(401, ...)"
 // was being reported to callers as a 500 Internal Server Error.
 @ExceptionHandler(ResponseStatusException.class) ResponseEntity<ApiError> statusEx(ResponseStatusException e){int code=e.getStatusCode().value(); return ResponseEntity.status(code).body(new ApiError(Instant.now().toString(),code,e.getStatusCode().toString(),e.getReason()));}
 @ExceptionHandler(Exception.class) ResponseEntity<ApiError> other(Exception e){return ResponseEntity.status(500).body(new ApiError(Instant.now().toString(),500,"INTERNAL_SERVER_ERROR",e.getMessage()));}
}
