package org.jboss.weld.spock.util

import org.junit.platform.engine.DiscoverySelector
import org.junit.platform.testkit.engine.EngineTestKit
import spock.util.EmbeddedSpecRunner

/**
 * Work-around for a strange Groovy behavior.
 *
 * <p>The embedded spec runner is used to test the exception if there is automagic mode combined with
 * {@code @WeldSetup}. But for whatever reason, without this wrapper that just has a plain copy of the private
 * {@code doRunRequest} method in the parent class, we get a {@code NoMethodFoundException}.
 *
 * <p>Until someone finds out where this problem comes from, this ugly hack at least works in the meantime.
 *
 * @author Björn Kautler
 */
class EmbeddedSpecRunnerWrapper extends EmbeddedSpecRunner {
    SummarizedEngineExecutionResults doRunRequest(List<DiscoverySelector> selectors) {
        def executionResults = EngineTestKit
                .engine("spock")
                .selectors(*selectors)
                .execute()
        def first = executionResults.allEvents().executions().failed().stream().findFirst()
        if (first.present) {
            throw first.get().terminationInfo.executionResult.throwable.get()
        }
        return new SummarizedEngineExecutionResults(executionResults)
    }
}
