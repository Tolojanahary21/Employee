package com.example.frontend

import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.frontend.adapter.EmployeeAdapter
import com.example.frontend.api.ApiService
import com.example.frontend.api.RetrofitClient
import com.example.frontend.model.Employee
import kotlin.math.min

class HomeActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService
    private lateinit var listViewEmployes: ListView
    private lateinit var txtTotal: TextView
    private lateinit var txtMin: TextView
    private lateinit var txtMax: TextView
    private lateinit var employeeAdapter: EmployeeAdapter

    private lateinit var editNumEmp: EditText
    private lateinit var editNom: EditText
    private lateinit var editSalaire: EditText
    private lateinit var btnAjouter: Button
    private lateinit var backbutton: ImageView

    // Variables pour le diagramme camembert
    private lateinit var txtPourcentageFaible: TextView
    private lateinit var txtPourcentageMoyen: TextView
    private lateinit var txtPourcentageEleve: TextView
    private lateinit var pieChartView: View

    // Boutons de pagination
    private lateinit var btnPagePrecedente: Button
    private lateinit var btnPageSuivante: Button
    private lateinit var txtPageInfo: TextView

    private var allEmployees = mutableListOf<Employee>()  // Tous les employés
    private var displayedEmployees = mutableListOf<Employee>()  // Employés affichés (page courante)

    private var currentPage = 0
    private val pageSize = 3  // 3 éléments par page
    private var totalPages = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupListeners()
        apiService = RetrofitClient.getApiService()
        fetchEmployees()
    }

    private fun initViews() {
        listViewEmployes = findViewById(R.id.listViewEmployes)
        txtTotal = findViewById(R.id.txtTotal)
        txtMin = findViewById(R.id.txtMin)
        txtMax = findViewById(R.id.txtMax)

        editNumEmp = findViewById(R.id.editNumEmp)
        editNom = findViewById(R.id.editNom)
        editSalaire = findViewById(R.id.editSalaire)
        btnAjouter = findViewById(R.id.btnAjouter)
        backbutton = findViewById(R.id.backbutton)

        // Initialisation des boutons de pagination
        btnPagePrecedente = findViewById(R.id.btnPagePrecedente)
        btnPageSuivante = findViewById(R.id.btnPageSuivante)
        txtPageInfo = findViewById(R.id.txtPageInfo)

        // Initialisation des vues du diagramme camembert
        txtPourcentageFaible = findViewById(R.id.txtPourcentageFaible)
        txtPourcentageMoyen = findViewById(R.id.txtPourcentageMoyen)
        txtPourcentageEleve = findViewById(R.id.txtPourcentageEleve)
        pieChartView = findViewById(R.id.pieChart)

        // Adaptateur avec tous les listeners
        employeeAdapter = EmployeeAdapter(
            this,
            displayedEmployees,
            EmployeeAdapter.OnDeleteClickListener { numEmp ->
                supprimerEmploye(numEmp)
            },
            EmployeeAdapter.OnEditClickListener { employee ->
                ouvrirDialogModification(employee)
            },
            null,
            null
        )
        listViewEmployes.adapter = employeeAdapter
    }

    private fun setupListeners() {
        btnAjouter.setOnClickListener {
            ajouterEmploye()
        }

        backbutton.setOnClickListener {
            finish()
        }

        // Listeners pour la pagination
        btnPagePrecedente.setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                afficherPage(currentPage)
            }
        }

        btnPageSuivante.setOnClickListener {
            if (currentPage < totalPages - 1) {
                currentPage++
                afficherPage(currentPage)
            }
        }
    }

    private fun fetchEmployees() {
        val call = apiService.getEmployees()

        call.enqueue(object : retrofit2.Callback<List<Employee>> {
            override fun onResponse(
                call: retrofit2.Call<List<Employee>>,
                response: retrofit2.Response<List<Employee>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val employees = response.body()!!

                    allEmployees.clear()
                    allEmployees.addAll(employees)

                    runOnUiThread {
                        // Calculer le nombre de pages
                        totalPages = (allEmployees.size + pageSize - 1) / pageSize
                        currentPage = 0

                        // Afficher la première page
                        afficherPage(currentPage)

                        // Calculer les statistiques sur TOUS les employés
                        calculerStatistiques(allEmployees)

                        Log.d("API_HOME", "Employés chargés: ${allEmployees.size}, Pages: $totalPages")
                    }
                } else {
                    Log.e("API_HOME", "Erreur code: ${response.code()}")
                    runOnUiThread {
                        Toast.makeText(this@HomeActivity, "Erreur: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: retrofit2.Call<List<Employee>>, t: Throwable) {
                Log.e("API_HOME", "Échec connexion: ${t.message}")
                runOnUiThread {
                    Toast.makeText(this@HomeActivity, "Échec connexion: ${t.message}", Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun afficherPage(page: Int) {
        // Calculer les indices de début et fin
        val start = page * pageSize
        var end = start + pageSize
        if (end > allEmployees.size) {
            end = allEmployees.size
        }

        // Mettre à jour la liste affichée
        displayedEmployees.clear()
        if (start < allEmployees.size) {
            displayedEmployees.addAll(allEmployees.subList(start, end))
        }

        // Mettre à jour l'adaptateur
        employeeAdapter.notifyDataSetChanged()

        // Forcer la hauteur du ListView après mise à jour
        setListViewHeightBasedOnChildren()

        // Mettre à jour les boutons de pagination
        btnPagePrecedente.isEnabled = page > 0
        btnPageSuivante.isEnabled = page < totalPages - 1

        // Mettre à jour le texte de la page
        txtPageInfo.text = "Page ${page + 1} / $totalPages"
    }

    private fun setListViewHeightBasedOnChildren() {
        val listAdapter = listViewEmployes.adapter ?: return
        if (listAdapter.count == 0) return

        var totalHeight = 0
        for (i in 0 until listAdapter.count) {
            val listItem = listAdapter.getView(i, null, listViewEmployes)
            listItem.measure(0, 0)
            totalHeight += listItem.measuredHeight
        }

        val params = listViewEmployes.layoutParams
        params.height = totalHeight + (listViewEmployes.dividerHeight * (listAdapter.count - 1))
        listViewEmployes.layoutParams = params
        listViewEmployes.requestLayout()
    }

    private fun ajouterEmploye() {
        val numero = editNumEmp.text.toString().trim()
        val nom = editNom.text.toString().trim()
        val salaireStr = editSalaire.text.toString().trim()

        if (TextUtils.isEmpty(numero)) {
            editNumEmp.error = "Le numéro est requis"
            return
        }
        if (TextUtils.isEmpty(nom)) {
            editNom.error = "Le nom est requis"
            return
        }
        if (TextUtils.isEmpty(salaireStr)) {
            editSalaire.error = "Le salaire est requis"
            return
        }

        if (allEmployees.any { it.numEmp == numero }) {
            editNumEmp.error = "Ce numéro existe déjà"
            return
        }

        val salaire = try {
            salaireStr.toDouble()
        } catch (e: NumberFormatException) {
            editSalaire.error = "Salaire invalide"
            return
        }

        // Créer l'employé
        val nouvelEmploye = Employee()
        nouvelEmploye.numEmp = numero
        nouvelEmploye.nom = nom
        nouvelEmploye.salaire = salaire.toString()

        // Appel API pour ajouter
        val call = apiService.createEmployee(nouvelEmploye)
        call.enqueue(object : retrofit2.Callback<Employee> {
            override fun onResponse(call: retrofit2.Call<Employee>, response: retrofit2.Response<Employee>) {
                if (response.isSuccessful) {
                    // Succès - Ajouter à la liste complète
                    allEmployees.add(nouvelEmploye)

                    // Recalculer le nombre de pages
                    totalPages = (allEmployees.size + pageSize - 1) / pageSize

                    // Aller à la dernière page pour voir le nouvel employé
                    currentPage = totalPages - 1
                    afficherPage(currentPage)

                    editNumEmp.text.clear()
                    editNom.text.clear()
                    editSalaire.text.clear()

                    // Recalculer les statistiques
                    calculerStatistiques(allEmployees)

                    Toast.makeText(this@HomeActivity, "Employé ajouté avec succès", Toast.LENGTH_SHORT).show()
                    Log.d("API", "✅ Employé ajouté: ${response.body()}")
                } else {
                    Log.e("API", "❌ Erreur ${response.code()}: ${response.errorBody()?.string()}")
                    Toast.makeText(this@HomeActivity, "Erreur lors de l'ajout", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<Employee>, t: Throwable) {
                Log.e("API", "❌ Échec connexion: ${t.message}")
                Toast.makeText(this@HomeActivity, "Erreur de connexion au serveur", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun supprimerEmploye(numEmp: String) {
        // Appel API pour supprimer
        val call = apiService.deleteEmployee(numEmp)
        call.enqueue(object : retrofit2.Callback<Void> {
            override fun onResponse(call: retrofit2.Call<Void>, response: retrofit2.Response<Void>) {
                if (response.isSuccessful) {
                    // Succès - Supprimer de la liste complète
                    allEmployees.removeAll { it.numEmp == numEmp }

                    // Recalculer le nombre de pages
                    totalPages = if (allEmployees.isEmpty()) 1 else (allEmployees.size + pageSize - 1) / pageSize

                    // Ajuster la page courante si nécessaire
                    if (currentPage >= totalPages) {
                        currentPage = totalPages - 1
                    }
                    if (currentPage < 0) currentPage = 0

                    // Afficher la page mise à jour
                    afficherPage(currentPage)

                    // Recalculer les statistiques
                    calculerStatistiques(allEmployees)

                    Toast.makeText(this@HomeActivity, "Employé supprimé", Toast.LENGTH_SHORT).show()
                    Log.d("API", "✅ Employé supprimé: $numEmp")
                } else {
                    Log.e("API", "❌ Erreur ${response.code()}")
                    Toast.makeText(this@HomeActivity, "Erreur lors de la suppression", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<Void>, t: Throwable) {
                Log.e("API", "❌ Échec connexion: ${t.message}")
                Toast.makeText(this@HomeActivity, "Erreur de connexion", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun ouvrirDialogModification(employee: Employee) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_modifier_employe, null)
        val editNom = dialogView.findViewById<EditText>(R.id.editNomModif)
        val editSalaire = dialogView.findViewById<EditText>(R.id.editSalaireModif)

        editNom.setText(employee.nom)
        editSalaire.setText(employee.salaire)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Modifier l'employé")
            .setView(dialogView)
            .setPositiveButton("Modifier") { _, _ ->
                val nouveauNom = editNom.text.toString().trim()
                val nouveauSalaire = editSalaire.text.toString().trim()

                if (nouveauNom.isNotEmpty() && nouveauSalaire.isNotEmpty()) {
                    modifierEmploye(employee.numEmp, nouveauNom, nouveauSalaire)
                } else {
                    Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Annuler", null)
            .create()

        dialog.show()
    }

    private fun modifierEmploye(numEmp: String, nouveauNom: String, nouveauSalaire: String) {
        val employeModifie = Employee().apply {
            this.numEmp = numEmp
            nom = nouveauNom
            salaire = nouveauSalaire
        }

        val call = apiService.updateEmployee(numEmp, employeModifie)

        call.enqueue(object : retrofit2.Callback<Employee> {
            override fun onResponse(call: retrofit2.Call<Employee>, response: retrofit2.Response<Employee>) {
                if (response.isSuccessful) {
                    // Mettre à jour la liste complète
                    val index = allEmployees.indexOfFirst { it.numEmp == numEmp }
                    if (index != -1) {
                        allEmployees[index].nom = nouveauNom
                        allEmployees[index].salaire = nouveauSalaire
                    }

                    // Rafraîchir l'affichage
                    afficherPage(currentPage)
                    calculerStatistiques(allEmployees)

                    Toast.makeText(this@HomeActivity, "Employé modifié avec succès", Toast.LENGTH_SHORT).show()
                    Log.d("UPDATE", "✅ Employé modifié: $numEmp")
                } else {
                    Log.e("UPDATE", "❌ Erreur ${response.code()}")
                    Toast.makeText(this@HomeActivity, "Erreur lors de la modification", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<Employee>, t: Throwable) {
                Log.e("UPDATE", "❌ Échec connexion: ${t.message}")
                Toast.makeText(this@HomeActivity, "Erreur de connexion: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun calculerStatistiques(employees: List<Employee>) {
        if (employees.isEmpty()) {
            txtTotal.text = "0 Ar"
            txtMin.text = "0 Ar"
            txtMax.text = "0 Ar"
            mettreAJourCamembert(employees)
            return
        }

        var total = 0.0
        var min = Double.MAX_VALUE
        var max = Double.MIN_VALUE

        for (emp in employees) {
            try {
                val salaire = emp.salaire.toDouble()
                total += salaire
                if (salaire < min) min = salaire
                if (salaire > max) max = salaire
            } catch (e: NumberFormatException) {
                // Ignorer les salaires non numériques
            }
        }

        txtTotal.text = String.format("%.2f Ar", total)
        txtMin.text = String.format("%.2f Ar", min)
        txtMax.text = String.format("%.2f Ar", max)

        // Mettre à jour le diagramme camembert
        mettreAJourCamembert(employees)
    }

    /**
     * Met à jour le diagramme camembert avec les données des employés
     */
    private fun mettreAJourCamembert(employees: List<Employee>) {
        if (employees.isEmpty()) {
            txtPourcentageFaible.text = "0%"
            txtPourcentageMoyen.text = "0%"
            txtPourcentageEleve.text = "0%"
            pieChartView.background = createPieChartDrawable(0, 0, 0)
            return
        }

        var faible = 0
        var moyen = 0
        var eleve = 0

        for (emp in employees) {
            try {
                val salaire = emp.salaire.toDouble()
                when {
                    salaire < 1000 -> faible++
                    salaire in 1000.0..5000.0 -> moyen++
                    salaire > 5000 -> eleve++
                }
            } catch (e: NumberFormatException) {
                // Ignorer les salaires invalides
            }
        }

        val total = faible + moyen + eleve

        if (total == 0) {
            txtPourcentageFaible.text = "0%"
            txtPourcentageMoyen.text = "0%"
            txtPourcentageEleve.text = "0%"
            pieChartView.background = createPieChartDrawable(0, 0, 0)
            return
        }

        val pourcentageFaible = (faible * 100.0 / total).toInt()
        val pourcentageMoyen = (moyen * 100.0 / total).toInt()
        val pourcentageEleve = (eleve * 100.0 / total).toInt()

        txtPourcentageFaible.text = "$pourcentageFaible%"
        txtPourcentageMoyen.text = "$pourcentageMoyen%"
        txtPourcentageEleve.text = "$pourcentageEleve%"

        // Créer et appliquer le drawable du camembert
        pieChartView.background = createPieChartDrawable(pourcentageFaible, pourcentageMoyen, pourcentageEleve)
    }

    /**
     * Crée un drawable personnalisé pour le diagramme camembert
     */
    private fun createPieChartDrawable(faible: Int, moyen: Int, eleve: Int): Drawable {
        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        val colors = listOf(
            Color.parseColor("#F44336"), // Rouge - Faible (<1000 Ar)
            Color.parseColor("#FFA726"), // Orange - Moyen (1000-5000 Ar)
            Color.parseColor("#4CAF50")  // Vert - Élevé (>5000 Ar)
        )

        val angles = listOf(
            (faible * 360 / 100).toFloat(),
            (moyen * 360 / 100).toFloat(),
            (eleve * 360 / 100).toFloat()
        )

        return object : Drawable() {
            override fun draw(canvas: android.graphics.Canvas) {
                val rect = bounds
                val centerX = rect.centerX().toFloat()
                val centerY = rect.centerY().toFloat()
                val radius = min(rect.width(), rect.height()) / 2f

                var startAngle = -90f // Commencer à 12h (en haut)

                for (i in colors.indices) {
                    if (angles[i] > 0) {
                        paint.color = colors[i]
                        canvas.drawArc(
                            centerX - radius,
                            centerY - radius,
                            centerX + radius,
                            centerY + radius,
                            startAngle,
                            angles[i],
                            true,
                            paint
                        )
                        startAngle += angles[i]
                    }
                }
            }

            override fun setAlpha(alpha: Int) {}
            override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {}
            override fun getOpacity(): Int = PixelFormat.OPAQUE
        }
    }
}