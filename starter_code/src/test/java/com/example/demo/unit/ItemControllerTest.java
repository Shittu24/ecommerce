package com.example.demo.unit;

import com.example.demo.controllers.ItemController;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.repositories.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ItemControllerTest {

    @InjectMocks
    private ItemController itemController;

    @Mock
    private ItemRepository itemRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getItems_success() {
        List<Item> items = new ArrayList<>();
        Item item1 = new Item();
        item1.setId(1L);
        item1.setName("Item 1");
        item1.setPrice(BigDecimal.TEN);
        item1.setDescription("Description 1");
        items.add(item1);

        Item item2 = new Item();
        item2.setId(2L);
        item2.setName("Item 2");
        item2.setPrice(BigDecimal.valueOf(20));
        item2.setDescription("Description 2");
        items.add(item2);

        when(itemRepository.findAll()).thenReturn(items);

        ResponseEntity<List<Item>> response = itemController.getItems();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
        verify(itemRepository, times(1)).findAll();
    }

    @Test
    public void getItemById_success() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Item 1");
        item.setPrice(BigDecimal.TEN);
        item.setDescription("Description 1");

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        ResponseEntity<Item> response = itemController.getItemById(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Item 1", response.getBody().getName());
        verify(itemRepository, times(1)).findById(1L);
    }

    @Test
    public void getItemById_notFound() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<Item> response = itemController.getItemById(1L);

        assertEquals(404, response.getStatusCodeValue());
        verify(itemRepository, times(1)).findById(1L);
    }

    @Test
    public void getItemsByName_success() {
        List<Item> items = new ArrayList<>();
        Item item = new Item();
        item.setId(1L);
        item.setName("Item 1");
        item.setPrice(BigDecimal.TEN);
        item.setDescription("Description 1");
        items.add(item);

        when(itemRepository.findByName("Item 1")).thenReturn(items);

        ResponseEntity<List<Item>> response = itemController.getItemsByName("Item 1");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        verify(itemRepository, times(1)).findByName("Item 1");
    }

    @Test
    public void getItemsByName_notFound() {
        when(itemRepository.findByName("Item 1")).thenReturn(new ArrayList<>());

        ResponseEntity<List<Item>> response = itemController.getItemsByName("Item 1");

        assertEquals(404, response.getStatusCodeValue());
        verify(itemRepository, times(1)).findByName("Item 1");
    }
}
