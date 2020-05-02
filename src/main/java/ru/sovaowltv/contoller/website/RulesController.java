package ru.sovaowltv.contoller.website;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class RulesController {
    @GetMapping("/rules")
    public String getRulesPage() {
        return "rules";
    }
}
