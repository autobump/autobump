import com.github.autobump.core.model.PullRequest;
import model.BitBucketAccount;
import model.BitBucketGitProvider;

public class Main {
    public static void main(String[] args) {
        PullRequest pr = PullRequest.builder()
                .title("test1")
                .branchName("test")
                .repoName("testMavenProject")
                .repoOwner("grietvermeesch")
                .build();
        new BitBucketGitProvider(new BitBucketAccount("glenn.schrooyen@student.kdg.be","AutoBump2209")).MakePullRequest(pr);
    }
}
