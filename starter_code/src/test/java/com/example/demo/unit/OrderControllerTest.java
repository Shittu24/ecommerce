package com.example.demo.unit;

import com.example.demo.controllers.OrderController;
import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.persistence.repositories.OrderRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class OrderControllerTest {

    @InjectMocks
    private OrderController orderController;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void submit_order_success() {
        User user = new User();
        Cart cart = new Cart();
        List<Item> items = new ArrayList<>();
        Item item = new Item();
        item.setPrice(BigDecimal.valueOf(100));
        items.add(item);
        cart.setItems(items);
        cart.setTotal(BigDecimal.valueOf(100));
        user.setCart(cart);
        when(userRepository.findByUsername("testUser")).thenReturn(user);

        ResponseEntity<UserOrder> response = orderController.submit("testUser");

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(orderRepository, times(1)).save(any(UserOrder.class));
    }

    @Test
    public void submit_order_userNotFound() {
        when(userRepository.findByUsername("testUser")).thenReturn(null);

        ResponseEntity<UserOrder> response = orderController.submit("testUser");

        assertEquals(404, response.getStatusCodeValue());
        verify(orderRepository, times(0)).save(any(UserOrder.class));
    }

    @Test
    public void getOrdersForUser_success() {
        User user = new User();
        List<UserOrder> orders = new ArrayList<>();
        orders.add(new UserOrder());
        when(userRepository.findByUsername("testUser")).thenReturn(user);
        when(orderRepository.findByUser(user)).thenReturn(orders);

        ResponseEntity<List<UserOrder>> response = orderController.getOrdersForUser("testUser");

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        verify(orderRepository, times(1)).findByUser(user);
    }

    @Test
    public void getOrdersForUser_userNotFound() {
        when(userRepository.findByUsername("testUser")).thenReturn(null);

        ResponseEntity<List<UserOrder>> response = orderController.getOrdersForUser("testUser");

        assertEquals(404, response.getStatusCodeValue());
        verify(orderRepository, times(0)).findByUser(any(User.class));
    }
}
