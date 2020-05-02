package ru.sovaowltv.model.chat;

import org.springframework.stereotype.Service;

@Service
public interface Smiles {
    boolean isSmile(String smile);

    String getSmile(String smile);

    boolean canUseSmile(String smile);
}
