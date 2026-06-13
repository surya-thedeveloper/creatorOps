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
}
