package com.survey.repo;

import com.survey.domain.Question;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findBySurveyIdOrderBySortOrderAsc(Long surveyId);

    void deleteBySurveyId(Long surveyId);
}