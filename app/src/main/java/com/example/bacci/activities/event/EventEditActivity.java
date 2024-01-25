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
import com.example.bacci.models.Event;
import com.example.bacci.models.Product;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
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

public class EventEditActivity extends AppCompatActivity {

    private EditText eventNameTextView;
    private EditText editTextStartDate;
    private EditText editTextEndDate;

    private Button buttonAddProduct;

    private Spinner spinnerProduct;

    private Calendar startDateCalendar;
    private Calendar endDateCalendar;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private LinearLayout productsLayout;
    private List<Product> selectedProducts = new ArrayList<>();
    private Map<String, Integer> productQuantities = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_edit);

        eventNameTextView = findViewById(R.id.editTextEventName);
        editTextStartDate = findViewById(R.id.editTextStartDate);
        editTextEndDate = findViewById(R.id.editTextEndDate);

        spinnerProduct = findViewById(R.id.spinnerProduct);

        buttonAddProduct = findViewById(R.id.buttonAddProductEvent);

        productsLayout = findViewById(R.id.productsLayoutEventEdit);

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

        String eventId = getIntent().getStringExtra("eventId");

        loadEventDetails(eventId);

        buttonAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addProductToEvent();
            }
        });

        Button buttonSaveEvent = findViewById(R.id.buttonSaveEvent);
        buttonSaveEvent.setOnClickListener(v -> {
            saveEventChanges(eventId);
        });

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
                                    EventEditActivity.this,
                                    android.R.layout.simple_spinner_item,
                                    productList
                            );

                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                            spinnerProduct.setAdapter(adapter);

                        } else {
                            Toast.makeText(getApplicationContext(), "Falha ao carregar produtos.", Toast.LENGTH_SHORT).show();
                        }
                    }
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

    private void loadEventDetails(String eventId) {
        db.collection("events").document(eventId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Event event = document.toObject(Event.class);

                            if (event != null) {
                                eventNameTextView.setText(event.getNome());
                                editTextStartDate.setText(event.getDataInicio());
                                editTextEndDate.setText(event.getDataFim());

                                loadEventProducts(eventId);
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Falha ao mostrar evento.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Falha ao consultar evento.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadEventProducts(String eventId) {
        db.collection("events-products")
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        selectedProducts.clear();
                        productQuantities.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String productId = document.getString("productId");
                            int stockQuantity = document.getLong("stockQuantity").intValue();

                            loadProductDetails(productId, stockQuantity);
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Falha ao carregar produtos associados ao evento.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadProductDetails(String productId, int stockQuantity) {
        db.collection("products").document(productId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Product product = document.toObject(Product.class);

                            if (product != null) {
                                selectedProducts.add(product);
                                productQuantities.put(product.getId(), stockQuantity);

                                displaySelectedProducts();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Produto não encontrado.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Falha ao carregar detalhes do produto.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addProductToEvent() {
        Product selectedProduct = (Product) spinnerProduct.getSelectedItem();
        EditText editTextStockQuantity = findViewById(R.id.editTextStockQuantity);

        if (selectedProduct != null) {
            try {
                int manuallyEnteredQuantity = Integer.parseInt(editTextStockQuantity.getText().toString());
                int existingQuantity = productQuantities.getOrDefault(selectedProduct.getId(), 0);

                if (selectedProducts.contains(selectedProduct)) {
                    // Se o produto já existe na lista, apenas atualize a quantidade
                    int newQuantity = existingQuantity + manuallyEnteredQuantity;
                    productQuantities.put(selectedProduct.getId(), newQuantity);
                } else {
                    // Se o produto não existe na lista, adicione-o à lista e ajuste a quantidade
                    selectedProducts.add(selectedProduct);
                    productQuantities.put(selectedProduct.getId(), manuallyEnteredQuantity);
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
        productsLayout.removeAllViews();

        for (Product product : selectedProducts) {
            int quantity = productQuantities.getOrDefault(product.getId(), 0);

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

            productsLayout.addView(productLayout);
        }
    }

    private void deleteProduct(Product product) {
        selectedProducts.remove(product);
        productQuantities.remove(product.getId());

        displaySelectedProducts();

        String eventId = getIntent().getStringExtra("eventId");

        db.collection("events-products")
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("productId", product.getId())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            db.collection("events-products")
                                    .document(document.getId())
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(getApplicationContext(), "Produto removido com sucesso.", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getApplicationContext(), "Falha ao remover produto.", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Falha ao encontrar o produto associado ao evento.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveEventChanges(String eventId) {
        String eventName = eventNameTextView.getText().toString();
        String startDate = editTextStartDate.getText().toString();
        String endDate = editTextEndDate.getText().toString();

        if (eventName.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Preencha todos os campos antes de salvar.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("events").document(eventId)
                .update(
                        "nome", eventName,
                        "dataInicio", startDate,
                        "dataFim", endDate
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getApplicationContext(), "Alterações salvas com sucesso!", Toast.LENGTH_SHORT).show();

                    for (Product product : selectedProducts) {
                        int quantity = productQuantities.getOrDefault(product.getId(), 0);
                        saveEventProductRelation(eventId, product.getId(), quantity);
                    }

                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), "Falha ao salvar alterações.", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveEventProductRelation(String eventId, String productId, int stockQuantity) {
        db.collection("events-products")
                .whereEqualTo("eventId", eventId)
                .whereEqualTo("productId", productId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        } else {
                            Map<String, Object> relationData = new HashMap<>();
                            relationData.put("eventId", eventId);
                            relationData.put("productId", productId);
                            relationData.put("stockQuantity", stockQuantity);

                            db.collection("events-products").document(UUID.randomUUID().toString())
                                    .set(relationData)
                                    .addOnCompleteListener(taskRelation -> {
                                        if (taskRelation.isSuccessful()) {
                                            Toast.makeText(getApplicationContext(), "Relação produto e evento salva com sucesso!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Falha ao salvar relação produto e evento.", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show());
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Falha ao verificar relação existente.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}