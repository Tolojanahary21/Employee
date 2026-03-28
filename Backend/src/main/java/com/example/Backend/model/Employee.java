package com.example.Backend.model;

import jakarta.persistence.*;
@Entity
@Table(name = "employee")

public class Employee {
    //Creer les attributs dans mon back
     @Id
     @Column(nullable = false)
     private String numEmp;

     @Column(nullable = false)
     private String nom;

    @Column(nullable = false)
    private String salaire;

    //GETTERS and SETTERS
    public String getNumEmp() {
        return numEmp;
    }
    public void setNumEmp(String numEmp) {
        this.numEmp = numEmp;
    }

    public String getNom(){
        return nom;
    }
    public void setNom(String nom){
        this.nom = nom;
    }
    public String getSalaire(){
        return salaire;
    }
    public void setSalaire(String salaire){
        this.salaire = salaire;
    }
}
