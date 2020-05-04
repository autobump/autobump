package model;

import exceptions.BranchNotFoundException;
import exceptions.RemoteNotFoundException;
import exceptions.UnauthorizedException;
import feign.Response;
import feign.codec.ErrorDecoder;

public class BitBucketErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        Exception exception;
        switch (response.status()) {
            case 400:
                exception = new BranchNotFoundException("Branch not found");
                break;
            case 401:
                exception = new UnauthorizedException("Could not authenticate");
                break;
            case 404: {
                exception = new RemoteNotFoundException("Remote not found");
                break;
            }
            default:
                exception = new RuntimeException(response.reason());
                break;
        }
        return exception;
    }
}
