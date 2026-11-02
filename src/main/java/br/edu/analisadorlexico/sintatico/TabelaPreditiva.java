package main.java.br.edu.analisadorlexico.sintatico;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TabelaPreditiva {

    private final Gramatica gramatica;
    private final CalculadoraFirstFollow calculadora;

    // Cada célula associa um não-terminal e um terminal a uma produção.
    private final Map<String, Map<String, List<String>>> tabela;

    public TabelaPreditiva(
            Gramatica gramatica,
            CalculadoraFirstFollow calculadora
    ) {
        this.gramatica = gramatica;
        this.calculadora = calculadora;

        tabela = new LinkedHashMap<>();

        inicializarTabela();
        construirTabela();
    }

    private void inicializarTabela() {
        for (String naoTerminal : gramatica.getNaoTerminais()) {
            tabela.put(naoTerminal, new LinkedHashMap<>());
        }
    }

    private void construirTabela() {
        Map<String, Set<String>> first = calculadora.getFirst();
        Map<String, Set<String>> follow = calculadora.getFollow();

        for (Map.Entry<String, List<List<String>>> entrada
                : gramatica.getProducoes().entrySet()) {

            String naoTerminal = entrada.getKey();

            for (List<String> producao : entrada.getValue()) {

                Set<String> firstProducao =
                        calcularFirstProducao(producao, first);

                // Regra 1: M[A, a] = A -> produção,
                // para cada terminal a em FIRST(produção).
                for (String terminal : firstProducao) {
                    if (!terminal.equals(gramatica.getEpsilon())) {
                        adicionarCelula(
                                naoTerminal,
                                terminal,
                                producao
                        );
                    }
                }

                // Regra 2: se a produção puder gerar epsilon,
                // usamos FOLLOW(A) para preencher outras células.
                if (firstProducao.contains(gramatica.getEpsilon())) {
                    for (String terminal : follow.get(naoTerminal)) {
                        adicionarCelula(
                                naoTerminal,
                                terminal,
                                producao
                        );
                    }
                }
            }
        }
    }

    private Set<String> calcularFirstProducao(
            List<String> producao,
            Map<String, Set<String>> first
    ) {
        Set<String> resultado =
                new java.util.LinkedHashSet<>();

        boolean todosPodemProduzirEpsilon = true;

        for (String simbolo : producao) {

            if (simbolo.equals(gramatica.getEpsilon())) {
                resultado.add(gramatica.getEpsilon());
                break;
            }

            // Se for terminal, ele inicia a produção.
            if (gramatica.getTerminais().contains(simbolo)) {
                resultado.add(simbolo);
                todosPodemProduzirEpsilon = false;
                break;
            }

            // Se for não-terminal, adicionamos seu FIRST,
            // ignorando epsilon temporariamente.
            Set<String> firstSimbolo = first.get(simbolo);

            if (firstSimbolo == null) {
                todosPodemProduzirEpsilon = false;
                break;
            }

            for (String elemento : firstSimbolo) {
                if (!elemento.equals(gramatica.getEpsilon())) {
                    resultado.add(elemento);
                }
            }

            // Se não puder gerar epsilon, não precisamos
            // examinar os próximos símbolos da produção.
            if (!firstSimbolo.contains(gramatica.getEpsilon())) {
                todosPodemProduzirEpsilon = false;
                break;
            }
        }

        if (todosPodemProduzirEpsilon) {
            resultado.add(gramatica.getEpsilon());
        }

        return resultado;
    }

    private void adicionarCelula(
            String naoTerminal,
            String terminal,
            List<String> producao
    ) {
        Map<String, List<String>> linha = tabela.get(naoTerminal);

        List<String> producaoExistente = linha.get(terminal);

        if (producaoExistente != null
                && !producaoExistente.equals(producao)) {

            System.out.println(
                    "Conflito LL(1) na célula M["
                            + naoTerminal + ", " + terminal + "]: "
                            + producaoExistente + " / " + producao
            );

            return;
        }

        linha.put(terminal, producao);
    }

    public List<String> getProducao(
            String naoTerminal,
            String terminal
    ) {
        Map<String, List<String>> linha = tabela.get(naoTerminal);

        if (linha == null) {
            return null;
        }

        return linha.get(terminal);
    }

    public Map<String, Map<String, List<String>>> getTabela() {
        return tabela;
    }

    public void exibirTabela() {
        System.out.println("\n===== TABELA DE ANÁLISE PREDITIVA M =====");

        // Cabeçalho com todos os terminais.
        System.out.printf("%-12s", "NT / T");

        for (String terminal : gramatica.getTerminais()) {
            System.out.printf("%-24s", terminal);
        }

        System.out.println();

        // Uma linha para cada não-terminal.
        for (String naoTerminal : gramatica.getNaoTerminais()) {
            System.out.printf("%-12s", naoTerminal);

            for (String terminal : gramatica.getTerminais()) {
                List<String> producao =
                        getProducao(naoTerminal, terminal);

                if (producao == null) {
                    System.out.printf("%-24s", "-");
                } else {
                    String textoProducao =
                            naoTerminal + " -> "
                                    + String.join(" ", producao);

                    System.out.printf("%-24s", textoProducao);
                }
            }

            System.out.println();
        }
    }
}