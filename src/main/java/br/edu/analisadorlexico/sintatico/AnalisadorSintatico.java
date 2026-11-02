
package main.java.br.edu.analisadorlexico.sintatico;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import main.java.br.edu.analisadorlexico.lexer.Token;
import main.java.br.edu.analisadorlexico.lexer.TokenType;

public class AnalisadorSintatico {

    private final Gramatica gramatica;
    private final TabelaPreditiva tabela;

    public AnalisadorSintatico(
            Gramatica gramatica,
            TabelaPreditiva tabela
    ) {
        this.gramatica = gramatica;
        this.tabela = tabela;
    }

    // Representa um nó da árvore sintática.
    private static class NoArvore {

        private final String simbolo;
        private final List<NoArvore> filhos;

        public NoArvore(String simbolo) {
            this.simbolo = simbolo;
            this.filhos = new ArrayList<>();
        }
    }

    // Associa um símbolo da pilha ao seu nó na árvore.
    private static class EntradaPilha {

        private final String simbolo;
        private final NoArvore no;

        public EntradaPilha(String simbolo, NoArvore no) {
            this.simbolo = simbolo;
            this.no = no;
        }
    }

    public boolean analisar(List<Token> tokens) {

        List<String> entrada = converterTokens(tokens);

        // Impede a análise sintática se houver erros léxicos.
        if (entrada == null) {
            System.out.println("\nANÁLISE SINTÁTICA NÃO REALIZADA.");
            System.out.println(
                    "Corrija os erros léxicos antes de continuar."
            );
            return false;
        }

        entrada.add(gramatica.getFimCadeia());

        // Cria a raiz da árvore sintática.
        NoArvore raiz = new NoArvore(
                gramatica.getSimboloInicial()
        );

        // Pilha associa símbolos aos respectivos nós.
        Deque<EntradaPilha> pilha = new ArrayDeque<>();

        pilha.push(new EntradaPilha(
                gramatica.getFimCadeia(), null
        ));

        pilha.push(new EntradaPilha(
                gramatica.getSimboloInicial(), raiz
        ));

        int indiceEntrada = 0;
        int passo = 1;

        boolean aceita = false;
        int errosSintaticos = 0;

        System.out.println("\n===== ANÁLISE SINTÁTICA LL(1) =====");

        System.out.printf(
                "%-6s | %-40s | %-40s | %s%n",
                "Passo", "Pilha (topo à esquerda)",
                "Entrada", "Ação"
        );

        while (!pilha.isEmpty()) {

            EntradaPilha entradaPilha = pilha.peek();

            String topo = entradaPilha.simbolo;
            String simboloAtual = entrada.get(indiceEntrada);

            // Monta a representação textual da pilha.
            List<String> simbolosPilha = new ArrayList<>();

            for (EntradaPilha item : pilha) {
                simbolosPilha.add(item.simbolo);
            }

            String pilhaTexto = String.join(" ", simbolosPilha);

            String entradaTexto = String.join(
                    " ", entrada.subList(indiceEntrada, entrada.size())
            );

            String acao;

            // Caso 1: fim da pilha e fim da entrada.
            if (topo.equals(gramatica.getFimCadeia())
                    && simboloAtual.equals(gramatica.getFimCadeia())) {

                acao = "Aceitar: entrada reconhecida";
                aceita = true;

                imprimirPasso(
                        passo, pilhaTexto, entradaTexto, acao
                );

                break;
            }

            // Caso 2: o topo da pilha é um terminal.
            if (gramatica.getTerminais().contains(topo)) {

                if (topo.equals(simboloAtual)) {

                    acao = "Casar terminal: " + topo;

                    imprimirPasso(
                            passo, pilhaTexto, entradaTexto, acao
                    );

                    pilha.pop();
                    indiceEntrada++;

                } else {

                    errosSintaticos++;

                    acao = "ERRO: esperado '" + topo
                            + "', encontrado '" + simboloAtual + "'";

                    // Detecta ponto e vírgula ausente antes de outro comando.
                    if (topo.equals(";") && ehInicioComando(simboloAtual)) {

                        acao += " | Recuperação: inserir ';'";

                        pilha.pop();

                    } else if (ehPontoSincronizacao(simboloAtual)) {

                        acao += " | Recuperação: inserir '" + topo + "'";

                        pilha.pop();

                    } else {

                        acao += " | Recuperação: descartar '"
                                + simboloAtual + "'";

                        indiceEntrada++;
                    }

                    imprimirPasso(
                            passo, pilhaTexto, entradaTexto, acao
                    );
                }

            } else {

                // Caso 3: o topo é um não terminal.
                List<String> producao =
                        tabela.getProducao(topo, simboloAtual);

                if (producao == null) {

                    errosSintaticos++;

                    Set<String> esperados =
                            obterTerminaisEsperados(topo);

                    acao = "ERRO: não há produção para M["
                            + topo + ", " + simboloAtual + "]. "
                            + "Esperado: " + esperados;

                    // Procura um símbolo que permita retomar
                    // a análise do não terminal atual.
                    while (indiceEntrada < entrada.size() - 1
                            && tabela.getProducao(
                            topo, entrada.get(indiceEntrada)
                    ) == null
                            && !ehPontoSincronizacao(
                            entrada.get(indiceEntrada)
                    )) {

                        indiceEntrada++;
                    }

                    String novoSimbolo = entrada.get(indiceEntrada);

                    if (tabela.getProducao(topo, novoSimbolo) != null) {

                        acao += " | Recuperação: descartar tokens"
                                + " até '" + novoSimbolo + "'";

                    } else {

                        acao += " | Recuperação: descartar "
                                + topo;

                        pilha.pop();
                    }

                    imprimirPasso(
                            passo, pilhaTexto, entradaTexto, acao
                    );

                } else {

                    acao = "Aplicar " + topo + " -> "
                            + String.join(" ", producao);

                    imprimirPasso(
                            passo, pilhaTexto, entradaTexto, acao
                    );

                    pilha.pop();

                    boolean producaoVazia = (
                            producao.size() == 1
                                    && producao.get(0).equals(
                                    gramatica.getEpsilon()
                            )
                    );

                    if (producaoVazia) {

                        entradaPilha.no.filhos.add(
                                new NoArvore(gramatica.getEpsilon())
                        );

                    } else {

                        List<EntradaPilha> novosItens =
                                new ArrayList<>();

                        for (String simbolo : producao) {

                            NoArvore filho = new NoArvore(simbolo);

                            entradaPilha.no.filhos.add(filho);

                            novosItens.add(
                                    new EntradaPilha(simbolo, filho)
                            );
                        }

                        // Empilha os símbolos na ordem inversa.
                        for (int i = novosItens.size() - 1;
                             i >= 0; i--) {

                            pilha.push(novosItens.get(i));
                        }
                    }
                }
            }

            passo++;

            // Proteção contra uma possível recuperação sem progresso.
            if (indiceEntrada >= entrada.size()) {
                break;
            }
        }

        System.out.println("\n===== RESULTADO =====");

        if (aceita && errosSintaticos == 0) {

            System.out.println(
                    "Programa sintaticamente válido!"
            );

            System.out.println("\n===== ÁRVORE SINTÁTICA =====");

            exibirArvore(raiz, "", true);

            return true;
        }

        System.out.println(
                "Programa sintaticamente inválido."
        );

        System.out.println(
                "Quantidade de erros sintáticos encontrados: "
                        + errosSintaticos
        );

        if (errosSintaticos > 0) {
            System.out.println(
                    "A análise continuou utilizando recuperação por modo pânico."
            );
        }

        return false;
    }

