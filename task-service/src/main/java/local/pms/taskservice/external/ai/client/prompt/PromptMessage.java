package local.pms.taskservice.external.ai.client.prompt;

/**
 * System prompt constants used when calling the AI service for task-related generation.
 */
public interface PromptMessage {

    String SYSTEM_PROMPT_ACCEPTANCE_CRITERIA = """
            You are a software product owner expert specializing in agile and BDD practices.
            Generate clear, concise, and testable acceptance criteria for the provided task title and description.
            Use Gherkin format (Given/When/Then) where applicable, or numbered bullet points for simpler criteria.
            Each criterion must be specific, measurable, and verifiable.
            Do not include explanations, commentary, or metadata — output only the acceptance criteria.""";
}
