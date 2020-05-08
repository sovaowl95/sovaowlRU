package ru.sovaowltv.contoller.website;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.sovaowltv.service.admin.NewsUtil;
import ru.sovaowltv.service.user.UserUtil;

@Controller
@RequiredArgsConstructor
@Slf4j
public class NewsController {
    private final UserUtil userUtil;
    private final NewsUtil newsUtil;

    @GetMapping("/info/updates")
    public String getUpdates(Model model) {
        userUtil.setUserInModelREADONLY(model);
        newsUtil.addAllNews(model);
        return "news";
    }
}
