package ru.sovaowltv.service.stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sovaowltv.exceptions.stream.StreamNotFoundException;
import ru.sovaowltv.model.stream.Stream;
import ru.sovaowltv.model.user.User;
import ru.sovaowltv.repositories.stream.StreamRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StreamRepositoryHandler {
    private final StreamRepository streamRepository;

    public Stream getStreamById(Long id) {
        Optional<Stream> streamOptional = streamRepository.findById(id);
        if (streamOptional.isPresent()) {
            return streamOptional.get();
        } else {
            log.error("cant find stream by id");
            throw new StreamNotFoundException("Stream not found by id: " + id.toString());
        }
    }

    public void save(Stream stream) {
        streamRepository.save(stream);
    }

    public List<Stream> getAll() {
        return streamRepository.findAll();
        //todo:
    }

    public Optional<Stream> getByUser(User channelOwner) {
        return streamRepository.findByUser(channelOwner);
        //todo:
    }

    public void delete(Stream stream) {
        streamRepository.delete(stream);
        //todo:
    }

    public Optional<Stream> getByUserId(long id) {
        return streamRepository.findByUserId(id);
        //todo:
    }

    public Stream saveAndFlush(Stream stream) {
        return streamRepository.saveAndFlush(stream);
    }
}
