package com.bidpro.dto;

import org.springframework.http.HttpStatus;

/**
 * Generic response wrapper for API responses.
 * Encapsulates data, message, status code, and error code for both success and error responses.
 *
 * @param <T> The type of data to be included in the response
 */
public class ApiResponse<T> {

    private T data;
    private String message;
    private int statusCode;
    private String errorCode;
    private boolean success;

    // Constructor for success response
    private ApiResponse(T data, String message, int statusCode) {
        this.data = data;
        this.message = message;
        this.statusCode = statusCode;
        this.success = true;
        this.errorCode = null;
    }

    // Constructor for error response
    private ApiResponse(String message, String errorCode, int statusCode) {
        this.data = null;
        this.message = message;
        this.statusCode = statusCode;
        this.success = false;
        this.errorCode = errorCode;
    }

    /**
     * Creates a success response with the given data and message.
     *
     * @param data    The data to include in the response
     * @param message The success message
     * @param <T>     The type of the data
     * @return ApiResponse instance
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(data, message, HttpStatus.OK.value());
    }

    /**
     * Creates an error response with the given message and error code.
     *
     * @param message   The error message
     * @param errorCode The error code
     * @param <T>       The type of the data (typically null for errors)
     * @return ApiResponse instance
     */
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return new ApiResponse<>(message, errorCode, HttpStatus.BAD_REQUEST.value());
    }

    /**
     * Creates an error response with a custom status code.
     *
     * @param message    The error message
     * @param errorCode  The error code
     * @param statusCode The HTTP status code
     * @param <T>        The type of the data (typically null for errors)
     * @return ApiResponse instance
     */
    public static <T> ApiResponse<T> error(String message, String errorCode, int statusCode) {
        return new ApiResponse<>(message, errorCode, statusCode);
    }

    // Getters and setters
    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}