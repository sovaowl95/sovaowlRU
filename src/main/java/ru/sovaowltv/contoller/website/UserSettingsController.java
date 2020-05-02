package ru.sovaowltv.contoller.website;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.sovaowltv.model.chat.SavedSmile;
import ru.sovaowltv.service.data.DataExtractor;
import ru.sovaowltv.service.styles.StyleUtil;
import ru.sovaowltv.service.user.UserSettingsUtil;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class UserSettingsController {
    private final UserSettingsUtil userSettingsUtil;
    private final StyleUtil styleUtil;

    private final DataExtractor dataExtractor;

    @PostMapping("/settings/style/{style}")
    @ResponseStatus(HttpStatus.OK)
    public void setStyle(@PathVariable String style) {
        styleUtil.setStyle(style);
    }

    @PostMapping("/settings/style/premtext/{val}")
    @ResponseStatus(HttpStatus.OK)
    public void setPremiumText(@PathVariable boolean val) {
        userSettingsUtil.setPremiumText(val);
    }

    @PostMapping("/settings/style/smileSize/{val}")
    @ResponseStatus(HttpStatus.OK)
    public void setSmilesSize(@PathVariable Integer val) {
        userSettingsUtil.setSmilesSize(val);
    }

    @PostMapping("/settings/style/textSize/{size}")
    @ResponseStatus(HttpStatus.OK)
    public void setTextSize(@PathVariable Integer size) {
        userSettingsUtil.setTextSize(size);
    }

    @PostMapping("/settings/showTime/{val}")
    @ResponseStatus(HttpStatus.OK)
    public void setShowTime(@PathVariable boolean val) {
        userSettingsUtil.setShowTime(val);
    }

    @PostMapping("/settings/smiles/add/")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public SavedSmile addSmile(@RequestBody String body) {
        Map<String, Object> map = dataExtractor.extractMapFromString(body);
        return userSettingsUtil.addSavedSmile(map);
    }
}
