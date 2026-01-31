package com.irlan.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.irlan.app.R;
import com.irlan.app.model.Despesa;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter para exibir a lista de despesas no RecyclerView
 * Responsável por criar e popular os itens da lista
 */
public class DespesaAdapter extends RecyclerView.Adapter<DespesaAdapter.DespesaViewHolder> {

    // Lista de despesas a serem exibidas
    private List<Despesa> listaDespesas;

    // Interface para callback de cliques (opcional - para implementação futura)
    private OnItemClickListener listener;

    // Construtor
    public DespesaAdapter() {
        this.listaDespesas = new ArrayList<>();
    }

    // Construtor com lista inicial
    public DespesaAdapter(List<Despesa> listaDespesas) {
        this.listaDespesas = listaDespesas != null ? listaDespesas : new ArrayList<>();
    }

    @NonNull
    @Override
    public DespesaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Infla o layout do item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_despesa, parent, false);
        return new DespesaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DespesaViewHolder holder, int position) {
        // Obtém a despesa da posição atual
        Despesa despesa = listaDespesas.get(position);

        // Popula os campos do ViewHolder
        holder.tvDescricao.setText(despesa.getDescricao());
        holder.tvCartao.setText("💳 " + despesa.getCartao());
        holder.tvData.setText("📅 " + despesa.getDataFormatada());
        holder.tvValor.setText(despesa.getValorFormatado());

        // Configura o listener de clique (se existir)
        if (listener != null) {
            holder.itemView.setOnClickListener(v ->
                    listener.onItemClick(despesa, position)
            );
        }
    }

    @Override
    public int getItemCount() {
        return listaDespesas.size();
    }

    /**
     * Atualiza a lista completa de despesas
     * @param novaLista Nova lista de despesas
     */
    public void atualizarLista(List<Despesa> novaLista) {
        this.listaDespesas = novaLista != null ? novaLista : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * Adiciona uma despesa à lista
     * @param despesa Despesa a ser adicionada
     */
    public void adicionarDespesa(Despesa despesa) {
        listaDespesas.add(0, despesa); // Adiciona no início
        notifyItemInserted(0);
    }

    /**
     * Remove uma despesa da lista
     * @param position Posição da despesa
     */
    public void removerDespesa(int position) {
        if (position >= 0 && position < listaDespesas.size()) {
            listaDespesas.remove(position);
            notifyItemRemoved(position);
        }
    }

    /**
     * Limpa toda a lista
     */
    public void limparLista() {
        listaDespesas.clear();
        notifyDataSetChanged();
    }

    /**
     * Retorna a lista atual de despesas
     */
    public List<Despesa> getListaDespesas() {
        return listaDespesas;
    }

    /**
     * Define o listener para cliques nos itens
     * @param listener Listener a ser configurado
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // ==================== VIEW HOLDER ====================

    /**
     * ViewHolder que mantém as referências das views de cada item
     */
    static class DespesaViewHolder extends RecyclerView.ViewHolder {

        TextView tvDescricao;
        TextView tvCartao;
        TextView tvData;
        TextView tvValor;

        public DespesaViewHolder(@NonNull View itemView) {
            super(itemView);

            // Inicializa as views
            tvDescricao = itemView.findViewById(R.id.tvItemDescricao);
            tvCartao = itemView.findViewById(R.id.tvItemCartao);
            tvData = itemView.findViewById(R.id.tvItemData);
            tvValor = itemView.findViewById(R.id.tvItemValor);
        }
    }

    // ==================== INTERFACE DE CALLBACK ====================

    /**
     * Interface para callback de cliques nos itens
     * Para implementar funcionalidades como edição/exclusão
     */
    public interface OnItemClickListener {
        void onItemClick(Despesa despesa, int position);
    }
}