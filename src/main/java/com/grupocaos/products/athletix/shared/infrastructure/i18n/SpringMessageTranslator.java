package com.grupocaos.products.athletix.shared.infrastructure.i18n;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import com.grupocaos.products.athletix.shared.domain.i18n.MessageTranslator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SpringMessageTranslator implements MessageTranslator {

	private final MessageSource messageSource;

	@Override
	public String translate(String key, Object... args) {
		try {
			Locale locale = LocaleContextHolder.getLocale();
			return messageSource.getMessage(key, args, locale);
		} catch (Exception e) {
			log.warn("Translation not found for key: {} - returning key", key);
			return key;
		}
	}

	@Override
	public String translate(String key, String localeString, Object... args) {
		try {
			Locale locale = Locale.forLanguageTag(localeString);
			return messageSource.getMessage(key, args, locale);
		} catch (Exception e) {
			log.warn("Translation not found for key: {} and locale: {}", key, localeString);
			return key;
		}
	}

}
