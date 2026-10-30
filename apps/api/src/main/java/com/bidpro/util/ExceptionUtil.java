package com.bidpro.util;
import com.bidpro.custom_exceptions.*;
import com.bidpro.dto.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
@RestControllerAdvice
public class ExceptionUtil {
    private static final Logger LOG = LoggerFactory.getLogger(ExceptionUtil.class);
    private ResponseEntity<ApiResponse<Void>> error(int status, String message, String code) { return ResponseEntity.status(status).body(ApiResponse.error(message, code, status)); }
    @ExceptionHandler({EntityNotFoundException.class, ResourceNotFoundException.class})
    public ResponseEntity<ApiResponse<Void>> missing(RuntimeException ex) { return error(404, ex.getMessage(), "NOT_FOUND"); }
    @ExceptionHandler(AuthenticationFailureException.class)
    public ResponseEntity<ApiResponse<Void>> auth(RuntimeException ex) { return error(401, ex.getMessage(), "INVALID_CREDENTIALS"); }
    @ExceptionHandler({AccessDeniedException.class, ActionNotAllowedException.class})
    public ResponseEntity<ApiResponse<Void>> denied(RuntimeException ex) { return error(403, ex.getMessage(), "FORBIDDEN"); }
    @ExceptionHandler({InvalidInputException.class, IllegalArgumentException.class, CustomException.class, ConstraintViolationException.class})
    public ResponseEntity<ApiResponse<Void>> invalid(RuntimeException ex) { return error(400, ex.getMessage(), "INVALID_INPUT"); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> validation(MethodArgumentNotValidException ex) {
        var fields = new java.util.LinkedHashMap<String, String>();
        ex.getBindingResult().getFieldErrors().forEach(e -> fields.putIfAbsent(e.getField(), e.getDefaultMessage() == null ? "Invalid value" : e.getDefaultMessage()));
        String message = fields.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue()).collect(java.util.stream.Collectors.joining("; "));
        return ResponseEntity.badRequest().body(ApiResponse.<Void>error(message, "VALIDATION_ERROR", 400).withFieldErrors(fields));
    }
    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class,
        MethodArgumentTypeMismatchException.class, MissingServletRequestPartException.class})
    public ResponseEntity<ApiResponse<Void>> malformed(Exception ex) { return error(400, "Request contains missing or invalid values", "INVALID_REQUEST"); }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> conflict(Exception ex) { return error(409, "This change conflicts with existing records", "CONFLICT"); }
    @ExceptionHandler(ConcurrencyFailureException.class)
    public ResponseEntity<ApiResponse<Void>> concurrent(Exception ex) { return error(409, "This record changed during your request. Refresh and try again", "CONCURRENT_CHANGE"); }
    @ExceptionHandler({DataAccessResourceFailureException.class, QueryTimeoutException.class})
    public ResponseEntity<ApiResponse<Void>> unavailable(Exception ex) { return error(503, "A required service is temporarily unavailable. Please try again", "SERVICE_UNAVAILABLE"); }
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> noRoute(Exception ex) { return error(404, "Endpoint not found", "NOT_FOUND"); }
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> method(HttpRequestMethodNotSupportedException ex) {
        var builder = ResponseEntity.status(405);
        if (ex.getSupportedHttpMethods() != null) builder.allow(ex.getSupportedHttpMethods().toArray(org.springframework.http.HttpMethod[]::new));
        return builder.body(ApiResponse.error("This HTTP method is not supported for the endpoint", "METHOD_NOT_ALLOWED", 405));
    }
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> contentType(Exception ex) { return error(415, "Content type is not supported for the endpoint", "UNSUPPORTED_MEDIA_TYPE"); }
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> upload(Exception ex) { return error(413, "Image must be smaller than 5 MB", "FILE_TOO_LARGE"); }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> unexpected(Exception ex) { LOG.error("Unhandled API error", ex); return error(500, "An unexpected server error occurred", "INTERNAL_ERROR"); }
}
