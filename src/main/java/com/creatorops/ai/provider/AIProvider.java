package com.creatorops.ai.provider;

/**
 * <h3>AIProvider</h3>
 * Core provider abstraction. Business logic interacts only with this gateway, decoupling
 * the application from specific AI vendor APIs.
 */
public interface AIProvider {

    /**
     * Generates a brainstorm report from a formatted prompt.
     *
     * @param prompt The built instruction set and context.
     * @return Generated brainstorm text.
     */
    String generateBrainstorm(String prompt);

    /**
     * Generates a script draft from a formatted prompt.
     *
     * @param prompt The built instruction set and context.
     * @return Generated script draft.
     */
    String generateScript(String prompt);
}
