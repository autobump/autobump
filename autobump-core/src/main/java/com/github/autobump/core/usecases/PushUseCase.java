package com.github.autobump.core.usecases;

import com.github.autobump.core.model.domain.Bump;
import com.github.autobump.core.model.domain.Workspace;
import com.github.autobump.core.model.gitclients.GitClient;
import lombok.Builder;

@Builder
public class PushUseCase {
    GitClient gitClient;

    public void doPush(Workspace workspace, Bump bump, String branchName){
        gitClient.commitToExistingBranch(workspace, bump, branchName);
    }

}
