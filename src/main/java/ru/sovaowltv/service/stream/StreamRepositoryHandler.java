package ru.sovaowltv.service.stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sovaowltv.exceptions.stream.StreamNotFoundException;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.repositories.stream.StreamRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class StreamRepositoryHandler {
    private final StreamRepository streamRepository;

    private final Map<Long, Stream> streamMap = new ConcurrentHashMap<>();

    public synchronized List<Stream> getAll() {
        loadAllInMap();
        return new ArrayList<>(streamMap.values());
    }

    public synchronized Optional<Stream> getByUser(User channelOwner) {
        return getByUserId(channelOwner.getId());
    }

    public synchronized Optional<Stream> getByUserId(long id) {
        loadAllInMap();

        Optional<Stream> streamOptional = streamMap.values().stream()
                .filter(stream -> stream.getUser().getId() == id)
                .findFirst();
        if (streamOptional.isEmpty()) {
            streamOptional = streamRepository.findByUserId(id);
            streamOptional.ifPresent(this::addStreamToLocalDB);
        }

        return streamOptional;
    }

    public synchronized Stream getStreamById(Long id) {
        loadAllInMap();
        Stream stream = streamMap.get(id);
        if (stream != null) return stream;

        Optional<Stream> streamOptional = streamRepository.findById(id);
        if (streamOptional.isEmpty()) {
            throw new StreamNotFoundException("Stream not found by id: " + id.toString());
        } else {
            addStreamToLocalDB(streamOptional.get());
            return streamOptional.get();
        }
    }

    private synchronized void loadAllInMap() {
        if (streamMap.isEmpty()) {
            streamRepository.findAll()
                    .forEach(this::addStreamToLocalDB);
        }
    }

    private synchronized void addStreamToLocalDB(Stream stream) {
        streamMap.put(stream.getId(), stream);
    }

    public void save(Stream stream) {
        streamRepository.save(stream);
        addStreamToLocalDB(stream);
    }

    public void delete(Stream stream) {
        streamRepository.delete(stream);
        streamMap.remove(stream.getId());
    }

    public Stream saveAndFlush(Stream stream) {
        stream = streamRepository.saveAndFlush(stream);
        addStreamToLocalDB(stream);
        return stream;
    }
}
