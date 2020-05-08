package ru.sovaowltv.service.unclassified;

import org.springframework.stereotype.Service;
import ru.sovaowltv.model.apinotification.NotificationFor;
import ru.sovaowltv.model.stream.Stream;

@Service
public class KeyWordsReplacerUtil {
    public String replaceAllKeyWords(Stream stream, String text, NotificationFor notificationFor) {
        StringBuilder sb = new StringBuilder(text);
        replaceUSER(stream, sb);
        replaceLINKS(stream, sb, notificationFor);
        replaceSTREAMNAME(stream, sb);
        replaceSTREAMGAME(stream, sb);
        replaceNEWLINES(sb);
        replaceSOVAOWL(stream, sb);
        return sb.toString();
    }

    private void replaceUSER(Stream stream, StringBuilder sb) {
        int i;
        while ((i = sb.indexOf("{user}")) != -1) {
            sb.replace(i, i + "{user}".length(), stream.getUser().getNickname());
        }
    }

    private void replaceLINKS(Stream stream, StringBuilder sb, NotificationFor notificationFor) {
        int i;
        while ((i = sb.indexOf("{links}")) != -1) {
            sb.replace(i, i + "{links}".length(), replaceLinksForCurrentService(stream, notificationFor));
        }
    }

    private String replaceLinksForCurrentService(Stream stream, NotificationFor notificationFor) {
        StringBuilder localSB = new StringBuilder();
        appendWebsite(stream, localSB, notificationFor);
        appendTwitchLink(stream, localSB, notificationFor);
        appendGGLink(stream, localSB, notificationFor);
        appendYTLink(stream, localSB, notificationFor);
        return localSB.toString();
    }

    private void appendWebsite(Stream stream, StringBuilder sb, NotificationFor notificationFor) {
        sb.append("Website - ");
        resolveNotificationForDiscord(sb, notificationFor, "<");
        sb.append("https://sovaowl.ru/")
                .append(stream.getUser().getNickname());
        resolveNotificationForDiscord(sb, notificationFor, ">");
        sb.append("\n");
    }


    private void appendYTLink(Stream stream, StringBuilder sb, NotificationFor notificationFor) {
        if (stream.getUser().getUserGoogle() != null) {
            sb.append("Youtube - ");
            resolveNotificationForDiscord(sb, notificationFor, "<");
            sb.append("https://www.youtube.com/watch?v=")
                    .append(stream.getUser().getUserGoogle().getVideoId());
            resolveNotificationForDiscord(sb, notificationFor, ">");
            sb.append("\n");
        }
    }

    private void appendGGLink(Stream stream, StringBuilder sb, NotificationFor notificationFor) {
        if (stream.getUser().getUserGG() != null) {
            sb.append("GG - ");
            resolveNotificationForDiscord(sb, notificationFor, "<");
            sb.append("https://goodgame.ru/channel/")
                    .append(stream.getUser().getUserGG().getNick());
            resolveNotificationForDiscord(sb, notificationFor, ">");
            sb.append("\n");
        }
    }

    private void appendTwitchLink(Stream stream, StringBuilder sb, NotificationFor notificationFor) {
        if (stream.getUser().getUserTwitch() != null) {
            sb.append("Twitch - ");
            resolveNotificationForDiscord(sb, notificationFor, "<");
            sb.append("https://twitch.tv/")
                    .append(stream.getUser().getUserTwitch().getNick());
            resolveNotificationForDiscord(sb, notificationFor, ">");
            sb.append("\n");
        }
    }

    private void resolveNotificationForDiscord(StringBuilder sb, NotificationFor notificationFor, String tag) {
        if (notificationFor == NotificationFor.DISCORD)
            sb.append(tag);
    }

    private void replaceSTREAMNAME(Stream stream, StringBuilder sb) {
        int i;
        while ((i = sb.indexOf("{streamName}")) != -1) {
            sb.replace(i, i + "{streamName}".length(), stream.getStreamName());
        }
    }

    private void replaceSTREAMGAME(Stream stream, StringBuilder sb) {
        int i;
        while ((i = sb.indexOf("{streamGame}")) != -1) {
            sb.replace(i, i + "{streamGame}".length(), stream.getGame());
        }
    }

    private void replaceNEWLINES(StringBuilder sb) {
        int i;
        while ((i = sb.indexOf("{n}")) != -1) {
            sb.replace(i, i + "{n}".length(), "\n");
        }
    }

    private void replaceSOVAOWL(Stream stream, StringBuilder sb) {
        int i;
        while ((i = sb.indexOf("{sovaowl}")) != -1) {
            sb.replace(i, i + "{sovaowl}".length(), "https://sovaowl.ru/" + stream.getUser().getNickname());
        }
    }
}
