package com.socialcoisa.rede.presence;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.socialcoisa.rede.shared.web.TestAuthHelper;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class PresenceControllerTests {
	@Autowired private MockMvc mockMvc;
	@Autowired private ObjectMapper objectMapper;

	@Test
	void persistsSelectionAndHidesInvisibleStatusFromOtherUsers() throws Exception {
		var session = new TestAuthHelper(mockMvc, objectMapper).authenticate("presence_" + System.nanoTime());
		String username = objectMapper.readTree(mockMvc.perform(get("/me")
			.header("X-Session-Token", session.token())).andReturn().getResponse().getContentAsString())
			.get("username").asText();

		mockMvc.perform(get("/me/presence").header("X-Session-Token", session.token()))
			.andExpect(status().isOk()).andExpect(jsonPath("$.status").value("AVAILABLE"));
		mockMvc.perform(put("/me/presence").header("X-Session-Token", session.token())
			.contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"INVISIBLE\"}"))
			.andExpect(status().isOk()).andExpect(jsonPath("$.status").value("INVISIBLE"));
		mockMvc.perform(post("/me/presence/heartbeat").header("X-Session-Token", session.token()))
			.andExpect(status().isOk()).andExpect(jsonPath("$.status").value("INVISIBLE"));
		mockMvc.perform(get("/users/" + username + "/presence"))
			.andExpect(status().isOk()).andExpect(jsonPath("$.status").value("OFFLINE"));
	}

	@Test
	void rejectsMissingSessionAndInvalidStatus() throws Exception {
		var session = new TestAuthHelper(mockMvc, objectMapper).authenticate("presence_invalid_" + System.nanoTime());
		mockMvc.perform(put("/me/presence").contentType(MediaType.APPLICATION_JSON)
			.content("{\"status\":\"AWAY\"}")).andExpect(status().isUnauthorized());
		mockMvc.perform(put("/me/presence").header("X-Session-Token", session.token())
			.contentType(MediaType.APPLICATION_JSON).content("{\"status\":null}"))
			.andExpect(status().isBadRequest());
	}
}
