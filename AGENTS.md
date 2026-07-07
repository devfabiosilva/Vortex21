# AGENTS.md — Vortex21 · Geração de Testes (DSL de comparação de objetos parseados e validados de WITSML 2.1 → BSON)

> Spec de referência para qualquer IA (Gemini, Claude, Copilot, etc.) gerar os
> blocos de teste da DSL fluente do Vortex21. Objetivo: eliminar a necessidade
> de reescrever contexto/prompt a cada bloco XML novo.
> Uso: cole o bloco XML preenchido + "aplica AGENTS.md nesse bloco" — nada mais.

## 1. Contexto da arquitetura (não alterar, só ler)

Isto **não é** um teste JUnit clássico com `assertEquals` por campo. É uma **DSL
fluente de comparação**:

1. Constrói-se o objeto **esperado** chamando `.build()` da classe Java
   correspondente, na ordem exata dos parâmetros da classe.
2. O último argumento da chamada de topo (`.test()`) é o BSON **real**, obtido
   navegando o documento já parseado via `navigate(...)`, convertido com
   `(BsonDocument)`.
3. `.test()` faz a comparação interna entre o objeto esperado e o BSON real.

Não há mock, não há chamada de rede, não há estado externo. É determinístico:
mesmo XML → mesma chamada de `.build()`, sempre.

> **`navigate(...)` — assinatura confirmada:**
> `navigate(BsonDocument root, String... path)` — variádico. O primeiro
> argumento é sempre a raiz do documento BSON já parseado; os argumentos
> seguintes são os segmentos do caminho, na ordem de descida pela árvore,
> um por nível de aninhamento. O retorno é o valor BSON encontrado naquele
> caminho, já no tipo correspondente (`BsonString`, `BsonDocument`, etc.) —
> daí o cast `(BsonDocument)` quando o alvo é um objeto complexo.
>
> Exemplo mínimo: para `<well><a><b>teste</b></a></well>`, parseado para
> `wellInBson`, o campo `b` é obtido com `navigate(wellInBson, "well", "a", "b")`,
> retornando um `BsonString` com valor `"teste"`. O número de segmentos varia
> conforme a profundidade do campo alvo — não é fixo em 1 ou 2, é "um segmento
> por nível até chegar no campo desejado".

Exemplo:

Na pasta `wrappers/java/TestFiles/xmls/strict_valid/Well.xml` temos vários
objetos e vamos validar o `DataSourceOrganization` através do
`wellDataSourceOrganizationTest()` localizado no `WellTest.java`.

