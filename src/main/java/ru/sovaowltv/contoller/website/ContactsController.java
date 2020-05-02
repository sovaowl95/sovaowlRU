package ru.sovaowltv.contoller.website;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.sovaowltv.service.user.UserUtil;

@Controller
@RequiredArgsConstructor
public class ContactsController {
    private final UserUtil userUtil;

    @GetMapping("/contacts")
    public String getContacts(Model model) {
        userUtil.setUserInModelREADONLY(model);
        return "contacts";
    }
}
