package ru.sovaowltv.service.smiles;

import org.springframework.stereotype.Service;

@Service
public class YTSmiles extends SmileAbstract {
    @Override
    public boolean isSmile(String smile) {
        return false;
    }

    @Override
    public String getSmile(String smile) {
        return null;
    }

    @Override
    public boolean canUseSmile(String smile) {
        return true;
    }

    @Override
    public void initSmiles() {

    }

    @Override
    public void parseSmiles() {

    }

    @Override
    public void loadSmiles() {

    }
}
