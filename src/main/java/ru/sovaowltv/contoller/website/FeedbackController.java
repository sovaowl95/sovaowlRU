package ru.sovaowltv.contoller.website;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sovaowltv.service.data.DataExtractor;
import ru.sovaowltv.service.data.DataFieldsChecker;
import ru.sovaowltv.service.feedback.FeedbackUtil;
import ru.sovaowltv.service.user.UserUtil;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class FeedbackController {
    private final UserUtil userUtil;
    private final FeedbackUtil feedbackUtil;

    private final DataFieldsChecker dataFieldsChecker;
    private final DataExtractor dataExtractor;

    @GetMapping("/feedback")
    public String getFeedbackPage(Model model) {
        userUtil.setUserIfExistInModelREADONLY(model);
        return "feedback";
    }

    @PostMapping("/feedback")
    @ResponseStatus(HttpStatus.OK)
    public void createFeedbackMessage(@RequestBody String json) {
        Map<String, Object> map = dataExtractor.extractMapFromString(json);
        dataFieldsChecker.checkFeedbackMessage(map);
        feedbackUtil.createAndSaveFeedbackMessage(map);
    }
}
