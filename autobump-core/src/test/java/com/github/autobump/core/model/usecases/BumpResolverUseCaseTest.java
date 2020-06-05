package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.IgnoreRepository;
import com.github.autobump.core.model.Setting;
import com.github.autobump.core.model.SettingsRepository;
import com.github.autobump.core.model.Version;
import com.github.autobump.core.model.VersionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class BumpResolverUseCaseTest {

    public static final String ORG_GITHUB = "org.github";
    public static final String MAJORTEST = "majortest";
    @Mock
    SettingsRepository settingsRepository;

    @Mock
    VersionRepository versionRepository;

    Set<Dependency> dependencySet;

    @BeforeEach
    void setUp() {
        Mockito.lenient().when(settingsRepository.getAllIgnores()).thenReturn(
                List.of(Setting.builder()
                                .key("org.github:testdependency").value("10.0.3")
                                .type(Setting.SettingsType.IGNORE).repositoryName("test")
                                .build(),
                        Setting.builder()
                                .key("org.github:majortest:10.0.3:12.0.1").value("major")
                                .type(Setting.SettingsType.IGNORE).repositoryName("test")
                                .build())
        );
        var dependency1 = Dependency.builder()
                .name("testdependency").group(ORG_GITHUB).version(new TestVersion("9.1.0"))
                .build();
        var dependency2 = Dependency.builder()
                .name(MAJORTEST).group(ORG_GITHUB).version(new TestVersion("10.0.3"))
                .build();
        Mockito.lenient().when(versionRepository.getAllAvailableVersions(dependency1)).thenReturn(
                Set.of(new TestVersion("10.0.3"))
        );
        Mockito.lenient().when(versionRepository.getAllAvailableVersions(dependency2)).thenReturn(
                Set.of(new TestVersion("12.0.1"))
        );
        dependencySet = Set.of(dependency1, dependency2);
    }

    @Test
    void doResolveIgnoreAll() {
        var bumps = BumpResolverUseCase.builder()
                .settingsRepository(settingsRepository)
                .ignoreRepository(Mockito.mock(IgnoreRepository.class))
                .dependencies(dependencySet)
                .versionRepository(versionRepository)
                .build()
                .doResolve("test");
        assertThat(bumps).isEmpty();
    }

    @Test
    void doResolvewithreturn() {
        setupMocking();

        var bumps = BumpResolverUseCase.builder()
                .settingsRepository(settingsRepository)
                .ignoreRepository(Mockito.mock(IgnoreRepository.class))
                .dependencies(dependencySet)
                .versionRepository(versionRepository)
                .build()
                .doResolve("test");
        assertThat(bumps.size()).isEqualTo(4);
    }

    private void setupMocking() {
        var dependency = Dependency.builder()
                .name("test").group("succesfull").version(new TestVersion("filter"))
                .build();
        var dep1 = Dependency.builder()
                .name("minortest").group(ORG_GITHUB).version(new TestVersion("testy"))
                .build();
        var dep2 = Dependency.builder()
                .group(ORG_GITHUB).name(MAJORTEST).version(new TestVersion("testy"))
                .build();
        var dep3 = Dependency.builder()
                .group(ORG_GITHUB).name(MAJORTEST).version(new TestVersion("10.0.3"))
                .build();
        configureMocks(dep1, dep2, dep3);
        dependencySet = new HashSet<>(dependencySet);
        dependencySet.addAll(Set.of(dependency, dep1, dep2, dep3));
        Mockito.when(versionRepository.getAllAvailableVersions(dependency))
                .thenReturn(Set.of(new TestVersion("worked")));
    }

    private void configureMocks(Dependency dep1, Dependency dep2, Dependency dep3) {
        Mockito.when(versionRepository.getAllAvailableVersions(dep1))
                .thenReturn(Set.of(new TestVersion("10.2.3")));
        Mockito.when(versionRepository.getAllAvailableVersions(dep2))
                .thenReturn(Set.of(new TestVersion("10.2.3")));
        Mockito.when(versionRepository.getAllAvailableVersions(dep3))
                .thenReturn(Set.of(new TestVersion("11.1.0")));
    }

    private static class TestVersion implements Version {

        private final String versionNumber;

        private TestVersion(String versionNumber) {
            this.versionNumber = versionNumber;
        }

        @Override
        public String getVersionNumber() {
            return versionNumber;
        }

        @Override
        public UpdateType getUpdateType(Version otherVersion) {
            return null;
        }

        @Override
        public int compareTo(Version o) {
            return 1;
        }
    }
}
