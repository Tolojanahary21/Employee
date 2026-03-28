package com.example.Backend.controller;

import com.example.Backend.model.Employee;
import com.example.Backend.service.EmployeeService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "*")  // Important pour Android
public class EmployeeController {
    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    // 1. Récupérer tous les employés (GET)
    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {
        List<Employee> employees = service.getAllEmployees();
        return new ResponseEntity<>(employees, HttpStatus.OK);
    }

    // 2. Récupérer un employé par son numéro (GET)
    @GetMapping("/{numEmp}")
    public ResponseEntity<Employee> getEmployeeByNum(@PathVariable String numEmp) {
        Employee employee = service.getEmployeeByNum(numEmp);
        if (employee != null) {
            return new ResponseEntity<>(employee, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // 3. Ajouter un employé (POST)
    @PostMapping
    public ResponseEntity<Employee> addEmployee(@RequestBody Employee employee) {
        Employee newEmployee = service.addEmployee(employee);
        return new ResponseEntity<>(newEmployee, HttpStatus.CREATED);
    }

    // 4. Modifier un employé (PUT)
    @PutMapping("/{numEmp}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable String numEmp, @RequestBody Employee employee) {
        Employee updatedEmployee = service.updateEmployee(numEmp, employee);
        if (updatedEmployee != null) {
            return new ResponseEntity<>(updatedEmployee, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // 5. Supprimer un employé (DELETE)
    @DeleteMapping("/{numEmp}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable String numEmp) {
        boolean deleted = service.deleteEmployee(numEmp);
        if (deleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}