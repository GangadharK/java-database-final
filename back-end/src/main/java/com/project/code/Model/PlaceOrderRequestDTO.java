package com.project.code.Model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
public class PlaceOrderRequestDTO {
    @NotNull
    private Long storeId;
    @NotBlank
    private String customerName;
    @NotBlank
    @Email
    private String customerEmail;
    @NotBlank
    private String customerPhone;
    private String datetime;
    @NotEmpty
    private List<@Valid PurchaseProductDTO> purchaseProduct;
    private Double totalPrice;



    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public List<PurchaseProductDTO> getPurchaseProduct() {
        return purchaseProduct;
    }

    public void setPurchaseProduct(List<PurchaseProductDTO> purchaseProduct) {
        this.purchaseProduct = purchaseProduct;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }
}
