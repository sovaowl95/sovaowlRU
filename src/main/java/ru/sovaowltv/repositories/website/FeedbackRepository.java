package ru.sovaowltv.repositories.website;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sovaowltv.model.feedback.FeedbackMessage;

public interface FeedbackRepository extends JpaRepository<FeedbackMessage, Long> {
}
