package com.survey.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.survey.domain.Answer;
import com.survey.domain.Question;
import com.survey.domain.QuestionOption;
import com.survey.domain.QuestionType;
import com.survey.domain.Response;
import com.survey.domain.Survey;
import com.survey.domain.SurveyStatus;
import com.survey.dto.Dtos;
import com.survey.repo.AnswerRepository;
import com.survey.repo.QuestionOptionRepository;
import com.survey.repo.QuestionRepository;
import com.survey.repo.ResponseRepository;
import com.survey.repo.SurveyRepository;
import java.io.IOException;
import java.io.Writer;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SurveyService {
    private final SurveyRepository surveyRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository optionRepository;
    private final ResponseRepository responseRepository;
    private final AnswerRepository answerRepository;
    private final ObjectMapper objectMapper;
    private final SecureRandom random = new SecureRandom();

    public SurveyService(
            SurveyRepository surveyRepository,
            QuestionRepository questionRepository,
            QuestionOptionRepository optionRepository,
            ResponseRepository responseRepository,
            AnswerRepository answerRepository,
            ObjectMapper objectMapper) {
        this.surveyRepository = surveyRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.responseRepository = responseRepository;
        this.answerRepository = answerRepository;
        this.objectMapper = objectMapper;
    }

    public List<Dtos.SurveySummaryDto> list() {
        return surveyRepository.findAll().stream()
                .map(s -> new Dtos.SurveySummaryDto(
                        s.getId(), s.getTitle(), s.getStatus(), s.getPublicToken(), s.isAllowMultiple()))
                .toList();
    }

    public Dtos.SurveyDetailDto getDetail(Long id) {
        Survey survey = requireSurvey(id);
        return toDetail(survey);
    }

    @Transactional
    public Dtos.SurveyDetailDto create(Dtos.SurveyCreateRequest req) {
        Survey survey = new Survey();
        survey.setTitle(req.title());
        survey.setDescription(req.description());
        survey.setAllowMultiple(req.allowMultiple());
        surveyRepository.save(survey);
        return toDetail(survey);
    }

    @Transactional
    public Dtos.SurveyDetailDto update(Long id, Dtos.SurveyUpdateRequest req) {
        Survey survey = requireSurvey(id);
        if (survey.getStatus() != SurveyStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "已发布或已关闭的问卷不可修改题目");
        }
        survey.setTitle(req.title());
        survey.setDescription(req.description());
        survey.setAllowMultiple(req.allowMultiple());
        survey.touchUpdated();

        List<Long> existingQids = questionRepository.findBySurveyIdOrderBySortOrderAsc(id).stream()
                .map(Question::getId)
                .toList();
        if (!existingQids.isEmpty()) {
            optionRepository.deleteByQuestionIdIn(existingQids);
        }
        questionRepository.deleteBySurveyId(id);

        if (req.questions() != null) {
            for (Dtos.QuestionInput qin : req.questions()) {
                Question q = new Question();
                q.setSurveyId(id);
                q.setType(qin.type());
                q.setTitle(qin.title());
                q.setRequired(qin.required());
                q.setSortOrder(qin.sortOrder());
                questionRepository.save(q);
                if (qin.options() != null) {
                    for (Dtos.OptionInput oin : qin.options()) {
                        QuestionOption opt = new QuestionOption();
                        opt.setQuestionId(q.getId());
                        opt.setLabel(oin.label());
                        opt.setSortOrder(oin.sortOrder());
                        optionRepository.save(opt);
                    }
                }
            }
        }
        surveyRepository.save(survey);
        return toDetail(survey);
    }

    @Transactional
    public Dtos.SurveyDetailDto publish(Long id) {
        Survey survey = requireSurvey(id);
        if (survey.getStatus() == SurveyStatus.PUBLISHED) {
            return toDetail(survey);
        }
        List<Question> questions = questionRepository.findBySurveyIdOrderBySortOrderAsc(id);
        if (questions.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请至少添加一道题目后再发布");
        }
        survey.setStatus(SurveyStatus.PUBLISHED);
        if (survey.getPublicToken() == null) {
            survey.setPublicToken(generateToken());
        }
        survey.touchUpdated();
        surveyRepository.save(survey);
        return toDetail(survey);
    }

    @Transactional
    public Dtos.SurveyDetailDto close(Long id) {
        Survey survey = requireSurvey(id);
        survey.setStatus(SurveyStatus.CLOSED);
        survey.touchUpdated();
        surveyRepository.save(survey);
        return toDetail(survey);
    }

    public Dtos.PublicSurveyDto getPublic(String token) {
        Survey survey =
                surveyRepository.findByPublicToken(token).orElseThrow(() -> notFound("问卷不存在"));
        if (survey.getStatus() != SurveyStatus.PUBLISHED) {
            throw new ResponseStatusException(HttpStatus.GONE, "问卷未开放或已关闭");
        }
        return new Dtos.PublicSurveyDto(survey.getTitle(), survey.getDescription(), loadQuestions(survey.getId()));
    }

    @Transactional
    public void submitPublic(String token, Dtos.SubmitResponseRequest req) {
        Survey survey =
                surveyRepository.findByPublicToken(token).orElseThrow(() -> notFound("问卷不存在"));
        if (survey.getStatus() != SurveyStatus.PUBLISHED) {
            throw new ResponseStatusException(HttpStatus.GONE, "问卷未开放或已关闭");
        }
        String visitorKey = req.visitorKey();
        if (visitorKey == null || visitorKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "缺少 visitorKey");
        }
        if (!survey.isAllowMultiple()
                && responseRepository.existsBySurveyIdAndVisitorKey(survey.getId(), visitorKey)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "您已提交过本问卷");
        }

        List<Question> questions = questionRepository.findBySurveyIdOrderBySortOrderAsc(survey.getId());
        validateAnswers(questions, req.answers());

        Response response = new Response();
        response.setSurveyId(survey.getId());
        response.setVisitorKey(visitorKey);
        responseRepository.save(response);

        for (Question q : questions) {
            Object raw = req.answers().get(String.valueOf(q.getId()));
            if (raw == null) {
                continue;
            }
            Answer answer = new Answer();
            answer.setResponseId(response.getId());
            answer.setQuestionId(q.getId());
            try {
                answer.setValueJson(objectMapper.writeValueAsString(raw));
            } catch (JsonProcessingException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "答案格式无效");
            }
            answerRepository.save(answer);
        }
    }

    public Dtos.StatsDto stats(Long id) {
        requireSurvey(id);
        long count = responseRepository.findBySurveyIdOrderBySubmittedAtDesc(id).size();
        List<Question> questions = questionRepository.findBySurveyIdOrderBySortOrderAsc(id);
        List<Dtos.QuestionStats> qstats = new ArrayList<>();
        for (Question q : questions) {
            List<Answer> answers = answerRepository.findByQuestionId(q.getId());
            Map<String, Long> counts = new HashMap<>();
            List<String> texts = new ArrayList<>();
            for (Answer a : answers) {
                try {
                    Object val = objectMapper.readValue(a.getValueJson(), Object.class);
                    if (q.getType() == QuestionType.SINGLE) {
                        String key = String.valueOf(val);
                        counts.merge(key, 1L, Long::sum);
                    } else if (q.getType() == QuestionType.MULTIPLE) {
                        List<?> list = objectMapper.convertValue(val, new TypeReference<List<Object>>() {});
                        for (Object item : list) {
                            counts.merge(String.valueOf(item), 1L, Long::sum);
                        }
                    } else {
                        texts.add(String.valueOf(val));
                    }
                } catch (Exception ignored) {
                    texts.add(a.getValueJson());
                }
            }
            qstats.add(new Dtos.QuestionStats(q.getId(), q.getTitle(), q.getType(), counts, texts));
        }
        return new Dtos.StatsDto(count, qstats);
    }

    public void exportCsv(Long id, Writer writer) throws IOException {
        requireSurvey(id);
        List<Question> questions = questionRepository.findBySurveyIdOrderBySortOrderAsc(id);
        List<Response> responses = responseRepository.findBySurveyIdOrderBySubmittedAtDesc(id);

        writer.write("response_id,submitted_at");
        for (Question q : questions) {
            writer.write(",");
            writer.write(escapeCsv("Q" + q.getId() + "_" + q.getTitle()));
        }
        writer.write("\n");

        for (Response r : responses) {
            Map<Long, String> row = new HashMap<>();
            answerRepository.findByResponseId(r.getId()).forEach(a -> row.put(a.getQuestionId(), a.getValueJson()));
            writer.write(String.valueOf(r.getId()));
            writer.write(",");
            writer.write(r.getSubmittedAt().toString());
            for (Question q : questions) {
                writer.write(",");
                writer.write(escapeCsv(row.getOrDefault(q.getId(), "")));
            }
            writer.write("\n");
        }
        writer.flush();
    }

    private void validateAnswers(List<Question> questions, Map<String, Object> answers) {
        for (Question q : questions) {
            Object val = answers.get(String.valueOf(q.getId()));
            if (q.isRequired() && (val == null || isBlankValue(val))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "题目未填写: " + q.getTitle());
            }
        }
    }

    private boolean isBlankValue(Object val) {
        if (val instanceof String s) {
            return s.isBlank();
        }
        if (val instanceof List<?> l) {
            return l.isEmpty();
        }
        return false;
    }

    private Dtos.SurveyDetailDto toDetail(Survey survey) {
        boolean locked = survey.getStatus() != SurveyStatus.DRAFT;
        return new Dtos.SurveyDetailDto(
                survey.getId(),
                survey.getTitle(),
                survey.getDescription(),
                survey.getStatus(),
                survey.getPublicToken(),
                survey.isAllowMultiple(),
                locked,
                loadQuestions(survey.getId()));
    }

    private List<Dtos.QuestionDto> loadQuestions(Long surveyId) {
        return questionRepository.findBySurveyIdOrderBySortOrderAsc(surveyId).stream()
                .map(q -> {
                    List<Dtos.OptionDto> opts = optionRepository.findByQuestionIdOrderBySortOrderAsc(q.getId()).stream()
                            .map(o -> new Dtos.OptionDto(o.getId(), o.getLabel(), o.getSortOrder()))
                            .toList();
                    return new Dtos.QuestionDto(q.getId(), q.getType(), q.getTitle(), q.isRequired(), q.getSortOrder(), opts);
                })
                .collect(Collectors.toList());
    }

    private Survey requireSurvey(Long id) {
        return surveyRepository.findById(id).orElseThrow(() -> notFound("问卷不存在"));
    }

    private ResponseStatusException notFound(String msg) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, msg);
    }

    private String generateToken() {
        byte[] bytes = new byte[18];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        String v = value.replace("\"", "\"\"");
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
            return "\"" + v + "\"";
        }
        return v;
    }
}