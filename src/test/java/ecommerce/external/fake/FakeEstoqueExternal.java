package ecommerce.external.fake;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ecommerce.dto.DisponibilidadeDTO;
import ecommerce.dto.EstoqueBaixaDTO;
import ecommerce.external.IEstoqueExternal;

public class FakeEstoqueExternal implements IEstoqueExternal {

    private Map<Long, Long> estoque = new HashMap<>();
    private boolean shouldFailBaixa = false;

    public void setEstoque(Long produtoId, Long quantidade) {
        estoque.put(produtoId, quantidade);
    }

    public Long getQuantidade(Long produtoId) {
        return estoque.getOrDefault(produtoId, 0L);
    }
    
    public void setShouldFailBaixa(boolean shouldFail) {
        this.shouldFailBaixa = shouldFail;
    }

    @Override
    public DisponibilidadeDTO verificarDisponibilidade(List<Long> produtosIds, List<Long> produtosQuantidades) {
        for (int i = 0; i < produtosIds.size(); i++) {
            Long id = produtosIds.get(i);
            Long qtd = produtosQuantidades.get(i);
            
            if (!estoque.containsKey(id) || estoque.get(id) < qtd) {
                return new DisponibilidadeDTO(false, List.of(id));
            }
        }
        return new DisponibilidadeDTO(true, List.of());
    }

    @Override
    public EstoqueBaixaDTO darBaixa(List<Long> produtosIds, List<Long> produtosQuantidades) {
        if (shouldFailBaixa) {
            return new EstoqueBaixaDTO(false);
        }
        
        // Check availability again to be safe, though not strictly required by interface contract if called after verify
        for (int i = 0; i < produtosIds.size(); i++) {
            Long id = produtosIds.get(i);
            Long qtd = produtosQuantidades.get(i);
             if (!estoque.containsKey(id) || estoque.get(id) < qtd) {
                return new EstoqueBaixaDTO(false);
            }
        }

        // Deduct
        for (int i = 0; i < produtosIds.size(); i++) {
            Long id = produtosIds.get(i);
            Long qtd = produtosQuantidades.get(i);
            estoque.put(id, estoque.get(id) - qtd);
        }

        return new EstoqueBaixaDTO(true);
    }
}
