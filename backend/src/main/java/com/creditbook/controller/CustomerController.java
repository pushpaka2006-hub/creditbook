package com.creditbook.controller;

import com.creditbook.dto.CustomerRequest;
import com.creditbook.dto.CustomerResponse;
import com.creditbook.entity.CustomerStatus;
import com.creditbook.security.UserPrincipal;
import com.creditbook.service.CustomerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> listCustomers(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(required = false) CustomerStatus status,
            @RequestParam(required = false) String search) {
        List<CustomerResponse> customers = customerService.listCustomers(currentUser.getId(), status, search);
        return ResponseEntity.ok(customers);
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CustomerRequest request,
            HttpServletRequest httpRequest) {
        CustomerResponse created = customerService.createCustomer(currentUser.getId(), request, httpRequest.getRemoteAddr());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomer(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id) {
        CustomerResponse customer = customerService.getCustomer(currentUser.getId(), id);
        return ResponseEntity.ok(customer);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request,
            HttpServletRequest httpRequest) {
        CustomerResponse updated = customerService.updateCustomer(currentUser.getId(), id, request, httpRequest.getRemoteAddr());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteCustomer(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        customerService.deleteCustomer(currentUser.getId(), id, httpRequest.getRemoteAddr());
        Map<String, String> response = new HashMap<>();
        response.put("message", "Customer successfully deleted");
        return ResponseEntity.ok(response);
    }
}
