package ru.sovaowltv.service.smiles;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sovaowltv.model.shop.Rarity;
import ru.sovaowltv.model.shop.Smile;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.repositories.website.SmilesRepository;
import ru.sovaowltv.service.user.UserUtil;
import ru.sovaowltv.service.user.UsersRepositoryHandler;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSiteSmileAbstract extends SmileAbstract {
    private final SmilesRepository smilesRepository;

    private final UsersRepositoryHandler usersRepositoryHandler;
    private final UserUtil userUtil;

    public boolean isSmile(String smile) {
        return smiles.containsKey(smile);
    }

    public String getSmile(String smile) {
        return smiles.get(smile);
    }

    @Override
    public boolean canUseSmile(String smile) {
        User user = null;
        try {
            user = userUtil.getUser();
            Set<Smile> smiles = user.getSmiles();
            Smile o = new Smile();
            o.setName(smile);
            return smiles.contains(o);
        } catch (Exception e) {
            log.warn("can use smile error {} {}", smile, e);
            return false;
        } finally {
            usersRepositoryHandler.free(user);
        }
    }

    public void initSiteSmiles() {
        List<Smile> smileList = smilesRepository.findAll();
        smileList.forEach(smile -> smiles.put(smile.getName(), smile.getLink()));
    }

    public void createNewSmile() {
        Smile smile;
        smile = new Smile();
        smile.setLink("link");
        smile.setName("smileName");
        smile.setPrice(0);
        smile.setRarity(Rarity.COMMON);
        smilesRepository.save(smile);
    }

    public void addSmileForEveryone(Smile smile) {
        usersRepositoryHandler.findAll().forEach(user -> {
            user.getSmiles().add(smile);
            usersRepositoryHandler.saveAndFree(user);
        });
    }

    @Override
    public void initSmiles() {
        log.info("website init smiles no need to implement");
    }

    @Override
    public void parseSmiles() {
        log.info("website parse smiles no need to implement");
    }

    @Override
    public void loadSmiles() {
        log.info("website load smiles no need to implement");
    }
}
