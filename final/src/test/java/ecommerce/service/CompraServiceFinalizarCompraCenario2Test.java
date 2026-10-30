package ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ecommerce.dto.CompraDTO;
import ecommerce.dto.DisponibilidadeDTO;
import ecommerce.dto.EstoqueBaixaDTO;
import ecommerce.dto.PagamentoDTO;
import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.Cliente;
import ecommerce.entity.ItemCompra;
import ecommerce.entity.Produto;
import ecommerce.entity.Regiao;
import ecommerce.entity.TipoCliente;
import ecommerce.external.IEstoqueExternal;
import ecommerce.external.IPagamentoExternal;
import ecommerce.repository.fake.FakeCarrinhoDeComprasRepository;
import ecommerce.repository.fake.FakeClienteRepository;

class CompraServiceFinalizarCompraCenario2Test {

    private CompraService compraService;
    private CarrinhoDeComprasService carrinhoService;
    private ClienteService clienteService;
    private IEstoqueExternal estoqueExternalMock;
    private IPagamentoExternal pagamentoExternalMock;
    
    private FakeCarrinhoDeComprasRepository carrinhoRepositoryFake;
    private FakeClienteRepository clienteRepositoryFake;

    private Cliente cliente;
    private CarrinhoDeCompras carrinho;
    private Produto produto;

    @BeforeEach
    void setUp() {
        carrinhoRepositoryFake = new FakeCarrinhoDeComprasRepository();
        clienteRepositoryFake = new FakeClienteRepository();
        
        carrinhoService = new CarrinhoDeComprasService(carrinhoRepositoryFake);
        clienteService = new ClienteService(clienteRepositoryFake);
        
        estoqueExternalMock = mock(IEstoqueExternal.class);
        pagamentoExternalMock = mock(IPagamentoExternal.class);

        compraService = new CompraService(carrinhoService, clienteService, estoqueExternalMock, pagamentoExternalMock);

        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setRegiao(Regiao.SUL);
        cliente.setTipo(TipoCliente.OURO);
        clienteRepositoryFake.saveCliente(cliente);

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
        carrinhoRepositoryFake.saveCarrinho(carrinho);
    }

    @Test
    void testFinalizarCompraSucesso() {
        when(estoqueExternalMock.verificarDisponibilidade(eq(List.of(10L)), eq(List.of(2L))))
            .thenReturn(new DisponibilidadeDTO(true, List.of()));
        
        when(pagamentoExternalMock.autorizarPagamento(eq(1L), any(Double.class)))
            .thenReturn(new PagamentoDTO(true, 123L));
            
        when(estoqueExternalMock.darBaixa(eq(List.of(10L)), eq(List.of(2L))))
            .thenReturn(new EstoqueBaixaDTO(true));

        CompraDTO result = compraService.finalizarCompra(1L, 1L);
        
        assertTrue(result.sucesso());
        assertEquals("Compra finalizada com sucesso.", result.mensagem());
        assertEquals(123L, result.transacaoPagamentoId());
    }

    @Test
    void testFinalizarCompraSemEstoque() {
        when(estoqueExternalMock.verificarDisponibilidade(anyList(), anyList()))
            .thenReturn(new DisponibilidadeDTO(false, List.of(10L)));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            compraService.finalizarCompra(1L, 1L);
        });

        assertEquals("Itens fora de estoque.", exception.getMessage());
    }

    @Test
    void testFinalizarCompraPagamentoNaoAutorizado() {
        when(estoqueExternalMock.verificarDisponibilidade(anyList(), anyList()))
            .thenReturn(new DisponibilidadeDTO(true, List.of()));
            
        when(pagamentoExternalMock.autorizarPagamento(eq(1L), any(Double.class)))
            .thenReturn(new PagamentoDTO(false, null));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            compraService.finalizarCompra(1L, 1L);
        });

        assertEquals("Pagamento não autorizado.", exception.getMessage());
    }

    @Test
    void testFinalizarCompraErroBaixaEstoque() {
        when(estoqueExternalMock.verificarDisponibilidade(anyList(), anyList()))
            .thenReturn(new DisponibilidadeDTO(true, List.of()));
            
        when(pagamentoExternalMock.autorizarPagamento(eq(1L), any(Double.class)))
            .thenReturn(new PagamentoDTO(true, 123L));
            
        when(estoqueExternalMock.darBaixa(anyList(), anyList()))
            .thenReturn(new EstoqueBaixaDTO(false));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            compraService.finalizarCompra(1L, 1L);
        });

        assertEquals("Erro ao dar baixa no estoque.", exception.getMessage());
        verify(pagamentoExternalMock).cancelarPagamento(1L, 123L);
    }
}
