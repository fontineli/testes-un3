package ecommerce.external.fake;

import java.util.HashSet;
import java.util.Set;

import ecommerce.dto.PagamentoDTO;
import ecommerce.external.IPagamentoExternal;

public class FakePagamentoExternal implements IPagamentoExternal {

    private boolean shouldAuthorize = true;
    private Set<Long> cancelledTransactions = new HashSet<>();
    private long nextTransacaoId = 1;

    public void setShouldAuthorize(boolean shouldAuthorize) {
        this.shouldAuthorize = shouldAuthorize;
    }

    public boolean isTransactionCancelled(Long transacaoId) {
        return cancelledTransactions.contains(transacaoId);
    }

    @Override
    public PagamentoDTO autorizarPagamento(Long clienteId, Double custoTotal) {
        if (shouldAuthorize) {
            return new PagamentoDTO(true, nextTransacaoId++);
        } else {
            return new PagamentoDTO(false, null);
        }
    }

    @Override
    public void cancelarPagamento(Long clienteId, Long pagamentoTransacaoId) {
        cancelledTransactions.add(pagamentoTransacaoId);
    }
}
