package com.example.bacci.activities.product;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.bacci.R;
import com.example.bacci.models.Product;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProductEditActivity extends AppCompatActivity {

    private EditText productNameTextView;
    private EditText productPriceTextView;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_edit);

        productNameTextView = findViewById(R.id.editTextProductName);
        productPriceTextView = findViewById(R.id.editTextProductPrice);

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

        String productId = getIntent().getStringExtra("productId");

        loadProductDetails(productId);

        Button buttonSaveEvent = findViewById(R.id.buttonSaveProduct);
        buttonSaveEvent.setOnClickListener(v -> {
            saveProductChanges(productId);
        });
    }

    private void loadProductDetails(String productId) {
        db.collection("products").document(productId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Product product = document.toObject(Product.class);

                            if (product != null) {
                                productNameTextView.setText(product.getNome());
                                productPriceTextView.setText(String.valueOf(product.getPreco()));
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Falha ao mostrar produto.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Falha ao consultar produto.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveProductChanges(String productId) {
        String productName = productNameTextView.getText().toString();
        double productPrice = Double.parseDouble(productPriceTextView.getText().toString());

        if (productName.isEmpty() || productPrice == 0.0) {
            Toast.makeText(getApplicationContext(), "Preencha todos os campos antes de salvar.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("products").document(productId)
                .update(
                        "nome", productName,
                        "preco", productPrice
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getApplicationContext(), "Alterações salvas com sucesso!", Toast.LENGTH_SHORT).show();

                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), "Falha ao salvar alterações.", Toast.LENGTH_SHORT).show();
                });
    }
}