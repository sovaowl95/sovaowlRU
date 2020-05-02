package ru.sovaowltv.service.admin.smiles;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.sovaowltv.model.shop.Smile;
import ru.sovaowltv.repositories.website.SmilesRepository;
import ru.sovaowltv.service.data.DataExtractor;
import ru.sovaowltv.service.smiles.WebSiteSmileAbstract;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminSmilesUtil {
    private final SmilesRepository smilesRepository;

    private final WebSiteSmileAbstract webSiteSmilesUtil;
    private final DataExtractor dataExtractor;

    public void addNewSmileStub() {
        webSiteSmilesUtil.createNewSmile();
    }

    public void addSmileForEveryone(String body) {
        JsonObject jsonObject = dataExtractor.extractJsonFromString(body);
        long id = jsonObject.getAsJsonPrimitive("id").getAsLong();
        Smile smile = getSmileById(id);
        webSiteSmilesUtil.addSmileForEveryone(smile);
    }

    private Smile getSmileById(Long id) {
        Optional<Smile> smileOptional = smilesRepository.findById(id);
        if (smileOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't find smile by id");
        }
        return smileOptional.get();
    }
}
