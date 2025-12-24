package com.perezper.authserver.dto;

public class StandardRequest<T> {
    private T payload;

    public StandardRequest() {}

    public StandardRequest(T payload) { this.payload = payload; }

    public T getPayload() { return payload; }
    public void setPayload(T payload) { this.payload = payload; }
}
