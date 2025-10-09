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
    
    // Find volunteer by user
    Optional<Volunteer> findByUser(User user);
    
    // Find volunteer by username
    Optional<Volunteer> findByUserUsername(String username);
    
    // Search volunteers by name or email
    @Query("SELECT v FROM Volunteer v WHERE " +
           "LOWER(v.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(v.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(v.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Volunteer> searchVolunteers(@Param("searchTerm") String searchTerm);
    
    // Alternative search method using method naming
    List<Volunteer> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
        String firstName, String lastName, String email);
    
    // Find volunteers by skill
    List<Volunteer> findBySkillsContainingIgnoreCase(String skill);
    
    // Count active volunteers
    Long countByUserActiveTrue();
    
    // Find all active volunteers
    List<Volunteer> findByUserActiveTrue();
    
    // Find all inactive volunteers
    List<Volunteer> findByUserActiveFalse();
    
    // ADD THESE MISSING METHODS:
    boolean existsByEmail(String email);
    
    @Query("SELECT COUNT(v) FROM Volunteer v WHERE v.user.active = true")
    Long countActiveVolunteers();
}