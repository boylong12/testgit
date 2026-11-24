package com.survey.repo;

import com.survey.domain.Response;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResponseRepository extends JpaRepository<Response, Long> {
    boolean existsBySurveyIdAndVisitorKey(Long surveyId, String visitorKey);

    List<Response> findBySurveyIdOrderBySubmittedAtDesc(Long surveyId);
}