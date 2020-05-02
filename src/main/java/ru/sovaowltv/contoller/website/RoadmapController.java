package ru.sovaowltv.contoller.website;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sovaowltv.service.roadmap.RoadmapUtil;


@Controller
@RequiredArgsConstructor
public class RoadmapController {
    private final RoadmapUtil roadmapUtil;

    @GetMapping("/roadmap")
    public String getRoadmap(Model model) {
        roadmapUtil.prepareRoadmapPage(model);
        return "roadmap";
    }

    @PostMapping("/roadmap/{id}/{action}")
    @ResponseStatus(HttpStatus.OK)
    public void updateRoadmap(@PathVariable String id, @PathVariable String action) {
        roadmapUtil.changeRoadmapRating(id, action);
    }
}
