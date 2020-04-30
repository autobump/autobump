package model;

import lombok.NonNull;
import lombok.Value;

@Value
public class BitBucketAccount {
    @NonNull
    String username;
    @NonNull
    String password;
}
