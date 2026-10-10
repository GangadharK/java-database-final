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
public class StoreController {
    private final OrderService orderService;
    private final StoreRepository storeRepository;

    public StoreController(OrderService orderService, StoreRepository storeRepository) {
        this.orderService = orderService;
        this.storeRepository = storeRepository;
    }











    @PostMapping({"/store", "/store/"})
    public Map<String,String> addStore(@Valid @RequestBody Store store) {
        storeRepository.save(store);
        return Map.of("message","Store added successfully");
    }






    @GetMapping({
            "/store/validate/{storeId}",
            "/validate/store/{storeId}",
            "/store/validate/store/{storeId}"
    })
    public Boolean validateStore(@PathVariable("storeId") Long storeId) {
        return storeRepository.existsById(storeId);
    }





    @PostMapping("/store/placeOrder")
    public ResponseEntity<Map<String,String>> placeOrder(@Valid @RequestBody PlaceOrderRequestDTO placeOrderRequestDTO) {
        try{
            orderService.saveOrder(placeOrderRequestDTO);
            return ResponseEntity.ok(Map.of("message","Place order added successfully"));

        }catch(ResponseStatusException e){
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("Error","Place order could not be saved"));
        }
    }








}
