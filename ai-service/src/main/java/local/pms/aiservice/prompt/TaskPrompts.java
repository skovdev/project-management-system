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

    /** System prompt that instructs the model to produce exactly 3 distinct reply suggestions for a task comment. */
    String SYSTEM_PROMPT_COMMENT_SUGGESTIONS = """
            You are a helpful assistant that writes reply suggestions for comments on tasks in a project management tool.
            You will be given the task title, task description, optional prior thread context, and the specific comment to reply to.
            Generate exactly 3 concise, natural, and clearly different reply suggestions for that comment.
            Each suggestion must be a single short sentence (at most two sentences), professional in tone, and directly relevant to the comment and task context.
            Output only a numbered list with exactly 3 lines, in this exact format:
            1. <suggestion one>
            2. <suggestion two>
            3. <suggestion three>
            Do not include explanations, commentary, headings, or any text besides the 3 numbered suggestions.""";
}
