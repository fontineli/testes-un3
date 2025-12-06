package ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.Cliente;
import ecommerce.entity.ItemCompra;
import ecommerce.entity.Produto;
import ecommerce.entity.Regiao;
import ecommerce.entity.TipoCliente;
import ecommerce.external.IEstoqueExternal;
import ecommerce.external.IPagamentoExternal;

class CompraServiceTest {

    private CompraService compraService;
    private CarrinhoDeCompras carrinho;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        CarrinhoDeComprasService carrinhoService = mock(CarrinhoDeComprasService.class);
        ClienteService clienteService = mock(ClienteService.class);
        IEstoqueExternal estoqueExternal = mock(IEstoqueExternal.class);
        IPagamentoExternal pagamentoExternal = mock(IPagamentoExternal.class);

        compraService = new CompraService(carrinhoService, clienteService, estoqueExternal, pagamentoExternal);
        
        carrinho = new CarrinhoDeCompras();
        cliente = new Cliente();
        cliente.setRegiao(Regiao.NORDESTE); // Irrelevant for new logic but required by signature
        cliente.setTipo(TipoCliente.BRONZE); // Irrelevant for new logic but required by signature
    }

    private Produto createProduto(BigDecimal preco, BigDecimal peso, boolean fragil) {
        Produto p = new Produto();
        p.setPreco(preco);
        p.setPesoFisico(peso);
        p.setFragil(fragil);
        return p;
    }

    private void addItem(Produto p, Long quantidade) {
        ItemCompra item = new ItemCompra();
        item.setProduto(p);
        item.setQuantidade(quantidade);
        carrinho.getItens().add(item);
    }

    @Test
    void testSemDescontoSemFreteSemFragil() {
        // Total < 500 (400), Peso <= 5 (4kg), Not Fragile
        // Cost: 400
        // Discount: 0
        // Freight: 0
        // Total: 400.00
        addItem(createProduto(new BigDecimal("100.00"), new BigDecimal("1.00"), false), 4L);

        BigDecimal result = compraService.calcularCustoTotal(carrinho, cliente.getRegiao(), cliente.getTipo());
        assertEquals(new BigDecimal("400.00"), result);
    }

    @Test
    void testDesconto10PorcentoSemFrete() {
        // Total >= 500 < 1000 (500), Peso <= 5 (5kg)
        // Cost: 500
        // Discount: 50 (10%) -> 450
        // Freight: 0
        // Total: 450.00
        addItem(createProduto(new BigDecimal("100.00"), new BigDecimal("1.00"), false), 5L);

        BigDecimal result = compraService.calcularCustoTotal(carrinho, cliente.getRegiao(), cliente.getTipo());
        assertEquals(new BigDecimal("450.00"), result);
    }

    @Test
    void testDesconto20PorcentoSemFrete() {
        // Total >= 1000 (1000), Peso <= 5 (2kg)
        // Cost: 1000
        // Discount: 200 (20%) -> 800
        // Freight: 0
        // Total: 800.00
        addItem(createProduto(new BigDecimal("500.00"), new BigDecimal("1.00"), false), 2L);

        BigDecimal result = compraService.calcularCustoTotal(carrinho, cliente.getRegiao(), cliente.getTipo());
        assertEquals(new BigDecimal("800.00"), result);
    }

    @Test
    void testFreteFaixaB() {
        // Total < 500 (100), Peso > 5 <= 10 (6kg)
        // Cost: 100
        // Discount: 0
        // Freight: 6 * 2.00 = 12.00
        // Total: 112.00
        addItem(createProduto(new BigDecimal("100.00"), new BigDecimal("6.00"), false), 1L);

        BigDecimal result = compraService.calcularCustoTotal(carrinho, cliente.getRegiao(), cliente.getTipo());
        assertEquals(new BigDecimal("112.00"), result);
    }

    @Test
    void testFreteFaixaC() {
        // Total < 500 (100), Peso > 10 <= 50 (11kg)
        // Cost: 100
        // Discount: 0
        // Freight: 11 * 4.00 = 44.00
        // Total: 144.00
        addItem(createProduto(new BigDecimal("100.00"), new BigDecimal("11.00"), false), 1L);

        BigDecimal result = compraService.calcularCustoTotal(carrinho, cliente.getRegiao(), cliente.getTipo());
        assertEquals(new BigDecimal("144.00"), result);
    }

    @Test
    void testFreteFaixaD() {
        // Total < 500 (100), Peso > 50 (51kg)
        // Cost: 100
        // Discount: 0
        // Freight: 51 * 7.00 = 357.00
        // Total: 457.00
        addItem(createProduto(new BigDecimal("100.00"), new BigDecimal("51.00"), false), 1L);

        BigDecimal result = compraService.calcularCustoTotal(carrinho, cliente.getRegiao(), cliente.getTipo());
        assertEquals(new BigDecimal("457.00"), result);
    }

    @Test
    void testProdutoFragil() {
        // Total < 500 (100), Peso <= 5 (1kg), Fragile
        // Cost: 100 + 5 (fragile) = 105
        // Discount: 0 (based on total cost? Logic says discount on total value. 
        // Wait, logic says: "1. Subtotal = soma do preço unitário × quantidade." -> Fragile added here?
        // Re-reading logic:
        // 1. Subtotal = soma do preço unitário × quantidade.
        // 2. Aplicar o desconto conforme a regra.
        // 3. Calcular o frete conforme a tabela.
        // 4. Total = subtotalComDesconto + frete.
        // 5. Arredondar o valor final para duas casas decimais.
        // Observation: "Produtos frágeis: adicionar R$ 5,00 por unidade frágil."
        // Where is this added? Usually to the subtotal or freight.
        // My implementation added it to 'total' inside the loop, so it IS part of the subtotal used for discount.
        // Let's verify if that's correct. "Subtotal = soma do preço unitário × quantidade."
        // Then "Produtos frágeis: adicionar R$ 5,00 por unidade frágil."
        // If it's an additional cost, it likely contributes to the base price for discount or is a fee.
        // Given "1. Subtotal...", if fragile fee is part of the product cost, it should be in subtotal.
        // Let's assume it is part of the subtotal before discount.
        
        // Test: 1 item, 100.00, fragile.
        // Subtotal: 100 + 5 = 105.
        // Discount: 0.
        // Freight: 0.
        // Total: 105.00.
        addItem(createProduto(new BigDecimal("100.00"), new BigDecimal("1.00"), true), 1L);

        BigDecimal result = compraService.calcularCustoTotal(carrinho, cliente.getRegiao(), cliente.getTipo());
        assertEquals(new BigDecimal("105.00"), result);
    }
    
    @Test
    void testProdutoFragilComDesconto() {
        // 10 items, 100.00 each, fragile.
        // Subtotal: (100 * 10) + (5 * 10) = 1000 + 50 = 1050.
        // Discount: >= 1000 -> 20%. 1050 * 0.20 = 210.
        // Total after discount: 1050 - 210 = 840.
        // Freight: 10 * 1kg = 10kg -> Faixa B (>5 <=10) -> 2.00/kg.
        // Freight cost: 10 * 2.00 = 20.00.
        // Final Total: 840 + 20 = 860.00.
        addItem(createProduto(new BigDecimal("100.00"), new BigDecimal("1.00"), true), 10L);
        
        BigDecimal result = compraService.calcularCustoTotal(carrinho, cliente.getRegiao(), cliente.getTipo());
        assertEquals(new BigDecimal("860.00"), result);
    }

    @Test
    void testFreteLimite5kg() {
        // Total < 500, Peso = 5kg
        // Freight: 0 (<= 5)
        addItem(createProduto(new BigDecimal("100.00"), new BigDecimal("5.00"), false), 1L);
        BigDecimal result = compraService.calcularCustoTotal(carrinho, cliente.getRegiao(), cliente.getTipo());
        assertEquals(new BigDecimal("100.00"), result);
    }

    @Test
    void testFreteLimite10kg() {
        // Total < 500, Peso = 10kg
        // Freight: 10 * 2.00 = 20.00 (> 5 <= 10)
        addItem(createProduto(new BigDecimal("100.00"), new BigDecimal("10.00"), false), 1L);
        BigDecimal result = compraService.calcularCustoTotal(carrinho, cliente.getRegiao(), cliente.getTipo());
        assertEquals(new BigDecimal("120.00"), result);
    }

    @Test
    void testFreteLimite50kg() {
        // Total < 500, Peso = 50kg
        // Freight: 50 * 4.00 = 200.00 (> 10 <= 50)
        addItem(createProduto(new BigDecimal("100.00"), new BigDecimal("50.00"), false), 1L);
        BigDecimal result = compraService.calcularCustoTotal(carrinho, cliente.getRegiao(), cliente.getTipo());
        assertEquals(new BigDecimal("300.00"), result);
    }

    @Test
    void testArredondamento() {
        // Price 10.123, Qty 1
        // Total 10.123
        // Discount 0
        // Freight 0 (assuming weight 0 or small)
        // Result should be 10.12
        addItem(createProduto(new BigDecimal("10.123"), new BigDecimal("1.00"), false), 1L);
        BigDecimal result = compraService.calcularCustoTotal(carrinho, cliente.getRegiao(), cliente.getTipo());
        assertEquals(new BigDecimal("10.12"), result);
    }

    @Test
    void testArredondamentoHalfUp() {
        // Price 10.125, Qty 1
        // Result should be 10.13
        addItem(createProduto(new BigDecimal("10.125"), new BigDecimal("1.00"), false), 1L);
        BigDecimal result = compraService.calcularCustoTotal(carrinho, cliente.getRegiao(), cliente.getTipo());
        assertEquals(new BigDecimal("10.13"), result);
    }

    @Test
    void testProdutoFragilNull() {
        // Fragil is null. Should be treated as false.
        Produto p = createProduto(new BigDecimal("100.00"), new BigDecimal("1.00"), false);
        p.setFragil(null);
        addItem(p, 1L);
        
        BigDecimal result = compraService.calcularCustoTotal(carrinho, cliente.getRegiao(), cliente.getTipo());
        assertEquals(new BigDecimal("100.00"), result);
    }

    @Test
    void testItensDuplicados() {
        // Same product added twice.
        // Product A: 100.00, 1kg.
        // Item 1: Qty 1.
        // Item 2: Qty 1.
        // Total: 200.00.
        // Weight: 2kg.
        // Discount: 0.
        // Freight: 0.
        Produto p = createProduto(new BigDecimal("100.00"), new BigDecimal("1.00"), false);
        p.setId(1L); // Ensure ID is set for equality check if needed, though we use object ref in addItem
        
        addItem(p, 1L);
        addItem(p, 1L);
        
        BigDecimal result = compraService.calcularCustoTotal(carrinho, cliente.getRegiao(), cliente.getTipo());
        assertEquals(new BigDecimal("200.00"), result);
    }
}
