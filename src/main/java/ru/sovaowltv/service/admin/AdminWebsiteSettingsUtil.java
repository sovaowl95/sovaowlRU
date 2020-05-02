package ru.sovaowltv.service.admin;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sovaowltv.model.admin.AdminSettings;
import ru.sovaowltv.repositories.admin.AdminSettingsRepository;
import ru.sovaowltv.service.data.DataExtractor;
import ru.sovaowltv.service.unclassified.Constants;
import ru.sovaowltv.service.unclassified.HtmlTagsClear;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminWebsiteSettingsUtil {
    private final AdminSettingsRepository adminSettingsRepository;

    private final DataExtractor dataExtractor;
    private final HtmlTagsClear htmlTagsClear;
    private final Constants constants;

    private static final String NEW_MESSAGES = "newMessages";

    public void setDailyMessage(String json) {
        JsonObject jsonObject = dataExtractor.extractJsonFromString(json);
        String info = dataExtractor.getPrimitiveAsStringFromJson(jsonObject, "info");
        String text = htmlTagsClear.removeTags(info);

        AdminSettings adminTopMenuInfo = adminSettingsRepository.findByKeyWord("adminTopMenuInfo");
        adminTopMenuInfo.setText(text);

        adminSettingsRepository.save(adminTopMenuInfo);
        constants.setAdminTopMenuInfo(text);
    }

    public void addNewFeedbackMessage() {
        AdminSettings adminSettings = adminSettingsRepository.findByKeyWord(NEW_MESSAGES);
        if (adminSettings == null) {
            AdminSettings setting = new AdminSettings();
            setting.setKeyWord(NEW_MESSAGES);
            setting.setText("0");
            adminSettings = setting;
        }

        int i = Integer.parseInt(adminSettings.getText()) + 1;
        adminSettings.setText(Integer.toString(i));
        adminSettingsRepository.save(adminSettings);
    }

    public String getNewMessage() {
        AdminSettings newMessages = adminSettingsRepository.findByKeyWord(NEW_MESSAGES);
        if (newMessages == null) return "0";
        return newMessages.getText();
    }

    public void readNewMessages() {
        AdminSettings newMessages = adminSettingsRepository.findByKeyWord(NEW_MESSAGES);
        if (newMessages == null) {
            addNewFeedbackMessage();
            readNewMessages();
        } else {
            newMessages.setText("0");
            adminSettingsRepository.save(newMessages);
        }
    }
}
