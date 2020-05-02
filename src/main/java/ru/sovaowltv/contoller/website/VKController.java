package ru.sovaowltv.contoller.website;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.sovaowltv.service.data.DataExtractor;
import ru.sovaowltv.service.notifications.vk.VKUtil;

@Controller
@RequiredArgsConstructor
public class VKController {
    private final VKUtil vkUtil;

    private final DataExtractor dataExtractor;

    @PostMapping("/vk/add")
    public String setVKNotificationKey() {
        return vkUtil.addVKNotification();
    }

    @PostMapping("/vk/settings")
    public String setVKNotificationGroup(@RequestBody String json) {
        return vkUtil.setParams(dataExtractor.extractMapFromString(json));
    }

    @PostMapping("/vk/remove")
    public String removeVKNotificationKey() {
        return vkUtil.removeVKNotification();
    }
}
