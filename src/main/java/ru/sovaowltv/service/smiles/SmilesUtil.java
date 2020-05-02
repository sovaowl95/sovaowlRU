package ru.sovaowltv.service.smiles;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.sovaowltv.model.shop.Rarity;
import ru.sovaowltv.model.shop.Smile;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.repositories.website.SmilesRepository;
import ru.sovaowltv.service.caravan.RarityUtil;
import ru.sovaowltv.service.unclassified.RandomUtil;
import ru.sovaowltv.service.user.UserUtil;
import ru.sovaowltv.service.user.UsersRepositoryHandler;
import ru.sovaowltv.service.user.params.UserCoinsUtil;

import java.io.BufferedReader;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@PropertySource("classpath:constants.yml")
public class SmilesUtil {
    private final SmilesRepository smilesRepository;

    private final UsersRepositoryHandler usersRepositoryHandler;

    private final UserUtil userUtil;
    private final RarityUtil rarityUtil;
    private final RandomUtil randomUtil;
    private final UserCoinsUtil userCoinsUtil;

    @Value("${caravanDuplicatePriceDelimiter}")
    private int caravanDuplicatePriceDelimiter;

    @Value("${caravanLowPriceCommon}")
    private int caravanLowPriceCommon;

    @Value("${caravanHighPriceCommon}")
    private int caravanHighPriceCommon;

    void makeSureFilesWithSmilesExists(String filePath1, String filePath2) {
        makeSureFileWithSmilesExists(filePath1);
        makeSureFileWithSmilesExists(filePath2);
    }

    void makeSureFileWithSmilesExists(String pathUrl) {
        try {
            Files.createFile(Paths.get(pathUrl));
        } catch (FileAlreadyExistsException ignored) {
        } catch (Exception e) {
            log.error("make sure file with smiles exists", e);
        }
    }

    void smilesInit(String link, Map<String, String> smiles) {
        makeSureFileWithSmilesExists(link);
        if (!smiles.isEmpty()) smiles.clear();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(link))) {
            String input;
            while ((input = br.readLine()) != null) {
                if (!smiles.containsKey(input.split(" ")[1]))
                    smiles.put(input.split(" ")[1], input.split(" ")[0]);
            }
        } catch (Exception e) {
            log.error("smile init", e);
        }
    }

    public void buySmileById(String id) {
        User user = userUtil.getUser();
        Smile smile = getSmileById(id);
        if (!user.getSmiles().contains(smile) && userCoinsUtil.withdrawMoney(user, smile.getPrice())) {
            user.getSmiles().add(smile);
            usersRepositoryHandler.saveAndFree(user);
            return;
        }
        usersRepositoryHandler.saveAndFree(user);
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ERR buying smiles");
    }

    private Smile getSmileById(String id) {
        try {
            Optional<Smile> smileOptional = smilesRepository.findById(Long.valueOf(id));
            if (smileOptional.isEmpty()) {
                log.error("can't find smile by id " + id);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't find file by id");
            }
            return smileOptional.get();
        } catch (NumberFormatException e) {
            log.error("smile id must be number, but was: " + id, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id must be number");
        } catch (Throwable e) {
            log.error("unknown error", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown error");
        }
    }

    public String getSmileReward(User user, Rarity caravanRarity, double chanceMultiplier) {
        Rarity rarity = rarityUtil.generateRarity(rarityUtil.getMinRarity(caravanRarity), caravanRarity, chanceMultiplier);
        List<Smile> allSmilesByCurrentRarity = smilesRepository.findAllByRarity(rarity);
        List<Smile> targetSmiles = new ArrayList<>();
        allSmilesByCurrentRarity.forEach(smile -> {
            if (!user.getSmiles().contains(smile)) targetSmiles.add(smile);
        });
        if (!targetSmiles.isEmpty()) {
            int index = randomUtil.nextInt(targetSmiles.size());
            Smile smile = targetSmiles.get(index);
            user.getSmiles().add(smile);
            return convertSmileToString(user, smile, false);
        } else {
            return getDuplicateSmileReward(user, allSmilesByCurrentRarity);
        }
    }

    private String getDuplicateSmileReward(User user, List<Smile> allSmilesByCurrentRarity) {
        int index = randomUtil.nextInt(allSmilesByCurrentRarity.size());
        Smile smile = allSmilesByCurrentRarity.get(index);
        setPriceIfFreeSmile(user, smile);
        int price = smile.getPrice() / caravanDuplicatePriceDelimiter;
        smile.setPrice(price);
        userCoinsUtil.addCoins(user, price);
        return convertSmileToString(user, smile, true);
    }

    private void setPriceIfFreeSmile(User user, Smile smile) {
        if (smile.getPrice() == 0) {
            int price = randomUtil.getIntWithBounds(caravanLowPriceCommon, caravanHighPriceCommon);
            double realPrice = userCoinsUtil.calcCoinsRewardNoModificator(user, price);
            smile.setPrice((int) realPrice);
        }
    }

    private String convertSmileToString(User user, Smile smile, boolean duplicate) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", "smile");
        map.put("name", smile.getName());
        map.put("link", smile.getLink());
        map.put("rarity", smile.getRarity());
        map.put("price", smile.getPrice());
        map.put("exp", 0);
        map.put("premiumUser", user.isPremiumUser());
        map.put("nickname", user.getNickname());
        if (duplicate) map.put("duplicate", true);
        return new Gson().toJson(map);
    }
}