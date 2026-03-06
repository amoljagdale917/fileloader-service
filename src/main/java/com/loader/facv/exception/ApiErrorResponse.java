package com.loader.facv.exception;

import lombok.Value;

@Value
public class ApiErrorResponse {
    String timestamp;
    int status;
    String error;
    String message;
    String path;
}
