package com.example.bacci.activities.product;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.bacci.R;
import com.example.bacci.models.Product;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProductCreateActivity extends AppCompatActivity {

    private EditText editTextProductName;
    private EditText editTextProductPrice;
    private Button buttonCreateProduct;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_create);

        db = FirebaseFirestore.getInstance();

        editTextProductName = findViewById(R.id.editTextProductName);
        editTextProductPrice = findViewById(R.id.editTextProductPrice);
        buttonCreateProduct = findViewById(R.id.buttonCreateProduct);

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

        buttonCreateProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String productName = editTextProductName.getText().toString();
                double productPrice = Double.parseDouble(editTextProductPrice.getText().toString());

                Product novoProduto = new Product(UUID.randomUUID().toString(), productName, productPrice);

                uploadData(novoProduto);
            }
        });
    }

    private void uploadData(Product novoProduto){

        Map<String, Object> doc = new HashMap<>();

        doc.put("id", novoProduto.getId());
        doc.put("nome", novoProduto.getNome());
        doc.put("preco", novoProduto.getPreco());

        db.collection("products").document(novoProduto.getId()).set(doc)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Produto adicionado com sucesso!", Toast.LENGTH_SHORT).show();

                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "Falha ao adicionar produto.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Exibir um alerta de falha com a mensagem de erro
                        Toast.makeText(getApplicationContext(), "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}