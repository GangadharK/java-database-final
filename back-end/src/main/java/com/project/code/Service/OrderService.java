package com.project.code.Service;

import com.project.code.Model.*;
import com.project.code.Repo.*;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final StoreRepository storeRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderDetailsRepository orderDetailsRepository;

    public OrderService(ProductRepository productRepository, CustomerRepository customerRepository,
                        StoreRepository storeRepository, InventoryRepository inventoryRepository,
                        OrderItemRepository orderItemRepository, OrderDetailsRepository orderDetailsRepository) {
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.storeRepository = storeRepository;
        this.inventoryRepository = inventoryRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderDetailsRepository = orderDetailsRepository;
    }
    @Transactional
    public void saveOrder(PlaceOrderRequestDTO placeOrderRequestDTO) throws ResponseStatusException {
        if (placeOrderRequestDTO == null || placeOrderRequestDTO.getStoreId() == null
                || placeOrderRequestDTO.getPurchaseProduct() == null
                || placeOrderRequestDTO.getPurchaseProduct().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order must include a store and products");
        }
        List<PurchaseProductDTO> purchaseProductDTOs = placeOrderRequestDTO.getPurchaseProduct();
        Store store = validateStoreId(placeOrderRequestDTO.getStoreId());
        Map<Long, Product> products = new HashMap<>();
        Map<Long, Inventory> inventories = new HashMap<>();
        Map<Long, Integer> requestedQuantities = new HashMap<>();
        BigDecimal total = BigDecimal.ZERO;

        for (PurchaseProductDTO purchaseProductDTO : purchaseProductDTOs) {
            if (purchaseProductDTO == null || purchaseProductDTO.getId() == null
                    || purchaseProductDTO.getQuantity() == null || purchaseProductDTO.getQuantity() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Each order item needs a product and positive quantity");
            }
            Long productId = purchaseProductDTO.getId();
            Product product = products.computeIfAbsent(productId, id -> productRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + id)));
            Inventory inventory = inventories.computeIfAbsent(productId, id ->
                    inventoryRepository.findByProduct_IdAndStore_Id(id, store.getId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                    "Product is not stocked at this store: " + id)));
            int requested = requestedQuantities.merge(productId, purchaseProductDTO.getQuantity(), Integer::sum);
            if (inventory.getStockLevel() < requested) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient stock for product: " + productId);
            }
            if (product.getPrice() == null) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Product price is not configured: " + productId);
            }
            total = total.add(BigDecimal.valueOf(product.getPrice())
                    .multiply(BigDecimal.valueOf(purchaseProductDTO.getQuantity())));
        }

        Customer customer = getCustomer(placeOrderRequestDTO);
        OrderDetails orderDetails = orderDetailsRepository.save(
                saveOrderDetails(customer, store, total.doubleValue()));
        List<OrderItem> orderItems = new ArrayList<>();
        for (PurchaseProductDTO purchaseProductDTO : purchaseProductDTOs) {
            Long productId = purchaseProductDTO.getId();
            Product product = products.get(productId);
            OrderItem orderItem = new OrderItem(orderDetails, product, purchaseProductDTO.getQuantity(),
                    product.getPrice());
            orderItems.add(orderItemRepository.save(orderItem));
            Inventory inventory = inventories.get(productId);
            inventory.setStockLevel(inventory.getStockLevel() - purchaseProductDTO.getQuantity());
            inventoryRepository.save(inventory);
        }
        orderDetails.setOrderItems(orderItems);
        orderDetailsRepository.save(orderDetails);

    }




    public Customer getCustomer(PlaceOrderRequestDTO placeOrderRequestDTO){
        if (placeOrderRequestDTO.getCustomerName() == null || placeOrderRequestDTO.getCustomerName().isBlank()
                || placeOrderRequestDTO.getCustomerEmail() == null || placeOrderRequestDTO.getCustomerEmail().isBlank()
                || placeOrderRequestDTO.getCustomerPhone() == null || placeOrderRequestDTO.getCustomerPhone().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer name, email, and phone are required");
        }
        Customer existingCustomer = customerRepository.findByEmail(placeOrderRequestDTO.getCustomerEmail());
        if (existingCustomer != null) {
            return existingCustomer;
        }
        Customer customer = new Customer();
        customer.setEmail(placeOrderRequestDTO.getCustomerEmail());
        customer.setName(placeOrderRequestDTO.getCustomerName());
        customer.setPhone(placeOrderRequestDTO.getCustomerPhone());
        return customerRepository.save(customer);
    }




    public Store validateStoreId(long id) throws ResponseStatusException {
        return storeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
    }




    public OrderDetails saveOrderDetails(Customer customer, Store store, Double totalPrice) {
        return new OrderDetails(customer, store, totalPrice, LocalDateTime.now());
    }




    public OrderItem saveOrderItem(PurchaseProductDTO purchaseProductDTO,OrderDetails orderDetails,Long storeId) {
        Product product = productRepository.findById(purchaseProductDTO.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        return new OrderItem(orderDetails, product, purchaseProductDTO.getQuantity(), product.getPrice());
    }





}
