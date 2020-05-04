package ru.sovaowltv.service.unclassified;

import org.springframework.stereotype.Service;

@Service
public class HtmlTagsClear {
    public String removeTags(String text) {
        return text
                .replace("&nbsp;", " ")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replaceAll("\\$", "&dollar;")
                .trim();
    }
}
