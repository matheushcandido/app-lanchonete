package com.example.bacci.activities.event;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bacci.R;
import com.example.bacci.models.Event;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.ViewHolder> {

    private List<Event> eventList;

    public EventListAdapter(List<Event> eventList) {
        this.eventList = eventList;
    }

    public void setEventList(List<Event> eventList) {
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public interface OnItemClickListener {
        void onDeleteClick(int position);
        void onEditClick(int position);
    }

    private OnItemClickListener mListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView eventNameTextView;
        private final TextView eventStartDateTextView;
        private final TextView eventEndDateTextView;
        private final Button deleteButton;
        private final Button editButton;
        FirebaseFirestore db;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            eventNameTextView = itemView.findViewById(R.id.eventNameTextView);
            eventStartDateTextView = itemView.findViewById(R.id.eventStartDateTextView);
            eventEndDateTextView = itemView.findViewById(R.id.eventEndDateTextView);
            deleteButton = itemView.findViewById(R.id.buttonDeleteEvent);
            editButton = itemView.findViewById(R.id.buttonEditEvent);

            db = FirebaseFirestore.getInstance();
        }

        public void bind(Event event) {
            eventNameTextView.setText(event.getNome());
            eventStartDateTextView.setText("Data de início: " + event.getDataInicio());
            eventEndDateTextView.setText("Data do fim: " + event.getDataFim());

            deleteButton.setOnClickListener(v -> {
                mostrarDialogoConfirmacaoExclusao(event.getId());
            });

            editButton.setOnClickListener(v -> {
                if (mListener != null) {
                    mListener.onEditClick(getBindingAdapterPosition());
                }
            });
        }

        private void mostrarDialogoConfirmacaoExclusao(String eventId) {
            AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
            builder.setTitle("Confirmar Exclusão");
            builder.setMessage("Tem certeza de que deseja excluir este evento?");

            builder.setPositiveButton("Sim", (dialog, which) -> {
                excluirProduto(eventId, getAdapterPosition());
            });

            builder.setNegativeButton("Não", (dialog, which) -> {
            });

            builder.show();
        }

        private void excluirProduto(String id, int position) {
            db.collection("events").document(id)
                    .delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            eventList.remove(position);
                            notifyItemRemoved(position);
                            Toast.makeText(itemView.getContext(), "Evento excluído com sucesso", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(itemView.getContext(), "Falha ao excluir o evento", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(itemView.getContext(), "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
