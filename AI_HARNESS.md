# Harness de IA — CieloTickets

Este documento registra como ferramentas de IA foram usadas na construção do app, conforme exigido pelo desafio técnico.

O fluxo de trabalho ao longo do projeto foi consistente: eu decidia o que precisava ser feito (com base na leitura da documentação da Cielo, nos requisitos do desafio, e no comportamento real que eu observava testando o app), pedia ao **Claude** ajuda para estruturar essa decisão em um prompt técnico, executava o prompt no **Gemini** (integrado ao Android Studio) para gerar o código, e revisava o resultado antes de aceitar — foi nessa revisão que identifiquei pontos como a fragilidade da idempotência ou a falta de validação do `reference` no callback, detalhados nas fases abaixo. Para trechos pequenos escritos por mim diretamente, usei o **GitHub Copilot** em modo autocomplete inline.

---

## Fase 1 — Arquitetura e setup do projeto

**Objetivo:** definir a stack e estrutura antes de qualquer código — decisão minha, com o Claude ajudando a formalizar em prompt.

**Restrições que eu defini:** Kotlin nativo, Clean Architecture + MVI, minSdk 24/targetSdk 29 (exigência da Cielo Smart, que eu havia lido na documentação), sem WebView (proibido pela Cielo), pacotes `presentation/domain/data` em módulo único (decisão minha de escopo, dado o prazo de 3 dias).

**Prompt 1 — Setup do projeto (Gemini):** configuração de `build.gradle.kts` (Compose, Hilt, Coroutines, Navigation, Room, kotlinx.serialization), manifest com `<queries>` para o app integrador da Cielo, `Application` class Hilt, base MVI (`UiState`/`UiIntent`/`UiEffect`/`BaseViewModel`), e modelos de domínio iniciais (`Event`, `Ticket`, `PaymentState`).

**Resultado:** projeto compilando após resolver 3 problemas de ferramental não relacionados ao prompt em si — bug do KSP2 com Hilt (`ksp.useKSP2=false`), incompatibilidade de JVM target entre Java/Kotlin, e versão desalinhada entre plugin e biblioteca do Hilt no `build.gradle.kts` raiz.

---

## Fase 2 — Catálogo de eventos (Firestore + Room)

**Prompt 2 — Firebase Firestore (Gemini):** configuração do plugin `google-services`, `EventRepository`/`GetEventsUseCase` no domain, e camada `data` completa seguindo padrão single-source-of-truth: `EventRemoteDataSource` (Firestore) → `EventEntity`/`EventDao` (Room, cache) → `EventRepositoryImpl` expondo o Room como fonte de verdade.

**Prompt 3 — Tela de lista de eventos (Gemini):** contrato MVI (`EventListContract`), `EventListViewModel`, `EventListScreen` em Compose com `LazyColumn`, loading e erro tratados no `State`.

**Prompt 4 — Detalhe do evento + navegação (Gemini):** `GetEventByIdUseCase`, `EventDetailScreen` com seletor de quantidade (limites min/max via `availableTickets`), e `NavGraph.kt` conectando as telas via Navigation Compose.

**Decisões tomadas durante a execução:**
- Regras do Firestore inicialmente bloqueavam leitura por padrão (causa raiz de uma lista vazia) — liberamos leitura pública apenas da coleção `events`, escrita permanece restrita.
- `price` armazenado em centavos (Int) no Firestore, formatado apenas na camada de apresentação, para evitar imprecisão de ponto flutuante em valores monetários.

---

## Fase 3 — Integração de pagamento Cielo Smart (núcleo do desafio)

Esta foi a fase mais investigativa, e a que exigiu mais de mim diretamente: antes de qualquer prompt de código, precisei estudar a documentação oficial da Cielo Smart (glossário, modelos de integração, formato do JSON de pagamento, códigos de erro) e trazer esse entendimento pro Claude, que não tinha acesso a ela — incluindo uma descoberta que eu fiz sozinha, testando com `adb`, e trouxe pra discutir: **o pacote real do app integrador instalado pelo emulador é `br.com.cielosmart.orderservice`, não `com.ads.lio.uriappclient` como a documentação pública menciona**.

