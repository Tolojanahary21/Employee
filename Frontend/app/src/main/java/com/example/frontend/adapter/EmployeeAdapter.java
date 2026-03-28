package com.example.frontend.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Filter;
import android.widget.Filterable;
import androidx.appcompat.app.AlertDialog;
import com.example.frontend.R;
import com.example.frontend.model.Employee;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EmployeeAdapter extends BaseAdapter implements Filterable {
    private Context context;
    private List<Employee> employeeList;
    private List<Employee> employeeListFull; // Pour la recherche/filtrage
    private LayoutInflater inflater;
    private OnEmployeeClickListener onEmployeeClickListener;
    private OnDeleteClickListener onDeleteClickListener;
    private OnEditClickListener onEditClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    // Interfaces pour les différents listeners
    public interface OnDeleteClickListener {
        void onDeleteClick(String numEmp);
    }

    public interface OnEditClickListener {
        void onEditClick(Employee employee);
    }

    public interface OnEmployeeClickListener {
        void onEmployeeClick(Employee employee);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Employee employee);
    }

    // Constructeur avec tous les listeners
    public EmployeeAdapter(Context context, List<Employee> employeeList,
                           OnDeleteClickListener deleteListener,
                           OnEditClickListener editListener,
                           OnEmployeeClickListener clickListener,
                           OnItemLongClickListener longClickListener) {
        this.context = context;
        this.employeeList = employeeList;
        this.employeeListFull = new ArrayList<>(employeeList);
        this.inflater = LayoutInflater.from(context);
        this.onDeleteClickListener = deleteListener;
        this.onEditClickListener = editListener;
        this.onEmployeeClickListener = clickListener;
        this.onItemLongClickListener = longClickListener;
    }

    // Constructeur simplifié pour compatibilité
    public EmployeeAdapter(Context context, List<Employee> employeeList, OnDeleteClickListener listener) {
        this(context, employeeList, listener, null, null, null);
    }

    @Override
    public int getCount() {
        return employeeList.size();
    }

    @Override
    public Employee getItem(int position) {
        return employeeList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.employee_item, parent, false);
            holder = new ViewHolder();
            holder.txtNumEmp = convertView.findViewById(R.id.txtNumEmp);
            holder.txtNom = convertView.findViewById(R.id.txtNom);
            holder.txtSalaire = convertView.findViewById(R.id.txtSalaire);
            holder.txtCategorie = convertView.findViewById(R.id.txtCategorie);
            holder.btnDelete = convertView.findViewById(R.id.btnDelete);
            holder.btnEdit = convertView.findViewById(R.id.btnEdit);
            holder.viewColorIndicator = convertView.findViewById(R.id.viewColorIndicator);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Employee employee = employeeList.get(position);

        // Afficher les données
        holder.txtNumEmp.setText(employee.getNumEmp());
        holder.txtNom.setText(employee.getNom());

        // Formater le salaire
        try {
            double salaire = Double.parseDouble(employee.getSalaire());
            NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
            format.setMaximumFractionDigits(2);
            format.setMinimumFractionDigits(2);
            holder.txtSalaire.setText(format.format(salaire) + " DH");
        } catch (NumberFormatException e) {
            holder.txtSalaire.setText(employee.getSalaire() + " DH");
        }

        // Calculer et afficher la catégorie avec couleur
        try {
            double salaire = Double.parseDouble(employee.getSalaire());
            String categorie;
            int color;
            int bgColor;

            if (salaire < 1000) {
                categorie = "Faible";
                color = 0xFFF44336; // Rouge
                bgColor = 0x1AF44336; // Rouge transparent
            } else if (salaire <= 5000) {
                categorie = "Moyen";
                color = 0xFFFFA726; // Orange
                bgColor = 0x1AFFA726; // Orange transparent
            } else {
                categorie = "Élevé";
                color = 0xFF4CAF50; // Vert
                bgColor = 0x1A4CAF50; // Vert transparent
            }
            holder.txtCategorie.setText(categorie);
            holder.txtCategorie.setTextColor(color);
            holder.viewColorIndicator.setBackgroundColor(color);
            convertView.setBackgroundColor(bgColor);
        } catch (NumberFormatException e) {
            holder.txtCategorie.setText("N/A");
            holder.viewColorIndicator.setBackgroundColor(Color.GRAY);
        }

        // Gestion du clic sur l'item
        convertView.setOnClickListener(v -> {
            if (onEmployeeClickListener != null) {
                onEmployeeClickListener.onEmployeeClick(employee);
            }
        });

        // Gestion du clic long sur l'item
        convertView.setOnLongClickListener(v -> {
            if (onItemLongClickListener != null) {
                onItemLongClickListener.onItemLongClick(employee);
                return true;
            }
            return false;
        });

        // Gestion du clic sur le bouton supprimer
        holder.btnDelete.setOnClickListener(v -> {
            showDeleteConfirmationDialog(employee);
        });

        // Gestion du clic sur le bouton modifier
        holder.btnEdit.setOnClickListener(v -> {
            if (onEditClickListener != null) {
                onEditClickListener.onEditClick(employee);
            }
        });

        return convertView;
    }

    // Dialogue de confirmation pour la suppression
    private void showDeleteConfirmationDialog(final Employee employee) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirmation de suppression");
        builder.setMessage("Voulez-vous vraiment supprimer l'employé " + employee.getNom() + " ?");

        builder.setPositiveButton("Supprimer", (dialog, which) -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDeleteClick(employee.getNumEmp());
            }
        });

        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Méthodes CRUD
    public void addEmployee(Employee employee) {
        employeeList.add(employee);
        employeeListFull.add(employee);
        notifyDataSetChanged();
    }

    public void updateEmployee(String numEmp, Employee updatedEmployee) {
        for (int i = 0; i < employeeList.size(); i++) {
            if (employeeList.get(i).getNumEmp().equals(numEmp)) {
                employeeList.set(i, updatedEmployee);
                break;
            }
        }
        for (int i = 0; i < employeeListFull.size(); i++) {
            if (employeeListFull.get(i).getNumEmp().equals(numEmp)) {
                employeeListFull.set(i, updatedEmployee);
                break;
            }
        }
        notifyDataSetChanged();
    }

    public void deleteEmployee(String numEmp) {
        // Supprimer de la liste principale
        for (int i = 0; i < employeeList.size(); i++) {
            if (employeeList.get(i).getNumEmp().equals(numEmp)) {
                employeeList.remove(i);
                break;
            }
        }
        // Supprimer de la liste complète
        for (int i = 0; i < employeeListFull.size(); i++) {
            if (employeeListFull.get(i).getNumEmp().equals(numEmp)) {
                employeeListFull.remove(i);
                break;
            }
        }
        notifyDataSetChanged();
    }

    public void clearAll() {
        employeeList.clear();
        employeeListFull.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Employee> employees) {
        employeeList.addAll(employees);
        employeeListFull.addAll(employees);
        notifyDataSetChanged();
    }

    // Recherche/Filtrage
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Employee> filteredList = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(employeeListFull);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();

                    for (Employee employee : employeeListFull) {
                        if (employee.getNom().toLowerCase().contains(filterPattern) ||
                                employee.getNumEmp().toLowerCase().contains(filterPattern)) {
                            filteredList.add(employee);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filteredList;
                results.count = filteredList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                employeeList.clear();
                employeeList.addAll((List) results.values);
                notifyDataSetChanged();
            }
        };
    }

    // Méthodes utilitaires
    public Employee getEmployeeByNum(String numEmp) {
        for (Employee employee : employeeListFull) {
            if (employee.getNumEmp().equals(numEmp)) {
                return employee;
            }
        }
        return null;
    }

    public List<Employee> getEmployeesByCategory(String category) {
        List<Employee> result = new ArrayList<>();
        for (Employee employee : employeeListFull) {
            try {
                double salaire = Double.parseDouble(employee.getSalaire());
                String empCategory;
                if (salaire < 1000) empCategory = "Faible";
                else if (salaire <= 5000) empCategory = "Moyen";
                else empCategory = "Élevé";

                if (empCategory.equals(category)) {
                    result.add(employee);
                }
            } catch (NumberFormatException e) {
                // Ignorer
            }
        }
        return result;
    }

    public double getTotalSalaires() {
        double total = 0;
        for (Employee employee : employeeListFull) {
            try {
                total += Double.parseDouble(employee.getSalaire());
            } catch (NumberFormatException e) {
                // Ignorer
            }
        }
        return total;
    }

    public double getMoyenneSalaires() {
        if (employeeListFull.isEmpty()) return 0;
        return getTotalSalaires() / employeeListFull.size();
    }

    public Employee getMinSalaire() {
        if (employeeListFull.isEmpty()) return null;
        Employee minEmp = employeeListFull.get(0);
        double minSalaire = Double.MAX_VALUE;

        for (Employee employee : employeeListFull) {
            try {
                double salaire = Double.parseDouble(employee.getSalaire());
                if (salaire < minSalaire) {
                    minSalaire = salaire;
                    minEmp = employee;
                }
            } catch (NumberFormatException e) {
                // Ignorer
            }
        }
        return minEmp;
    }

    public Employee getMaxSalaire() {
        if (employeeListFull.isEmpty()) return null;
        Employee maxEmp = employeeListFull.get(0);
        double maxSalaire = Double.MIN_VALUE;

        for (Employee employee : employeeListFull) {
            try {
                double salaire = Double.parseDouble(employee.getSalaire());
                if (salaire > maxSalaire) {
                    maxSalaire = salaire;
                    maxEmp = employee;
                }
            } catch (NumberFormatException e) {
                // Ignorer
            }
        }
        return maxEmp;
    }

    // Mise à jour des données
    public void updateData(List<Employee> newList) {
        this.employeeList = newList;
        this.employeeListFull = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    // ViewHolder pour optimisation
    private static class ViewHolder {
        TextView txtNumEmp, txtNom, txtSalaire, txtCategorie;
        ImageView btnDelete, btnEdit;
        View viewColorIndicator;
    }
}