```xml
<?xml version="1.0" encoding="UTF-8"?>
  <rdw211:Well uuid="123e4567-e89b-12d3-a456-426614174001" schemaVersion="" objectVersion="">
   <rdw212:Aliases xsi:type="rdw212:ObjectAlias" authority="A">
    <rdw212:Identifier xsi:type="rdw212:String64">Identifier A</rdw212:Identifier>
    <rdw212:IdentifierKind xsi:type="rdw212:AliasIdentifierKindExt">IdentifierKind A</rdw212:IdentifierKind>
    <rdw212:Description xsi:type="rdw212:String2000">Description A</rdw212:Description>
    <rdw212:EffectiveDateTime xsi:type="rdw212:TimeStamp">2021-02-03T04:05:06Z</rdw212:EffectiveDateTime>
    <rdw212:TerminationDateTime xsi:type="rdw212:TimeStamp">2021-09-25T19:29:41Z</rdw212:TerminationDateTime>
   </rdw212:Aliases>
   <rdw212:Aliases xsi:type="rdw212:ObjectAlias" authority="B">
    <rdw212:Identifier xsi:type="rdw212:String64">Identifier B</rdw212:Identifier>
    <rdw212:IdentifierKind xsi:type="rdw212:AliasIdentifierKindExt">IdentifierKind B</rdw212:IdentifierKind>
    <rdw212:Description xsi:type="rdw212:String2000">Description B</rdw212:Description>
    <rdw212:EffectiveDateTime xsi:type="rdw212:TimeStamp">2023-02-03T04:05:06Z</rdw212:EffectiveDateTime>
    <rdw212:TerminationDateTime xsi:type="rdw212:TimeStamp">2023-09-25T19:29:41Z</rdw212:TerminationDateTime>
   </rdw212:Aliases>

   ...

   <!-- Queremos testar esse objeto complexo de nome DataSourceOrganization do tipo DataObjectReference -->
   <rdw211:DataSourceOrganization xsi:type="rdw212:DataObjectReference">
    <rdw212:Uuid xsi:type="rdw212:UuidString">123e4567-e89b-12d3-a456-426614174fc2</rdw212:Uuid>
    <rdw212:ObjectVersion xsi:type="rdw212:String64">DataSourceOrganization ObjectVersion</rdw212:ObjectVersion>
    <rdw212:QualifiedType xsi:type="rdw212:QualifiedType">custom81.test</rdw212:QualifiedType>
    <rdw212:Title xsi:type="rdw212:String2000">DataSourceOrganization title</rdw212:Title>
    <rdw212:EnergisticsUri xsi:type="xsd:anyURI">http://www.example.com/schema/anyURIDataSourceOrganizationA</rdw212:EnergisticsUri>
    <rdw212:LocatorUrl xsi:type="xsd:anyURI">http://www.example.com/schema/anyURIDataSourceOrganizationA1</rdw212:LocatorUrl>
    <rdw212:LocatorUrl xsi:type="xsd:anyURI">http://www.example.com/schema/anyURIDataSourceOrganizationA2</rdw212:LocatorUrl>
    <rdw212:ExtensionNameValue xsi:type="rdw212:ExtensionNameValue">
     <rdw212:Name xsi:type="rdw212:String64">ENV A 1 NAME</rdw212:Name>
     <rdw212:Value xsi:type="rdw212:StringMeasure" uom="UomA1">ValueA1</rdw212:Value>
     <rdw212:MeasureClass xsi:type="rdw212:MeasureClass">attenuation per frequency interval</rdw212:MeasureClass>
     <rdw212:DTim xsi:type="rdw212:TimeStamp">2022-03-02T18:28:17Z</rdw212:DTim>
     <rdw212:Index xsi:type="xsd:long">99999</rdw212:Index>
     <rdw212:Description xsi:type="rdw212:String2000">DESC ABCD</rdw212:Description>
    </rdw212:ExtensionNameValue>
    <rdw212:ExtensionNameValue xsi:type="rdw212:ExtensionNameValue">
     <rdw212:Name xsi:type="rdw212:String64">ENV B 2 NAME</rdw212:Name>
     <rdw212:Value xsi:type="rdw212:StringMeasure" uom="UomB2">ValueB2</rdw212:Value>
     <rdw212:MeasureClass xsi:type="rdw212:MeasureClass">cation exchange capacity</rdw212:MeasureClass>
     <rdw212:DTim xsi:type="rdw212:TimeStamp">2021-01-15T19:30:27Z</rdw212:DTim>
     <rdw212:Index xsi:type="xsd:long">19287674318</rdw212:Index>
     <rdw212:Description xsi:type="rdw212:String2000">DESC XYZABC</rdw212:Description>
    </rdw212:ExtensionNameValue>
   </rdw211:DataSourceOrganization>

   ...
  </rdw211:Well>
```

No `wellDataSourceOrganizationTest()` localizado no `WellTest.java` temos:

```java
    @Before
    public void setUp() throws Exception {
        this.parser1 = W21ParserLoader.begin().withInputRulesValidator().withInputWitsmlStrict().withResourceStats().withIgnoreInputWitsmlNS().build();
        try {
            this.parser1.readFromFile(fromPath("Well"), W21ParserLoader.W21Object.Well); // Carregamento do arquivo para o Parser em JNI. Inicializando na memória
        } catch (W21Exception e) {
            printW21Exception(logger, e);
            throw e;
        }
        try {
            this.wellDocument = (BsonDocument) this.parser1.parse(W21ParserLoader.W21OutputType.BSON); // Parseando todo o arquivo validado para o tipo BSON
        } catch (W21Exception e) {
            printW21Exception(logger, e);
            throw e;
        }
    }

    @After
    public void tearDown() {
        assertEquals("Parser 1 close method must return 0", 0, this.parser1.close()); // Liberando a memória do JNI
    }

    ...

    @Test
    public void wellDataSourceOrganizationTest() throws Exception {
        DataObjectReference.build(
                "123e4567-e89b-12d3-a456-426614174fc2",
                "DataSourceOrganization ObjectVersion",
                "custom81.test",
                "DataSourceOrganization title",
                "http://www.example.com/schema/anyURIDataSourceOrganizationA",
                List.of(
                        "http://www.example.com/schema/anyURIDataSourceOrganizationA1",
                        "http://www.example.com/schema/anyURIDataSourceOrganizationA2"
                ),
                List.of(
                        ExtensionNameValue.build(
                                "ENV A 1 NAME",
                                "UomA1",
                                "ValueA1",
                                "attenuation per frequency interval",
                                "2022-03-02T18:28:17Z",
                                99999L,
                                "DESC ABCD"
                        ),
                        ExtensionNameValue.build(
                                "ENV B 2 NAME",
                                "UomB2",
                                "ValueB2",
                                "cation exchange capacity",
                                "2021-01-15T19:30:27Z",
                                19287674318L,
                                "DESC XYZABC"
                        )
                ),
                (BsonDocument) navigate(this.wellDocument, "Well", "DataSourceOrganization")
        ).test();
    }
```

