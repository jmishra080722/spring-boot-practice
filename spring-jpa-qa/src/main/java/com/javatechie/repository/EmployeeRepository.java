package com.javatechie.repository;

import com.javatechie.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee,Integer> {

//fetch employee by salary
    //1. method syntax
    //2. QUERY (JPQL)
    //3. SQL
    //select * from Employee where salary=?//SQL
    //select * from Employee e where e.salary=?//JPQL

    @Query(value = "SELECT e from Employee e WHERE e.salary > :salary")
    List<Employee> findEmployeeWithJPQL(@Param("salary") double salary);

    @Query(value = "SELECT * from Employee where salary >?1",nativeQuery = true)
    List<Employee> findEmployeeWithSQL( double salary);

    List<Employee> findBySalaryGreaterThan(double salary);


    List<Employee> findByAgeBetween(int min, int max);

//    findBySalaryAvg
//    avgSalary();

    @Query(value = "SELECT AVG(e.salary) FROM Employee e")
    Optional<Double> avgSalary();
    
}
