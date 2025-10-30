package com.grupocaos.products.athletix.shared.domain.usecase;

public interface UseCase<Command, Result> {
    Result execute(Command command);
}
