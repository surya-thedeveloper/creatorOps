package com.creatorops.integration;

import com.creatorops.organization.entity.Organization;
import com.creatorops.organization.repository.OrganizationRepository;
import com.creatorops.auth.entity.User;
import com.creatorops.auth.entity.UserRole;
import com.creatorops.auth.repository.UserRepository;
import com.creatorops.brand.entity.Brand;
import com.creatorops.brand.repository.BrandRepository;
import com.creatorops.content.entity.Content;
import com.creatorops.content.entity.ContentType;
import com.creatorops.content.entity.ContentStage;
import com.creatorops.content.entity.ContentPriority;
import com.creatorops.content.repository.ContentRepository;
import com.creatorops.assignment.entity.Assignment;
import com.creatorops.assignment.entity.AssignmentType;
import com.creatorops.assignment.repository.AssignmentRepository;
import com.creatorops.assignment.service.AssignmentService;
import com.creatorops.assignment.dto.AssignmentRequest;
import com.creatorops.task.entity.Task;
import com.creatorops.task.entity.TaskPriority;
import com.creatorops.task.repository.TaskRepository;
import com.creatorops.task.service.TaskService;
import com.creatorops.task.dto.TaskRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class TenantIsolationIntegrationTests {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskService taskService;

    private Organization org1;
    private Organization org2;

    private User managerOrg1;
    private User managerOrg2;
    private User contributorOrg1;
    private User contributorOrg2;

    private Brand brandOrg1;
    private Brand brandOrg2;

    private Content contentOrg1;
    private Content contentOrg2;

    private Assignment assignmentOrg1;

    @BeforeEach
    void setUp() {
        // Create Organizations
        org1 = new Organization("Org One", "http://logo1.com");
        org1 = organizationRepository.save(org1);

        org2 = new Organization("Org Two", "http://logo2.com");
        org2 = organizationRepository.save(org2);

        // Create Users for Org 1
        managerOrg1 = new User("Manager One", "manager1@org1.com", "password");
        managerOrg1.setRole(UserRole.MANAGER);
        managerOrg1.setOrganization(org1);
        managerOrg1 = userRepository.save(managerOrg1);

        contributorOrg1 = new User("Contributor One", "contrib1@org1.com", "password");
        contributorOrg1.setRole(UserRole.CONTRIBUTOR);
        contributorOrg1.setOrganization(org1);
        contributorOrg1 = userRepository.save(contributorOrg1);

        // Create Users for Org 2
        managerOrg2 = new User("Manager Two", "manager2@org2.com", "password");
        managerOrg2.setRole(UserRole.MANAGER);
        managerOrg2.setOrganization(org2);
        managerOrg2 = userRepository.save(managerOrg2);

        contributorOrg2 = new User("Contributor Two", "contrib2@org2.com", "password");
        contributorOrg2.setRole(UserRole.CONTRIBUTOR);
        contributorOrg2.setOrganization(org2);
        contributorOrg2 = userRepository.save(contributorOrg2);

        // Create Brands
        brandOrg1 = new Brand("Brand One", "Desc 1", "http://brand1.com", org1);
        brandOrg1 = brandRepository.save(brandOrg1);

        brandOrg2 = new Brand("Brand Two", "Desc 2", "http://brand2.com", org2);
        brandOrg2 = brandRepository.save(brandOrg2);

        // Create Content
        contentOrg1 = new Content(brandOrg1, "Content One", "Desc Content 1", ContentType.YOUTUBE_VIDEO, ContentStage.IDEA, ContentPriority.MEDIUM, null, null);
        contentOrg1 = contentRepository.save(contentOrg1);

        contentOrg2 = new Content(brandOrg2, "Content Two", "Desc Content 2", ContentType.YOUTUBE_VIDEO, ContentStage.IDEA, ContentPriority.MEDIUM, null, null);
        contentOrg2 = contentRepository.save(contentOrg2);

        // Create Assignment under Org 1
        assignmentOrg1 = new Assignment();
        assignmentOrg1.setContent(contentOrg1);
        assignmentOrg1.setAssignedToUser(contributorOrg1);
        assignmentOrg1.setAssignedByUser(managerOrg1);
        assignmentOrg1.setAssignmentType(AssignmentType.SCRIPT);
        assignmentOrg1.setStatus(com.creatorops.assignment.entity.AssignmentStatus.ASSIGNED);
        assignmentOrg1.setDueDate(OffsetDateTime.now().plusDays(2));
        assignmentOrg1 = assignmentRepository.save(assignmentOrg1);
    }

    @Test
    void testAssignmentCreation_TenantIsolation() {
        // Manager of Org 1 tries to create assignment for content of Org 2
        AssignmentRequest request = new AssignmentRequest(contributorOrg1.getId(), AssignmentType.SCRIPT, "Notes", OffsetDateTime.now().plusDays(2));
        
        assertThrows(AccessDeniedException.class, () -> {
            assignmentService.createAssignment(contentOrg2.getId(), managerOrg1.getEmail(), request);
        });

        // Manager of Org 1 tries to assign a user from Org 2 to content of Org 1
        AssignmentRequest crossAssignRequest = new AssignmentRequest(contributorOrg2.getId(), AssignmentType.SCRIPT, "Notes", OffsetDateTime.now().plusDays(2));
        
        assertThrows(IllegalArgumentException.class, () -> {
            assignmentService.createAssignment(contentOrg1.getId(), managerOrg1.getEmail(), crossAssignRequest);
        });
    }

    @Test
    void testAssignmentAccess_TenantIsolation() {
        // Manager of Org 2 tries to access assignment of Org 1
        assertThrows(AccessDeniedException.class, () -> {
            assignmentService.getAssignmentById(assignmentOrg1.getId(), managerOrg2.getEmail());
        });
    }

    @Test
    void testTaskCreation_TenantIsolation() {
        // Manager of Org 2 tries to create a task under Assignment of Org 1
        TaskRequest taskRequest = new TaskRequest("Task Title", "Task Desc", TaskPriority.HIGH, contributorOrg2.getId(), OffsetDateTime.now().plusDays(2));

        assertThrows(AccessDeniedException.class, () -> {
            taskService.createTask(assignmentOrg1.getId(), managerOrg2.getEmail(), taskRequest);
        });

        // Manager of Org 1 tries to assign task to a user of Org 2
        TaskRequest crossAssignTaskRequest = new TaskRequest("Task Title", "Task Desc", TaskPriority.HIGH, contributorOrg2.getId(), OffsetDateTime.now().plusDays(2));
        
        assertThrows(IllegalArgumentException.class, () -> {
            taskService.createTask(assignmentOrg1.getId(), managerOrg1.getEmail(), crossAssignTaskRequest);
        });
    }
}
