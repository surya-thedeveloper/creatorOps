package com.creatorops.content.repository;

import com.creatorops.content.entity.Content;
import com.creatorops.content.entity.ContentStage;
import com.creatorops.content.entity.ContentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * <h3>Why this class exists</h3>
 * The {@code ContentRepository} interface handles direct database interactions for {@link Content} entities.
 * <p>
 * <h3>Why it belongs in this package</h3>
 * It belongs in {@code com.creatorops.content.repository} to bundle database operations within the content domain.
 * <p>
 * <h3>Key Annotations</h3>
 * <ul>
 *   <li>{@code @Repository}: Marks it as a Spring Data repository bean.</li>
 *   <li>{@code @Query}: Declares a custom JPQL query for multi-conditional searching.</li>
 *   <li>{@code @Param}: Associates query variables with method parameters.</li>
 * </ul>
 * <p>
 * <h3>Design Decisions</h3>
 * The {@code searchContents} method executes a robust single-query execution that aggregates optional parameters
 * and ensures tenant isolation by joining with {@code Brand} and verifying {@code organizationId}.
 */
@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {

    @Query("SELECT c FROM Content c JOIN c.brand b " +
           "WHERE b.organization.id = :organizationId " +
           "AND (:brandId IS NULL OR b.id = :brandId) " +
           "AND (:stage IS NULL OR c.stage = :stage) " +
           "AND (:type IS NULL OR c.type = :type) " +
           "AND (:titleSearch IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :titleSearch, '%')))")
    Page<Content> searchContents(
            @Param("organizationId") Long organizationId,
            @Param("brandId") Long brandId,
            @Param("stage") ContentStage stage,
            @Param("type") ContentType type,
            @Param("titleSearch") String titleSearch,
            Pageable pageable
    );

    @Query("SELECT c FROM Content c JOIN FETCH c.brand b " +
           "WHERE b.organization.id = :organizationId " +
           "AND c.publishDate >= :startDate " +
           "AND c.publishDate <= :endDate " +
           "AND (:brandId IS NULL OR b.id = :brandId) " +
           "AND (:contentType IS NULL OR c.type = :contentType) " +
           "AND (:stage IS NULL OR c.stage = :stage) " +
           "ORDER BY c.publishDate ASC")
    java.util.List<Content> findCalendarEvents(
            @Param("organizationId") Long organizationId,
            @Param("startDate") java.time.OffsetDateTime startDate,
            @Param("endDate") java.time.OffsetDateTime endDate,
            @Param("brandId") Long brandId,
            @Param("contentType") ContentType contentType,
            @Param("stage") ContentStage stage
    );

    @Query("SELECT c FROM Content c JOIN FETCH c.brand b " +
           "WHERE b.organization.id = :organizationId " +
           "AND c.publishDate >= :now " +
           "ORDER BY c.publishDate ASC")
    Page<Content> findUpcomingContent(
            @Param("organizationId") Long organizationId,
            @Param("now") java.time.OffsetDateTime now,
            Pageable pageable
    );

    @Query("SELECT c FROM Content c JOIN FETCH c.brand b " +
           "WHERE b.organization.id = :organizationId " +
           "AND c.stage = com.creatorops.content.entity.ContentStage.SCHEDULED " +
           "AND (:brandId IS NULL OR b.id = :brandId) " +
           "AND (:contentType IS NULL OR c.type = :contentType) " +
           "ORDER BY c.publishDate ASC")
    Page<Content> findScheduledContent(
            @Param("organizationId") Long organizationId,
            @Param("brandId") Long brandId,
            @Param("contentType") ContentType contentType,
            Pageable pageable
    );

    @Query("SELECT c FROM Content c JOIN FETCH c.brand b " +
           "WHERE b.organization.id = :organizationId " +
           "AND c.stage = com.creatorops.content.entity.ContentStage.PUBLISHED " +
           "AND (:startDate IS NULL OR c.publishDate >= :startDate) " +
           "AND (:endDate IS NULL OR c.publishDate <= :endDate) " +
           "ORDER BY c.publishDate DESC")
    Page<Content> findPublishedContent(
            @Param("organizationId") Long organizationId,
            @Param("startDate") java.time.OffsetDateTime startDate,
            @Param("endDate") java.time.OffsetDateTime endDate,
            Pageable pageable
    );

    @Query("SELECT c FROM Content c JOIN FETCH c.brand b " +
           "WHERE b.organization.id = :organizationId " +
           "AND c.dueDate < :now " +
           "AND c.stage != com.creatorops.content.entity.ContentStage.PUBLISHED " +
           "AND c.stage != com.creatorops.content.entity.ContentStage.CANCELLED " +
           "ORDER BY c.dueDate ASC")
    java.util.List<Content> findOverdueContent(
            @Param("organizationId") Long organizationId,
            @Param("now") java.time.OffsetDateTime now
    );

    @Query("SELECT COUNT(c) FROM Content c WHERE c.brand.organization.id = :organizationId")
    long countByOrganizationId(@Param("organizationId") Long organizationId);

    @Query("SELECT COUNT(c) FROM Content c WHERE c.brand.organization.id = :organizationId AND c.stage = com.creatorops.content.entity.ContentStage.SCHEDULED")
    long countScheduledByOrganizationId(@Param("organizationId") Long organizationId);

    @Query("SELECT COUNT(c) FROM Content c WHERE c.brand.organization.id = :organizationId AND c.stage = com.creatorops.content.entity.ContentStage.PUBLISHED")
    long countPublishedByOrganizationId(@Param("organizationId") Long organizationId);

    @Query("SELECT COUNT(c) FROM Content c WHERE c.brand.organization.id = :organizationId AND c.dueDate < :now AND c.stage != com.creatorops.content.entity.ContentStage.PUBLISHED AND c.stage != com.creatorops.content.entity.ContentStage.CANCELLED")
    long countOverdueByOrganizationId(@Param("organizationId") Long organizationId, @Param("now") java.time.OffsetDateTime now);

    @Query("SELECT c.stage, COUNT(c) FROM Content c WHERE c.brand.organization.id = :organizationId GROUP BY c.stage")
    java.util.List<Object[]> countByStage(@Param("organizationId") Long organizationId);

    @Query("SELECT c.type, COUNT(c) FROM Content c WHERE c.brand.organization.id = :organizationId GROUP BY c.type")
    java.util.List<Object[]> countByType(@Param("organizationId") Long organizationId);

    @Query("SELECT c.priority, COUNT(c) FROM Content c WHERE c.brand.organization.id = :organizationId GROUP BY c.priority")
    java.util.List<Object[]> countByPriority(@Param("organizationId") Long organizationId);

    @Query("SELECT COUNT(c) FROM Content c WHERE c.brand.organization.id = :organizationId AND c.stage = :stage AND c.publishDate >= :start AND c.publishDate <= :end")
    long countByStageAndPublishDateRange(
            @Param("organizationId") Long organizationId,
            @Param("stage") ContentStage stage,
            @Param("start") java.time.OffsetDateTime start,
            @Param("end") java.time.OffsetDateTime end
    );

    @Query("SELECT COUNT(c) FROM Content c WHERE c.brand.organization.id = :organizationId AND c.publishDate >= :start AND c.publishDate <= :end")
    long countByPublishDateRange(
            @Param("organizationId") Long organizationId,
            @Param("start") java.time.OffsetDateTime start,
            @Param("end") java.time.OffsetDateTime end
    );
}
