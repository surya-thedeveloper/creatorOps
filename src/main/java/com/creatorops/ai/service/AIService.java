package com.creatorops.ai.service;

import com.creatorops.research.dto.ResearchItemResponse;
import com.creatorops.script.dto.ScriptResponse;

/**
 * Service contract for AI-assisted brainstorming and script drafting flows.
 */
public interface AIService {

    /**
     * Generates a brainstorm research card using compiled research.
     *
     * @param contentId ID of the parent content card.
     * @param userEmail Email of the authenticated creator user.
     * @return ResearchItemResponse containing the generated brainstorm.
     */
    ResearchItemResponse generateBrainstorm(Long contentId, String userEmail);

    /**
     * Generates a first script draft using compiled research and brainstorms.
     *
     * @param contentId ID of the parent content card.
     * @param userEmail Email of the authenticated creator user.
     * @return ScriptResponse containing the generated script version.
     */
    ScriptResponse generateScript(Long contentId, String userEmail);
}
