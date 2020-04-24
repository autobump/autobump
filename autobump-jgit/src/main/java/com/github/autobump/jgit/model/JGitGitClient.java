package com.github.autobump.jgit.model;

import com.github.autobump.core.model.GitClient;
import com.github.autobump.core.model.Workspace;
import com.github.autobump.jgit.exception.GitException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.net.URI;
import java.util.UUID;

public class JGitGitClient implements GitClient {
    @Override
    public Workspace clone(URI uri) {
        try {
            Git git = Git.cloneRepository().setURI(uri.toString()).setDirectory(new File(System.getProperty("java.io.tmpdir") + "/" + UUID.randomUUID().toString())).call();
            return getWorkspace(git.getRepository().getDirectory().getPath());
        } catch (GitAPIException e) {
            throw new GitException("something went wrong while cloneing the repo", e);
        }
    }

    private Workspace getWorkspace(String path) {
        if (containsPom(path)){

        }
    }

    private boolean containsPom(String path) {
        return false;
    }
}
