package com.creatorops.assignment.entity;

/**
 * <h3>Why this class exists</h3>
 * {@code AssignmentType} lists the supported execution components/stages that can be assigned to team members.
 * <p>
 * <h3>How it supports creator collaboration</h3>
 * Allows agency managers to designate specific team roles (e.g. Writer for Script, Editor for Editing) on a Content card.
 */
public enum AssignmentType {
    RESEARCH,
    SCRIPT,
    PRODUCTION,
    EDITING,
    REVIEW,
    PUBLISHING
}
