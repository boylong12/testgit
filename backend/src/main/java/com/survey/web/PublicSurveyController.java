package com.survey.web;

import com.survey.dto.Dtos;
import com.survey.service.SurveyService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/surveys")
public class PublicSurveyController {
    private final SurveyService surveyService;

    public PublicSurveyController(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    @GetMapping("/{token}")
    public Dtos.PublicSurveyDto get(@PathVariable String token) {
        return surveyService.getPublic(token);
    }

    @PostMapping("/{token}/responses")
    public void submit(@PathVariable String token, @Valid @RequestBody Dtos.SubmitResponseRequest req) {
        surveyService.submitPublic(token, req);
    }
}