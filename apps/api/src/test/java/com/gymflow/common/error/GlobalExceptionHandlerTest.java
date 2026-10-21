package com.gymflow.common.error;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest
@ContextConfiguration(classes = {GlobalExceptionHandler.class, GlobalExceptionHandlerTest.ProbeController.class})
class GlobalExceptionHandlerTest {

    @Autowired
    MockMvc mvc;

    @Test
    void validationErrorsListTheInvalidFields() throws Exception {
        mvc.perform(post("/probe/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
            .andExpect(jsonPath("$.errors[0].field").value("name"));
    }

    @Test
    void missingResourcesReturnNotFound() throws Exception {
        mvc.perform(get("/probe/missing"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("NOT_FOUND"))
            .andExpect(jsonPath("$.detail").value("Member not found"));
    }

    @Test
    void unknownRoutesReturnNotFound() throws Exception {
        mvc.perform(get("/probe/does-not-exist"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void malformedJsonIsABadRequest() throws Exception {
        mvc.perform(post("/probe/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{not json"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void unexpectedErrorsDoNotLeakInternalDetails() throws Exception {
        mvc.perform(get("/probe/boom"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
            .andExpect(content().string(not(containsString("tenant-db.internal"))));
    }

    @RestController
    static class ProbeController {

        record ProbeRequest(@NotBlank String name) {}

        @PostMapping("/probe/validate")
        String validate(@Valid @RequestBody ProbeRequest request) {
            return request.name();
        }

        @GetMapping("/probe/missing")
        String missing() {
            throw new ResourceNotFoundException("Member not found");
        }

        @GetMapping("/probe/boom")
        String boom() {
            throw new IllegalStateException("connection refused: tenant-db.internal:5432");
        }
    }
}