    // Verifica os pontos de sincronização.
    private boolean ehPontoSincronizacao(String simbolo) {

        return simbolo.equals(";")
                || simbolo.equals("}")
                || simbolo.equals(")")
                || simbolo.equals(gramatica.getFimCadeia());
    }

    // Imprime a árvore sintática.
    private void exibirArvore(
            NoArvore no,
            String prefixo,
            boolean ultimo
    ) {

        System.out.println(
                prefixo
                        + (ultimo ? "└── " : "├── ")
                        + no.simbolo
        );

        for (int i = 0; i < no.filhos.size(); i++) {

            boolean ultimoFilho = (i == no.filhos.size() - 1);

            String novoPrefixo = prefixo
                    + (ultimo ? "    " : "│   ");

            exibirArvore(
                    no.filhos.get(i),
                    novoPrefixo,
                    ultimoFilho
            );
        }
    }

    // Converte os tokens do analisador léxico.
    private List<String> converterTokens(List<Token> tokens) {

        List<String> simbolos = new ArrayList<>();
        boolean encontrouErroLexico = false;

        for (Token token : tokens) {

            if (token.getTipo() == TokenType.ERROR) {

                System.out.println(
                        "Erro léxico encontrado: " + token
                );

                encontrouErroLexico = true;
                continue;
            }

            String simbolo;

            switch (token.getTipo()) {

                case IDENTIFIER:
                    simbolo = "id";
                    break;

                case INTEGER:
                case FLOAT:
                    simbolo = "num";
                    break;

                default:
                    simbolo = token.getLexema();
                    break;
            }

            simbolos.add(simbolo);
        }

        if (encontrouErroLexico) {
            return null;
        }

        return simbolos;
    }

    // Obtém os terminais esperados para um não terminal.
    private Set<String> obterTerminaisEsperados(
            String naoTerminal
    ) {

        Set<String> esperados = new LinkedHashSet<>();

        Map<String, List<String>> linha =
                tabela.getTabela().get(naoTerminal);

        if (linha != null) {
            esperados.addAll(linha.keySet());
        }

        return esperados;
    }

    // Imprime cada passo da análise.
    private void imprimirPasso(
            int passo,
            String pilha,
            String entrada,
            String acao
    ) {

        System.out.printf(
                "%-6d | %-40s | %-40s | %s%n",
                passo, pilha, entrada, acao
        );
    }

    // Verifica se o símbolo pode iniciar um comando.
    private boolean ehInicioComando(String simbolo) {

        return simbolo.equals("id")
                || simbolo.equals("if")
                || simbolo.equals("int")
                || simbolo.equals("float")
                || simbolo.equals("char");
    }
}
