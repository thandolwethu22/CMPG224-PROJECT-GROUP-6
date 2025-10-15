package com.nwu.csvts;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithAnonymousUser;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CsvtsApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    // Test Case 1: Volunteer Registration (Public endpoint - should work without auth)
    @Test
    @WithAnonymousUser
    void testCase_F1_TC1_1_VolunteerProfileCreation() throws Exception {
        String volunteerJson = "{\"username\":\"testuser\",\"password\":\"TestPass123\",\"email\":\"test@example.com\",\"phone\":\"0123456789\",\"skills\":[\"Teaching\"],\"availability\":\"Weekends\"}";
        
        mockMvc.perform(post("/register")
                .contentType("application/json")
                .content(volunteerJson))
                .andExpect(status().isOk());
    }

    // Test Case 2: Task Creation (Admin only - should work with ADMIN role)
    @Test
    @WithMockUser(roles = "ADMIN")
    void testCase_F2_TC2_1_TaskCreationAndAssignment() throws Exception {
        String taskJson = "{\"title\":\"Community Cleanup\",\"description\":\"Clean up local park\",\"deadline\":\"2025-12-31\"}";
        
        mockMvc.perform(post("/admin/tasks")
                .contentType("application/json")
                .content(taskJson))
                .andExpect(status().isOk());
    }

    // Test Case 3: Search Volunteers by Skills (Admin only)
    @Test
    @WithMockUser(roles = "ADMIN")
    void testCase_F3_TC3_1_SearchVolunteersBySkills() throws Exception {
        mockMvc.perform(get("/admin/volunteers/search")
                .param("skill", "First Aid"))
                .andExpect(status().isOk());
    }

    // Test Case 4: Volunteer Hour Submission (Volunteer only)
    @Test
    @WithMockUser(roles = "VOLUNTEER")
    void testCase_F4_TC4_1_SubmitVolunteerHours() throws Exception {
        String hoursJson = "{\"taskId\":1,\"hours\":5,\"date\":\"2025-10-15\",\"description\":\"Completed park cleanup\"}";
        
        mockMvc.perform(post("/volunteer/hours")
                .contentType("application/json")
                .content(hoursJson))
                .andExpect(status().isOk());
    }

    // Test Case 5: Access Control - Volunteer trying to access admin endpoint (should be 403)
    @Test
    @WithMockUser(roles = "VOLUNTEER")
    void testCase_F5_TC5_1_VolunteerAccessRestriction() throws Exception {
        mockMvc.perform(get("/admin/volunteers"))
                .andExpect(status().isForbidden()); // Should be 403 Forbidden
    }

    // Additional security test: Unauthenticated user trying to access protected endpoint
    @Test
    @WithAnonymousUser
    void testUnauthenticatedAccessToProtectedEndpoint() throws Exception {
        mockMvc.perform(get("/admin/tasks"))
                .andExpect(status().is3xxRedirection()); // Should redirect to login page
    }

    // Additional test: Admin accessing volunteer endpoint (should work)
    @Test
    @WithMockUser(roles = "ADMIN")
    void testAdminAccessingVolunteerEndpoint() throws Exception {
        mockMvc.perform(get("/volunteer/hours"))
                .andExpect(status().isOk()); // Admin should be able to access volunteer endpoints
    }
}