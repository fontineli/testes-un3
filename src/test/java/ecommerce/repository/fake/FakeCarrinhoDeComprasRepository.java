package ecommerce.repository.fake;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;

import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.Cliente;
import ecommerce.repository.CarrinhoDeComprasRepository;

public class FakeCarrinhoDeComprasRepository implements CarrinhoDeComprasRepository {

    private List<CarrinhoDeCompras> carrinhos = new ArrayList<>();

    public void saveCarrinho(CarrinhoDeCompras carrinho) {
        carrinhos.add(carrinho);
    }

    @Override
    public Optional<CarrinhoDeCompras> findByIdAndCliente(Long id, Cliente cliente) {
        return carrinhos.stream()
                .filter(c -> c.getId().equals(id) && c.getCliente().getId().equals(cliente.getId()))
                .findFirst();
    }

    // Unimplemented methods required by JpaRepository interface
    @Override
    public void flush() {}
    @Override
    public <S extends CarrinhoDeCompras> S saveAndFlush(S entity) { return null; }
    @Override
    public <S extends CarrinhoDeCompras> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
    @Override
    public void deleteAllInBatch(Iterable<CarrinhoDeCompras> entities) {}
    @Override
    public void deleteAllByIdInBatch(Iterable<Long> ids) {}
    @Override
    public void deleteAllInBatch() {}
    @Override
    public CarrinhoDeCompras getOne(Long id) { return null; }
    @Override
    public CarrinhoDeCompras getById(Long id) { return null; }
    @Override
    public CarrinhoDeCompras getReferenceById(Long id) { return null; }
    @Override
    public <S extends CarrinhoDeCompras> List<S> findAll(Example<S> example) { return null; }
    @Override
    public <S extends CarrinhoDeCompras> List<S> findAll(Example<S> example, Sort sort) { return null; }
    @Override
    public <S extends CarrinhoDeCompras> List<S> saveAll(Iterable<S> entities) { return null; }
    @Override
    public List<CarrinhoDeCompras> findAll() { return null; }
    @Override
    public List<CarrinhoDeCompras> findAllById(Iterable<Long> ids) { return null; }
    @Override
    public <S extends CarrinhoDeCompras> S save(S entity) { return null; }
    @Override
    public Optional<CarrinhoDeCompras> findById(Long id) { return null; }
    @Override
    public boolean existsById(Long id) { return false; }
    @Override
    public long count() { return 0; }
    @Override
    public void deleteById(Long id) {}
    @Override
    public void delete(CarrinhoDeCompras entity) {}
    @Override
    public void deleteAllById(Iterable<? extends Long> ids) {}
    @Override
    public void deleteAll(Iterable<? extends CarrinhoDeCompras> entities) {}
    @Override
    public void deleteAll() {}
    @Override
    public List<CarrinhoDeCompras> findAll(Sort sort) { return null; }
    @Override
    public Page<CarrinhoDeCompras> findAll(Pageable pageable) { return null; }
    @Override
    public <S extends CarrinhoDeCompras> Optional<S> findOne(Example<S> example) { return null; }
    @Override
    public <S extends CarrinhoDeCompras> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
    @Override
    public <S extends CarrinhoDeCompras> long count(Example<S> example) { return 0; }
    @Override
    public <S extends CarrinhoDeCompras> boolean exists(Example<S> example) { return false; }
    @Override
    public <S extends CarrinhoDeCompras, R> R findBy(Example<S> example, Function<FetchableFluentQuery<S>, R> queryFunction) { return null; }
}
