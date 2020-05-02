package ru.sovaowltv.service.unclassified;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import ru.sovaowltv.service.security.SecurityUtil;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RegFormInitModel {
    private final SecurityUtil securityUtil;

    public void initModelForRegForm(HttpSession session, Model model, Map<String, Object> data) {
        securityUtil.generateSecTokenStateForSession(session, model);
        session.setAttribute("data", data);
        fillModel(model, data);
    }

    private void fillModel(Model model, Map<String, Object> data) {
        String from = data.get("from").toString();
        String link = data.get("link").toString();
        String username = data.get("username").toString();
        boolean needEmail = (boolean) data.get("needEmail");

        model.addAttribute("from", from);
        model.addAttribute("needEmail", needEmail);
        model.addAttribute("link", link);
        model.addAttribute("preferedNick", username);
    }
}
