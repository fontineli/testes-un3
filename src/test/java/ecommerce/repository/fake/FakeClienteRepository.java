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

import ecommerce.entity.Cliente;
import ecommerce.repository.ClienteRepository;

public class FakeClienteRepository implements ClienteRepository {

    private List<Cliente> clientes = new ArrayList<>();

    public void saveCliente(Cliente cliente) {
        clientes.add(cliente);
    }

    @Override
    public Optional<Cliente> findById(Long id) {
        return clientes.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst();
    }

    // Unimplemented methods required by JpaRepository interface
    @Override
    public void flush() {}
    @Override
    public <S extends Cliente> S saveAndFlush(S entity) { return null; }
    @Override
    public <S extends Cliente> List<S> saveAllAndFlush(Iterable<S> entities) { return null; }
    @Override
    public void deleteAllInBatch(Iterable<Cliente> entities) {}
    @Override
    public void deleteAllByIdInBatch(Iterable<Long> ids) {}
    @Override
    public void deleteAllInBatch() {}
    @Override
    public Cliente getOne(Long id) { return null; }
    @Override
    public Cliente getById(Long id) { return null; }
    @Override
    public Cliente getReferenceById(Long id) { return null; }
    @Override
    public <S extends Cliente> List<S> findAll(Example<S> example) { return null; }
    @Override
    public <S extends Cliente> List<S> findAll(Example<S> example, Sort sort) { return null; }
    @Override
    public <S extends Cliente> List<S> saveAll(Iterable<S> entities) { return null; }
    @Override
    public List<Cliente> findAll() { return null; }
    @Override
    public List<Cliente> findAllById(Iterable<Long> ids) { return null; }
    @Override
    public <S extends Cliente> S save(S entity) { return null; }
    @Override
    public boolean existsById(Long id) { return false; }
    @Override
    public long count() { return 0; }
    @Override
    public void deleteById(Long id) {}
    @Override
    public void delete(Cliente entity) {}
    @Override
    public void deleteAllById(Iterable<? extends Long> ids) {}
    @Override
    public void deleteAll(Iterable<? extends Cliente> entities) {}
    @Override
    public void deleteAll() {}
    @Override
    public List<Cliente> findAll(Sort sort) { return null; }
    @Override
    public Page<Cliente> findAll(Pageable pageable) { return null; }
    @Override
    public <S extends Cliente> Optional<S> findOne(Example<S> example) { return null; }
    @Override
    public <S extends Cliente> Page<S> findAll(Example<S> example, Pageable pageable) { return null; }
    @Override
    public <S extends Cliente> long count(Example<S> example) { return 0; }
    @Override
    public <S extends Cliente> boolean exists(Example<S> example) { return false; }
    @Override
    public <S extends Cliente, R> R findBy(Example<S> example, Function<FetchableFluentQuery<S>, R> queryFunction) { return null; }
}
