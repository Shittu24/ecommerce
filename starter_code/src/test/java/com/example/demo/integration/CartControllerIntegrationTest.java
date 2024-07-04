package com.example.demo.integration;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.example.demo.model.persistence.repositories.OrderRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.ModifyCartRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CartControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private User user;
    private Item item;

    @BeforeTransaction
    public void setupBeforeTransaction() {
        orderRepository.deleteAll();
        cartRepository.deleteAll();
        userRepository.deleteAll();
        itemRepository.deleteAll();
    }

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setUsername("testUser");
        user.setPassword(bCryptPasswordEncoder.encode("password"));

        // Save the User entity first to generate the ID
        user = userRepository.saveAndFlush(user);

        // Create and associate the Cart entity with the saved User
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setItems(new ArrayList<>());
        cart = cartRepository.saveAndFlush(cart);

        // Associate the saved Cart with the User and update the User entity
        user.setCart(cart);
        user = userRepository.saveAndFlush(user);

        item = new Item();
        item.setName("testItem");
        item.setPrice(BigDecimal.TEN);
        item.setDescription("A test item");
        item = itemRepository.saveAndFlush(item);
    }

    @AfterEach
    @Rollback
    public void tearDown() {
        // Cleanup in case the transaction is not rolled back properly
        orderRepository.deleteAll();
        cartRepository.deleteAll();
        userRepository.deleteAll();
        itemRepository.deleteAll();
        TestTransaction.end();
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
    public void addToCart_success() throws Exception {
        String token = loginAndGetToken();
        ModifyCartRequest request = new ModifyCartRequest();
        request.setUsername("testUser");
        request.setItemId(item.getId());
        request.setQuantity(2);

        mockMvc.perform(post("/api/cart/addToCart")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"testUser\", \"itemId\": " + item.getId() + ", \"quantity\": 2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.total").value(20));
    }

    @Test
    public void removeFromCart_success() throws Exception {
        String token = loginAndGetToken();
        Cart cart = user.getCart();
        cart.addItem(item);
        cart.addItem(item);
        cart = cartRepository.saveAndFlush(cart);  // Save and flush to ensure it is in the persistence context

        System.out.println("Cart items before removal: " + cart.getItems().size());

        mockMvc.perform(post("/api/cart/removeFromCart")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"testUser\", \"itemId\": " + item.getId() + ", \"quantity\": 2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0))
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    public void addToCart_userNotFound() throws Exception {
        String token = loginAndGetToken();
        mockMvc.perform(post("/api/cart/addToCart")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"nonExistentUser\", \"itemId\": " + item.getId() + ", \"quantity\": 1}"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void addToCart_itemNotFound() throws Exception {
        String token = loginAndGetToken();
        mockMvc.perform(post("/api/cart/addToCart")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"testUser\", \"itemId\": 999, \"quantity\": 1}"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void removeFromCart_userNotFound() throws Exception {
        String token = loginAndGetToken();
        mockMvc.perform(post("/api/cart/removeFromCart")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"nonExistentUser\", \"itemId\": " + item.getId() + ", \"quantity\": 1}"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void removeFromCart_itemNotFound() throws Exception {
        String token = loginAndGetToken();
        mockMvc.perform(post("/api/cart/removeFromCart")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"testUser\", \"itemId\": 999, \"quantity\": 1}"))
                .andExpect(status().isNotFound());
    }
}
