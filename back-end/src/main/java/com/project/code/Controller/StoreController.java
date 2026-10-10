package com.project.code.Controller;

import com.project.code.Model.PlaceOrderRequestDTO;
import com.project.code.Model.Store;
import com.project.code.Repo.StoreRepository;
import com.project.code.Service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/store")
public class StoreController {
    private final OrderService orderService;
    private final StoreRepository storeRepository;

    public StoreController(OrderService orderService, StoreRepository storeRepository) {
        this.orderService = orderService;
        this.storeRepository = storeRepository;
    }
    // 1. Set Up the Controller Class:
//    - Annotate the class with `@RestController` to designate it as a REST controller for handling HTTP requests.
//    - Map the class to the `/store` URL using `@RequestMapping("/store")`.


    // 2. Autowired Dependencies:
//    - Inject the following dependencies via `@Autowired`:
//        - `StoreRepository` for managing store data.
//        - `OrderService` for handling order-related functionality.


    @PostMapping
    public Map<String,String> addStore(@Valid @RequestBody Store store) {
        storeRepository.save(store);
        return Map.of("message","Store added successfully");
    }
// 3. Define the `addStore` Method:
//    - Annotate with `@PostMapping` to create an endpoint for adding a new store.
//    - Accept `Store` object in the request body.
//    - Return a success message in a `Map<String, String>` with the key `message` containing store creation confirmation.


    @GetMapping("validate/{storeId}")
    public Boolean validateStore(@PathVariable("storeId") Long storeId) {
        return storeRepository.existsById(storeId);
    }
    // 4. Define the `validateStore` Method:
//    - Annotate with `@GetMapping("validate/{storeId}")` to check if a store exists by its `storeId`.
//    - Return a **boolean** indicating if the store exists.


    @PostMapping("/placeOrder")
    public ResponseEntity<Map<String,String>> placeOrder(@Valid @RequestBody PlaceOrderRequestDTO placeOrderRequestDTO) {
        try{
            orderService.saveOrder(placeOrderRequestDTO);
            return ResponseEntity.ok(Map.of("message","Place order added successfully"));

        }catch(ResponseStatusException e){
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("Error","Place order could not be saved"));
        }
    }
 // 5. Define the `placeOrder` Method:
//    - Annotate with `@PostMapping("/placeOrder")` to handle order placement.
//    - Accept `PlaceOrderRequestDTO` in the request body.
//    - Return a success message with key `message` if the order is successfully placed.
//    - Return an error message with key `Error` if there is an issue processing the order.


   
}
