package com.accesosport.bootstrap.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.accesosport.shared.domain.usecase.UseCase;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExecuteSystemInitializersUseCase
		extends UseCase<Void, ExecuteSystemInitializersUseCase.InitializaitonSummary> {

	private List<SystemInitializer> initializers = null;

    public ExecuteSystemInitializersUseCase(List<SystemInitializer> initializers) {
        this.initializers = initializers.stream()
                .sorted(Comparator.comparingInt(SystemInitializer::getOrder))
                .toList();
    }

    @Override
	protected InitializaitonSummary internalExecute(Void command) {
		log.info("Starting system initialization with {} initializers", initializers.size());

		InitializaitonSummary summary = new InitializaitonSummary();

		for (SystemInitializer initializer : initializers) {
			if (!initializer.shouldExecute()) {
				log.info("Skipping initializer: {}", initializer.getName());
				summary.addSkipped(initializer.getName());
				continue;
			}

			try {
				log.info("Executing initializer: {}", initializer.getName());
				Long startTime = System.currentTimeMillis();

				initializer.initialize();

				Long duration = System.currentTimeMillis() - startTime;
				log.info("Initializer {} completed in {}ms", initializer.getName(), duration);
				summary.addSuccess(initializer.getName(), duration);
			} catch (Exception e) {
				log.error("Initializer {} failed", initializer.getName(), e);
				summary.addFailure(initializer.getName(), e.getMessage());
			}
		}
		return summary;
	}

	public static class InitializaitonSummary {

		@Getter
		private final List<InitializerResult> results = new ArrayList<>();

		public void addSuccess(String name, Long duration) {
			results.add(new InitializerResult(name, Status.SUCCESS, duration, null));
		}

		public void addFailure(String name, String error) {
			results.add(new InitializerResult(name, Status.FAILED, 0L, error));
		}

		public void addSkipped(String name) {
			results.add(new InitializerResult(name, Status.SKIPPED, 0L, null));
		}

		public boolean hasFailures() {
			return results.stream().anyMatch(r -> r.status == Status.FAILED);
		}

		public record InitializerResult(String name, Status status, Long durationMs, String error) {
		}

		public enum Status {
			SUCCESS, FAILED, SKIPPED
		}

	}
}
