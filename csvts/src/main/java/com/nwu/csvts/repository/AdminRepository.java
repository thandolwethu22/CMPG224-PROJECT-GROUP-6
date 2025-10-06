package com.nwu.csvts.repository;

import com.nwu.csvts.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    @Query("SELECT a FROM Admin a WHERE a.user.userId = :userId")
    Optional<Admin> findByUserId(@Param("userId") Long userId);
}