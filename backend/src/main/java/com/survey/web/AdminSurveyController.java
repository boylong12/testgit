package com.survey.web;

import com.survey.dto.Dtos;
import com.survey.service.SurveyService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/surveys")
public class AdminSurveyController {
    private final SurveyService surveyService;

    public AdminSurveyController(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    @GetMapping
    public java.util.List<Dtos.SurveySummaryDto> list() {
        return surveyService.list();
    }

    @GetMapping("/{id}")
    public Dtos.SurveyDetailDto get(@PathVariable Long id) {
        return surveyService.getDetail(id);
    }

    @PostMapping
    public Dtos.SurveyDetailDto create(@Valid @RequestBody Dtos.SurveyCreateRequest req) {
        return surveyService.create(req);
    }

    @PutMapping("/{id}")
    public Dtos.SurveyDetailDto update(@PathVariable Long id, @Valid @RequestBody Dtos.SurveyUpdateRequest req) {
        return surveyService.update(id, req);
    }

    @PostMapping("/{id}/publish")
    public Dtos.SurveyDetailDto publish(@PathVariable Long id) {
        return surveyService.publish(id);
    }

    @PostMapping("/{id}/close")
    public Dtos.SurveyDetailDto close(@PathVariable Long id) {
        return surveyService.close(id);
    }

    @GetMapping("/{id}/stats")
    public Dtos.StatsDto stats(@PathVariable Long id) {
        return surveyService.stats(id);
    }

    @GetMapping(value = "/{id}/export.csv", produces = "text/csv")
    public void export(@PathVariable Long id, HttpServletResponse response) throws IOException {
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=survey-" + id + ".csv");
        response.setContentType(MediaType.TEXT_PLAIN_VALUE);
        surveyService.exportCsv(id, response.getWriter());
    }
}