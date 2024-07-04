package com.example.demo.integration;

import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.Transactional;
import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ItemControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private Item item1;
    private Item item2;

    @BeforeEach
    public void setUp() throws Exception {
        itemRepository.deleteAll();
        userRepository.deleteAll();

        item1 = new Item();
        item1.setName("Item 1");
        item1.setPrice(BigDecimal.TEN);
        item1.setDescription("Description 1");
        item1 = itemRepository.saveAndFlush(item1);

        item2 = new Item();
        item2.setName("Item 2");
        item2.setPrice(BigDecimal.valueOf(20));
        item2.setDescription("Description 2");
        item2 = itemRepository.saveAndFlush(item2);

        // Create a user
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("testUser");
        createUserRequest.setPassword("password");
        createUserRequest.setConfirmPassword("password");
        mockMvc.perform(post("/api/user/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"testUser\", \"password\": \"password\", \"confirmPassword\": \"password\"}"))
                .andExpect(status().isOk());
    }

    private String loginAndGetToken() throws Exception {
        String loginRequest = "{\"username\": \"testUser\", \"password\": \"password\"}";
        String response = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn().getResponse().getHeader("Authorization");
        return response;
    }

    @Test
    public void getItems_success() throws Exception {
        String token = loginAndGetToken();
        mockMvc.perform(get("/api/item")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Item 1"))
                .andExpect(jsonPath("$[1].name").value("Item 2"));
    }

    @Test
    public void getItemById_success() throws Exception {
        String token = loginAndGetToken();
        mockMvc.perform(get("/api/item/{id}", item1.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Item 1"));
    }

    @Test
    public void getItemById_notFound() throws Exception {
        String token = loginAndGetToken();
        mockMvc.perform(get("/api/item/{id}", 999L)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getItemsByName_success() throws Exception {
        String token = loginAndGetToken();
        mockMvc.perform(get("/api/item/name/{name}", "Item 1")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Item 1"));
    }

    @Test
    public void getItemsByName_notFound() throws Exception {
        String token = loginAndGetToken();
        mockMvc.perform(get("/api/item/name/{name}", "Nonexistent Item")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
