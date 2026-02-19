package com.javatechie.common;

import com.javatechie.entity.Customer;
import com.javatechie.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest<T> {

    private T customer;
}
