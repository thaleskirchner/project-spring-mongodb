# Testes automatizados

Este documento descreve **o que cada teste verifica e por quê**, além de explicar as
decisões de projeto por trás da suíte (por que os testes de integração não dependem de
um MongoDB real, como a cobertura é medida, etc.).

Para rodar tudo:

```bash
./mvnw test
```

Isso roda os testes unitários e os de integração juntos (ver `maven-surefire-plugin` em
`pom.xml`, que inclui tanto `**/*Test.java` quanto `**/*IT.java`), gera o relatório de
cobertura HTML em `target/site/jacoco/index.html` e falha o build se a cobertura de
linhas do projeto ficar abaixo de **90%**.

## Índice

- [Estratégia geral](#estratégia-geral)
- [Testes unitários](#testes-unitários)
- [Testes de integração](#testes-de-integração)
- [Cobertura de código (JaCoCo)](#cobertura-de-código-jacoco)

## Estratégia geral

- **Testes unitários** (`*Test.java`) isolam uma classe por vez. Onde a classe depende de
  outra (por exemplo, um `Service` que depende de um `Repository`), a dependência é
  substituída por um mock do Mockito — o objetivo é validar a lógica da própria classe,
  não a de quem ela chama.
- **Testes de integração** (`*IT.java`, no pacote `integration`) sobem o contexto
  completo do Spring (`@SpringBootTest`) em uma porta HTTP real e batem nos endpoints via
  `TestRestTemplate`, percorrendo a cadeia real *Servlet → DispatcherServlet →
  Controller → Service → Repository → serialização JSON → `@ControllerAdvice`*.

### Por que os testes de integração não usam um MongoDB real

`UserRepository` e `PostRepository` são interfaces do Spring Data — não têm lógica própria
para testar, e a aplicação de exemplo (`Instantiation`) já apaga e recria as coleções
inteiras toda vez que o contexto sobe. Exigir um MongoDB rodando (localmente ou via
Testcontainers) tornaria a suíte frágil em qualquer ambiente de CI que não provisione o
banco como serviço. Por isso, nos testes de integração os dois repositórios são
substituídos por mocks (`@MockitoBean`), e cada teste programa o retorno esperado do
mock (`when(repository.findById(...))...`). O que a suíte garante é a integração **entre
as camadas da aplicação** (roteamento HTTP, injeção de dependência, conversão
DTO ↔ domínio, serialização JSON, tratamento de erros) — não o comportamento do driver do
MongoDB em si, que é responsabilidade do próprio Spring Data/driver oficial e está fora do
escopo deste projeto de estudo.

Pelo mesmo motivo, `WorkshopmongoApplicationTests` (o teste de contexto original do
projeto) também mocka os dois repositórios: sem isso, o `CommandLineRunner`
(`Instantiation`) que roda automaticamente na subida do contexto tentaria conectar a um
MongoDB real e o teste falharia em qualquer máquina sem o banco de pé.

### Por que `WorkshopmongoApplication` é excluída da cobertura

O `pom.xml` exclui `WorkshopmongoApplication.class` da checagem de cobertura do JaCoCo. A
única linha executável dessa classe é `SpringApplication.run(...)` dentro do `main` —
chamá-la de verdade significaria subir a aplicação real (Tomcat + tentativa de conexão
com o MongoDB de produção) só para marcar uma linha como coberta, o que não agrega
confiança nenhuma. O restante do bootstrap do Spring (criação de beans, autoconfiguração
etc.) já é exercitado pelos testes de integração via `@SpringBootTest`.

## Testes unitários

### `domain/PostTest.java` e `domain/UserTest.java`
Verificam o contrato dos documentos MongoDB (`@Document`): construtores, getters/setters
e, principalmente, que `equals`/`hashCode` são baseados **somente no `id`** — importante
porque o Spring Data e estruturas como `Set`/`Map` dependem dessa identidade para
funcionar corretamente com entidades persistidas.

### `dto/AuthorDTOTest.java`, `dto/CommentDTOTest.java`, `dto/UserDTOTest.java`
Verificam que os DTOs copiam corretamente os dados de um `User` (no caso de `AuthorDTO` e
`UserDTO`) e que os construtores/setters preenchem todos os campos esperados —
garantindo que a "cópia rasa" usada para embutir autor/comentários dentro de um post não
perde nem inverte nenhum campo.

### `resources/util/URLTest.java`
Verifica o utilitário usado pelos controllers para ler parâmetros de query string:
- `decodeParam` decodifica texto URL-encoded (ex.: `Bom%20dia` → `Bom dia`) e devolve o
  texto original quando não há nada para decodificar.
- `convertDate` faz o parse de datas no formato `yyyy-MM-dd` e devolve o valor padrão
  informado quando o texto está vazio ou é inválido — é esse fallback que garante que
  `/posts/fullsearch` não quebra quando `minDate`/`maxDate` não são enviados.

### `resources/exceptions/StandardErrorTest.java`
Confirma que o objeto de erro devolvido ao cliente (`StandardError`) expõe corretamente
todos os campos (`timestamp`, `status`, `error`, `message`, `path`) via construtor e via
setters.

### `resources/exceptions/ResourceExceptionHandlerTest.java`
Chama diretamente o `@ExceptionHandler` que traduz uma `ObjectNotFoundException` em uma
resposta HTTP, sem depender de uma requisição HTTP real. Garante que o status devolvido é
sempre `404`, que a mensagem da exceção é propagada para o corpo da resposta e que o
`path` do erro é o da requisição que falhou — esse é o comportamento que os testes de
integração depois confirmam de ponta a ponta.

### `services/exceptions/ObjectNotFoundExceptionTest.java`
Teste simples confirmando que a exceção carrega a mensagem recebida e é uma
`RuntimeException` (não verificada), como o restante do código espera.

### `services/PostServiceTest.java`
Isola `PostService`, mockando `PostRepository`:
- `findById` devolve o post quando ele existe e lança `ObjectNotFoundException` quando
  não existe (é esse comportamento que, na camada REST, vira um `404`).
- `findByTitle` apenas repassa a busca para `repository.searchTitle`.
- `fullSearch` verifica a regra de negócio mais sutil do projeto: o `maxDate` recebido é
  estendido em exatamente **+1 dia** antes de ser repassado ao repositório, para que a
  busca inclua o dia inteiro informado como limite superior (sem essa regra, um post
  criado às 23h do `maxDate` ficaria de fora do resultado).

### `services/UserServiceTest.java`
Isola `UserService`, mockando `UserRepository`, cobrindo cada operação pública:
`findAll`, `findById` (achado e não encontrado), `insert`, `update` (carrega o usuário
existente, copia nome/e-mail e salva — sem sobrescrever o restante do documento),
`delete` (busca o usuário antes de apagar, e **não** chama `deleteById` quando o usuário
não existe) e `fromDTO` (conversão `UserDTO` → `User`).

## Testes de integração

### `integration/UserResourceIT.java`
Sobe o contexto completo em uma porta HTTP aleatória e testa, via `TestRestTemplate`,
cada endpoint de `/users`:
- `findAllReturnsUsersAsDTOs` — `GET /users` devolve a lista serializada como `UserDTO`
  (sem os posts, que não fazem parte do DTO público).
- `findByIdReturnsUserWhenPresent` / `findByIdReturns404WithStandardErrorBodyWhenAbsent`
  — o caminho feliz e o tratamento de erro (`404` + `StandardError`) para `GET /users/{id}`.
- `findPostsReturnsThePostsOwnedByTheUser` — `GET /users/{id}/posts` devolve a lista de
  posts do usuário.
- `insertCreatesUserAndReturnsLocationHeader` — `POST /users` responde `201 Created` com
  o header `Location` apontando para o novo recurso.
- `deleteRemovesExistingUserAndReturnsNoContent` /
  `deleteReturnsNotFoundAndDoesNotCallRepositoryWhenUserIsMissing` — `DELETE /users/{id}`
  remove o usuário existente e devolve `404` (sem tentar apagar) quando o `id` não existe.
- `updateChangesNameAndEmailOfExistingUser` — `PUT /users/{id}` atualiza nome e e-mail do
  usuário existente e devolve `204 No Content`.

### `integration/PostResourceIT.java`
Mesma abordagem para `/posts`:
- `findByIdReturnsPostWhenPresent` / `findByIdReturns404WithStandardErrorBodyWhenAbsent`
  — `GET /posts/{id}`, incluindo a serialização aninhada de `AuthorDTO` dentro do post.
- `findByTitleDecodesTheQueryParamAndDelegatesToRepository` — `GET
  /posts/titlesearch?text=...` decodifica o parâmetro (`URL.decodeParam`) antes de
  repassá-lo ao repositório.
- `findByTitleDefaultsToEmptyTextWhenParamIsMissing` — o mesmo endpoint sem o parâmetro
  `text` usa `""` como padrão em vez de falhar.
- `fullSearchParsesDatesAndDelegatesToRepository` — `GET
  /posts/fullsearch?text=&minDate=&maxDate=` faz o parse das datas e delega a busca
  combinada ao repositório.

### `WorkshopmongoApplicationTests.java`
Teste de "smoke": garante que o contexto Spring inteiro sobe sem erros — todo controller,
service e repositório é instanciado e conectado corretamente. Ver a explicação acima sobre
por que os repositórios são mockados aqui também.

## Cobertura de código (JaCoCo)

O `pom.xml` configura o `jacoco-maven-plugin` para:
1. Instrumentar o código durante `mvn test` (`prepare-agent`).
2. Gerar um relatório HTML em `target/site/jacoco/index.html` (`report`).
3. **Falhar o build** se a cobertura de linhas do projeto (contando unitários +
   integração juntos) ficar abaixo de **90%** (`check`, regra `LINE` /
   `COVEREDRATIO >= 0.90`).

`WorkshopmongoApplication` é a única classe excluída dessa checagem, pelo motivo já
explicado acima.
