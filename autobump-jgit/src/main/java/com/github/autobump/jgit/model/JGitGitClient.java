package com.github.autobump.jgit.model;

import com.github.autobump.core.model.AutoBumpRebaseResult;
import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.CommitResult;
import com.github.autobump.core.model.DeleteBranchResult;
import com.github.autobump.core.model.GitClient;
import com.github.autobump.core.model.Workspace;
import com.github.autobump.jgit.exception.GitException;
import lombok.AllArgsConstructor;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RebaseCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import javax.inject.Named;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import static org.eclipse.jgit.lib.Constants.MASTER;
import static org.eclipse.jgit.lib.Constants.R_HEADS;

@AllArgsConstructor
public class JGitGitClient implements GitClient {
    private final String username;
    private final String password;

    @Override
    public Workspace clone(URI uri) {
        try (Repository repo = Git.cloneRepository().setURI(uri.toString())
                .setDirectory(Files.createTempDirectory("cloned_repos").toFile())
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password))
                .call().getRepository()) {
            return new Workspace(repo.getDirectory().getPath().replace(".git", ""));
        } catch (GitAPIException | IOException e) {
            throw new GitException("something went wrong while cloning the repo", e);
        }
    }

    @Override
    public CommitResult commitToNewBranch(Workspace workspace, Bump bump) {
        try (Git git = Git.open(Path.of(workspace.getProjectRoot()).toFile())) {
            String branchName = createBranch(git, bump);
            String commitMessage = commitAndPushToBranch(git, bump);
            git.checkout().setName(MASTER).call();
            return new CommitResult(branchName, commitMessage);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (GitAPIException g) {
            throw new GitException("Something went wrong while creating the new branch or committing to it", g);
        }
    }

    @Override
    public CommitResult commitToExistingBranch(Workspace workspace, Bump bump, String branchName) {
        try (Git git = Git.open(Path.of(workspace.getProjectRoot()).toFile())) {
            git.checkout().setName(branchName).call();
            String commitMessage = commitAndPushToBranch(git, bump);
            git.checkout().setName(MASTER).call();
            return new CommitResult(branchName, commitMessage);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (GitAPIException g) {
            throw new GitException("Something went wrong while creating the new branch or committing to it", g);
        }
    }

    @Override
    public AutoBumpRebaseResult rebaseBranchFromMaster(Workspace workspace, String branchName) {
        try (Git git = Git.open(Path.of(workspace.getProjectRoot()).toFile())) {
            return getAutoBumpRebaseResult(branchName, git);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (GitAPIException g) {
            throw new GitException("Something went wrong while checking out the branch or rebasing to it", g);
        }
    }

    @Override
    public DeleteBranchResult deleteBranch(Workspace workspace, String branchName) {
        try (Git git = Git.open(Path.of(workspace.getProjectRoot()).toFile())) {
            RefSpec refSpec = new RefSpec()
                    .setSource(null)
                    .setDestination(R_HEADS + branchName);
            pushDeleteChanges(git, refSpec);
            return new DeleteBranchResult(branchName);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (GitAPIException g) {
            throw new GitException("Something went wrong while deleting branch: " + R_HEADS + branchName, g);
        }
    }

    public void pushDeleteChanges(Git git, RefSpec refSpec) throws GitAPIException {
        git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password))
                .setRefSpecs(refSpec).setRemote(Constants.DEFAULT_REMOTE_NAME)
                .call();
    }

    public AutoBumpRebaseResult getAutoBumpRebaseResult(String branchName, Git git)
            throws GitAPIException, IOException {
        checkoutBumpBranch(git, branchName);
        git.rebase().setUpstream(R_HEADS + MASTER).call();
        AutoBumpRebaseResult result = new AutoBumpRebaseResult(!git.status().call().getConflicting().isEmpty());

        if (result.isConflicted()) {
            git.rebase().setOperation(RebaseCommand.Operation.ABORT).call();
            git.merge().include(git.getRepository()
                    .exactRef(R_HEADS + MASTER)).setStrategy(MergeStrategy.THEIRS).call();
        }
        return result;
    }

    private void checkoutBumpBranch(Git git, String branchName) throws GitAPIException {
        var checkout = git.checkout();
        if (!git.branchList().call().stream()
                .map(Ref::getName)
                .collect(Collectors.toUnmodifiableSet())
                .contains(R_HEADS + branchName)) {
            checkout.setCreateBranch(true)
                    .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK).
                    setStartPoint("origin/" + branchName);
        }
        checkout.
                setName(branchName).
                call();
    }

    public String createBranch(Git git, Bump bump) throws GitAPIException {
        String branchName = String.format("autobump/%s/%s",
                bump.getGroup(),
                bump.getUpdatedVersion().getVersionNumber());
        git.branchCreate().setName(branchName).call();
        git.checkout().setName(branchName).call();
        return branchName;
    }

    public String commitAndPushToBranch(Git git, Bump bump) throws GitAPIException {
        git.add().addFilepattern(".").call();
        String commitMessage = String.format("Autobump %s version: %s",
                bump.getGroup(),
                bump.getUpdatedVersion().getVersionNumber());
        git.commit().setMessage(commitMessage).call();
        git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password)).call();
        return commitMessage;
    }
}
