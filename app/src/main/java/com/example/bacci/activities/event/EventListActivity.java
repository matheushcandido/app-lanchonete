package com.example.bacci.activities.event;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.bacci.R;
import com.example.bacci.models.Event;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class EventListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EventListAdapter adapter;
    private List<Event> eventList;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        Button buttonGoToEventsCreate = findViewById(R.id.buttonGoToEventsCreate);

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

        recyclerView = findViewById(R.id.recyclerViewEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        eventList = new ArrayList<>();
        adapter = new EventListAdapter(eventList);
        recyclerView.setAdapter(adapter);

        adapter = new EventListAdapter(eventList);
        adapter.setOnItemClickListener(new EventListAdapter.OnItemClickListener() {
            @Override
            public void onDeleteClick(int position) {
                Toast.makeText(EventListActivity.this, "Evento excluído", Toast.LENGTH_SHORT).show();

                eventList.remove(position);

                adapter.notifyItemRemoved(position);
            }
            @Override
            public void onEditClick(int position) {
                String eventId = eventList.get(position).getId();

                Intent intent = new Intent(EventListActivity.this, EventEditActivity.class);
                intent.putExtra("eventId", eventId);
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);

        buttonGoToEventsCreate.setOnClickListener(v -> {
            Intent intent = new Intent(EventListActivity.this, EventCreateActivity.class);
            startActivity(intent);
        });

        loadProducts();
    }

    private void loadProducts() {
        db.collection("events")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Event> productList = new ArrayList<>();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Event event = new Event(
                                        document.getString("id"),
                                        document.getString("nome"),
                                        document.getString("dataInicio"),
                                        document.getString("dataFim")
                                );
                                eventList.add(event);
                            }

                            adapter.setEventList(eventList);
                            adapter.notifyDataSetChanged();

                        } else {
                            Toast.makeText(getApplicationContext(), "Falha ao carregar eventos.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}