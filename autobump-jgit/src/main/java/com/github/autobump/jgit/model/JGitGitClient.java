package com.github.autobump.jgit.model;

import com.github.autobump.core.model.AutoBumpRebaseResult;
import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.CommitResult;
import com.github.autobump.core.model.GitClient;
import com.github.autobump.core.model.Workspace;
import com.github.autobump.jgit.exception.GitException;
import lombok.AllArgsConstructor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.RebaseCommand;
import org.eclipse.jgit.api.RebaseResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.attributes.AttributesNodeProvider;
import org.eclipse.jgit.errors.IllegalTodoFileModification;
import org.eclipse.jgit.lib.ObjectDatabase;
import org.eclipse.jgit.lib.RebaseTodoLine;
import org.eclipse.jgit.lib.RefDatabase;
import org.eclipse.jgit.lib.ReflogReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.merge.ResolveMerger;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class JGitGitClient implements GitClient {
    private final String username;
    private final String password;

    @Override
    public Workspace clone(URI uri) {
        try (Repository repo = Git.cloneRepository().setURI(uri.toString())
                .setDirectory(Files.createTempDirectory("cloned_repos").toFile())
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
            git.checkout().setName("refs/heads/master").call();
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
            git.checkout().setName("refs/heads/master").call();
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
            git.fetch().call();
            var checkout = git.checkout();
            if (!git.branchList().call().stream()
                    .map(b -> b.getName())
                    .collect(Collectors.toUnmodifiableSet())
                    .contains("refs/heads/" + branchName)) {
                checkout.setCreateBranch(true);
            }
            checkout.
                    setName(branchName).
                    setStartPoint(branchName).
                    call();
            git.pull().setRemoteBranchName(branchName).call();

//            ResolveMerger merger = (ResolveMerger) MergeStrategy.OURS.newMerger(git.getRepository(),false);

            PullResult result = git.pull()
                    .setRemoteBranchName("master")
                    .setRemote("origin")
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password))

//                    .setStrategy(MergeStrategy.THEIRS)
                    .setRebase(true)
                    .call();

            return new AutoBumpRebaseResult(result.getRebaseResult().getConflicts() != null
                    && !result.getRebaseResult().getConflicts().isEmpty());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (GitAPIException g) {
            throw new GitException("Something went wrong while checking out the branch or rebasing to it", g);
        }
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
