# Sistema de Controle de Estoque (Almoxarifado)

## 📌 Problema e Contexto

Atualmente, o responsável pelo almoxarifado realiza o controle de entradas e saídas de materiais utilizando uma planilha manual. Esse formato apresenta riscos operacionais, como a falta de rastreabilidade das alterações e a vulnerabilidade a edições acidentais ou não autorizadas.

## 🎯 Objetivo e Usuário

O sistema foi desenvolvido para atender diretamente o **responsável pelo almoxarifado**, oferecendo um meio seguro e auditável para:

* Controlar o saldo atual dos materiais em tempo real.

* Consultar o histórico completo de movimentações (entradas e saídas).

* Identificar rapidamente itens que atingiram ou estão abaixo do estoque mínimo pré-definido.

## 🛠️ Escopo do MVP

### Funcionalidades

1. **Cadastro de Produtos**

   * Nome do produto (único no sistema).

   * Unidade de medida padronizada (`un`, `kg` ou `L`).

   * Definição de quantidade para estoque mínimo.

   * Saldo inicial cadastrado automaticamente como `0`.

2. **Gestão de Movimentações**

   * Registro de movimentações do tipo **Entrada** ou **Saída**.

   * Obrigatoriedade de informar quantidade positiva e o motivo da movimentação.

   * **Regra de Negócio (Estoque Insuficiente):** Tentativas de saída com quantidade superior ao saldo atual são recusadas com status HTTP `409 Conflict`, sem gerar histórico de movimentação.

3. **Consultas e Relatórios**

   * Consulta detalhada de produto e seu histórico de movimentações.

   * Listagem de produtos com saldo atual abaixo do estoque mínimo configurado.

## 📋 Requisitos Mínimos da Aplicação

Para que a aplicação opere corretamente e mantenha a integridade das informações do almoxarifado, as seguintes condições mínimas são obrigatórias:

1. **Unicidade de Identificação do Produto:** Cada produto deve obrigatoriamente possuir um nome único no sistema para evitar duplicidade no inventário.

2. **Padronização das Unidades de Medida:** A aplicação aceita estritamente as unidades de medida `un` (unidade), `kg` (quilograma) ou `L` (litro).

3. **Validação de Valor Positivo em Movimentações:** Toda operação de entrada ou saída deve ser registrada com quantidade estritamente maior que zero (`> 0`) e acompanhada de uma justificativa/motivo.

4. **Integridade de Saldo Não Negativo:** O sistema deve impedir saídas que resultem em saldo negativo, rejeitando a requisição imediatamente e mantendo o histórico inalterado.

5. **Monitoramento e Conectividade do Serviço:** A aplicação deve manter um endpoint acessível (`GET /health`) capaz de responder sobre o estado de operação da API e a conectividade com a camada de dados.

## 🔒 Requisitos Técnicos e Segurança

* **Validação:** Todas as regras e entradas de dados são validadas no lado do servidor.

* **Segurança no Banco de Dados:** Utilização exclusiva de consultas parametrizadas para evitar ataques de SQL Injection.

* **Tratamento de Erros:** Respostas de erro padronizadas para o cliente, sem expor detalhes internos da aplicação ou do banco de dados.

* **Health Check:** Rota `GET /health` disponível para verificação do status da aplicação e teste de conectividade com o banco de dados.

## 🚀 Publicação e Deploy

* **Aplicação:** App Service (Azure ou provedor similar).

* **Banco de Dados:** Banco relacional em nuvem.

> *Nota: O deploy em ambiente de produção depende da conta/assinatura ativa do grupo.*

## 🚦 Endpoints Principais (Resumo)

| Método | Rota | Descrição | 
| ----- | ----- | ----- | 
| `POST` | `/api/produtos` | Cadastra um novo produto (saldo inicial = 0) | 
| `GET` | `/api/produtos` | Lista os produtos ordenados por nome, com saldo atual | 
| `GET` | `/api/produtos/{id}` | Consulta os dados e o saldo de um produto | 
| `GET` | `/api/produtos/abaixo-do-minimo` | Lista produtos com saldo abaixo do estoque mínimo | 
| `POST` | `/api/produtos/{id}/movimentacoes` | Registra uma entrada ou saída no estoque do produto | 
| `GET` | `/api/produtos/{id}/movimentacoes` | Consulta o histórico de movimentações do produto (mais recentes primeiro) | 

Exemplos de requisição e resposta de cada rota estão em [docs/api.md](docs/api.md).

## 🧱 Tecnologias

* **Backend:** Java 17, Spring Boot 3.3 (Web, Validation, Data JPA), Flyway.
* **Banco de dados:** PostgreSQL 16 (H2 em memória nos testes automatizados).
* **Frontend:** Angular 17 (componentes standalone).
* **CI/CD:** GitHub Actions com deploy no Azure App Service e Azure Static Web Apps.

## 📂 Estrutura do Repositório

```
backend/    API Spring Boot (controllers, services, repositories, migrations Flyway)
frontend/   Aplicação Angular
db/         Scripts SQL de seed e de consultas de evidência
docs/       Deploy no Azure, divisão da equipe e documentação da API
.github/    Workflows de build e deploy
```

## ▶️ Como Executar Localmente

Pré-requisitos: Docker, JDK 17+, Maven 3.9+, Node.js 20+.

1. **Banco de dados** (na raiz do projeto):

   ```bash
   docker compose up -d
   ```

2. **Backend** (porta 8080; o Flyway cria as tabelas no primeiro start):

   ```bash
   cd backend
   mvn spring-boot:run
   ```

3. **Dados de exemplo** (opcional, depois que o backend subir):

   ```bash
   docker exec -i almoxarifado-db psql -U postgres -d almoxarifado < db/seed.sql
   ```

4. **Frontend** (porta 4200; o proxy encaminha `/api` para o backend):

   ```bash
   cd frontend
   npm install
   npm start
   ```

   Acesse `http://localhost:4200`.

### Variáveis de ambiente do backend

| Variável | Padrão | Descrição |
| ----- | ----- | ----- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/almoxarifado` | URL JDBC do banco |
| `DB_USER` | `postgres` | Usuário do banco |
| `DB_PASSWORD` | `postgres` | Senha do banco |
| `CORS_ORIGINS` | `http://localhost:4200` | Origens liberadas no CORS (separadas por vírgula) |
| `PORT` | `8080` | Porta HTTP da API |

## ✅ Testes Automatizados

Os testes do backend sobem a aplicação com banco H2 em memória (não precisam do Docker):

```bash
cd backend
mvn test
```

* `CriteriosAceitacaoTest`: critérios de aceitação CA1 a CA5.
* `ValidacoesTest`: validações de entrada e formato padronizado das respostas de erro.
