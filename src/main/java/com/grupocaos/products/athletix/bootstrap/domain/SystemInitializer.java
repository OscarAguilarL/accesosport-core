package com.grupocaos.products.athletix.bootstrap.domain;

public interface SystemInitializer {

	String getName();

	Integer getOrder();

	void initialize();

	default boolean shouldExecute() {
		return true;
	}
}
