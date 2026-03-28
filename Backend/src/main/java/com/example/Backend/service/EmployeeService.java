package com.example.Backend.service;

import com.example.Backend.model.Employee;
import com.example.Backend.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EmployeeService {
    private final EmployeeRepository repository;

    public EmployeeService(EmployeeRepository repository) {
        this.repository = repository;
    }

    public List<Employee> getAllEmployees() {
        return repository.findAll();
    }

    public Employee getEmployeeByNum(String numEmp) {
        return repository.findById(numEmp).orElse(null);
    }

    public Employee addEmployee(Employee employee) {
        return repository.save(employee);
    }

    public Employee updateEmployee(String numEmp, Employee employee) {
        if (repository.existsById(numEmp)) {
            employee.setNumEmp(numEmp);
            return repository.save(employee);
        }
        return null;
    }

    public boolean deleteEmployee(String numEmp) {
        if (repository.existsById(numEmp)) {
            repository.deleteById(numEmp);
            return true;
        }
        return false;
    }
}