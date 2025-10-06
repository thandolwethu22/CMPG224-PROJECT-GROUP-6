package com.nwu.csvts.repository;

import com.nwu.csvts.model.Volunteer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface VolunteerRepository extends JpaRepository<Volunteer, Long> {
    Optional<Volunteer> findByEmail(String email);
    boolean existsByEmail(String email);
    
    @Query("SELECT v FROM Volunteer v WHERE v.user.userId = :userId")
    Optional<Volunteer> findByUserId(@Param("userId") Long userId);
}