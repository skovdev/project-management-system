package local.pms.aiservice.model.request;

import local.pms.aiservice.model.Message;

import java.util.List;

public record ChatRequest(String model, List<Message> messages) {}
