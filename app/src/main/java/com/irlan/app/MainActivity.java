package com.irlan.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.irlan.app.adapter.DespesaAdapter;
import com.irlan.app.database.DatabaseHelper;
import com.irlan.app.model.Despesa;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Activity principal do aplicativo FinanceApp
 * Gerencia cadastro, busca, visualização e exportação de despesas
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // ==================== COMPONENTES DA UI ====================

    // Seção Cadastro
    private EditText etValor, etDescricao, etDataCadastro;
    private Spinner spCartaoCadastro;
    private Button btnSalvar;

    // Seção Busca
    private EditText etValorBusca, etDescricaoBusca, etDataInicial, etDataFinal;
    private Spinner spCartaoBusca;
    private Button btnBuscar, btnLimpar, btnExportar;

    // Seção Resultados
    private RecyclerView recyclerResultados;
    private TextView tvTotal, tvMensagemVazia, tvPaginacao;
    private Button btnPaginaAnterior, btnProximaPagina;
    private ProgressBar progressBar;

    // ==================== VARIÁVEIS DE CONTROLE ====================

    private DatabaseHelper dbHelper;
    private DespesaAdapter adapter;
    private List<Despesa> listaDespesasCompleta;
    private List<Despesa> listaDespesasFiltrada;

    // Paginação
    private static final int ITENS_POR_PAGINA = 10;
    private int paginaAtual = 0;
    private int totalPaginas = 0;

    // Calendários para DatePicker
    private Calendar calendarioCadastro;
    private Calendar calendarioInicial;
    private Calendar calendarioFinal;

    // Opções de cartões
    private String[] opcoesCartoes = {"Nubank", "MercadoPago", "Neon", "Caixa", "Outros"};
    private String[] opcoesCartoesBusca = {"Todos", "Nubank", "MercadoPago", "Neon", "Caixa", "Outros"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializa componentes
        inicializarComponentes();
        configurarSpinners();
        configurarDatePickers();
        configurarRecyclerView();
        configurarBotoes();

        // Carrega dados iniciais
        carregarTodasDespesas();
    }

    // ==================== INICIALIZAÇÃO ====================

    private void inicializarComponentes() {
        // Banco de dados
        dbHelper = new DatabaseHelper(this);

        // Cadastro
        etValor = findViewById(R.id.etValor);
        etDescricao = findViewById(R.id.etDescricao);
        spCartaoCadastro = findViewById(R.id.spCartaoCadastro);
        etDataCadastro = findViewById(R.id.etDataCadastro);
        btnSalvar = findViewById(R.id.btnSalvar);

        // Busca
        etValorBusca = findViewById(R.id.etValorBusca);
        etDescricaoBusca = findViewById(R.id.etDescricaoBusca);
        spCartaoBusca = findViewById(R.id.spCartaoBusca);
        etDataInicial = findViewById(R.id.etDataInicial);
        etDataFinal = findViewById(R.id.etDataFinal);
        btnBuscar = findViewById(R.id.btnBuscar);
        btnLimpar = findViewById(R.id.btnLimpar);
        btnExportar = findViewById(R.id.btnExportar);

        // Resultados
        recyclerResultados = findViewById(R.id.recyclerResultados);
        tvTotal = findViewById(R.id.tvTotal);
        tvMensagemVazia = findViewById(R.id.tvMensagemVazia);
        tvPaginacao = findViewById(R.id.tvPaginacao);
        btnPaginaAnterior = findViewById(R.id.btnPaginaAnterior);
        btnProximaPagina = findViewById(R.id.btnProximaPagina);
        progressBar = findViewById(R.id.progressBar);

        // Calendários
        calendarioCadastro = Calendar.getInstance();
        calendarioInicial = Calendar.getInstance();
        calendarioFinal = Calendar.getInstance();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        etDataCadastro.setText(sdf.format(calendarioCadastro.getTime()));


        // Listas
        listaDespesasCompleta = new ArrayList<>();
        listaDespesasFiltrada = new ArrayList<>();
    }

    private void configurarSpinners() {
        // Spinner de cadastro
        ArrayAdapter<String> adapterCadastro = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                opcoesCartoes
        );
        adapterCadastro.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCartaoCadastro.setAdapter(adapterCadastro);

        // Spinner de busca
        ArrayAdapter<String> adapterBusca = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                opcoesCartoesBusca
        );
        adapterBusca.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCartaoBusca.setAdapter(adapterBusca);
    }

    private void configurarDatePickers() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));

        // DatePicker para cadastro
        etDataCadastro.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(
                    MainActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        calendarioCadastro.set(year, month, dayOfMonth);
                        etDataCadastro.setText(sdf.format(calendarioCadastro.getTime()));
                    },
                    calendarioCadastro.get(Calendar.YEAR),
                    calendarioCadastro.get(Calendar.MONTH),
                    calendarioCadastro.get(Calendar.DAY_OF_MONTH)
            );
            dialog.show();
        });

        // DatePicker para data inicial
        etDataInicial.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(
                    MainActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        calendarioInicial.set(year, month, dayOfMonth);
                        etDataInicial.setText(sdf.format(calendarioInicial.getTime()));
                    },
                    calendarioInicial.get(Calendar.YEAR),
                    calendarioInicial.get(Calendar.MONTH),
                    calendarioInicial.get(Calendar.DAY_OF_MONTH)
            );
            dialog.show();
        });

        // DatePicker para data final
        etDataFinal.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(
                    MainActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        calendarioFinal.set(year, month, dayOfMonth);
                        etDataFinal.setText(sdf.format(calendarioFinal.getTime()));
                    },
                    calendarioFinal.get(Calendar.YEAR),
                    calendarioFinal.get(Calendar.MONTH),
                    calendarioFinal.get(Calendar.DAY_OF_MONTH)
            );
            dialog.show();
        });
    }

    private void configurarRecyclerView() {
        adapter = new DespesaAdapter();
        recyclerResultados.setLayoutManager(new LinearLayoutManager(this));
        recyclerResultados.setAdapter(adapter);
    }

    private void configurarBotoes() {
        btnSalvar.setOnClickListener(v -> salvarDespesa());
        btnBuscar.setOnClickListener(v -> buscarDespesas());
        btnLimpar.setOnClickListener(v -> limparFiltros());
        btnExportar.setOnClickListener(v -> exportarParaExcel());
        btnPaginaAnterior.setOnClickListener(v -> paginaAnterior());
        btnProximaPagina.setOnClickListener(v -> proximaPagina());
    }

    // ==================== CADASTRO ====================

    private void salvarDespesa() {
        // Validação de campos
        String valorStr = etValor.getText().toString().trim();
        String descricao = etDescricao.getText().toString().trim();
        String dataStr = etDataCadastro.getText().toString().trim();
        String cartao = spCartaoCadastro.getSelectedItem().toString();

        if (valorStr.isEmpty()) {
            Toast.makeText(this, "❌ Por favor, insira o valor", Toast.LENGTH_SHORT).show();
            etValor.requestFocus();
            return;
        }

        if (descricao.isEmpty()) {
            Toast.makeText(this, "❌ Por favor, insira a descrição", Toast.LENGTH_SHORT).show();
            etDescricao.requestFocus();
            return;
        }

        if (dataStr.isEmpty()) {
            Toast.makeText(this, "❌ Por favor, selecione a data", Toast.LENGTH_SHORT).show();
            return;
        }

        // Normalização do valor (vírgula para ponto)
        valorStr = valorStr.replace(",", ".");

        double valor;
        try {
            valor = Double.parseDouble(valorStr);
            if (valor <= 0) {
                Toast.makeText(this, "❌ O valor deve ser maior que zero", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "❌ Valor inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Converte data de dd/MM/yyyy para yyyy-MM-dd
        String dataFormatoBanco = converterDataParaBanco(dataStr);

        // Mostra loading
        mostrarLoading(true);

        // Cria objeto Despesa
        Despesa despesa = new Despesa(valor, descricao, cartao, dataFormatoBanco);

        // Insere no banco
        long id = dbHelper.inserirDespesa(despesa);

        mostrarLoading(false);

        if (id != -1) {
            Toast.makeText(this, "✅ Despesa salva com sucesso!", Toast.LENGTH_SHORT).show();
            limparCamposCadastro();
            carregarTodasDespesas();
        } else {
            Toast.makeText(this, "❌ Erro ao salvar despesa", Toast.LENGTH_SHORT).show();
        }
    }

    private void limparCamposCadastro() {
        etValor.setText("");
        etDescricao.setText("");
        etDataCadastro.setText("");
        spCartaoCadastro.setSelection(0);
        etValor.requestFocus();
    }

    // ==================== BUSCA E FILTROS ====================

    private void carregarTodasDespesas() {
        mostrarLoading(true);

        listaDespesasCompleta = dbHelper.buscarTodasDespesas();
        listaDespesasFiltrada = new ArrayList<>(listaDespesasCompleta);

        mostrarLoading(false);

        atualizarResultados();
    }

    private void buscarDespesas() {
        String valorBusca = etValorBusca.getText().toString().trim();
        String descricaoBusca = etDescricaoBusca.getText().toString().trim();
        String cartaoBusca = spCartaoBusca.getSelectedItem().toString();
        String dataInicialStr = etDataInicial.getText().toString().trim();
        String dataFinalStr = etDataFinal.getText().toString().trim();

        // Converte datas para formato do banco
        String dataInicial = dataInicialStr.isEmpty() ? null : converterDataParaBanco(dataInicialStr);
        String dataFinal = dataFinalStr.isEmpty() ? null : converterDataParaBanco(dataFinalStr);

        mostrarLoading(true);

        // Busca com filtros
        listaDespesasFiltrada = dbHelper.buscarDespesasComFiltros(
                valorBusca.isEmpty() ? null : valorBusca,
                descricaoBusca.isEmpty() ? null : descricaoBusca,
                cartaoBusca,
                dataInicial,
                dataFinal
        );

        mostrarLoading(false);

        atualizarResultados();

        String mensagem = listaDespesasFiltrada.isEmpty()
                ? "😊 Nenhuma despesa encontrada com esses filtros"
                : "✅ " + listaDespesasFiltrada.size() + " despesa(s) encontrada(s)";

        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show();
    }

    private void limparFiltros() {
        etValorBusca.setText("");
        etDescricaoBusca.setText("");
        etDataInicial.setText("");
        etDataFinal.setText("");
        spCartaoBusca.setSelection(0);

        carregarTodasDespesas();
        Toast.makeText(this, "🗑️ Filtros limpos", Toast.LENGTH_SHORT).show();
    }

    // ==================== PAGINAÇÃO ====================

    private void atualizarResultados() {
        paginaAtual = 0;

        if (listaDespesasFiltrada.isEmpty()) {
            tvMensagemVazia.setVisibility(View.VISIBLE);
            recyclerResultados.setVisibility(View.GONE);
            tvTotal.setText("Total: R$ 0,00");
            tvPaginacao.setText("Página 0 de 0");
            btnPaginaAnterior.setEnabled(false);
            btnProximaPagina.setEnabled(false);
            return;
        }

        tvMensagemVazia.setVisibility(View.GONE);
        recyclerResultados.setVisibility(View.VISIBLE);

        // Calcula total de páginas
        totalPaginas = (int) Math.ceil((double) listaDespesasFiltrada.size() / ITENS_POR_PAGINA);

        // Atualiza total
        double total = dbHelper.calcularTotal(listaDespesasFiltrada);
        tvTotal.setText(String.format("Total: R$ %,.2f", total).replace(",", "X").replace(".", ",").replace("X", "."));

        // Atualiza página
        atualizarPagina();
    }

    private void atualizarPagina() {
        int inicio = paginaAtual * ITENS_POR_PAGINA;
        int fim = Math.min(inicio + ITENS_POR_PAGINA, listaDespesasFiltrada.size());

        List<Despesa> paginaAtualLista = listaDespesasFiltrada.subList(inicio, fim);
        adapter.atualizarLista(paginaAtualLista);

        // Atualiza texto de paginação
        tvPaginacao.setText(String.format("Página %d de %d", paginaAtual + 1, totalPaginas));

        // Habilita/desabilita botões
        btnPaginaAnterior.setEnabled(paginaAtual > 0);
        btnProximaPagina.setEnabled(paginaAtual < totalPaginas - 1);
    }

    private void proximaPagina() {
        if (paginaAtual < totalPaginas - 1) {
            paginaAtual++;
            atualizarPagina();
        }
    }

    private void paginaAnterior() {
        if (paginaAtual > 0) {
            paginaAtual--;
            atualizarPagina();
        }
    }

    // ==================== EXPORTAÇÃO EXCEL ====================

    private void exportarParaExcel() {
        if (listaDespesasFiltrada.isEmpty()) {
            Toast.makeText(this, "❌ Não há dados para exportar", Toast.LENGTH_SHORT).show();
            return;
        }

        mostrarLoading(true);

        try {
            // Cria workbook
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Despesas");

            // Estilos
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle totalStyle = workbook.createCellStyle();
            Font totalFont = workbook.createFont();
            totalFont.setBold(true);
            totalStyle.setFont(totalFont);

            // Cabeçalho
            Row headerRow = sheet.createRow(0);
            String[] colunas = {"ID", "Valor", "Descrição", "Cartão", "Data"};

            for (int i = 0; i < colunas.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(colunas[i]);
                cell.setCellStyle(headerStyle);
            }

            // Dados
            int rowNum = 1;
            for (Despesa despesa : listaDespesasFiltrada) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(despesa.getId());
                row.createCell(1).setCellValue(despesa.getValor());
                row.createCell(2).setCellValue(despesa.getDescricao());
                row.createCell(3).setCellValue(despesa.getCartao());
                row.createCell(4).setCellValue(despesa.getDataFormatada());
            }

            // Total
            Row totalRow = sheet.createRow(rowNum);
            Cell totalLabelCell = totalRow.createCell(0);
            totalLabelCell.setCellValue("TOTAL:");
            totalLabelCell.setCellStyle(totalStyle);

            Cell totalValueCell = totalRow.createCell(1);
            double total = dbHelper.calcularTotal(listaDespesasFiltrada);
            totalValueCell.setCellValue(total);
            totalValueCell.setCellStyle(totalStyle);

            // old Ajusta largura das colunas
            // for (int i = 0; i < colunas.length; i++) {
                //sheet.autoSizeColumn(i);
            //}

            // new Ajusta largura das colunas
            sheet.setColumnWidth(0, 20 * 256);
            sheet.setColumnWidth(1, 15 * 256);
            sheet.setColumnWidth(2, 18 * 256);
            sheet.setColumnWidth(3, 18 * 256);
            sheet.setColumnWidth(4, 18 * 256);

            // Salva arquivo
            salvarArquivoExcel(workbook);

        } catch (Exception e) {
            Log.e(TAG, "Erro ao exportar Excel", e);
            mostrarLoading(false);
            Toast.makeText(this, "❌ Erro ao exportar: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void salvarArquivoExcel(Workbook workbook) {
        try {
            // Nome do arquivo com data atual
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String nomeArquivo = "relatorio_" + sdf.format(Calendar.getInstance().getTime()) + ".xlsx";

            // Para Android 10+ (Q) usa MediaStore
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, nomeArquivo);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

                if (uri != null) {
                    OutputStream outputStream = getContentResolver().openOutputStream(uri);
                    workbook.write(outputStream);
                    outputStream.close();
                    workbook.close();

                    mostrarLoading(false);
                    Toast.makeText(this, "✅ Arquivo salvo em Downloads: " + nomeArquivo, Toast.LENGTH_LONG).show();
                }
            } else {
                // Para versões antigas do Android
                Toast.makeText(this, "❌ Versão do Android não suportada para exportação", Toast.LENGTH_LONG).show();
                mostrarLoading(false);
            }

        } catch (Exception e) {
            Log.e(TAG, "Erro ao salvar arquivo", e);
            mostrarLoading(false);
            Toast.makeText(this, "❌ Erro ao salvar arquivo: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private String converterDataParaBanco(String dataBrasileira) {
        try {
            // Converte dd/MM/yyyy para yyyy-MM-dd
            SimpleDateFormat sdfBr = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));
            SimpleDateFormat sdfBanco = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return sdfBanco.format(sdfBr.parse(dataBrasileira));
        } catch (Exception e) {
            Log.e(TAG, "Erro ao converter data", e);
            return "";
        }
    }

    private void mostrarLoading(boolean mostrar) {
        progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}