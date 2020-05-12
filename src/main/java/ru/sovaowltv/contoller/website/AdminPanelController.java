package ru.sovaowltv.contoller.website;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.sovaowltv.model.user.Achievements;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.service.admin.AdminCaravanUtil;
import ru.sovaowltv.service.admin.AdminStylesUtil;
import ru.sovaowltv.service.admin.AdminWebsiteSettingsUtil;
import ru.sovaowltv.service.admin.NewsUtil;
import ru.sovaowltv.service.admin.smiles.AdminSmilesGGUtil;
import ru.sovaowltv.service.admin.smiles.AdminSmilesTwitchUtil;
import ru.sovaowltv.service.admin.smiles.AdminSmilesUtil;
import ru.sovaowltv.service.admin.smiles.AdminSmilesYTUtil;
import ru.sovaowltv.service.data.DataExtractor;
import ru.sovaowltv.service.icons.IconsUtil;
import ru.sovaowltv.service.user.UserUtil;
import ru.sovaowltv.service.user.UsersRepositoryHandler;
import ru.sovaowltv.service.user.params.UserPremiumUtil;

import javax.annotation.security.RolesAllowed;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//todo: ANOTHER API SERVICE
@Controller
@RequiredArgsConstructor
@RolesAllowed("ROLE_ADMIN")
@Slf4j
public class AdminPanelController {
    private final UsersRepositoryHandler usersRepositoryHandler;

    private final UserUtil userUtil;
    private final AdminCaravanUtil adminCaravanUtil;
    private final AdminWebsiteSettingsUtil adminWebsiteSettingsUtil;
    private final UserPremiumUtil userPremiumUtil;
    private final NewsUtil newsUtil;
    private final IconsUtil iconsUtil;
    private final AdminSmilesUtil adminSmilesUtil;
    private final AdminSmilesTwitchUtil adminSmilesTwitchUtil;
    private final AdminSmilesGGUtil adminSmilesGGUtil;
    private final AdminSmilesYTUtil adminSmilesYTUtil;
    private final AdminStylesUtil adminStylesUtil;

    private final DataExtractor dataExtractor;

    @GetMapping("/admin")
    public String getAdminPage(Model model) {
        User user = userUtil.setUserIfExistInModelREADONLY(model);
        log.warn("someone access ADMIN page. {}", user.getLogin());
        model.addAttribute("newMessages", adminWebsiteSettingsUtil.getNewMessage());
        return "admin";
    }

    @PostMapping("/admin/setDaily")
    @ResponseStatus(HttpStatus.OK)
    public void setDailyMessage(@RequestBody String json) {
        adminWebsiteSettingsUtil.setDailyMessage(json);
    }

    @PostMapping("/admin/readNewMessages")
    @ResponseStatus(HttpStatus.OK)
    public void readNewMessages() {
        adminWebsiteSettingsUtil.readNewMessages();
    }

    /**
     * TWITCH
     */
    @PostMapping("/admin/twitch/all")
    @ResponseStatus(HttpStatus.OK)
    public void reloadTwitchSmiles() {
        adminSmilesTwitchUtil.reloadTwitchSmiles();
    }

    @PostMapping("/admin/twitch/loadFromTwitch")
    @ResponseStatus(HttpStatus.OK)
    public void loadSmilesFromTwitch() {
        adminSmilesTwitchUtil.loadSmilesFromTwitch();
    }

    @PostMapping("/admin/twitch/parse")
    @ResponseStatus(HttpStatus.OK)
    public void parseTwitchSmiles() {
        adminSmilesTwitchUtil.parseTwitchSmiles();
    }

    @PostMapping("/admin/twitch/init")
    @ResponseStatus(HttpStatus.OK)
    public void initTwitchSmiles() {
        adminSmilesTwitchUtil.initTwitchSmiles();
    }

    /**
     * GG
     */
    @PostMapping("/admin/gg/all")
    @ResponseStatus(HttpStatus.OK)
    public void reloadGGSmiles() {
        adminSmilesGGUtil.reloadGGSmiles();
    }

