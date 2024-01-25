package com.example.bacci.activities.sale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.tcp.TcpConnection;
import com.example.bacci.R;
import com.example.bacci.models.Sale;
import com.example.bacci.models.Product;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class SaleCreateActivity extends AppCompatActivity {

    private Spinner spinnerProduct;
    private Button buttonCreate, buttonIncrement, buttonDecrement;
    private TextView textCounter, textTotal;
    private int counter = 0;
    private TextView textTotalItems;
    private LinearLayout productsLayout;
    private List<Product> selectedProducts = new ArrayList<>();

    private Map<String, Integer> productQuantities = new HashMap<>();

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sale_create);

        db = FirebaseFirestore.getInstance();

        Spinner spinnerPaymentMethod = findViewById(R.id.spinnerPaymentMethod);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.payment_methods, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPaymentMethod.setAdapter(adapter);

        spinnerProduct = findViewById(R.id.spinnerProduct);
        buttonCreate = findViewById(R.id.buttonCreate);
        buttonIncrement = findViewById(R.id.buttonIncrement);
        buttonDecrement = findViewById(R.id.buttonDecrement);
        textCounter = findViewById(R.id.textCounter);
        textTotal = findViewById(R.id.textTotal);
        textTotalItems = findViewById(R.id.textTotalItems);

        productsLayout = findViewById(R.id.productsLayout);

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

        Button buttonAddProduct = findViewById(R.id.buttonAddProduct);
        buttonAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addProductToSale();
            }
        });

        loadProducts();
        updateCounterText();

        buttonIncrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                counter++;
                updateCounterText();
            }
        });

        buttonDecrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (counter > 0) {
                    counter--;
                    updateCounterText();
                }
            }
        });

        buttonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String metodoPagamento = spinnerPaymentMethod.getSelectedItem().toString();

                for (Product selectedProduct : selectedProducts) {
                    int quantidadeDesejada = productQuantities.getOrDefault(selectedProduct.getId(), 0);
                    double valorTotal = selectedProduct.getPreco() * quantidadeDesejada;

                    Sale novaVenda = new Sale(UUID.randomUUID().toString(), selectedProduct.getId(), quantidadeDesejada, valorTotal, metodoPagamento);

                    uploadData(novaVenda);
                }
            }
        });
    }

    private void updateCounterText() {
        textCounter.setText(String.valueOf(counter));

        if (spinnerProduct.getSelectedItem() != null) {
            Product selectedProduct = (Product) spinnerProduct.getSelectedItem();
            double valorTotal = selectedProduct.getPreco() * counter;
            textTotal.setText(String.format("Total: R$ %.2f", valorTotal));
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
                                    SaleCreateActivity.this,
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

    private void uploadData(Sale novaVenda) {
        Map<String, Object> doc = new HashMap<>();
        doc.put("id", novaVenda.getId());
        doc.put("idProduto", novaVenda.getIdProduto());
        doc.put("quantidade", novaVenda.getQuantidade());
        doc.put("valorTotal", novaVenda.getValorTotal());
        doc.put("metodoPagamento", novaVenda.getMetodoPagamento());

        db.collection("sales").document(novaVenda.getId()).set(doc)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Venda adicionada com sucesso!", Toast.LENGTH_SHORT).show();
                            //imprimirRecibo();
                        } else {
                            Toast.makeText(getApplicationContext(), "Falha ao adicionar venda.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void addProductToSale() {
        Product selectedProduct = (Product) spinnerProduct.getSelectedItem();

        if (selectedProduct != null && counter > 0) {
            if (selectedProducts.contains(selectedProduct)) {
                int existingQuantity = productQuantities.getOrDefault(selectedProduct.getId(), 0);
                int newQuantity = existingQuantity + counter;
                productQuantities.put(selectedProduct.getId(), newQuantity);
            } else {
                selectedProducts.add(selectedProduct);
                productQuantities.put(selectedProduct.getId(), counter);
            }

            displaySelectedProducts();
            calculateAndDisplayTotalValue();

            counter = 0;
            updateCounterText();
        } else {
            Toast.makeText(getApplicationContext(), "Selecione um produto e uma quantidade válida.", Toast.LENGTH_SHORT).show();
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

    private void calculateAndDisplayTotalValue() {
        double totalValue = 0;

        for (Product product : selectedProducts) {
            int quantity = productQuantities.getOrDefault(product.getId(), 0);
            totalValue += product.getPreco() * quantity;
        }

        textTotalItems.setText(String.format("Total Itens: R$ %.2f", totalValue));
    }

    private void deleteProduct(Product product) {
        selectedProducts.remove(product);
        productQuantities.remove(product.getId());

        displaySelectedProducts();
        calculateAndDisplayTotalValue();
    }

    private void imprimirRecibo() {
        try {
            EscPosPrinter printer = new EscPosPrinter(new TcpConnection("192.168.1.3", 9300, 15), 203, 48f, 32);
            printer
                    .printFormattedText(
                                    "[C]<u><font size='big'>ORDER N°045</font></u>\n" +
                                    "[L]\n" +
                                    "[C]================================\n" +
                                    "[L]\n" +
                                    "[L]<b>BEAUTIFUL SHIRT</b>[R]9.99e\n" +
                                    "[L]  + Size : S\n" +
                                    "[L]\n" +
                                    "[L]<b>AWESOME HAT</b>[R]24.99e\n" +
                                    "[L]  + Size : 57/58\n" +
                                    "[L]\n" +
                                    "[C]--------------------------------\n" +
                                    "[R]TOTAL PRICE :[R]34.98e\n" +
                                    "[R]TAX :[R]4.23e\n" +
                                    "[L]\n" +
                                    "[C]================================\n" +
                                    "[L]\n" +
                                    "[L]<font size='tall'>Customer :</font>\n" +
                                    "[L]Raymond DUPONT\n" +
                                    "[L]5 rue des girafes\n" +
                                    "[L]31547 PERPETES\n" +
                                    "[L]Tel : +33801201456\n" +
                                    "[L]\n" +
                                    "[C]<barcode type='ean13' height='10'>831254784551</barcode>\n" +
                                    "[C]<qrcode size='20'>https://dantsu.com/</qrcode>"
                    );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
