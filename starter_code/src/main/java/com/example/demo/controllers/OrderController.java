package com.example.demo.controllers;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.persistence.repositories.OrderRepository;
import com.example.demo.model.persistence.repositories.UserRepository;

@RestController
@RequestMapping("/api/order")
public class OrderController {

	private static final Logger log = LogManager.getLogger(OrderController.class);

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private OrderRepository orderRepository;

	@PostMapping("/submit/{username}")
	public ResponseEntity<UserOrder> submit(@PathVariable String username) {
		log.info("Submitting order for user: {}", username);
		User user = userRepository.findByUsername(username);
		if (user == null) {
			log.warn("Order submission failed: User with username {} not found", username);
			return ResponseEntity.notFound().build();
		}
		try {
			UserOrder order = UserOrder.createFromCart(user.getCart());
			orderRepository.save(order);
			log.info("Order for user {} submitted successfully", username);
			return ResponseEntity.ok(order);
		} catch (Exception e) {
			log.error("Exception occurred while submitting order for user {}: {}", username, e.getMessage());
			throw e;
		}
	}

	@GetMapping("/history/{username}")
	public ResponseEntity<List<UserOrder>> getOrdersForUser(@PathVariable String username) {
		log.info("Fetching order history for user: {}", username);
		User user = userRepository.findByUsername(username);
		if (user == null) {
			log.warn("Order history fetch failed: User with username {} not found", username);
			return ResponseEntity.notFound().build();
		}
		log.info("Order history for user {} fetched successfully", username);
		return ResponseEntity.ok(orderRepository.findByUser(user));
	}
}
