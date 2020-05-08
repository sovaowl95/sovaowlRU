package ru.sovaowltv.contoller.website.mulstistream;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.sovaowltv.service.multistream.MultiStreamUtil;

@Controller
@RequiredArgsConstructor
public class MultiStreamPageController {
    private final MultiStreamUtil multiStreamUtil;

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


    @GetMapping("/ms/{multiStreamId}/chat")
    public String getOnlyChat(@PathVariable Long multiStreamId, Model model) {
        multiStreamUtil.init(multiStreamId, model);
        return "fragments/chat";
    }

    @GetMapping("/ms/{multiStreamId}/publicchat")
    public String getOnlyChatPublic(@PathVariable Long multiStreamId, Model model) {
        multiStreamUtil.init(multiStreamId, model);
        return "fragments/chatPublic";
    }

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
