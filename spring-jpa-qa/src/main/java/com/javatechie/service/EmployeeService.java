package com.javatechie.service;

import com.javatechie.entity.Employee;
import com.javatechie.repository.EmployeeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Slf4j
public class EmployeeService {

    @Autowired
    private EmployeeRepository repository;

    public Employee saveEmployee(Employee employee) {
        return repository.save(employee);
    }

    public Employee getEmployee(int id) {
        Optional<Employee> employee = repository.findById(id);
        return employee.orElseThrow(() -> new NoSuchElementException("Employee with ID " + id + " not found"));
    }

    public List<Employee> getEmployees() {
        return repository.findAll();
    }

    public Employee updateEmployee(int id, Employee updatedEmployee) {
        Employee existingEmployee = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Employee with ID " + id + " not found"));
        existingEmployee.setName(updatedEmployee.getName());
        existingEmployee.setDeptName(updatedEmployee.getDeptName());
        existingEmployee.setSalary(updatedEmployee.getSalary());
        existingEmployee.setEmailId(updatedEmployee.getEmailId());
        existingEmployee.setAge(updatedEmployee.getAge());
        return repository.save(existingEmployee);
    }

    public void deleteEmployee(int id) {
        repository.deleteById(id);
    }




    public List<Employee> filterBySalary(double salary) {
        log.info("Results with JPQL {} ",repository.findEmployeeWithJPQL(salary));
        log.info("Results with NATIVE SQL {} ",repository.findEmployeeWithSQL(salary));
        return repository.findBySalaryGreaterThan(salary);
    }

    public List<Employee> findEmployeesByAgeRange(int minAge, int maxAge) {
        return  repository.findByAgeBetween(minAge, maxAge);
    }

    public Optional<Double> getAverageSalary() {
        return repository.avgSalary();
    }

    public List<Employee> findEmployeesWithSorting(String field){
        return  repository.findAll(Sort.by(Sort.Direction.ASC,field));
    }

    public Page<Employee> findEmployeesWithPagination(int pageNumber,int pageSize){
        return  repository.findAll(PageRequest.of(pageNumber, pageSize));
        //1000
        //pageSize : 1-100, 101-200 , 201-300
        //offset : 0,1,2,3,4,
        //20
        // pageSize=5 / 10 (0,1)
        //pageNumber : 0 (0-5) , 1 (6-10) , 2 (11-15) , 3 (16-20)
    }


    public Page<Employee> findEmployeesWithPaginationAndSorting(int pageNumber, int pageSize, String field){
        return repository.findAll(PageRequest.of(pageNumber, pageSize).withSort(Sort.by(field)));
    }

}
