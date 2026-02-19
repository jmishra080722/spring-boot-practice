package com.javatechie.repository;

import com.javatechie.common.CustomerOrderDTO;
import com.javatechie.entity.Customer;
import com.javatechie.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer,Integer> {

    //join query
    @Query(value = "SELECT c.name , COUNT(o) FROM Customer c JOIN c.orders o GROUP BY c.id")
    List<Object[]> findCustomerOrderCount();

    @Query(value = "SELECT  NEW com.javatechie.common.CustomerOrderDTO(c.name , COUNT(o), SUM(o.price)) FROM Customer c JOIN c.orders o GROUP BY c.id")
    List<CustomerOrderDTO> findCustomerOrderCountResponse();

}
