package ru.sovaowltv.service.unclassified;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class LanguageUtil {
    private final MessageSource messageSource;

    public String getStringFor(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
