package ru.sovaowltv.service.smiles;

import lombok.extern.slf4j.Slf4j;
import ru.sovaowltv.model.chat.Smiles;

import java.util.HashMap;

@Slf4j
public abstract class SmileAbstract implements Smiles {
    final HashMap<String, String> smiles = new HashMap<>();

    public abstract void initSmiles();

    public abstract void parseSmiles();

    public abstract void loadSmiles();

}