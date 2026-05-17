package com.platform.diagnostic.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AnswerSubmission(
        int questionNumber,
        String answer
) {
    public int getQuestionNumber() { return questionNumber; }
    public String getAnswer() { return answer; }
}