    @PostMapping("/admin/gg/loadFromGG")
    @ResponseStatus(HttpStatus.OK)
    public void loadSmilesFromGG() {
        adminSmilesGGUtil.loadSmilesFromGG();
    }

    @PostMapping("/admin/gg/parse")
    @ResponseStatus(HttpStatus.OK)
    public void parseGGSmiles() {
        adminSmilesGGUtil.parseGGSmiles();
    }

    @PostMapping("/admin/gg/init")
    @ResponseStatus(HttpStatus.OK)
    public void initGGSmiles() {
        adminSmilesGGUtil.initGGSmiles();
    }

    /**
     * YOUTUBE
     */
    @PostMapping("/admin/yt/all")
    @ResponseStatus(HttpStatus.OK)
    public void reloadYTSmiles() {
        adminSmilesYTUtil.reloadYTSmiles();
    }

    @PostMapping("/admin/yt/loadFromYT")
    @ResponseStatus(HttpStatus.OK)
    public void loadSmilesFromYT() {
        adminSmilesYTUtil.loadSmilesFromYT();
    }

    @PostMapping("/admin/yt/parse")
    @ResponseStatus(HttpStatus.OK)
    public void parseYTSmiles() {
        adminSmilesYTUtil.parseYTSmiles();
    }

    @PostMapping("/admin/yt/init")
    @ResponseStatus(HttpStatus.OK)
    public void initYTSmiles() {
        adminSmilesYTUtil.initYTSmiles();
    }

    /**
     * CARAVAN
     */
    @PostMapping("/admin/caravan/enable")
    @ResponseStatus(HttpStatus.OK)
    public void enableCaravan() {
        adminCaravanUtil.enableCaravan();
    }

    @PostMapping("/admin/caravan/disable")
    @ResponseStatus(HttpStatus.OK)
    public void disableCaravan() {
        adminCaravanUtil.disableCaravan();
    }

    @PostMapping("/admin/caravan/nextStep")
    @ResponseStatus(HttpStatus.OK)
    public void nextStepCaravan() {
        adminCaravanUtil.nextStepCaravan();
    }

    /**
     * SMILES
     */
    @PostMapping("/admin/smiles/addNewSmile")
    @ResponseStatus(HttpStatus.OK)
    public void addNewSmile() {
        adminSmilesUtil.addNewSmileStub();
    }

    @PostMapping("/admin/smiles/addSmileForEveryone")
    @ResponseStatus(HttpStatus.OK)
    public void addSmileForEveryone(@RequestBody String body) {
        adminSmilesUtil.addSmileForEveryone(body);
    }

    /**
     * STYLES
     */
    @PostMapping("/admin/styles/addNewStyle")
    @ResponseStatus(HttpStatus.OK)
    public void addNewStyle() {
        adminStylesUtil.addNewStyleStub();
    }

    /**
     * ICONS
     */
    @PostMapping("/admin/premiums/addNewIconToUser")
    @ResponseStatus(HttpStatus.OK)
    public void addNewIconToUser(@RequestBody String json) {
        User userById = null;
        try {
            JsonObject jsonObject = dataExtractor.extractJsonFromString(json);
            long id = dataExtractor.getPrimitiveAsIntFromJson(jsonObject, "id");
            userById = usersRepositoryHandler.getUserById(id);
            iconsUtil.addNewIconToUser(userById);
        } finally {
            usersRepositoryHandler.saveAndFree(userById);
        }
    }

    /**
     * ACHIEVEMENTS
     */
    @PostMapping("/admin/achievements/addAchievementToUser")
    @ResponseStatus(HttpStatus.OK)
    public void addAchievementToUser(@RequestBody String json) {
        User userById = null;
        try {
            JsonObject jsonObject = dataExtractor.extractJsonFromString(json);
            long id = dataExtractor.getPrimitiveAsIntFromJson(jsonObject, "id");
            String name = dataExtractor.getPrimitiveAsStringFromJson(jsonObject, "name");
            userById = usersRepositoryHandler.getUserById(id);
            userById.getAchievements().add(Achievements.valueOf(name.toUpperCase()));
        } finally {
            usersRepositoryHandler.saveAndFree(userById);
        }
    }

