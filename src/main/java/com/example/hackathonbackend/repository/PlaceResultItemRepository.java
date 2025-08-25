// PlaceResultItemRepository.java
package com.example.hackathonbackend.repository;

import com.example.hackathonbackend.model.PlaceResultItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlaceResultItemRepository extends JpaRepository<PlaceResultItem, Long> {

    // MySQL: RAND(), PostgreSQL: RANDOM()
    @Query(value = """
        SELECT *
        FROM place_result_items
        WHERE result_id = :resultId
          AND is_main = false
        ORDER BY RAND()
        """, nativeQuery = true)
    List<PlaceResultItem> pickRandomExtrasByResultId(
            @Param("resultId") Long resultId,
            Pageable pageable
    );
}