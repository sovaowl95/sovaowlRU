package ru.sovaowltv.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;
import ru.sovaowltv.model.admin.News;
import ru.sovaowltv.model.admin.NewsCategory;
import ru.sovaowltv.model.admin.NewsSubElement;
import ru.sovaowltv.repositories.admin.NewsRepository;
import ru.sovaowltv.repositories.admin.NewsSubElementRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsUtil {
    private final NewsRepository newsRepository;
    private final NewsSubElementRepository newsSubElementRepository;

    public void createEmptyNews() {
        News news = new News();
        news.setTitle("title");
        news.setPublicated(false);
        newsRepository.save(news);
    }

    public void addSubNews(String id) {
        News news = newsRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't find news by id: " + id));
        addSubNews(news);
    }

    public void addSubNews(News news) {
        NewsSubElement newsSubElement = new NewsSubElement();
        newsSubElement.setCategory(NewsCategory.CATEGORY);
        Set<String> set = new HashSet<>();
        set.add("text");
        newsSubElement.setText(set);
        newsSubElementRepository.save(newsSubElement);

        news.getElements().add(newsSubElement);
        newsRepository.save(news);
    }

    public void addAllNews(Model model) {
        List<News> news = newsRepository.findAll();
        news.sort((o1, o2) -> (int) (o2.getId() - o1.getId()));
        news.forEach(news1 -> {
            Set<NewsSubElement> elements = news1.getElements();
            Set<NewsSubElement> set = new TreeSet<>(Comparator.comparingLong(NewsSubElement::getId));
            set.addAll(elements);
            news1.setElements(set);
        });
        model.addAttribute("news", news);
    }

    public void addTextToSub(String id) {
        NewsSubElement newsSubElement = newsSubElementRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't find sub by id: " + id));
        newsSubElement.getText().add("newPlaceHolder" + UUID.randomUUID().toString());

        newsSubElementRepository.save(newsSubElement);
    }
}
