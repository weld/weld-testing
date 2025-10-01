package org.jboss.weld.junit.jupiter.compat;

import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.io.TempDir;

/**
 * This test runs without CDI in order to demonstrate that it works with plain JUnit.
 */
class JunitParameterResolverReferenceTest {

    @Test
    void testResolveTestInfo(TestInfo testInfo) {
        Assertions.assertNotNull(testInfo);
    }

    @RepeatedTest(1)
    void testResolveRepetitionInfo(RepetitionInfo repetitionInfo) {
        Assertions.assertNotNull(repetitionInfo);
    }

    @Test
    void testResolveTestReporter(TestReporter testReporter) {
        Assertions.assertNotNull(testReporter);
    }

    @Test
    void testResolveTempDir(@TempDir Path tempDir) {
        Assertions.assertNotNull(tempDir);
    }

}
