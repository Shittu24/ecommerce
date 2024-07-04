package com.example.demo.integration;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.example.demo.model.persistence.repositories.OrderRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
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
import java.util.ArrayList;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private Item item;

    @BeforeEach
    public void setUp() throws Exception {
        // Clear all references to items in carts and orders before deleting items
        cartRepository.findAll().forEach(cart -> {
            cart.setItems(new ArrayList<>());
            cartRepository.saveAndFlush(cart);
        });

        orderRepository.findAll().forEach(order -> {
            order.setItems(new ArrayList<>());
            orderRepository.saveAndFlush(order);
        });

        orderRepository.deleteAll();
        cartRepository.deleteAll();
        userRepository.deleteAll();
        itemRepository.deleteAll();

        // Create an item
        item = new Item();
        item.setName("testItem");
        item.setPrice(BigDecimal.TEN);
        item.setDescription("A test item");
        item = itemRepository.saveAndFlush(item);
    }

    private void createUserAndCart(String username) {
        // Create a user
        User user = new User();
        user.setUsername(username);
        user.setPassword(bCryptPasswordEncoder.encode("password"));
        user = userRepository.saveAndFlush(user);

        // Create a cart
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setTotal(BigDecimal.ZERO); // Initialize total as zero
        cart.setItems(new ArrayList<>()); // Initialize with mutable empty items list
        user.setCart(cart);

        // Save the cart
        cartRepository.saveAndFlush(cart);
        userRepository.saveAndFlush(user);
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        String loginRequest = String.format("{\"username\": \"%s\", \"password\": \"%s\"}", username, password);
        return mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn().getResponse().getHeader("Authorization");
    }

    @Test
    public void submitOrder_success() throws Exception {
        String username = "testUser1";
        createUserAndCart(username);
        String token = loginAndGetToken(username, "password");

        // Add an item to the cart
        mockMvc.perform(post("/api/cart/addToCart")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"" + username + "\", \"itemId\": " + item.getId() + ", \"quantity\": 1}"))
                .andExpect(status().isOk());

        // Submit the order
        mockMvc.perform(post("/api/order/submit/" + username)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.total").value(10));
    }

    @Test
    public void submitOrder_userNotFound() throws Exception {
        String username = "testUser2";
        createUserAndCart(username);
        String token = loginAndGetToken(username, "password");

        mockMvc.perform(post("/api/order/submit/nonExistentUser")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getOrdersForUser_success() throws Exception {
        String username = "testUser3";
        createUserAndCart(username);
        String token = loginAndGetToken(username, "password");

        // Add an item to the cart
        mockMvc.perform(post("/api/cart/addToCart")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"" + username + "\", \"itemId\": " + item.getId() + ", \"quantity\": 1}"))
                .andExpect(status().isOk());

        // Submit an order first
        mockMvc.perform(post("/api/order/submit/" + username)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/order/history/" + username)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    public void getOrdersForUser_userNotFound() throws Exception {
        String username = "testUser4";
        createUserAndCart(username);
        String token = loginAndGetToken(username, "password");

        mockMvc.perform(get("/api/order/history/nonExistentUser")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
