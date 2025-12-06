Aqui está o arquivo convertido integralmente para Markdown, mantendo estrutura, títulos, listas e tabelas, exatamente como no PDF enunciado-3a-unidade.pdf  ￼.

⸻

Testes de Software – Prof. Eiji Adachi

Trabalho da 3ª Unidade – Testes de Mutação e Testes com Dublês (Fakes e Mocks)

⸻

1. Contexto

Este trabalho é uma continuação direta do enunciado da 2ª unidade, no qual você testou o método calcularCustoTotal() de uma aplicação de e-commerce.

Nesta etapa, você deverá:
	•	testar uma versão simplificada do cálculo de custo total
	•	criar testes para o método finalizarCompra(), utilizando fakes e mocks

O trabalho pode ser realizado individualmente ou em dupla.

⸻

2. Método Simplificado calcularCustoTotal()

Você deverá implementar novamente o método calcularCustoTotal(), agora usando apenas as regras abaixo.

⸻

2.1 Regra de Desconto

Apenas uma regra: desconto pelo valor total do carrinho.
	•	Se valor total ≥ R$ 1000,00, aplique 20% de desconto.
	•	Se valor total ≥ R$ 500,00 e < R$ 1000,00, aplique 10% de desconto.
	•	Outros valores → sem desconto.

⸻

2.2 Regra de Frete

O peso total é calculado com base apenas no peso físico. A tabela é:

Faixa	Peso total	Valor do frete
A	0–5 kg	frete isento (R$ 0)
B	> 5 kg e ≤ 10 kg	R$ 2,00 por kg
C	> 10 kg e ≤ 50 kg	R$ 4,00 por kg
D	> 50 kg	R$ 7,00 por kg

Observações importantes:
	•	Produtos frágeis: adicionar R$ 5,00 por unidade frágil.
	•	Não existe adicional por região nem desconto por fidelidade.

⸻

2.3 Ordem de Cálculo
	1.	Subtotal = soma do preço unitário × quantidade.
	2.	Aplicar o desconto conforme a regra.
	3.	Calcular o frete conforme a tabela.
	4.	Total = subtotalComDesconto + frete.
	5.	Arredondar o valor final para duas casas decimais.

⸻

3. Testes Obrigatórios para calcularCustoTotal()

3.1 Cobertura Estrutural

Os testes devem obrigatoriamente atingir:
	•	100% de cobertura de arestas (branch coverage)

O relatório JaCoCo deve demonstrar que todas as arestas foram cobertas.

⸻

3.2 Mutação

É obrigatório:
	•	Utilizar PITEST
	•	Atingir 100% de mutantes mortos

O README deve documentar:
	•	Linha de comando utilizada
	•	Como verificar que não há mutantes sobreviventes
	•	Estratégias para matar mutantes sobreviventes

⸻

4. Testes do Método finalizarCompra()

Além dos testes estruturais, você deverá testar o método finalizarCompra() usando dublês de teste.

⸻

4.1 Objetivo do Teste

Cobrir completamente o fluxo:
	•	Verificação de estoque via serviço externo
	•	Cálculo do custo total
	•	Autorização de pagamento
	•	Atualização de estoque
	•	Finalização da compra

Os testes devem atingir:
	•	100% de cobertura de decisão do método
	•	Verificação de:
	•	métodos invocados corretamente
	•	retorno consistente com o comportamento esperado

⸻

4.2 Cenários de Teste

Implementar dois cenários, cada um em um arquivo .java separado.

⸻

Cenário 1
	•	Criar fakes para os serviços externos:
	•	IEstoqueExternal
	•	IPagamentoExternal
	•	Outras dependências internas → mocks com Mockito
	•	Alcançar a meta de cobertura.

⸻

Cenário 2
	•	Criar mocks com Mockito para os serviços externos:
	•	IEstoqueExternal
	•	IPagamentoExternal
	•	Dependências da camada repository → fakes
	•	Alcançar a meta de cobertura.

⸻

5. Entrega

A entrega deve conter:
	•	Projeto Maven compactado em ZIP
	•	Nome do projeto e artifactId no formato:

nome1-nome2

	•	Arquivo README.md contendo:
	•	Nome dos autores
	•	Instruções de execução
	•	Como rodar os testes
	•	Como visualizar relatórios de cobertura
	•	Como gerar e interpretar o relatório de mutação

⸻

Se quiser, posso:

✅ gerar README.md profissional (versão final para entregar)
✅ gerar estrutura completa do projeto com Maven
✅ criar classes, interfaces, fakes, mocks
✅ gerar todos os testes automatizados prontos
É só pedir!