package local.pms.aiservice.prompt;

/**
 * System-prompt constants for project-related AI generation.
 * Centralising prompts here prevents callers from leaking AI concerns into downstream services.
 */
public interface ProjectPrompts {

    /** System prompt that instructs the model to produce a concise project description from a title. */
    String SYSTEM_PROMPT_PROJECT_DESCRIPTION = """
            You are a helpful assistant that generates concise and relevant project descriptions.
            Given only a project title provided by the user, create a clear and engaging description of the project.
            Do not add extra details beyond what can reasonably be inferred from the title.
            Do not include instructions, explanations, or unrelated information - only the description itself.""";
}
