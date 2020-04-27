package com.github.autobump.jgit.model;

import com.github.autobump.core.model.GitClient;
import com.github.autobump.core.model.Workspace;
import com.github.autobump.jgit.exception.GitException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;

import java.io.File;
import java.net.URI;
import java.util.UUID;

public class JGitGitClient implements GitClient {
    @Override
    public Workspace clone(URI uri) {
        try(Repository repo = Git.cloneRepository().setURI(uri.toString())
                .setDirectory(
                        new File(System.getProperty("java.io.tmpdir") +
                                        File.separator +
                                        UUID.randomUUID().toString()))
                .call().getRepository()) {

            return new Workspace(repo.getDirectory().getPath().replace(".git", ""));
        } catch (GitAPIException e) {
            throw new GitException("something went wrong while cloneing the repo", e);
        }
    }
}
