package ru.sovaowltv.contoller.website.mulstistream;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.service.multistream.MultiStreamUtil;
import ru.sovaowltv.service.user.UserUtil;

@Controller
@RequiredArgsConstructor
public class MultiStreamPageController {
    private final MultiStreamUtil multiStreamUtil;
    private final UserUtil userUtil;

    @GetMapping("/ms")
    public String getMs(Model model) {
        User user = userUtil.setUserInModelREADONLY(model);
        multiStreamUtil.setMSIfExist(model, user);
        return "multistream/ms";
    }

    @GetMapping("/ms/{multiStreamId}")
    public String getMultiStreamPage(@PathVariable Long multiStreamId, Model model) {
        multiStreamUtil.init(multiStreamId, model);
        return "multistream/multiStreamPage";
    }

    @GetMapping("/ms/{multiStreamId}/settings")
    public String getMultiStreamSettings(@PathVariable Long multiStreamId, Model model) {
        multiStreamUtil.initSettings(multiStreamId, model);
        return "multistream/multiStreamPageSettings";
    }

//    @GetMapping("/ms/{multiStreamId}/chat")
//    public String getMultiStreamPage(@PathVariable Long multiStreamId) {
//
//    }

    @PostMapping("/ms/create")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String createMultiStreamPage() {
        return "/ms/" + multiStreamUtil.create().getId();
    }

    @PostMapping("/ms/delete")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String deleteMultiStreamPage() {
        multiStreamUtil.delete();
        return "/ms";
    }

    @GetMapping("/ms/join/{msId}/{code}")
    public String joinMultiStream(@PathVariable Long msId, @PathVariable String code) {
        return "redirect:/ms/" + multiStreamUtil.join(msId, code).getId();
    }

    @PostMapping("/ms/left/{msId}")
    public String leftMultiStream(@PathVariable Long msId) {
        multiStreamUtil.left(msId);
        return "redirect:/";
    }
}
