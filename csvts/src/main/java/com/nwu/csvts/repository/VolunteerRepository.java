package com.nwu.csvts.repository;

import com.nwu.csvts.model.Volunteer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VolunteerRepository extends JpaRepository<Volunteer, Long> {
    Optional<Volunteer> findByEmail(String email);
    List<Volunteer> findBySkillsContainingIgnoreCase(String skill);
    List<Volunteer> findByAvailabilityContainingIgnoreCase(String availability);
    
    @Query("SELECT v FROM Volunteer v WHERE LOWER(v.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(v.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Volunteer> findByNameContainingIgnoreCase(@Param("name") String name);
    
    boolean existsByEmail(String email);
}