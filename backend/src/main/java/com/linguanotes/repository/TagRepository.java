package com.linguanotes.repository;

import com.linguanotes.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByNameIgnoreCase(String name);

    @Query("select distinct t.name from Note n join n.tags t where n.owner.id = :ownerId order by t.name")
    List<String> findNamesByNoteOwnerId(@Param("ownerId") Long ownerId);
}