## 2. Regra de ouro nº 1 — fonte da verdade é o CÓDIGO, não o XML

**A ordem dos parâmetros de `.build()` vem da assinatura real da classe Java
já existente no repositório — NUNCA da ordem em que os elementos aparecem no
XML.**

Na prática, isso significa:
- A IA nunca deve *inferir* a ordem só olhando o XML, mesmo que pareça óbvio.
- Se a classe Java não foi fornecida no prompt, a IA deve **perguntar** ou
  pedir para ver a assinatura antes de gerar o código — nunca assumir.
- Se um parâmetro foi removido/refatorado na classe (ex.: um BSON filho que
  virou opcional por herança do pai), a IA não deve inserir `null` como
  placeholder "pra garantir". Ausência de necessidade = ausência do parâmetro.
- XML e assinatura da classe costumam coincidir em ordem, mas **coincidência
  não é garantia** — sempre confirmar contra o código quando houver dúvida.

## 3. Outras regras sempre válidas

**Sempre fazer:**
- Nome da classe Java = nome local do `xsi:type`, sem o prefixo de namespace
  (`rdw212:DataObjectReference` → `DataObjectReference`).
- Elemento XML repetido (mesma tag aparecendo 2+ vezes no mesmo nível) →
  `List.of(...)`, preservando a ordem de aparição no documento — vale tanto
  para tipo aninhado repetido (`OSDULineageAssertion.build(...)` dentro da
  lista) quanto para escalar repetido (`List.of("OwnerGroup test 1", ...)`).
- Atributo XML (ex.: `uom="..."`) → parâmetro próprio na chamada, nunca
  concatenado ao valor do elemento.
- Tipo complexo aninhado (ex.: `ExtensionNameValue` dentro de `Datum`,
  `OSDULineageAssertion` dentro de `OSDUIntegration`) → chamada recursiva ao
  `.build()` da classe filha, inline dentro do `List.of(...)` ou como
  argumento direto.
- `xsd:long` → literal Java com sufixo `L` (`99999L`).
- `xsd:anyURI`, `String*` (`String64`, `String2000`, `String256`, etc.) →
  string literal Java entre aspas, valor exatamente como está no XML, sem
  transformação.
- `TimeStamp` → manter como string literal ISO-8601 tal como está no XML,
  a menos que a classe Java declare um tipo diferente (`Instant`,
  `LocalDateTime`) — nesse caso, seguir o tipo da classe, não o formato do XML.
- Parâmetro BSON de navegação (`navigate(...)`) só aparece na chamada de
  **topo** que termina em `.test()`. Chamadas de `.build()` aninhadas
  (objetos filhos) não recebem esse parâmetro a menos que a classe exija.
  `navigate(BsonDocument root, String... path)` navega pelo documento BSON
  descendo um segmento de caminho por nível, e retorna o valor/tipo do campo
  encontrado — ver Seção 1 para o exemplo mínimo com a assinatura completa.

**Nunca fazer:**
- Nunca inventar ordem de parâmetro por "parece lógico" — checar a classe.
- Nunca inserir `null` como parâmetro "de segurança" sem confirmar que a
  classe realmente tem esse parâmetro na assinatura atual.
- Nunca fabricar valor que não está no bloco XML fornecido.
- Nunca achatar uma lista de elementos repetidos em um único valor concatenado.
- Nunca usar `new ClasseX(...)` — o padrão é sempre `ClasseX.build(...)`.

## 4. Como usar isso na prática

1. Cole o bloco XML preenchido.
2. Se a classe Java de destino não estiver óbvia pelo histórico da conversa,
   cole também a assinatura do `.build()` (ou diga "já sabe a classe").
3. Diga apenas: "aplica AGENTS.md nesse bloco".
4. Revise a saída contra a Seção 2 antes de aceitar — é o ponto onde erro
   silencioso mais acontece (ordem de parâmetro trocada, `null` fantasma).

## 5. Nota de manutenção

Se o padrão da DSL mudar (novo tipo de parâmetro, mudança em como listas ou
tipos aninhados são tratados, ou a assinatura de `navigate(...)` mudar),
atualizar a Seção 1 e a Seção 3 primeiro. Se uma classe específica for
refatorada, isso não precisa entrar aqui — a Seção 2 já cobre isso: a IA deve
sempre checar o código, não confiar em memória de conversas anteriores sobre
a assinatura.
