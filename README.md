# Trabalho da 3ª Unidade – Testes de Mutação e Testes com Dublês

**Autores:** Lulayne e Gabriel Fontineli

## Instruções de Execução

Este projeto utiliza Maven para gerenciamento de dependências e build. Certifique-se de ter o Java 17 e o Maven instalados.

### Como rodar os testes

Para executar todos os testes automatizados (unitários e de integração), execute o seguinte comando na raiz do projeto:

```bash
mvn test
```

### Como visualizar relatórios de cobertura (JaCoCo)

O projeto utiliza o JaCoCo para verificar a cobertura de código. Para gerar o relatório, execute:

```bash
mvn verify
```

O relatório estará disponível em:
`target/site/jacoco/index.html`

Abra este arquivo em um navegador para visualizar a cobertura detalhada (instruções, ramos, linhas, etc.).

### Como gerar e interpretar o relatório de mutação (PITEST)

O projeto utiliza o PITEST para testes de mutação. Para gerar o relatório, execute:

```bash
mvn org.pitest:pitest-maven:mutationCoverage
```

O relatório estará disponível em:
`target/pit-reports/index.html` (ou dentro de uma subpasta com data/hora)

Abra este arquivo em um navegador. O relatório mostrará:
- **Line Coverage**: Porcentagem de linhas cobertas pelos testes.
- **Mutation Coverage**: Porcentagem de mutantes mortos (detectados) pelos testes.
- Lista de classes e mutações geradas, indicando quais foram mortas (KILLED) e quais sobreviveram (SURVIVED).

**Interpretação:**
- **KILLED**: O teste falhou quando a mutação foi aplicada (bom sinal).
- **SURVIVED**: Os testes passaram mesmo com a mutação (sinal de alerta, o código mutado não foi coberto adequadamente ou a mutação é equivalente).
- **NO_COVERAGE**: A linha mutada não foi executada por nenhum teste.

## Estrutura dos Testes

- **Cenário 1 (`CompraServiceFinalizarCompraCenario1Test`)**: Utiliza Fakes para serviços externos (`IEstoqueExternal`, `IPagamentoExternal`) e Mocks para dependências internas.
- **Cenário 2 (`CompraServiceFinalizarCompraCenario2Test`)**: Utiliza Mocks para serviços externos e Fakes para repositórios (`CarrinhoDeComprasRepository`, `ClienteRepository`).
- **Cálculo de Custo (`CompraServiceTest`)**: Testes unitários para o método `calcularCustoTotal` visando 100% de cobertura de arestas e mutação.