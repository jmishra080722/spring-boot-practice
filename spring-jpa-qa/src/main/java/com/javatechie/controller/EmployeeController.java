package com.javatechie.controller;

import com.javatechie.entity.Employee;
import com.javatechie.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping
    public Employee saveEmployee(@RequestBody Employee employee) {
        return employeeService.saveEmployee(employee);
    }

    @GetMapping("/{id}")
    public Employee getEmployee(@PathVariable int id) {
        return employeeService.getEmployee(id);
    }

    @GetMapping
    public List<Employee> getEmployees() {
        return employeeService.getEmployees();
    }

    @PutMapping("/{id}")
    public Employee updateEmployee(@PathVariable int id, @RequestBody Employee updatedEmployee) {
        return employeeService.updateEmployee(id, updatedEmployee);
    }

    @DeleteMapping("/{id}")
    public void deleteEmployee(@PathVariable int id) {
        employeeService.deleteEmployee(id);
    }

    @GetMapping("/filterBySalary")
    public List<Employee> filterBySalary(@RequestParam double salary) {
        return employeeService.filterBySalary(salary);
    }

    @GetMapping("/filterByAgeRange/{minAge}/{maxAge}")
    public List<Employee> filterEmployeesByAgeRange(@PathVariable int minAge, @PathVariable int maxAge) {
        return employeeService.findEmployeesByAgeRange(minAge, maxAge);
    }

    @GetMapping("/averageSalary")
    public double averageSalary() {
        Optional<Double> averageSalary = employeeService.getAverageSalary();
        return averageSalary.orElse(0.0); // Return 0.0 if average salary is not present
    }


    @GetMapping("/sort")
    public List<Employee> findEmployeesWithSorting(@RequestParam String field) {
        return employeeService.findEmployeesWithSorting(field);
    }

    @GetMapping("/page")
    public Page<Employee> findEmployeesWithPagination(@RequestParam int offset, @RequestParam int pageSize) {
        return employeeService.findEmployeesWithPagination(offset, pageSize);
    }

    // Controller method for finding employees with pagination and sorting
    @GetMapping("/pageAndSort")
    public Page<Employee> findEmployeesWithPaginationAndSorting(@RequestParam int offset, @RequestParam int pageSize, @RequestParam String field) {
        return employeeService.findEmployeesWithPaginationAndSorting(offset, pageSize, field);
    }
}
