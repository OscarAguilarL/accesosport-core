package com.grupocaos.products.athletix.shared.domain.i18n;

public interface MessageTranslator {
	/**
	 * Translates a message using the current locale in context
	 * 
	 * @param key  Message key
	 * @param args Interpolation arguments
	 * @return Translated message
	 */
	String translate(String key, Object... args);

	/**
	 * Translates a message with an specific locale
	 * 
	 * @param key    Message key
	 * @param locale Language key (e.g.: "es", "en")
	 * @param args   Interpolation arguments
	 * @return Translated message
	 */
	String translate(String key, String locale, Object... args);
}
