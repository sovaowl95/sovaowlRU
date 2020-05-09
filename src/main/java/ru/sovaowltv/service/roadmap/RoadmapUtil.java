package ru.sovaowltv.service.roadmap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;
import ru.sovaowltv.model.roadmap.Roadmap;
import ru.sovaowltv.model.roadmap.RoadmapStatus;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.repositories.website.RoadmapRepository;
import ru.sovaowltv.service.user.UserUtil;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoadmapUtil {
    private final RoadmapRepository roadmapRepository;

    private final UserUtil userUtil;

    public void changeRoadmapRating(String id, String action) {
        Optional<Roadmap> optionalRoadmap = roadmapRepository.findById(Long.parseLong(id));
        if (optionalRoadmap.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ROADMAP NOT FOUND");
        }
        Roadmap roadmap = optionalRoadmap.get();
        User user = userUtil.getUserREADONLY();
        if (roadmap.getDown() == null) {
            roadmap.setDown(new HashSet<>());
        }
        if (roadmap.getUp() == null) {
            roadmap.setUp(new HashSet<>());
        }
        if (action.equals("plus")) {
            addRating(roadmap, user);
        } else if (action.equals("minus")) {
            removeRating(roadmap, user);
        }
        roadmapRepository.save(roadmap);
    }

    private void removeRating(Roadmap roadmap, User user) {
        if (roadmap.getDown().contains(user)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ALREADY VOTE");
        } else {
            if (roadmap.getUp().contains(user)) {
                roadmap.getUp().remove(user);
            } else {
                roadmap.getDown().add(user);
            }
        }
    }

    private void addRating(Roadmap roadmap, User user) {
        if (roadmap.getUp().contains(user)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ALREADY VOTE");
        } else {
            if (roadmap.getDown().contains(user)) {
                roadmap.getDown().remove(user);
            } else {
                roadmap.getUp().add(user);
            }
        }
    }

    public void prepareRoadmapPage(Model model) {
        userUtil.setUserInModelREADONLY(model);
        List<Roadmap> all = roadmapRepository.findAll();
        model.addAttribute("roadmap", all);
        model.addAttribute("order", Arrays.asList(RoadmapStatus.values()));
        Comparator<Roadmap> roadmapComparator = Comparator.comparingInt(o -> o.getDown().size() - o.getUp().size());
        model.addAttribute("comparator", roadmapComparator);
    }
}
