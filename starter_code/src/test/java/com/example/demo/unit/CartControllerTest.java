package com.example.demo.unit;

import com.example.demo.controllers.CartController;
import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.ModifyCartRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CartControllerTest {

    @InjectMocks
    private CartController cartController;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ItemRepository itemRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void addToCart_userNotFound() {
        ModifyCartRequest request = new ModifyCartRequest();
        request.setUsername("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(null);

        ResponseEntity<Cart> response = cartController.addTocart(request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userRepository, times(1)).findByUsername("testUser");
        verify(itemRepository, times(0)).findById(anyLong());
        verify(cartRepository, times(0)).save(any(Cart.class));
    }

    @Test
    public void addToCart_itemNotFound() {
        ModifyCartRequest request = new ModifyCartRequest();
        request.setUsername("testUser");
        request.setItemId(1L);
        when(userRepository.findByUsername("testUser")).thenReturn(new User());
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<Cart> response = cartController.addTocart(request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userRepository, times(1)).findByUsername("testUser");
        verify(itemRepository, times(1)).findById(1L);
        verify(cartRepository, times(0)).save(any(Cart.class));
    }

    @Test
    public void addToCart_success() {
        ModifyCartRequest request = new ModifyCartRequest();
        request.setUsername("testUser");
        request.setItemId(1L);
        request.setQuantity(2);

        User user = new User();
        Cart cart = new Cart();
        user.setCart(cart);
        Item item = new Item();
        item.setId(1L);
        item.setPrice(BigDecimal.TEN);

        when(userRepository.findByUsername("testUser")).thenReturn(user);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        ResponseEntity<Cart> response = cartController.addTocart(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, cart.getItems().size());
        assertEquals(BigDecimal.valueOf(20), cart.getTotal());

        verify(userRepository, times(1)).findByUsername("testUser");
        verify(itemRepository, times(1)).findById(1L);
        verify(cartRepository, times(1)).save(cart);
    }

    @Test
    public void removeFromCart_userNotFound() {
        ModifyCartRequest request = new ModifyCartRequest();
        request.setUsername("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(null);

        ResponseEntity<Cart> response = cartController.removeFromcart(request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userRepository, times(1)).findByUsername("testUser");
        verify(itemRepository, times(0)).findById(anyLong());
        verify(cartRepository, times(0)).save(any(Cart.class));
    }

    @Test
    public void removeFromCart_itemNotFound() {
        ModifyCartRequest request = new ModifyCartRequest();
        request.setUsername("testUser");
        request.setItemId(1L);
        when(userRepository.findByUsername("testUser")).thenReturn(new User());
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<Cart> response = cartController.removeFromcart(request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userRepository, times(1)).findByUsername("testUser");
        verify(itemRepository, times(1)).findById(1L);
        verify(cartRepository, times(0)).save(any(Cart.class));
    }

    @Test
    public void removeFromCart_success() {
        ModifyCartRequest request = new ModifyCartRequest();
        request.setUsername("testUser");
        request.setItemId(1L);
        request.setQuantity(2);

        User user = new User();
        Cart cart = new Cart();
        user.setCart(cart);
        Item item = new Item();
        item.setId(1L);
        item.setPrice(BigDecimal.TEN);
        cart.addItem(item);
        cart.addItem(item);

        when(userRepository.findByUsername("testUser")).thenReturn(user);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        ResponseEntity<Cart> response = cartController.removeFromcart(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, cart.getItems().size());
        assertEquals(BigDecimal.ZERO, cart.getTotal());

        verify(userRepository, times(1)).findByUsername("testUser");
        verify(itemRepository, times(1)).findById(1L);
        verify(cartRepository, times(1)).save(cart);
    }
}
