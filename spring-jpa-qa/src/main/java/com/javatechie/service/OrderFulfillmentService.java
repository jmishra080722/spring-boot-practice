package com.javatechie.service;

import com.javatechie.common.CustomerOrderDTO;
import com.javatechie.common.OrderRequest;
import com.javatechie.entity.Customer;
import com.javatechie.repository.CustomerRepository;
import com.javatechie.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderFulfillmentService {

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private OrderRepository orderRepository;

    public Customer createOrder(OrderRequest orderRequest) {
        Customer customer= (Customer) orderRequest.getCustomer();
        customer.getOrders().forEach(c->c.setCustomer(customer));
        return customerRepository.save(customer);
    }


    public List<Object[]> findCustomerOrderCount() {
        return customerRepository.findCustomerOrderCount();
    }

    public List<CustomerOrderDTO> findCustomerOrderCountResponse() {
        return customerRepository.findCustomerOrderCountResponse();
    }
}
