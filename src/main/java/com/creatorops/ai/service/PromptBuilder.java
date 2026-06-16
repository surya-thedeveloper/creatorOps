package com.creatorops.ai.service;

import com.creatorops.content.entity.Content;
import com.creatorops.research.entity.ResearchItem;

import java.util.List;

/**
 * <h3>PromptBuilder</h3>
 * Handles dynamic prompt construction. Isolating instructions and formatting templates
 * ensures that templates can be tweaked without altering the transactional service operations.
 */
public class PromptBuilder {

    /**
     * Builds the prompt for generating brainstorming recommendations.
     */
    public static String buildBrainstormPrompt(Content content,
                                               List<ResearchItem> notes,
                                               List<ResearchItem> links,
                                               List<ResearchItem> existingBrainstorms) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an expert creative content assistant helping a creator brainstorm ideas.\n\n");
        
        sb.append("--- CONTENT DETAILS ---\n");
        sb.append("Title: ").append(content.getTitle()).append("\n");
        if (content.getDescription() != null && !content.getDescription().isBlank()) {
            sb.append("Description: ").append(content.getDescription()).append("\n");
        }
        sb.append("Type: ").append(content.getType()).append("\n");
        sb.append("Priority: ").append(content.getPriority()).append("\n\n");

        sb.append("--- RESEARCH NOTES ---\n");
        if (notes == null || notes.isEmpty()) {
            sb.append("No notes provided.\n");
        } else {
            for (ResearchItem note : notes) {
                sb.append("- ").append(note.getTitle()).append(": ").append(note.getContentText()).append("\n");
            }
        }
        sb.append("\n");

        sb.append("--- RESEARCH REFERENCES & LINKS ---\n");
        if (links == null || links.isEmpty()) {
            sb.append("No links provided.\n");
        } else {
            for (ResearchItem link : links) {
                sb.append("- ").append(link.getTitle()).append(": ").append(link.getUrl()).append("\n");
            }
        }
        sb.append("\n");

        sb.append("--- EXISTING AI BRAINSTORM NOTES ---\n");
        if (existingBrainstorms == null || existingBrainstorms.isEmpty()) {
            sb.append("No previous brainstorm notes found.\n");
        } else {
            for (ResearchItem bs : existingBrainstorms) {
                sb.append("- ").append(bs.getTitle()).append(": ").append(bs.getContentText()).append("\n");
            }
        }
        sb.append("\n");

        sb.append("--- INSTRUCTIONS ---\n");
        sb.append("Analyze the provided content metadata and research contexts to generate a brainstorming report.\n");
        sb.append("Your response MUST include the following structured sections:\n");
        sb.append("1. **Creative Content Angles**: Recommend 3 distinct ways/perspectives to present this topic.\n");
        sb.append("2. **High-converting Hooks**: Suggest 3 hook variations for the start of the content.\n");
        sb.append("3. **Titles**: Recommend 3 catchy title suggestions.\n");
        sb.append("4. **Audience Questions**: List 3 main questions the target audience will expect answered.\n");
        sb.append("5. **Visual Thumbnail Ideas**: Provide 3 ideas for visual imagery/thumbnails.\n");
        sb.append("6. **Outline Structure**: Provide a suggested section breakdown/outline.\n\n");
        sb.append("Provide only clean, well-formatted text. Do not include markdown meta block quotes or system notes.");
        
        return sb.toString();
    }

    /**
     * Builds the prompt for generating script drafts.
     */
    public static String buildScriptPrompt(Content content,
                                           List<ResearchItem> notes,
                                           List<ResearchItem> links,
                                           List<ResearchItem> brainstorms) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an expert scriptwriter helping a creator draft a first script version.\n\n");

        sb.append("--- CONTENT DETAILS ---\n");
        sb.append("Title: ").append(content.getTitle()).append("\n");
        if (content.getDescription() != null && !content.getDescription().isBlank()) {
            sb.append("Description: ").append(content.getDescription()).append("\n");
        }
        sb.append("Type: ").append(content.getType()).append("\n\n");

        sb.append("--- RESEARCH DETAILS ---\n");
        if (notes == null || notes.isEmpty()) {
            sb.append("No notes provided.\n");
        } else {
            for (ResearchItem note : notes) {
                sb.append("- ").append(note.getTitle()).append(": ").append(note.getContentText()).append("\n");
            }
        }
        sb.append("\n");

        sb.append("--- REFERENCES & LINKS ---\n");
        if (links == null || links.isEmpty()) {
            sb.append("No links provided.\n");
        } else {
            for (ResearchItem link : links) {
                sb.append("- ").append(link.getTitle()).append(": ").append(link.getUrl()).append("\n");
            }
        }
        sb.append("\n");

        sb.append("--- AI BRAINSTORM NOTES ---\n");
        if (brainstorms == null || brainstorms.isEmpty()) {
            sb.append("No brainstorm notes provided.\n");
        } else {
            for (ResearchItem bs : brainstorms) {
                sb.append("- ").append(bs.getTitle()).append(": ").append(bs.getContentText()).append("\n");
            }
        }
        sb.append("\n");

        sb.append("--- INSTRUCTIONS ---\n");
        sb.append("Draft a complete first script draft based on the above information.\n");
        sb.append("The script MUST meet the following specifications:\n");
        sb.append("1. **Hook**: An engaging opening designed to capture attention in the first 5 seconds.\n");
        sb.append("2. **Body sections**: A logical flow of concepts answering target audience queries, written in a conversational tone suitable for ").append(content.getType()).append(".\n");
        sb.append("3. **CTA**: A clear, compelling closing Call to Action.\n\n");
        sb.append("Format your response cleanly. Provide only the actual script text.");

        return sb.toString();
    }
}
