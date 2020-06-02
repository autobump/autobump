package com.github.autobump.bitbucket.model;

import com.github.autobump.bitbucket.exceptions.BitbucketApiException;
import com.github.autobump.bitbucket.exceptions.BitbucketBadRequestException;
import com.github.autobump.bitbucket.exceptions.BitbucketNotFoundException;
import com.github.autobump.bitbucket.exceptions.BitbucketUnauthorizedException;
import feign.Response;
import feign.codec.ErrorDecoder;

public class BitBucketErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        Exception exception;
        switch (response.status()) {
            case 400:
                exception =
                        new BitbucketBadRequestException("Bad request: " + response.reason());
                break;
            case 401:
                exception =
                        new BitbucketUnauthorizedException("Could not authenticate: " + response.reason());
                break;
            case 404:
                exception =
                        new BitbucketNotFoundException("Resource not found: " + response.reason());
                break;
            default:
                exception =
                        new BitbucketApiException("Bitbucket Api error: " + response.reason());
                break;
        }
        return exception;
    }
}
