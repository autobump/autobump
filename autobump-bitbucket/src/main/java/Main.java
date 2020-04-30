import model.BitBucketGitProvider;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        new BitBucketGitProvider().MakePullRequest();
    }
}
