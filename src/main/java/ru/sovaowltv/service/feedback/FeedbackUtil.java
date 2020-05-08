package ru.sovaowltv.service.feedback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sovaowltv.model.feedback.FeedbackMessage;
import ru.sovaowltv.model.feedback.FeedbackStatus;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.repositories.website.FeedbackRepository;
import ru.sovaowltv.service.admin.AdminWebsiteSettingsUtil;
import ru.sovaowltv.service.unclassified.HtmlTagsClear;
import ru.sovaowltv.service.user.UserUtil;
import ru.sovaowltv.service.user.UsersRepositoryHandler;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedbackUtil {
    private final FeedbackRepository feedbackRepository;
    private final UsersRepositoryHandler usersRepositoryHandler;

    private final UserUtil userUtil;
    private final AdminWebsiteSettingsUtil adminWebsiteSettingsUtil;

    private final HtmlTagsClear htmlTagsClear;

    public void createAndSaveFeedbackMessage(Map<String, Object> map) {
        String message = String.valueOf(map.get("message"));
        String theme = String.valueOf(map.get("theme"));

        FeedbackMessage feedbackMessage = new FeedbackMessage();
        User user = userUtil.getUser();
        feedbackMessage.setUser(user);
        usersRepositoryHandler.saveAndFree(user);
        feedbackMessage.setTheme(htmlTagsClear.removeTags(theme));
        feedbackMessage.setMessage(htmlTagsClear.removeTags(message));
        feedbackMessage.setTime(LocalDate.now().toString());
        feedbackMessage.setFeedbackStatuses(Collections.singleton(FeedbackStatus.RECEIVED));

        feedbackRepository.save(feedbackMessage);

        adminWebsiteSettingsUtil.addNewFeedbackMessage();
    }
}
