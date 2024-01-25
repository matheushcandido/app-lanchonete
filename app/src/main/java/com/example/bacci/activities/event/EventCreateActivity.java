package com.example.bacci.activities.event;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.bacci.R;
import com.example.bacci.activities.sale.SaleCreateActivity;
import com.example.bacci.models.Event;
import com.example.bacci.models.Product;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class EventCreateActivity extends AppCompatActivity {

    private EditText editTextEventName;
    private EditText editTextStartDate;
    private EditText editTextEndDate;
    private Button buttonCreateEvent;

    private Spinner spinnerProductEvent;
    private Button buttonAddProductEvent;
    private LinearLayout productsLayoutEvent;
    private List<Product> selectedProductsEvent = new ArrayList<>();
    private Map<String, Integer> productQuantitiesEvent = new HashMap<>();


    private Calendar startDateCalendar;
    private Calendar endDateCalendar;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_create);

        db = FirebaseFirestore.getInstance();

        editTextEventName = findViewById(R.id.editTextEventName);
        editTextStartDate = findViewById(R.id.editTextStartDate);
        editTextEndDate = findViewById(R.id.editTextEndDate);
        buttonCreateEvent = findViewById(R.id.buttonCreateEvent);

        spinnerProductEvent = findViewById(R.id.spinnerProduct);
        buttonAddProductEvent = findViewById(R.id.buttonAddProductEvent);
        productsLayoutEvent = findViewById(R.id.productsLayoutEvent);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        startDateCalendar = Calendar.getInstance();
        endDateCalendar = Calendar.getInstance();

        updateEditText(editTextStartDate, null);
        updateEditText(editTextEndDate, null);

        editTextStartDate.setOnClickListener(v -> showDatePicker(startDateCalendar, editTextStartDate));
        editTextEndDate.setOnClickListener(v -> showDatePicker(endDateCalendar, editTextEndDate));

        loadProducts();

        buttonAddProductEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addProductToEvent();
            }
        });

        buttonCreateEvent.setOnClickListener(view -> {
            String eventName = editTextEventName.getText().toString();
            String startDate = editTextStartDate.getText().toString();
            String endDate = editTextEndDate.getText().toString();

            Event novoEvento = new Event(UUID.randomUUID().toString(), eventName, startDate, endDate);

            uploadData(novoEvento);
        });
    }

    private void showDatePicker(final Calendar calendar, final EditText editText) {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, monthOfYear, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            updateEditText(editText, calendar);
        };

        new DatePickerDialog(
                this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void updateEditText(EditText editText, Calendar calendar) {
        String dateFormat = "dd/MM/yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.getDefault());

        if (calendar != null) {
            editText.setText(simpleDateFormat.format(calendar.getTime()));
        } else {
            editText.setText("");
        }
    }

    private void loadProducts() {
        db.collection("products")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Product> productList = new ArrayList<>();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Product product = new Product(
                                        document.getString("id"),
                                        document.getString("nome"),
                                        document.getDouble("preco")
                                );
                                productList.add(product);
                            }

                            ArrayAdapter<Product> adapter = new ArrayAdapter<>(
                                    EventCreateActivity.this,
                                    android.R.layout.simple_spinner_item,
                                    productList
                            );

                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                            spinnerProductEvent.setAdapter(adapter);

                        } else {
                            Toast.makeText(getApplicationContext(), "Falha ao carregar produtos.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void addProductToEvent() {
        Product selectedProduct = (Product) spinnerProductEvent.getSelectedItem();
        EditText editTextStockQuantity = findViewById(R.id.editTextStockQuantity);

        if (selectedProduct != null) {
            try {
                int manuallyEnteredQuantity = Integer.parseInt(editTextStockQuantity.getText().toString());
                int existingQuantity = productQuantitiesEvent.getOrDefault(selectedProduct.getId(), 0);
                int newQuantity = existingQuantity + manuallyEnteredQuantity;
                productQuantitiesEvent.put(selectedProduct.getId(), newQuantity);

                if (!selectedProductsEvent.contains(selectedProduct)) {
                    selectedProductsEvent.add(selectedProduct);
                }

                displaySelectedProducts();
            } catch (NumberFormatException e) {
                Toast.makeText(getApplicationContext(), "Digite uma quantidade válida.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Selecione um produto.", Toast.LENGTH_SHORT).show();
        }
    }

    private void displaySelectedProducts() {
        productsLayoutEvent.removeAllViews();

        for (Product product : selectedProductsEvent) {
            int quantity = productQuantitiesEvent.getOrDefault(product.getId(), 0);

            LinearLayout productLayout = new LinearLayout(this);
            productLayout.setOrientation(LinearLayout.HORIZONTAL);

            TextView productTextView = new TextView(this);
            productTextView.setText(String.format("%s - Quantidade: %d", product.getNome(), quantity));

            Button deleteButton = new Button(this);
            deleteButton.setText("Excluir");
            deleteButton.setTag(product);

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Product productToDelete = (Product) view.getTag();
                    deleteProduct(productToDelete);
                }
            });

            productLayout.addView(productTextView);
            productLayout.addView(deleteButton);

            productsLayoutEvent.addView(productLayout);
        }
    }

    private void deleteProduct(Product product) {
        selectedProductsEvent.remove(product);
        productQuantitiesEvent.remove(product.getId());

        displaySelectedProducts();
    }

    private void uploadData(Event novoEvento) {
        Map<String, Object> doc = new HashMap<>();

        doc.put("id", novoEvento.getId());
        doc.put("nome", novoEvento.getNome());
        doc.put("dataInicio", novoEvento.getDataInicio());
        doc.put("dataFim", novoEvento.getDataFim());

        db.collection("events").document(novoEvento.getId()).set(doc)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Evento adicionado com sucesso!", Toast.LENGTH_SHORT).show();

                        for (Product product : selectedProductsEvent) {
                            int quantity = productQuantitiesEvent.getOrDefault(product.getId(), 0);
                            saveEventProductRelation(novoEvento.getId(), product.getId(), quantity);
                        }

                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Falha ao adicionar evento.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void saveEventProductRelation(String eventId, String productId, int stockQuantity) {
        Map<String, Object> relationData = new HashMap<>();
        relationData.put("eventId", eventId);
        relationData.put("productId", productId);
        relationData.put("stockQuantity", stockQuantity);

        db.collection("events-products").document(UUID.randomUUID().toString())
                .set(relationData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Relação produto e evento salva com sucesso!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Falha ao salvar relação produto e evento.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}


