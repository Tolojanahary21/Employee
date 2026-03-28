package com.example.Backend.repository;

import com.example.Backend.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, String> {
    //String cause de l'Id string
}
