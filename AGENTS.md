# AGENTS.md — Vortex21 · Geração de Testes (DSL de comparação de objetos parseados e validados de WITSML 2.1 → BSON)

> Spec de referência para qualquer IA (Gemini, Claude, Copilot, etc.) gerar os
> blocos de teste da DSL fluente do Vortex21. Objetivo: eliminar a necessidade
> de reescrever contexto/prompt a cada bloco XML novo.
> Uso: cole o bloco XML preenchido + "aplica AGENTS.md nesse bloco" — nada mais.

## 1. Contexto da arquitetura (não alterar, só ler)

Isto **não é** um teste JUnit clássico com `assertEquals` por campo. É uma **DSL
fluente de comparação**:

1. Constrói-se o objeto **esperado** chamando o construtor/`build()` da classe
   Java correspondente, na ordem exata dos parâmetros da classe.
2. O último argumento da chamada de topo (`.test()`) é o BSON **real**, obtido
   navegando o objeto pai já parseado: `navigate(<parentVar>, "<ElementName>")`,
   convertido com `(BsonDocument)`.
3. `.test()` faz a comparação interna entre o objeto esperado e o BSON real.

Não há mock, não há chamada de rede, não há estado externo. É determinístico:
mesmo XML → mesma chamada de construtor, sempre.

## 2. Regra de ouro nº 1 — fonte da verdade é o CÓDIGO, não o XML

**A ordem dos parâmetros do construtor/`build()` vem da assinatura real da
classe Java já existente no repositório — NUNCA da ordem em que os elementos
aparecem no XML.**

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
  `List.of(...)`, preservando a ordem de aparição no documento.
- Atributo XML (ex.: `uom="..."`) → parâmetro próprio na chamada, nunca
  concatenado ao valor do elemento.
- Tipo complexo aninhado (ex.: `ExtensionNameValue` dentro de `Datum`) → chamada
  recursiva ao `.build()`/construtor da classe filha, inline dentro do `List.of(...)`
  ou como argumento direto.
- `xsd:long` → literal Java com sufixo `L` (`1880999L`).
- `xsd:anyURI`, `String*` (`String64`, `String2000`, etc.) → string literal
  Java entre aspas, valor exatamente como está no XML, sem transformação.
- `TimeStamp` → manter como string literal ISO-8601 tal como está no XML,
  a menos que a classe Java declare um tipo diferente (`Instant`, `LocalDateTime`)
  — nesse caso, seguir o tipo da classe, não o formato do XML.
- Parâmetro BSON de navegação (`navigate(...)`) só aparece na chamada de
  **topo** que termina em `.test()`. Chamadas de construtor aninhadas
  (objetos filhos) não recebem esse parâmetro a menos que a classe exija.

**Nunca fazer:**
- Nunca inventar ordem de parâmetro por "parece lógico" — checar a classe.
- Nunca inserir `null` como parâmetro "de segurança" sem confirmar que a
  classe realmente tem esse parâmetro na assinatura atual.
- Nunca fabricar valor que não está no bloco XML fornecido.
- Nunca achatar uma lista de elementos repetidos em um único valor concatenado.

## 4. Template de referência (exemplo real, corrigido)

Bloco XML de entrada → chamada Java esperada:

```java
new DataObjectReference(
        "b23e4537-e89b-a2d3-a456-426614174ff2",   // Uuid
        "WellheadElevation ObjectVersion",          // ObjectVersion
        "custom99.testA",                           // QualifiedType
        "WellheadElevation title",                   // Title
        "http://www.example.com/schema/anyURIWellheadElevationA", // EnergisticsUri
        List.of(                                     // LocatorUrl (repetido)
                "http://www.example.com/schema/anyURIDataWellheadElevationA1",
                "http://www.example.com/schema/anyURIWellheadElevationA2"
        ),
        List.of(                                     // ExtensionNameValue (repetido, tipo aninhado)
                ExtensionNameValue.build(
                        "ENV WellheadElevation NAME",
                        "WellheadElevationUomA1",
                        "WellheadElevationValueA1",
                        "api gamma ray",
                        "2020-02-01T10:28:17Z",
                        1880999L,
                        "DESC ABCD WellheadElevation"
                        // sem parâmetro de BSON filho: é opcional, herda do pai
                ),
                ExtensionNameValue.build(
                        "ENV WellheadElevation B 2 NAME",
                        "WellheadElevationUomB2",
                        "WellheadElevationValueB2",
                        "diffusion coefficient",
                        "2023-05-15T13:28:21Z",
                        189127674312L,
                        "DESC XYZABC DEFG"
                )
        ),
        (BsonDocument) navigate(wellheadElevation, "Datum")
).test();
```

## 5. Como usar isso na prática

1. Cole o bloco XML preenchido.
2. Se a classe Java de destino não estiver óbvia pelo histórico da conversa,
   cole também a assinatura do construtor/`build()` (ou diga "já sabe a classe").
3. Diga apenas: "aplica AGENTS.md nesse bloco".
4. Revise a saída contra a Seção 2 antes de aceitar — é o ponto onde erro
   silencioso mais acontece (ordem de parâmetro trocada, `null` fantasma).

## 6. Nota de manutenção

Se o padrão da DSL mudar (novo tipo de parâmetro, mudança em como listas ou
tipos aninhados são tratados), atualizar a Seção 3 e o template da Seção 4
primeiro. Se uma classe específica for refatorada, isso não precisa entrar
aqui — a Seção 2 já cobre isso: a IA deve sempre checar o código, não confiar
em memória de conversas anteriores sobre a assinatura.
