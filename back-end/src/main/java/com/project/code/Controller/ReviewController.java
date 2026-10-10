package com.project.code.Controller;

import com.project.code.Model.Customer;
import com.project.code.Model.Review;
import com.project.code.Model.ReviewDTO;
import com.project.code.Repo.CustomerRepository;
import com.project.code.Repo.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    @Autowired
    ReviewRepository reviewRepository;
    @Autowired
    CustomerRepository customerRepository;
    @GetMapping
    public Map<String, Object> getAllReviews() {
        return Map.of("reviews", reviewRepository.findAll());
    }
    @GetMapping("/{storeId}/{productId}")
    public Map<String,Object> getReviews(@PathVariable("storeId") Long storeId, @PathVariable("productId") Long productId){
        List<Review> reviews=reviewRepository.findByStoreIdAndProductId( storeId,productId);
        List<ReviewDTO> reviewDTOs=new ArrayList<>();
        for(Review review:reviews){
            String customerName = customerRepository.findById(review.getCustomerId())
                    .map(Customer::getName)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Customer not found for review"));
            ReviewDTO reviewDTO=new ReviewDTO(customerName,
                    review.getComment(),
                    review.getRating());
            reviewDTOs.add(reviewDTO);
        }
        return Map.of("reviews",reviewDTOs);
    }
}
