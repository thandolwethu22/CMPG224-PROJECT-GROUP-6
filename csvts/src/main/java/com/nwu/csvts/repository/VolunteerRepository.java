package com.nwu.csvts.repository;

import com.nwu.csvts.model.Volunteer;
import com.nwu.csvts.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VolunteerRepository extends JpaRepository<Volunteer, Long> {
    
    Optional<Volunteer> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT v FROM Volunteer v WHERE v.user.userId = :userId")
    Optional<Volunteer> findByUserId(@Param("userId") Long userId);
    
    // Find by User entity
    default Optional<Volunteer> findByUser(User user) {
        return findByUserId(user.getUserId());
    }
    
    // Search methods
    @Query("SELECT v FROM Volunteer v WHERE LOWER(v.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(v.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Volunteer> findByNameContainingIgnoreCase(@Param("name") String name);
    
    List<Volunteer> findBySkillsContainingIgnoreCase(String skill);
    
    List<Volunteer> findByAvailabilityContainingIgnoreCase(String availability);
}