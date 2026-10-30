package ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ecommerce.dto.CompraDTO;
import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.Cliente;
import ecommerce.entity.ItemCompra;
import ecommerce.entity.Produto;
import ecommerce.entity.Regiao;
import ecommerce.entity.TipoCliente;
import ecommerce.external.fake.FakeEstoqueExternal;
import ecommerce.external.fake.FakePagamentoExternal;

class CompraServiceFinalizarCompraCenario1Test {

    private CompraService compraService;
    private CarrinhoDeComprasService carrinhoServiceMock;
    private ClienteService clienteServiceMock;
    private FakeEstoqueExternal estoqueExternalFake;
    private FakePagamentoExternal pagamentoExternalFake;

    private Cliente cliente;
    private CarrinhoDeCompras carrinho;
    private Produto produto;

    @BeforeEach
    void setUp() {
        carrinhoServiceMock = mock(CarrinhoDeComprasService.class);
        clienteServiceMock = mock(ClienteService.class);
        estoqueExternalFake = new FakeEstoqueExternal();
        pagamentoExternalFake = new FakePagamentoExternal();

        compraService = new CompraService(carrinhoServiceMock, clienteServiceMock, estoqueExternalFake, pagamentoExternalFake);

        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setRegiao(Regiao.SUL);
        cliente.setTipo(TipoCliente.OURO);

        produto = new Produto();
        produto.setId(10L);
        produto.setPreco(new BigDecimal("100.00"));
        produto.setPesoFisico(new BigDecimal("1.00"));
        produto.setFragil(false);

        ItemCompra item = new ItemCompra();
        item.setProduto(produto);
        item.setQuantidade(2L);

        carrinho = new CarrinhoDeCompras();
        carrinho.setId(1L);
        carrinho.setCliente(cliente);
        carrinho.getItens().add(item);

        when(clienteServiceMock.buscarPorId(1L)).thenReturn(cliente);
        when(carrinhoServiceMock.buscarPorCarrinhoIdEClienteId(1L, cliente)).thenReturn(carrinho);
    }

    @Test
    void testFinalizarCompraSucesso() {
        estoqueExternalFake.setEstoque(10L, 10L);
        
        CompraDTO result = compraService.finalizarCompra(1L, 1L);
        
        assertTrue(result.sucesso());
        assertEquals("Compra finalizada com sucesso.", result.mensagem());
        assertEquals(1L, result.transacaoPagamentoId());
        assertEquals(8L, estoqueExternalFake.getQuantidade(10L));
    }

    @Test
    void testFinalizarCompraSemEstoque() {
        estoqueExternalFake.setEstoque(10L, 1L); // Only 1 available, need 2

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            compraService.finalizarCompra(1L, 1L);
        });

        assertEquals("Itens fora de estoque.", exception.getMessage());
    }

    @Test
    void testFinalizarCompraPagamentoNaoAutorizado() {
        estoqueExternalFake.setEstoque(10L, 10L);
        pagamentoExternalFake.setShouldAuthorize(false);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            compraService.finalizarCompra(1L, 1L);
        });

        assertEquals("Pagamento não autorizado.", exception.getMessage());
    }

    @Test
    void testFinalizarCompraErroBaixaEstoque() {
        estoqueExternalFake.setEstoque(10L, 10L);
        estoqueExternalFake.setShouldFailBaixa(true);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            compraService.finalizarCompra(1L, 1L);
        });

        assertEquals("Erro ao dar baixa no estoque.", exception.getMessage());
        // Verify payment cancellation
        // Since we don't have a way to verify calls on the fake easily without adding inspection methods, 
        // let's assume the fake logic is correct or add inspection to fake.
        // I added isTransactionCancelled to FakePagamentoExternal.
        // But I need the transaction ID. The fake generates IDs starting from 1.
        // So the first transaction should be 1.
        assertTrue(pagamentoExternalFake.isTransactionCancelled(1L));
    }

    @Test
    void testFinalizarCompraItensDuplicados() {
        // Product 10, Stock 1.
        // Cart has 2 items of Product 10, Qty 1 each. Total needed 2.
        estoqueExternalFake.setEstoque(10L, 1L);
        
        // Add another item of same product to cart
        ItemCompra item2 = new ItemCompra();
        item2.setProduto(produto);
        item2.setQuantidade(1L);
        carrinho.getItens().add(item2);
        
        // Should fail because total needed (2) > stock (1)
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            compraService.finalizarCompra(1L, 1L);
        });

        assertEquals("Itens fora de estoque.", exception.getMessage());
    }
}
