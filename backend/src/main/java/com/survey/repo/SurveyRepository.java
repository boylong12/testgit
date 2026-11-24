package com.survey.repo;

import com.survey.domain.Survey;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyRepository extends JpaRepository<Survey, Long> {
    Optional<Survey> findByPublicToken(String publicToken);
}