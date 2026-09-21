<div align="center">

# 🍃 Workshop MongoDB

**API REST de um mini blog, construída com Spring Boot 4 e MongoDB**

[![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.5-brightgreen?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MongoDB](https://img.shields.io/badge/MongoDB-NoSQL-47A248?logo=mongodb&logoColor=white)](https://www.mongodb.com/)
[![Maven](https://img.shields.io/badge/Maven-Build%20Tool-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)

</div>

---

## 📌 Sobre o projeto

**Workshop MongoDB** é uma API REST que modela um mini blog: **usuários** que publicam **posts**, e **posts** que recebem **comentários**. O projeto existe como material de estudo sobre como integrar **Spring Boot** com **MongoDB**, cobrindo tanto o mapeamento de documentos quanto padrões comuns de modelagem NoSQL (documentos embutidos vs. referenciados).

O código segue a arquitetura em camadas **Controller (`resources`) → Service → Repository**, usando **Spring Data MongoDB** para as operações de persistência.

**O que a aplicação faz, na prática:**
- Expõe um CRUD de usuários (`/users`).
- Expõe consulta de posts por `id`, por título e uma busca "full text" combinando título, corpo e comentários, com filtro por intervalo de datas (`/posts`).
- Ao subir, popula o banco automaticamente com usuários e posts de exemplo (classe `Instantiation`), apagando o conteúdo anterior das coleções — útil para desenvolvimento, mas **não** é um comportamento adequado para produção.
- Traduz erros de domínio (objeto não encontrado) em respostas HTTP `404` com um corpo de erro padronizado (`StandardError`), via `@ControllerAdvice`.

---

## 🏛 Arquitetura

```
Cliente (HTTP)
     │
     ▼
┌─────────────────────┐
│  REST Controller     │  ← resources/ — recebe requisições HTTP, devolve JSON
└─────────┬───────────┘
          │
          ▼
┌─────────────────────┐
│      Service         │  ← services/ — regra de negócio (busca, validação de existência, montagem de DTOs)
└─────────┬───────────┘
          │
          ▼
┌─────────────────────┐
│    Repository        │  ← repositories/ — interfaces Spring Data MongoDB (inclusive @Query customizadas)
└─────────┬───────────┘
          │
          ▼
┌─────────────────────┐
│      MongoDB          │  ← banco de documentos NoSQL
└─────────────────────┘
```

### Modelo de dados

- **`User`** (coleção `user`): `id`, `name`, `email` e uma lista de `Post` referenciada via `@DBRef(lazy = true)` — ou seja, os posts do usuário ficam em documentos próprios na coleção de posts, e só são carregados quando `getPosts()` é de fato acessado.
- **`Post`** (coleção `post`): `id`, `date`, `title`, `body`, um `AuthorDTO` (cópia rasa do autor — id + nome, embutida no próprio documento) e uma lista de `CommentDTO` (também embutidos, cada um com texto, data e seu próprio `AuthorDTO`).

Essa combinação — referência (`@DBRef`) para "usuário → seus posts" e documentos embutidos para "post → autor/comentários" — é proposital: evita duplicar o post inteiro dentro do usuário, mas evita também um `JOIN`/segunda consulta só para saber quem comentou o quê.

---

## 🛠 Tech Stack

| Tecnologia | Versão | Uso |
|---|---|---|
| **Java** | 25 | Linguagem |
| **Spring Boot** | 4.0.5 | Framework da aplicação |
| **Spring Data MongoDB** | (gerenciado) | Camada de acesso a dados |
| **Spring Web MVC** | (gerenciado) | Camada REST |
| **MongoDB** | 6.0+ | Banco de dados NoSQL orientado a documentos |
| **Maven** | 3.8+ (wrapper incluso) | Build e gerência de dependências |
| **JUnit 5 / Mockito / AssertJ** | (gerenciado) | Testes unitários |
| **Spring Test (MockMvc / TestRestTemplate)** | (gerenciado) | Testes de integração |
| **JaCoCo** | 0.8.13 | Medição e verificação de cobertura de testes |

---

## ✅ Pré-requisitos

| Requisito | Versão | Download |
|---|---|---|
| JDK | 25+ | [openjdk.org](https://openjdk.org/) |
| MongoDB | 6.0+ | [mongodb.com](https://www.mongodb.com/try/download/community) |
| Git | qualquer | [git-scm.com](https://git-scm.com/) |

> **Observação:** Maven **não** é necessário — o projeto inclui os scripts `mvnw` / `mvnw.cmd`.

---

## ▶️ Como executar

1. Suba uma instância local do MongoDB na porta padrão (`27017`) — veja `src/main/resources/application.properties`, que aponta para `mongodb://localhost:27017/workshop_mongo`.
2. Rode a aplicação:

   ```bash
   ./mvnw spring-boot:run
   ```
3. Ao iniciar, a aplicação **apaga e recria** as coleções `user` e `post` com dados de exemplo (veja `config/Instantiation.java`). Isso acontece a cada reinicialização.
4. A API fica disponível em `http://localhost:8080`.

---

## 📡 Endpoints da API

> URL base: `http://localhost:8080`

### Usuários — `/users`

| Método | Endpoint | Descrição | Status |
|---|---|---|---|
| `GET` | `/users` | Lista todos os usuários | `200 OK` |
| `GET` | `/users/{id}` | Busca um usuário pelo `id` | `200 OK` / `404 Not Found` |
| `GET` | `/users/{id}/posts` | Lista os posts do usuário | `200 OK` / `404 Not Found` |
| `POST` | `/users` | Cria um novo usuário | `201 Created` (com header `Location`) |
| `PUT` | `/users/{id}` | Atualiza nome/e-mail de um usuário | `204 No Content` / `404 Not Found` |
| `DELETE` | `/users/{id}` | Remove um usuário | `204 No Content` / `404 Not Found` |

**Exemplo — criar usuário:**

```http
POST /users
Content-Type: application/json

{
  "name": "Bob Grey",
  "email": "bob@gmail.com"
}
```

Resposta: `201 Created`, com `Location: http://localhost:8080/users/<id gerado>`.

### Posts — `/posts`

| Método | Endpoint | Descrição | Status |
|---|---|---|---|
| `GET` | `/posts/{id}` | Busca um post pelo `id` | `200 OK` / `404 Not Found` |
| `GET` | `/posts/titlesearch?text=` | Busca posts cujo título contenha `text` (case-insensitive) | `200 OK` |
| `GET` | `/posts/fullsearch?text=&minDate=&maxDate=` | Busca por `text` no título, corpo ou comentários, dentro do intervalo `[minDate, maxDate]` (formato `yyyy-MM-dd`) | `200 OK` |

`text` é decodificado como URL-encoded (`URL.decodeParam`); `minDate`/`maxDate` ausentes ou inválidos usam `1970-01-01` como padrão (`URL.convertDate`), e `maxDate` é internamente estendido em +1 dia para incluir o dia inteiro na busca.

### Erros

Qualquer `id` inexistente (usuário ou post) resulta em `404 Not Found` com um corpo padronizado:

```json
{
  "timestamp": "2026-03-21T12:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Objeto não encontrado",
  "path": "/users/000000000000000000000000"
}
```

---

## 🧪 Testes

O projeto tem testes unitários e testes de integração; veja **[docs/TESTES.md](docs/TESTES.md)** para o objetivo de cada classe/teste.

```bash
./mvnw test
```

Isso executa toda a suíte (unitária + integração), gera o relatório de cobertura em `target/site/jacoco/index.html` e **falha o build** se a cobertura de linhas ficar abaixo de **90%** (configurado em `pom.xml`, plugin `jacoco-maven-plugin`).

---

## 📁 Estrutura do projeto

```
project-spring-mongodb/
│
├── src/
│   ├── main/
│   │   ├── java/com/thaleskirchner/workshopmongo/
│   │   │   ├── WorkshopmongoApplication.java   # ponto de entrada Spring Boot
│   │   │   ├── config/                         # seed de dados de exemplo (Instantiation)
│   │   │   ├── domain/                         # documentos MongoDB (@Document): User, Post
│   │   │   ├── dto/                            # objetos embutidos/expostos: AuthorDTO, CommentDTO, UserDTO
│   │   │   ├── repositories/                   # interfaces Spring Data MongoDB
│   │   │   ├── resources/                      # controllers REST + tratamento de exceções + util
│   │   │   └── services/                       # regras de negócio
│   │   │
│   │   └── resources/
│   │       └── application.properties          # configuração da aplicação
│   │
│   └── test/
│       └── java/com/thaleskirchner/workshopmongo/
│           ├── domain/, dto/, resources/, services/   # testes unitários
│           └── integration/                            # testes de integração (*IT)
│
├── docs/
│   └── TESTES.md                                # objetivo de cada teste
├── .mvn/wrapper/                                  # Maven wrapper
├── mvnw / mvnw.cmd                                # Maven wrapper (Linux/macOS / Windows)
├── pom.xml                                        # dependências, build e cobertura (JaCoCo)
├── .gitignore
└── README.md
```

---

## 👤 Autor

**Thales Kirchner**

- GitHub: [@thaleskirchner](https://github.com/thaleskirchner)

---

<div align="center">
  Made with ☕ and 🍃 MongoDB
</div>
