package main.java.br.edu.analisadorlexico.sintatico;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CalculadoraFirstFollow {

    private final Gramatica gramatica;

    private final Map<String, Set<String>> first;
    private final Map<String, Set<String>> follow;

    public CalculadoraFirstFollow(Gramatica gramatica) {
        this.gramatica = gramatica;

        first = new LinkedHashMap<>();
        follow = new LinkedHashMap<>();

        inicializarConjuntos();
    }

    private void inicializarConjuntos() {
        for (String naoTerminal : gramatica.getNaoTerminais()) {
            first.put(naoTerminal, new LinkedHashSet<>());
            follow.put(naoTerminal, new LinkedHashSet<>());
        }
    }

    public void calcular() {
        calcularFirst();
        calcularFollow();
    }

    private void calcularFirst() {
        boolean houveAlteracao;

        do {
            houveAlteracao = false;

            for (String naoTerminal : gramatica.getNaoTerminais()) {
                List<List<String>> alternativas =
                        gramatica.getProducoes().get(naoTerminal);

                for (List<String> producao : alternativas) {
                    Set<String> firstProducao =
                            calcularFirstSequencia(producao);

                    if (first.get(naoTerminal).addAll(firstProducao)) {
                        houveAlteracao = true;
                    }
                }
            }

        } while (houveAlteracao);
    }

    private Set<String> calcularFirstSequencia(List<String> sequencia) {
        Set<String> resultado = new LinkedHashSet<>();

        if (sequencia.isEmpty()) {
            resultado.add(gramatica.getEpsilon());
            return resultado;
        }

        boolean todosPodemProduzirEpsilon = true;

        for (String simbolo : sequencia) {

            if (simbolo.equals(gramatica.getEpsilon())) {
                resultado.add(gramatica.getEpsilon());
                break;
            }

            if (gramatica.getTerminais().contains(simbolo)) {
                resultado.add(simbolo);
                todosPodemProduzirEpsilon = false;
                break;
            }

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

    private void calcularFollow() {
        // O símbolo inicial sempre recebe o marcador de fim de cadeia.
        follow.get(gramatica.getSimboloInicial())
                .add(gramatica.getFimCadeia());

        boolean houveAlteracao;

        do {
            houveAlteracao = false;

            for (Map.Entry<String, List<List<String>>> entrada
                    : gramatica.getProducoes().entrySet()) {

                String ladoEsquerdo = entrada.getKey();

                for (List<String> producao : entrada.getValue()) {

                    for (int i = 0; i < producao.size(); i++) {
                        String simbolo = producao.get(i);

                        if (!gramatica.getNaoTerminais().contains(simbolo)) {
                            continue;
                        }

                        // Obtém os símbolos que aparecem depois do não-terminal.
                        List<String> sufixo =
                                new ArrayList<>(producao.subList(i + 1, producao.size()));

                        Set<String> firstSufixo =
                                calcularFirstSequencia(sufixo);

                        // Adiciona FIRST(sufixo), exceto epsilon.
                        for (String terminal : firstSufixo) {
                            if (!terminal.equals(gramatica.getEpsilon())) {
                                if (follow.get(simbolo).add(terminal)) {
                                    houveAlteracao = true;
                                }
                            }
                        }

                        // Se o sufixo puder gerar epsilon, ou estiver vazio,
                        // adiciona FOLLOW do lado esquerdo.
                        if (firstSufixo.contains(gramatica.getEpsilon())) {
                            if (follow.get(simbolo).addAll(
                                    follow.get(ladoEsquerdo))) {
                                houveAlteracao = true;
                            }
                        }
                    }
                }
            }

        } while (houveAlteracao);
    }

    public Map<String, Set<String>> getFirst() {
        return first;
    }

    public Map<String, Set<String>> getFollow() {
        return follow;
    }

    public void exibirFirst() {
        System.out.println("\n===== CONJUNTOS FIRST =====");

        for (String naoTerminal : gramatica.getNaoTerminais()) {
            System.out.println(
                    "FIRST(" + naoTerminal + ") = " + first.get(naoTerminal)
            );
        }
    }

    public void exibirFollow() {
        System.out.println("\n===== CONJUNTOS FOLLOW =====");

        for (String naoTerminal : gramatica.getNaoTerminais()) {
            System.out.println(
                    "FOLLOW(" + naoTerminal + ") = " + follow.get(naoTerminal)
            );
        }
    }

    public void exibirConjuntos() {
        exibirFirst();
        exibirFollow();
    }
}