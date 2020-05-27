package com.github.autobump.github.model;

import feign.Response;
import feign.codec.ErrorDecoder;

public class GithubErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        Exception exception;
        switch (response.status()) {
            case 400:
                exception = new RuntimeException("Not found");
                break;
            default:
                exception = new RuntimeException(response.reason());
                break;
        }
        return exception;
    }
}
