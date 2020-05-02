package ru.sovaowltv.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

@Configuration
public class MessageSourceConfiguration {
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource
                = new ReloadableResourceBundleMessageSource();
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setBasenames(
                "classpath:/locales/achievements",
                "classpath:/locales/caravan",
                "classpath:/locales/errors",
                "classpath:/locales/icons",
                "classpath:/locales/messages",
                "classpath:/locales/slot",
                "classpath:/locales/pages/admin/admin",
                "classpath:/locales/pages/chat/chat",
                "classpath:/locales/pages/contacts/contacts",
                "classpath:/locales/pages/command/commands",
                "classpath:/locales/pages/feedback/feedback",
                "classpath:/locales/pages/info/info",
                "classpath:/locales/pages/news/news",
                "classpath:/locales/pages/pay/pay",
                "classpath:/locales/pages/profile/profile",
                "classpath:/locales/pages/promo/promo",
                "classpath:/locales/pages/roadmap/roadmap",
                "classpath:/locales/pages/shop/shop",
                "classpath:/locales/pages/stream/stream",
                "classpath:/locales/pages/streamSettings/streamSettings",
                "classpath:/locales/pages/streamlist/streamlist"
        );
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }
}
