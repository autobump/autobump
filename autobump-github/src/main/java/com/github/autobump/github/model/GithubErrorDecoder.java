package com.github.autobump.github.model;

import com.github.autobump.github.exceptions.GithubApiException;
import com.github.autobump.github.exceptions.GithubBadRequestException;
import com.github.autobump.github.exceptions.GithubNotFoundException;
import com.github.autobump.github.exceptions.GithubUnauthorizedException;
import feign.Response;
import feign.codec.ErrorDecoder;

public class GithubErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        Exception exception;
        switch (response.status()) {
            case 400:
                exception =
                        new GithubBadRequestException("Bad request: " + response.reason());
                break;
            case 401:
                exception =
                        new GithubUnauthorizedException("Could not authenticate: " + response.reason());
                break;
            case 404:
                exception =
                        new GithubNotFoundException("Resource not found: " + response.reason());
                break;
            default:
                exception =
                        new GithubApiException("Github Api error: " + response.reason());
                break;
        }
        return exception;
    }
}
