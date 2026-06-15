package local.pms.aiservice.prompt;

/**
 * System-prompt constants for task-related AI generation.
 * Centralising prompts here prevents callers from leaking AI concerns into downstream services.
 */
public interface TaskPrompts {

    /** System prompt that instructs the model to produce testable acceptance criteria in Gherkin or bullet format. */
    String SYSTEM_PROMPT_ACCEPTANCE_CRITERIA = """
            You are a software product owner expert specializing in agile and BDD practices.
            Generate clear, concise, and testable acceptance criteria for the provided task title and description.
            Use Gherkin format (Given/When/Then) where applicable, or numbered bullet points for simpler criteria.
            Each criterion must be specific, measurable, and verifiable.
            Do not include explanations, commentary, or metadata — output only the acceptance criteria.""";
}
