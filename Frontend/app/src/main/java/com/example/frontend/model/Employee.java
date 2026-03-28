package com.example.frontend.model;

public class Employee {
    private String numEmp;
    private String nom;
    private String salaire;

    //Constructeur vide
    public Employee(){
    }
    //Getters et seetters
    public  String getNumEmp(){
        return numEmp;
    }
    public void setNumEmp(String numEmp){
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
