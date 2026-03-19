package com.accesosport.user.domain.model;

/**
 * Enumeration representing various user roles within the system.
 * <ul>
 * <li>ROLE_ADMIN: Represents an administrator with elevated permissions.</li>
 * <li>ROLE_USER: Represents a standard system user.</li>
 * <li>ROLE_ORGANIZER: Represents a user responsible for organizing events or activities.</li>
 * <li>ROLE_PARTICIPANT: Represents a user participating in events or activities.</li>
 * </ul>
 */
public enum RoleEnumeration {
    /**
     * Represents an administrator role with elevated permissions within the system.
     * This role typically grants access to administrative functions and operations
     * that are not available to standard users or other role types.
     */
    ROLE_ADMIN,
    /**
     * Represents the standard user role within the system.
     * This role is assigned to users who have access to general features of the application
     * without any special administrative or organizational permissions.
     */
    ROLE_USER,
    /**
     * Represents a user role responsible for organizing events or activities within the system.
     * This role typically includes permissions and functionality needed to plan, manage,
     * and oversee events or similar activities, distinguishing it from other user roles such
     * as participants or administrators.
     */
    ROLE_ORGANIZER,
    /**
     * Represents a participant role within the system.
     * This role is assigned to users who participate in events or activities
     * without administrative or organizational responsibilities.
     * Users with this role typically have access to features and functionality
     * required for event participation or similar use cases.
     */
    ROLE_PARTICIPANT
}
