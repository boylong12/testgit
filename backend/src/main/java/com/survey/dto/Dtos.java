package com.survey.dto;

import com.survey.domain.QuestionType;
import com.survey.domain.SurveyStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

public final class Dtos {
    private Dtos() {}

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}

    public record LoginResponse(String token, String username) {}

    public record OptionDto(Long id, String label, int sortOrder) {}

    public record QuestionDto(
            Long id,
            QuestionType type,
            String title,
            boolean required,
            int sortOrder,
            List<OptionDto> options) {}

    public record SurveySummaryDto(
            Long id, String title, SurveyStatus status, String publicToken, boolean allowMultiple) {}

    public record SurveyDetailDto(
            Long id,
            String title,
            String description,
            SurveyStatus status,
            String publicToken,
            boolean allowMultiple,
            boolean locked,
            List<QuestionDto> questions) {}

    public record SurveyCreateRequest(
            @NotBlank String title, String description, boolean allowMultiple) {}

    public record SurveyUpdateRequest(
            @NotBlank String title, String description, boolean allowMultiple, List<QuestionInput> questions) {}

    public record QuestionInput(
            Long id,
            @NotNull QuestionType type,
            @NotBlank String title,
            boolean required,
            int sortOrder,
            List<OptionInput> options) {}

    public record OptionInput(Long id, @NotBlank String label, int sortOrder) {}

    public record PublicSurveyDto(String title, String description, List<QuestionDto> questions) {}

    public record SubmitResponseRequest(
            @NotNull Map<String, Object> answers, String visitorKey) {}

    public record StatsDto(long responseCount, List<QuestionStats> questions) {}

    public record QuestionStats(Long questionId, String title, QuestionType type, Map<String, Long> counts, List<String> texts) {}
}