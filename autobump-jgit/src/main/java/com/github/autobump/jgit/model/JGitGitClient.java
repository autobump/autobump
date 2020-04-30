package com.github.autobump.jgit.model;

import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.GitClient;
import com.github.autobump.core.model.Workspace;
import com.github.autobump.jgit.exception.GitException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

public class JGitGitClient implements GitClient {
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
    public void CommitToNewBranch(Workspace workspace, Bump bump) {
        try (Git git = Git.open(Path.of(workspace.getProjectRoot()).toFile())) {
            createBranch(bump, git);
            commitAndPushToNewBranch(bump, git);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (GitAPIException g) {
            throw new GitException("Something went wrong while creating the new branch or committing to it", g);
        }
    }

    public void createBranch(Bump bump, Git git) throws GitAPIException {
        String branchName = String.format("autobump/%s/%s/%s",
                bump.getDependency().getGroup(),
                bump.getDependency().getName(),
                bump.getUpdatedVersion().getVersionNumber());
        git.branchCreate().setName(branchName).call();
    }

    public void commitAndPushToNewBranch(Bump bump, Git git) throws GitAPIException {
        git.add().addFilepattern(".").call();
        String commitMessage = String.format("Bump %s from %s to %s",
                bump.getDependency().getName(),
                bump.getDependency().getVersion(),
                bump.getUpdatedVersion().getVersionNumber());
        git.commit().setMessage(commitMessage);
        git.push().call();
    }
}