**Prompt 5 — Gateway de pagamento + idempotência (Gemini):** `PaymentRepository`/`PaymentRepositoryImpl` (construção da URI `lio://payment`, Base64, parsing do callback), DTOs de request/response/erro seguindo o schema oficial da Cielo, `PendingPurchaseEntity`/`Dao` para persistir a chave de idempotência, `GeneratePurchaseUseCase`, e credenciais lidas de `local.properties` via `BuildConfig` (nunca hardcoded).

**Prompt 6 — Fluxo de pagamento completo + geração de Ticket (Gemini):** `PaymentCallbackBus` (singleton `SharedFlow`, necessário porque o callback chega em uma `Activity` nova sem acesso direto ao `ViewModel` da tela de pagamento), `PaymentResponseActivity` real, `TicketRepository`/`CreateTicketUseCase`, e `PaymentViewModel`/`PaymentScreen` completos.

**Debug significativo nesta fase, feito por mim (o Claude ajudou a interpretar erros e propor correções, mas quem rodava, observava o comportamento real no emulador e trazia o log de volta era eu):
- Erro de configuração do emulador Windows (Hyper-V/BIOS)
- Emulador Cielo crashando por rodar em versão de Android não suportada (exigiu AVD dedicado em API 29)
- `<queries>` do manifest apontando para o pacote incorreto
- Bug de parsing: o `parsePaymentCallback` tentava decodificar sucesso como erro por não inspecionar a chave `code` do JSON antes de escolher o DTO
- `statusCode` do pagamento aprovado está aninhado dentro de `paymentFields`, não no nível raiz do objeto `Payment` — não documentado claramente, identificado por inspeção manual do JSON via log

**Prompt 9 — Correção de idempotência real (Gemini):** revisão de código identificou que a chave de idempotência só existia em memória (não sobrevivia à recriação do ViewModel), que o `reference` do callback nunca era validado contra a compra atual, e que não havia proteção contra ticket duplicado. Prompt corrigiu os três pontos: consulta ao banco antes de gerar nova chave, validação de `reference` no callback, e checagem de ticket existente antes de criar um novo.

**Prompt 7 — Comprovante + QR Code (Gemini):** `ReceiptScreen` com geração de QR Code via ZXing a partir de um payload vinculando `ticketId` + `cieloOrderId`, garantindo rastreabilidade da compra concluída.

**Prompt 8 — Histórico "Meus Ingressos" + menu lateral (Gemini):** `GetMyTicketsUseCase`, `MyTicketsScreen`, e `ModalNavigationDrawer` conectando as telas principais.

---

## Fase 4 — Qualidade, segurança e observabilidade

**Prompt 10 — Centralização de constantes (Gemini):** eliminação de strings duplicadas do protocolo Cielo (scheme/host) e das rotas de navegação, substituídas por `object` de constantes e uma `sealed class Screen` tipada.

**Prompt 11 — Testes automatizados (Gemini):** suíte cobrindo idempotência (`GeneratePurchaseUseCaseTest`), parsing de callback (`PaymentRepositoryImplTest`, com Robolectric), fluxo de duplicidade (`PaymentViewModelTest`, com MockK), e regras de negócio (`EventDetailViewModelTest`, `EventListViewModelTest`).

**Prompt 12 — CI + Detekt + Gitleaks (Gemini):** workflow do GitHub Actions (lint, testes, Detekt, upload de relatórios) e job paralelo de Gitleaks. CodeQL habilitado separadamente via configuração nativa do GitHub (não requer YAML).

