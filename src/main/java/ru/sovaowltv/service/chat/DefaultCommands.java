package ru.sovaowltv.service.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.service.caravan.CaravanUtil;
import ru.sovaowltv.service.slot.SlotUtil;

import java.net.MalformedURLException;
import java.net.URL;

import static ru.sovaowltv.service.unclassified.Constants.*;

@Component
@RequiredArgsConstructor
public class DefaultCommands {
    private final CaravanUtil caravanUtil;
    private final SlotUtil slotUtil;

    String imgCommand(String text) {
        String[] split = text.split(" ", 3);
        if (split.length == 2) {
            return "<img class='imageFromUser' src='" + split[1] + "'>";
        } else if (split.length == 3) {
            return split[2] + " <img class='imageFromUser' src='" + split[1] + "'>";
        } else {
            return "ERR -> !img link text";
        }
    }

    String youtubeCommand(String text) {
        String[] split = text.split(" ", 3);
        String placeHolder = "<iframe class='ytFromUser' " +
                "src=\"https://www.youtube.com/embed/{{link}}\" " +
                "frameborder=\"0\" " +
                "allow=\"accelerometer; " +
                "autoplay; " +
                "encrypted-media; " +
                "gyroscope; " +
                "picture-in-picture\" " +
                "allowfullscreen>" +
                "</iframe>";
        try {
            URL url = new URL(split[1]);
            if (!url.getProtocol().equalsIgnoreCase("https")) return "ERR -> wrong protocol. must be https";
            if (!url.getHost().equalsIgnoreCase("www.youtube.com") && !url.getHost().equalsIgnoreCase("youtu.be"))
                return "ERR -> wrong host. must be www.youtube.com or youtu.be";
            if (url.getHost().equalsIgnoreCase("www.youtube.com")) {
                if (!url.getPath().equalsIgnoreCase("/watch")) return "ERR -> wrong link. must contains /watch";
                String query = url.getQuery();
                String[] keyValue = query.split("(&|&amp;)");
                boolean find = false;
                for (String word : keyValue) {
                    if (word.startsWith("v=")) {
                        split[1] = word.replaceFirst("v=", "");
                        find = split[1].length() >= 11;
                        break;
                    }
                }
                if (!find) return "ERR -> cant find video value";
                String res = placeHolder.replaceAll("\\{\\{link}}", split[1]);
                if (split.length == 3) res = split[2] + " " + res;
                return res;
            } else if (url.getHost().equalsIgnoreCase("youtu.be")) {
                String[] spl = url.getPath().split("/");
                String res = placeHolder.replaceAll("\\{\\{link}}", spl[spl.length - 1]);
                if (split.length == 3) res = split[2] + " " + res;
                return res;
            }
        } catch (MalformedURLException e) {
            return "ERR -> !wrong url";
        } catch (Exception e) {
            return "ERR -> !yt link text";
        }
        return "ERR -> !yt link text";
    }

    String videoCommand(String text) {
        String[] split = text.split(" ", 3);
        if (split.length == 2) {
            return "<video controls autoplay loop muted class='videoFromUser'>" +
                    "<source src='" + split[1] + "' type='video/webm'>" +
                    "</video>";
        } else if (split.length == 3) {
            return split[2] +
                    " <video controls autoplay loop muted class='videoFromUser'>" +
                    "<source src='" + split[1] + "' type='video/webm'>" +
                    "</video>";
        } else {
            return "ERR -> !video link text";
        }
    }

    String coubCommand(String text) {
        String[] split = text.split(" ", 3);
        String placeHolder = "<iframe class='coubFromUser' " +
                "src=\"https://coub.com/embed/{{link}}?muted=false&autostart=false&originalSize=false&startWithHD=false\" " +
                "allowfullscreen frameborder=\"0\" allow=\"autoplay\">" +
                "</iframe>";
        try {
            URL url = new URL(split[1]);
            if (!url.getProtocol().equalsIgnoreCase("https")) return "ERR -> wrong protocol. must be https";
            if (!url.getHost().equalsIgnoreCase("coub.com"))
                return "ERR -> wrong host. must be coub.com";
            if (url.getPath().contains("/view/") || url.getPath().contains("/embed/")) {
                String[] spl = url.getPath().split("/");
                String res = placeHolder.replaceAll("\\{\\{link}}", spl[spl.length - 1]);
                if (split.length == 3) res = split[2] + " " + res;
                return res;
            } else {
                return "ERR -> wrong link. must contains /view/ or /embed/ ";
            }
        } catch (MalformedURLException e) {
            return "ERR -> !wrong url";
        } catch (Exception e) {
            return "ERR -> !coub link text";
        }
    }

    String robCommand(User user) {
        String res = caravanUtil.joinRobbery(user.getId());
        switch (res) {
            case CARAVAN_JOIN:
                return "CARAVAN -> " + CARAVAN_JOIN;
            case CARAVAN_JOIN_NOT_ENOUGH_MONEY:
                return "CARAVAN -> " + CARAVAN_JOIN_NOT_ENOUGH_MONEY;
            case CARAVAN_JOIN_ERR_ALREADY_IN_JOIN:
                return "CARAVAN -> " + CARAVAN_JOIN_ERR_ALREADY_IN_JOIN;
            case CARAVAN_JOIN_ERR_STATUS_JOIN:
                return "CARAVAN -> " + CARAVAN_JOIN_ERR_STATUS_JOIN;
            default:
                return "ERR -> some kind of caravan err :(";
        }
    }

    String slotCommand(User user, String text) {
        return "SLOT -> " + slotUtil.startSlotGame(user, text);
    }
}
