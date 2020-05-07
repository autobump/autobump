package com.github.autobump.cli.model;

import com.github.autobump.core.model.AutobumpResult;
import com.github.autobump.core.model.usecases.AutobumpUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;


class AutobumpTest {

    private static final String GIT_URL = "http://localhost:8091/testuser/testrepo.git";

    @Test
    void main_showsHelpWhenNoParameters() {
        CommandLine cmd = new CommandLine(new Autobump());
        StringWriter sw = new StringWriter();
        cmd.setErr(new PrintWriter(sw));
        cmd.execute();
        assertThat(sw.toString())
                .startsWith("Missing required options")
                .contains("--url")
                .contains("--username")
                .contains("--password");
    }

    @Test
    void main_SuccessfullyShowsResult(){
        String[] args = ("-u glenn.schrooyen@student.kdg.be -p AutoBump2209 -l " + GIT_URL).split(" ");
        CommandLine cmd = new CommandLine(new TestAutoBump());
        cmd.execute(args);
        if (cmd.getExecutionResult() instanceof AutobumpResult) {
            assertThat(((AutobumpResult) cmd.getExecutionResult()).getNumberOfBumps())
                    .isEqualTo(5);
        }
        else {
            fail("bad returntype");
        }
    }

    static class TestAutoBump extends Autobump {
        @Override
        public AutobumpUseCase getAutobumpUseCase() {
            AutobumpUseCase mocked = Mockito.mock(AutobumpUseCase.class);
            when(mocked.doAutoBump()).thenReturn(new AutobumpResult(5));
            return mocked;
        }
    }
}
