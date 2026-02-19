package com.javatechie.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
//@Table(name = "EMPLOYEE_TBL")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "EMPNAME")
    private String name;
    private String deptName;
    private double salary;
    private String emailId;
    private int age;

    public Employee(String name, String deptName, double salary, String emailId, int age) {
        this.name = name;
        this.deptName = deptName;
        this.salary = salary;
        this.emailId = emailId;
        this.age = age;
    }
}
