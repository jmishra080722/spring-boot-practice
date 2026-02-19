package com.javatechie;

import com.javatechie.entity.Employee;
import com.javatechie.repository.EmployeeRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class EmployeeServiceApplication {

    @Autowired
    private EmployeeRepository employeeRepository;

    //@PostConstruct
    public void initDBRecords() {
//        List<Employee> employees = new ArrayList<>();
//        employees.add(new Employee("John Doe", "HR", 50000.0, "john@example.com", 30));
//        employees.add(new Employee("Jane Smith", "Finance", 60000.0, "jane@example.com", 35));
//        employees.add(new Employee("David Johnson", "IT", 55000.0, "david@example.com", 32));
//        employees.add(new Employee("Emily Brown", "Marketing", 52000.0, "emily@example.com", 28));
//        employees.add(new Employee("Michael Wilson", "Operations", 58000.0, "michael@example.com", 33));
//        employees.add(new Employee("Sarah Lee", "Sales", 53000.0, "sarah@example.com", 31));
//        employees.add(new Employee("Christopher Clark", "Research", 54000.0, "christopher@example.com", 29));
//        employees.add(new Employee("Amanda Martinez", "Development", 57000.0, "amanda@example.com", 34));
//        employees.add(new Employee("James Taylor", "Customer Service", 51000.0, "james@example.com", 27));
//        employees.add(new Employee("Laura Rodriguez", "Quality Assurance", 59000.0, "laura@example.com", 36));
//        employees.add(new Employee("John Smith", "HR", 51000.0, "john.smith@example.com", 31));
//        employees.add(new Employee("Emily Johnson", "Finance", 62000.0, "emily.johnson@example.com", 36));
//        employees.add(new Employee("David Brown", "IT", 56000.0, "david.brown@example.com", 33));
//        employees.add(new Employee("Jane Wilson", "Marketing", 53000.0, "jane.wilson@example.com", 29));
//        employees.add(new Employee("Michael Lee", "Operations", 59000.0, "michael.lee@example.com", 34));
//        employees.add(new Employee("Sarah Clark", "Sales", 54000.0, "sarah.clark@example.com", 32));
//        employees.add(new Employee("Christopher Martinez", "Research", 55000.0, "christopher.martinez@example.com", 30));
//        employees.add(new Employee("Amanda Taylor", "Development", 58000.0, "amanda.taylor@example.com", 35));
//        employees.add(new Employee("James Rodriguez", "Customer Service", 52000.0, "james.rodriguez@example.com", 28));
//        employees.add(new Employee("Laura Brown", "Quality Assurance", 60000.0, "laura.brown@example.com", 37));
//        employeeRepository.saveAll(employees);
    }



    public static void main(String[] args) {
        SpringApplication.run(EmployeeServiceApplication.class, args);
    }

}