**Prompt 13 — Correção de lint (Compose `NonObservableLocale`) (Gemini):** centralização de formatação de moeda/data em funções de extensão puras (`toBrazilianCurrency`, `toBrazilianDateTime`, `toFormattedEventDate`) fora do escopo de `@Composable`, resolvendo o lint e eliminando duplicação de formatação em 5+ arquivos.

**Ajuste manual no `detekt.yml`:** a primeira versão gerada continha um grupo `security` inexistente no Detekt e uma propriedade depreciada (`LongParameterList.threshold`); corrigidos manualmente após identificar a causa via mensagens de erro do próprio Detekt. Regras ajustadas para não penalizar padrões legítimos de Compose (`ignoreAnnotated: ['Composable']` em `FunctionNaming`, `LongMethod`, `LongParameterList`).

**Prompt 14 — Crashlytics + Acessibilidade (Gemini):** configuração do Crashlytics, e `contentDescription`/semântica em imagens, QR Code e botões de quantidade nas telas principais. Decisão adicional: apenas `PaymentState.Error` (falha técnica) é reportado ao Crashlytics — `Denied`/`Cancelled` são desfechos normais do fluxo de negócio e não devem poluir o painel de monitoramento.

**Prompt 15 — Polish visual (Gemini):** correção de datas cruas (ISO) exibidas sem formatação, reestruturação do card de evento (imagem 16:9 no topo em vez de thumbnail lateral cortado), preenchimento do espaço vazio na tela de Checkout, elevação visual dos cards, e diferenciação semântica de cor (verde para "Aprovado", roxo reservado para ação principal).

**Prompt 17 — Spotless (Gemini):** formatação de código automatizada via `ktlint`, com `spotlessCheck` integrado ao pipeline de CI.

**Incidente de segurança identificado durante o processo (não pela IA — pelo secret scanning nativo do GitHub, que eu revisei):** o GitHub sinalizou a chave de API do Firebase exposta no `google-services.json` publicado. Avaliei a severidade real antes de agir: chave não-privilegiada por design (não é segredo de autenticação, só identifica o projeto Firebase). A mitigação inicial foi restringir a chave por SHA-1 de certificado — decisão que revisei e substituí na Fase 5, ao perceber que isso impediria outros desenvolvedores de rodar o projeto com dados reais.

---

## Fase 5 — Onboarding de avaliadores e tema visual

**Contexto:** após o app funcional, o foco passou a ser reduzir a barreira de entrada para quem for clonar e avaliar o repositório (sem acesso às credenciais pessoais de Cielo/Firebase da autora), e refinar a identidade visual com a cor de marca da Cielo.

**Prompt 18 — Validação de credenciais Cielo ausentes (Gemini):** `PaymentViewModel.startPayment()` passou a validar se `CIELO_CLIENT_ID`/`CIELO_ACCESS_TOKEN` estão vazios antes de montar a URI de pagamento, emitindo uma mensagem de erro amigável em vez de gerar uma URI inválida — evita que quem clonar o projeto sem credenciais tenha uma experiência quebrada ao tentar pagar.

**Decisão sobre o Firestore (revisão humana, não gerada por IA):** avaliação de que restringir a chave do Firebase por SHA-1 de certificado de debug impediria qualquer outro desenvolvedor de rodar o projeto com dados reais (cada máquina gera um certificado de debug próprio). Decisão: publicar o `google-services.json` no repositório (chave não-privilegiada por design) e manter a proteção real na regra do Firestore (leitura pública apenas do catálogo de eventos, escrita bloqueada) — prioriza a experiência de quem for avaliar o projeto sem abrir mão de segurança real.

**Prompt 19 — Tema claro/escuro com a cor de marca da Cielo (Gemini):** paleta gerada a partir de `#00AEEF` via Material Theme Builder oficial (ferramenta externa, não a IA de código), garantindo contraste adequado (WCAG) em ambos os modos — decisão explícita de não usar `dynamicColor`/Material You, para a identidade da Cielo prevalecer sobre o wallpaper do usuário.
