package main.java.br.edu.analisadorlexico.sintatico;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Gramatica {

    // Símbolos especiais da gramática
    public static final String INICIO = "PROG";
    public static final String EPSILON = "ε";
    public static final String FIM_CADEIA = "$";

    // Armazena as produções de cada não-terminal
    private final Map<String, List<List<String>>> producoes;

    // Armazena os símbolos não-terminais e terminais
    private final Set<String> naoTerminais;
    private final Set<String> terminais;

    public Gramatica() {
        producoes = new LinkedHashMap<>();
        naoTerminais = new LinkedHashSet<>();
        terminais = new LinkedHashSet<>();

        inicializarNaoTerminais();
        inicializarTerminais();
        inicializarProducoes();
    }

    private void inicializarNaoTerminais() {
        naoTerminais.addAll(Arrays.asList(
                "PROG",
                "CMDS",
                "CMD",
                "DECL",
                "TIPO",
                "ATRIB",
                "IF_STMT",
                "SENAO",
                "COND",
                "OP_REL",
                "E",
                "E'",
                "T",
                "T'",
                "F"
        ));
    }

    private void inicializarTerminais() {
        terminais.addAll(Arrays.asList(
                "public",
                "class",
                "static",
                "void",
                "int",
                "float",
                "char",
                "if",
                "else",
                "id",
                "num",
                "+",
                "-",
                "*",
                "/",
                "=",
                "==",
                "!=",
                "<",
                ">",
                "(",
                ")",
                "{",
                "}",
                ";",
                FIM_CADEIA
        ));
    }

    private void inicializarProducoes() {

        // 1. Início do programa
        adicionarProducao(
                "PROG",
                "public class id { public static void id ( ) { CMDS } }"
        );

        // 2. Comandos
        adicionarProducao(
                "CMDS",
                "CMD CMDS",
                EPSILON
        );

        adicionarProducao(
                "CMD",
                "DECL",
                "ATRIB",
                "IF_STMT"
        );

        // 3. Declarações de variáveis
        adicionarProducao(
                "DECL",
                "TIPO id ;"
        );

        adicionarProducao(
                "TIPO",
                "int",
                "float",
                "char"
        );

        // 4. Atribuições e estruturas condicionais
        adicionarProducao(
                "ATRIB",
                "id = E ;"
        );

        adicionarProducao(
                "IF_STMT",
                "if ( COND ) { CMDS } SENAO"
        );

        adicionarProducao(
                "SENAO",
                "else { CMDS }",
                EPSILON
        );

        // 5. Condições relacionais
        adicionarProducao(
                "COND",
                "E OP_REL E"
        );

        adicionarProducao(
                "OP_REL",
                "==",
                "!=",
                "<",
                ">"
        );

        // 6. Expressões aritméticas
        adicionarProducao(
                "E",
                "T E'"
        );

        adicionarProducao(
                "E'",
                "+ T E'",
                "- T E'",
                EPSILON
        );

        adicionarProducao(
                "T",
                "F T'"
        );

        adicionarProducao(
                "T'",
                "* F T'",
                "/ F T'",
                EPSILON
        );

        adicionarProducao(
                "F",
                "( E )",
                "id",
                "num"
        );
    }

    private void adicionarProducao(
            String naoTerminal,
            String... alternativas
    ) {
        List<List<String>> listaProducoes = new ArrayList<>();

        for (String alternativa : alternativas) {
            List<String> simbolos = Arrays.asList(
                    alternativa.trim().split("\\s+")
            );

            listaProducoes.add(new ArrayList<>(simbolos));
        }

        producoes.put(naoTerminal, listaProducoes);
    }

    public String getSimboloInicial() {
        return INICIO;
    }

    public String getEpsilon() {
        return EPSILON;
    }

    public String getFimCadeia() {
        return FIM_CADEIA;
    }

    public Map<String, List<List<String>>> getProducoes() {
        return producoes;
    }

    public Set<String> getNaoTerminais() {
        return naoTerminais;
    }

    public Set<String> getTerminais() {
        return terminais;
    }

    public void exibirProducoes() {
        System.out.println("===== GRAMÁTICA =====");

        for (Map.Entry<String, List<List<String>>> entrada
                : producoes.entrySet()) {

            String naoTerminal = entrada.getKey();

            System.out.print(naoTerminal + " -> ");

            List<List<String>> alternativas = entrada.getValue();

            for (int i = 0; i < alternativas.size(); i++) {

                List<String> simbolos = alternativas.get(i);

                System.out.print(String.join(" ", simbolos));

                if (i < alternativas.size() - 1) {
                    System.out.print(" | ");
                }
            }

            System.out.println();
        }
    }
}