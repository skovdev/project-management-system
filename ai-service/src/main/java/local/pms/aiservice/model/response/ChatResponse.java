package local.pms.aiservice.model.response;

import local.pms.aiservice.model.Choice;

import java.util.List;

public record ChatResponse(List<Choice> choices) {}
