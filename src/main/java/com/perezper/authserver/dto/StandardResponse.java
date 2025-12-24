package com.perezper.authserver.dto;

public class StandardResponse<T> {
    private boolean success;
    private T data;
    private ErrorResponse error;

    public StandardResponse() {}

    public StandardResponse(boolean success, T data, ErrorResponse error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public ErrorResponse getError() { return error; }
    public void setError(ErrorResponse error) { this.error = error; }
}
