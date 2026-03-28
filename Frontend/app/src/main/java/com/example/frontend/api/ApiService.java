package com.example.frontend.api;

import com.example.frontend.model.Employee;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {
    // Récup des employés
    @GET("api/employees")
    Call<List<Employee>> getEmployees();

    // Ajouter Employé
    @POST("api/employees")
    Call<Employee> createEmployee(@Body Employee employee);

    // Supprimer
    @DELETE("api/employees/{numEmp}")
    Call<Void> deleteEmployee(@Path("numEmp") String numEmp);

    // Modifier (AJOUTER CETTE MÉTHODE)
    @PUT("api/employees/{numEmp}")
    Call<Employee> updateEmployee(@Path("numEmp") String numEmp, @Body Employee employee);
}