package ecommerce.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ecommerce.dto.CompraDTO;
import ecommerce.dto.DisponibilidadeDTO;
import ecommerce.dto.EstoqueBaixaDTO;
import ecommerce.dto.PagamentoDTO;
import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.Cliente;
import ecommerce.entity.Regiao;
import ecommerce.entity.TipoCliente;
import ecommerce.external.IEstoqueExternal;
import ecommerce.external.IPagamentoExternal;
import jakarta.transaction.Transactional;

@Service
public class CompraService
{

	private final CarrinhoDeComprasService carrinhoService;
	private final ClienteService clienteService;

	private final IEstoqueExternal estoqueExternal;
	private final IPagamentoExternal pagamentoExternal;

	@Autowired
	public CompraService(CarrinhoDeComprasService carrinhoService, ClienteService clienteService,
			IEstoqueExternal estoqueExternal, IPagamentoExternal pagamentoExternal)
	{
		this.carrinhoService = carrinhoService;
		this.clienteService = clienteService;

		this.estoqueExternal = estoqueExternal;
		this.pagamentoExternal = pagamentoExternal;
	}

	@Transactional
	public CompraDTO finalizarCompra(Long carrinhoId, Long clienteId)
	{
		Cliente cliente = clienteService.buscarPorId(clienteId);
		CarrinhoDeCompras carrinho = carrinhoService.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente);

		List<Long> produtosIds = carrinho.getItens().stream().map(i -> i.getProduto().getId())
				.collect(Collectors.toList());
		List<Long> produtosQtds = carrinho.getItens().stream().map(i -> i.getQuantidade()).collect(Collectors.toList());

		DisponibilidadeDTO disponibilidade = estoqueExternal.verificarDisponibilidade(produtosIds, produtosQtds);

		if (!disponibilidade.disponivel())
		{
			throw new IllegalStateException("Itens fora de estoque.");
		}

		BigDecimal custoTotal = calcularCustoTotal(carrinho, cliente.getRegiao(), cliente.getTipo());

		PagamentoDTO pagamento = pagamentoExternal.autorizarPagamento(cliente.getId(), custoTotal.doubleValue());

		if (!pagamento.autorizado())
		{
			throw new IllegalStateException("Pagamento não autorizado.");
		}

		EstoqueBaixaDTO baixaDTO = estoqueExternal.darBaixa(produtosIds, produtosQtds);

		if (!baixaDTO.sucesso())
		{
			pagamentoExternal.cancelarPagamento(cliente.getId(), pagamento.transacaoId());
			throw new IllegalStateException("Erro ao dar baixa no estoque.");
		}

		CompraDTO compraDTO = new CompraDTO(true, pagamento.transacaoId(), "Compra finalizada com sucesso.");

		return compraDTO;
	} 

	public BigDecimal calcularCustoTotal(CarrinhoDeCompras carrinho, Regiao regiao, TipoCliente tipoCliente)
	{
		BigDecimal total = BigDecimal.ZERO;
		BigDecimal pesoTotal = BigDecimal.ZERO;

		for (ecommerce.entity.ItemCompra item : carrinho.getItens())
		{
			BigDecimal precoItem = item.getProduto().getPreco();
			BigDecimal quantidade = new BigDecimal(item.getQuantidade());
			total = total.add(precoItem.multiply(quantidade));
			
			BigDecimal pesoItem = item.getProduto().getPesoFisico();
			pesoTotal = pesoTotal.add(pesoItem.multiply(quantidade));
			
			if (Boolean.TRUE.equals(item.getProduto().isFragil())) {
				total = total.add(new BigDecimal("5.00").multiply(quantidade));
			}
		}

		BigDecimal desconto = BigDecimal.ZERO;
		if (total.compareTo(new BigDecimal("1000.00")) >= 0)
		{
			desconto = total.multiply(new BigDecimal("0.20"));
		}
		else if (total.compareTo(new BigDecimal("500.00")) >= 0)
		{
			desconto = total.multiply(new BigDecimal("0.10"));
		}

		total = total.subtract(desconto);

		BigDecimal frete = BigDecimal.ZERO;
		if (pesoTotal.compareTo(new BigDecimal("50.00")) > 0)
		{
			frete = pesoTotal.multiply(new BigDecimal("7.00"));
		}
		else if (pesoTotal.compareTo(new BigDecimal("10.00")) > 0)
		{
			frete = pesoTotal.multiply(new BigDecimal("4.00"));
		}
		else if (pesoTotal.compareTo(new BigDecimal("5.00")) > 0)
		{
			frete = pesoTotal.multiply(new BigDecimal("2.00"));
		}

		total = total.add(frete);

		return total.setScale(2, java.math.RoundingMode.HALF_UP);
	}
}
