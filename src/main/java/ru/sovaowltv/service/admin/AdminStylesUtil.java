package ru.sovaowltv.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sovaowltv.service.styles.StyleUtil;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminStylesUtil {
    private final StyleUtil styleUtil;

    public void addNewStyleStub() {
        styleUtil.addNewStyleStub();
    }
}
