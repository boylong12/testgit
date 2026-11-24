package com.survey.repo;

import com.survey.domain.QuestionOption;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionOptionRepository extends JpaRepository<QuestionOption, Long> {
    List<QuestionOption> findByQuestionIdOrderBySortOrderAsc(Long questionId);

    void deleteByQuestionIdIn(Iterable<Long> questionIds);
}