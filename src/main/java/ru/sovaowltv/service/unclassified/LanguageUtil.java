package ru.sovaowltv.service.unclassified;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;


@Service
@RequiredArgsConstructor
public class LanguageUtil {
    private final MessageSource messageSource;

    public String getStringWithLocale(String key, Locale locale) {
        return messageSource.getMessage(key, null, locale);
    }

    public String getStringFor(String key) {
        return getStringWithLocale(key, LocaleContextHolder.getLocale());
    }
}
