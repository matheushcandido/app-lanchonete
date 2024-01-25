package com.example.bacci;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.bacci.activities.event.EventListActivity;
import com.example.bacci.activities.product.ProductListActivity;
import com.example.bacci.activities.sale.SaleCreateActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Encontrar os botões pelo ID
        Button buttonGoToProducts = findViewById(R.id.buttonGoToProducts);
        Button buttonGoToEvents = findViewById(R.id.buttonGoToEvents);
        Button buttonGoToSale = findViewById(R.id.buttonGoToSale);

        // Definir o listener do botão para ir para a página de produtos
        buttonGoToProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProductListActivity.class);
                startActivity(intent);
            }
        });

        // Definir o listener do botão para ir para a página de eventos
        buttonGoToEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EventListActivity.class);
                startActivity(intent);
            }
        });

        // Definir o listener do botão para ir para a página de venda
        buttonGoToSale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SaleCreateActivity.class);
                startActivity(intent);
            }
        });
    }
}