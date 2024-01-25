package com.example.bacci.activities.product;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bacci.R;
import com.example.bacci.activities.event.EventEditActivity;
import com.example.bacci.activities.event.EventListActivity;
import com.example.bacci.models.Product;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProductListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductListAdapter adapter;
    private List<Product> productList;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        Button buttonGoToProductsCreate = findViewById(R.id.buttonGoToProductsCreate);

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

        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.recyclerViewProducts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        productList = new ArrayList<>();
        adapter = new ProductListAdapter(productList);
        recyclerView.setAdapter(adapter);

        adapter = new ProductListAdapter(productList);
        adapter.setOnItemClickListener(new ProductListAdapter.OnItemClickListener() {
            @Override
            public void onDeleteClick(int position) {
                Toast.makeText(ProductListActivity.this, "Produto excluído", Toast.LENGTH_SHORT).show();

                productList.remove(position);

                adapter.notifyItemRemoved(position);
            }

            @Override
            public void onEditClick(int position) {
                String productId = productList.get(position).getId();

                Intent intent = new Intent(ProductListActivity.this, ProductEditActivity.class);
                intent.putExtra("productId", productId);
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);

        buttonGoToProductsCreate.setOnClickListener(v -> {
            Intent intent = new Intent(ProductListActivity.this, ProductCreateActivity.class);
            startActivity(intent);
        });

        loadProducts();
    }

    private void loadProducts() {
        db.collection("products")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            productList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Product product = new Product(
                                        document.getString("id"),
                                        document.getString("nome"),
                                        document.getDouble("preco")
                                );
                                productList.add(product);
                            }

                            adapter.notifyDataSetChanged();  // Notifica o adaptador após atualizar a lista

                        } else {
                            Toast.makeText(getApplicationContext(), "Falha ao carregar produtos.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
