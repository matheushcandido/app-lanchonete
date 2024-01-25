package com.example.bacci.activities.product;

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
import com.example.bacci.models.Product;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.ViewHolder> {

    private static List<Product> productList;

    public ProductListAdapter(List<Product> productList) {
        this.productList = productList;
    }

    public void setProductList(List<Product> productList) {
        this.productList = productList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return productList.size();
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

        private final TextView productNameTextView;
        private final TextView productPriceTextView;
        private final Button deleteButton;
        private final Button editButton;
        FirebaseFirestore db;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productNameTextView = itemView.findViewById(R.id.productNameTextView);
            productPriceTextView = itemView.findViewById(R.id.productPriceTextView);
            deleteButton = itemView.findViewById(R.id.buttonDeleteProduct);
            editButton = itemView.findViewById(R.id.buttonEditProduct);
            db = FirebaseFirestore.getInstance();
        }

        public void bind(Product product) {
            productNameTextView.setText(product.getNome());
            productPriceTextView.setText(String.valueOf(product.getPreco()));

            deleteButton.setOnClickListener(v -> {
                mostrarDialogoConfirmacaoExclusao(product.getId());
            });

            editButton.setOnClickListener(v -> {
                if (mListener != null) {
                    mListener.onEditClick(getBindingAdapterPosition());
                }
            });
        }

        private void mostrarDialogoConfirmacaoExclusao(String productId) {
            AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
            builder.setTitle("Confirmar Exclusão");
            builder.setMessage("Tem certeza de que deseja excluir este produto?");

            builder.setPositiveButton("Sim", (dialog, which) -> {
                excluirProduto(productId, getAdapterPosition());
            });

            builder.setNegativeButton("Não", (dialog, which) -> {
            });

            builder.show();
        }

        private void excluirProduto(String id, int position) {
            db.collection("products").document(id)
                    .delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            productList.remove(position);
                            notifyItemRemoved(position);
                            Toast.makeText(itemView.getContext(), "Produto excluído com sucesso", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(itemView.getContext(), "Falha ao excluir o produto", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(itemView.getContext(), "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