    /**
     * USER MAP
     */
    @PostMapping("/admin/userHandler/saveUserMap")
    @ResponseStatus(HttpStatus.OK)
    public void saveUserMap() {
        usersRepositoryHandler.saveUserMap();
    }

    @PostMapping("/admin/userHandler/clearUserMap")
    @ResponseStatus(HttpStatus.OK)
    public void clearUserMap() {
        usersRepositoryHandler.saveAllAndClear();
    }

    /**
     * PREMIUMS
     */
    @PostMapping("/admin/premiums/revalidatePremiums")
    @ResponseStatus(HttpStatus.OK)
    public void revalidatePremiums() {
        userPremiumUtil.revalidatePremiums();
    }

    @PostMapping("/admin/premiums/addPremiumToUser")
    @ResponseStatus(HttpStatus.OK)
    public void addPremiumToUser(@RequestBody String json) {
        User userById = null;
        try {
            JsonObject jsonObject = dataExtractor.extractJsonFromString(json);
            long id = dataExtractor.getPrimitiveAsIntFromJson(jsonObject, "id");
            userById = usersRepositoryHandler.getUserById(id);
            userPremiumUtil.givePremiumForDaysToUser(userById, 30);
        } finally {
            usersRepositoryHandler.saveAndFree(userById);
        }
    }

    @PostMapping("/admin/premiums/giftPremiumForEveryoneForDays")
    @ResponseStatus(HttpStatus.OK)
    public void giftPremiumForEveryoneForDays(@RequestBody String json) {
        JsonObject jsonObject = dataExtractor.extractJsonFromString(json);
        long days = dataExtractor.getPrimitiveAsIntFromJson(jsonObject, "days");
        List<User> users = usersRepositoryHandler.findAll();
        users.forEach(user -> {
            try {
                userPremiumUtil.givePremiumForDaysToUser(user, (int) days);
            } finally {
                usersRepositoryHandler.saveAndFree(user);
            }
        });
    }

    /**
     * UPDATES
     */
    @PostMapping("/admin/news/createNews")
    @ResponseStatus(HttpStatus.OK)
    public void createNews() {
        newsUtil.createEmptyNews();
    }

    @PostMapping("/admin/news/createNewsSub")
    @ResponseStatus(HttpStatus.OK)
    public void createNews(@RequestBody String json) {
        JsonElement parse = JsonParser.parseString(json);
        String id = dataExtractor.getPrimitiveAsStringFromJson(parse.getAsJsonObject(), "id");
        newsUtil.addSubNews(id);
    }

    @PostMapping("/admin/news/addTextToSub")
    @ResponseStatus(HttpStatus.OK)
    public void addTextToSub(@RequestBody String json) {
        JsonElement parse = JsonParser.parseString(json);
        String id = dataExtractor.getPrimitiveAsStringFromJson(parse.getAsJsonObject(), "id");
        newsUtil.addTextToSub(id);
    }

    /**
     * GET LOGS
     */
    @GetMapping(value = "/admin/logs/last", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public FileSystemResource getLastFile() {
        return new FileSystemResource("logs/log.log");
    }

    @GetMapping(value = "/admin/logs/tree")
    public String getTree(Model model) throws IOException {
        try (Stream<Path> paths = Files.walk(Paths.get("logs"))) {
            List<String> list = paths
                    .filter(Files::isRegularFile)
                    .map(Path::toString)
                    .sorted()
                    .map(s -> s.replaceAll("\\\\", "/"))
                    .collect(Collectors.toList());
            model.addAttribute("paths", list);
            return "logs/tree.html";
        }
    }

    @GetMapping(value = "/admin/logs/custom", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public FileSystemResource getFileByName(@RequestParam String path) {
        return new FileSystemResource(path);
    }
}
