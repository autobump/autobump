package com.github.autobump.jgit.model;

import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.GitClient;
import com.github.autobump.core.model.Workspace;
import com.github.autobump.jgit.exception.GitException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.CanceledException;
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
        try(Repository repo = Git.cloneRepository().setURI(uri.toString())
                .setDirectory(Files.createTempDirectory("cloned_repos").toFile())
                .call().getRepository()) {

            return new Workspace(repo.getDirectory().getPath().replace(".git", ""));
        } catch (GitAPIException | IOException e) {
            throw new GitException("something went wrong while cloning the repo", e);
        }
    }

    @Override
    public void CommitToNewBranch(Workspace workspace, Bump bump) {
        try (Git git = Git.open(Path.of(workspace.getProjectRoot()).toFile())){
            try{
                createBranch(bump, git);
                commitAndPushToNewBranch(bump, git);
            } catch(CanceledException c){
                throw new GitException(c.getMessage(), c);
            }
        } catch (IOException e) {
            throw new UncheckedIOException((IOException) e);
        }
    }

    public void createBranch(Bump bump, Git git) throws CanceledException {
        String branchName = String.format("autobump/%s/%s/%s",
                bump.getDependency().getGroup(),
                bump.getDependency().getName(),
                bump.getUpdatedVersion().getVersionNumber());
        try {
            git.branchCreate().setName(branchName).call();
        } catch (GitAPIException e) {
            throw new GitException("Something went wrong while creating the new branch", e);
        }
    }

    public void commitAndPushToNewBranch(Bump bump, Git git) throws CanceledException {
        try {
            git.add().addFilepattern(".").call();
            String commitMessage = String.format("Bump %s from %s to %s",
                    bump.getDependency().getName(),
                    bump.getDependency().getVersion(),
                    bump.getUpdatedVersion().getVersionNumber());
            git.commit().setMessage(commitMessage);
            git.push().call();
        } catch (GitAPIException e) {
            throw new GitException("Something went wrong while committing to the new branch", e);
        }
    }
}
