package ru.sovaowltv.service.multistream;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import ru.sovaowltv.exceptions.multistream.*;
import ru.sovaowltv.exceptions.stream.StreamNotFoundException;
import ru.sovaowltv.model.multistream.MultiStream;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.repositories.multistream.MultiStreamRepository;
import ru.sovaowltv.service.stream.StreamRepositoryHandler;
import ru.sovaowltv.service.stream.StreamUtil;
import ru.sovaowltv.service.user.UserUtil;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MultiStreamUtil {
    private final MultiStreamRepository multiStreamRepository;
    private final StreamRepositoryHandler streamRepositoryHandler;

    private final UserUtil userUtil;
    private final StreamUtil streamUtil;

    public MultiStream create() {
        User userREADONLY = userUtil.getUserREADONLY();
        Optional<MultiStream> multiStreamOptional
                = multiStreamRepository.findByUserId(userREADONLY.getId());
        if (multiStreamOptional.isPresent()) {
            throw new MultiStreamAlreadyExistsException("multistream already exist. user:" + userREADONLY.getId());
        }

        Optional<Stream> streamOptional = streamRepositoryHandler.getByUserId(userREADONLY.getId());
        if (streamOptional.isEmpty()) {
            throw new StreamNotFoundException("multistream can't find stream by user id:" + userREADONLY.getId());
        }

        MultiStream multiStream = new MultiStream();
        multiStream.setStreamSet(Set.of(streamOptional.get()));
        multiStream.setInviteCode(UUID.randomUUID().toString());
        multiStream.setUser(userREADONLY);
        return multiStreamRepository.saveAndFlush(multiStream);
    }

    public void delete() {
        User userREADONLY = userUtil.getUserREADONLY();
        Optional<MultiStream> multiStreamOptional
                = multiStreamRepository.findByUserId(userREADONLY.getId());
        if (multiStreamOptional.isEmpty()) {
            throw new MultiStreamNotFoundException("cant find multistream. user:" + userREADONLY.getId());
        }
        multiStreamRepository.delete(multiStreamOptional.get());
    }

    public MultiStream join(Long msId, String code) {
        MultiStream multiStream = getMultiStream(msId);
        Stream stream = getStream();

        if (multiStream.getStreamSet().contains(stream)) {
            throw new StreamAlreadyInMultiStreamException("join ms:" + msId + " stream:" + stream.getId());
        }

        if (!multiStream.getInviteCode().equals(code)) {
            throw new IncorrectJoinCodeException("msId:" + msId + " code:" + code);
        }

        multiStream.getStreamSet().add(stream);
        return multiStreamRepository.saveAndFlush(multiStream);
    }

    public void left(Long msId) {
        MultiStream multiStream = getMultiStream(msId);
        Stream stream = getStream();

        if (!multiStream.getStreamSet().contains(stream)) {
            throw new StreamNotInMultiStreamException("stream " + stream.getId() + " not in ms:" + msId);
        }

        multiStream.getStreamSet().remove(stream);
        multiStreamRepository.save(multiStream);
    }

    public void init(Long multiStreamId, Model model) {
        MultiStream multiStream = getMultiStream(multiStreamId);
        model.addAttribute("streams", multiStream.getStreamSet());
        model.addAttribute("streamsLogins", multiStream.getStreamSet()
                .stream()
                .map(stream -> stream.getUser().getNickname())
                .collect(Collectors.toList())
        );
        model.addAttribute("msId", multiStream.getId());
        streamUtil.initStreamModelUserData(model);
    }

    @NotNull
    private Stream getStream() {
        User userREADONLY = userUtil.getUserREADONLY();
        Optional<Stream> streamOptional = streamRepositoryHandler.getByUserId(userREADONLY.getId());
        if (streamOptional.isEmpty()) {
            throw new StreamNotFoundException("can't find stream by user id:" + userREADONLY.getId());
        }
        return streamOptional.get();
    }

    @NotNull
    private MultiStream getMultiStream(Long msId) {
        Optional<MultiStream> multiStreamOptional = multiStreamRepository.findById(msId);
        if (multiStreamOptional.isEmpty()) {
            throw new MultiStreamNotFoundException("cant find multistream. msId:" + msId);
        }
        return multiStreamOptional.get();
    }

    public void setMSIfExist(Model model, User user) {
        if (user == null) return;
        Optional<MultiStream> multiStreamOptional = multiStreamRepository.findByUserId(user.getId());
        if (multiStreamOptional.isPresent()) {
            model.addAttribute("multiStream", multiStreamOptional.get());
            return;
        }

        List<MultiStream> results = new ArrayList<>();

        List<MultiStream> multiStreams = multiStreamRepository.findAll();
        for (MultiStream multiStream : multiStreams) {
            Set<Stream> streamSet = multiStream.getStreamSet();
            for (Stream stream : streamSet) {
                if (stream.getUser().equals(user)) {
                    results.add(multiStream);
                }
            }
        }

        if (!results.isEmpty()) {
            model.addAttribute("multiStreamIn", results);
        }
    }
}
