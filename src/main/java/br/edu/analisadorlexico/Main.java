
package main.java.br.edu.analisadorlexico;

import main.java.br.edu.analisadorlexico.lexer.Lexer;
import main.java.br.edu.analisadorlexico.lexer.Token;
import main.java.br.edu.analisadorlexico.lexer.TokenType;

import main.java.br.edu.analisadorlexico.sintatico.Gramatica;
import main.java.br.edu.analisadorlexico.sintatico.CalculadoraFirstFollow;
import main.java.br.edu.analisadorlexico.sintatico.TabelaPreditiva;
import main.java.br.edu.analisadorlexico.sintatico.AnalisadorSintatico;

import java.io.IOException;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {

        String caminhoArquivo = "arquivos-teste/Teste.java";

        // ==========================================
        // 1. ANALISE LEXICA
        // ==========================================

        Lexer lexer = new Lexer(caminhoArquivo);

        List<Token> tokens = lexer.analisar();

        System.out.println();
        System.out.println("==============================================================");
        System.out.println("                    ANALISADOR LEXICO");
        System.out.println("==============================================================");
        System.out.println("Arquivo analisado: " + caminhoArquivo);
        System.out.println("Quantidade de tokens: " + tokens.size());

        imprimirTabelaTokens(tokens);

        lexer.getTabelaSimbolos().imprimir();

        imprimirResumo(tokens);


        // ==========================================
        // 2. DEFINICAO DA GRAMATICA
        // ==========================================

        System.out.println();
        System.out.println("==============================================================");
        System.out.println("                 GRAMATICA UTILIZADA");
        System.out.println("==============================================================");

        Gramatica gramatica = new Gramatica();

        gramatica.exibirProducoes();


        // ==========================================
        // 3. CALCULO DE FIRST E FOLLOW
        // ==========================================

        System.out.println();
        System.out.println("==============================================================");
        System.out.println("                  CONJUNTOS FIRST E FOLLOW");
        System.out.println("==============================================================");

        CalculadoraFirstFollow calculadora =
                new CalculadoraFirstFollow(gramatica);

        calculadora.calcular();
        calculadora.exibirConjuntos();


        // ==========================================
        // 4. CONSTRUCAO DA TABELA PREDITIVA
        // ==========================================

        System.out.println();
        System.out.println("==============================================================");
        System.out.println("                   TABELA PREDITIVA LL(1)");
        System.out.println("==============================================================");

        TabelaPreditiva tabela =
                new TabelaPreditiva(gramatica, calculadora);

        tabela.exibirTabela();


        // ==========================================
        // 5. ANALISE SINTATICA
        // ==========================================

        System.out.println();
        System.out.println("==============================================================");
        System.out.println("                    ANALISADOR SINTATICO");
        System.out.println("==============================================================");

        AnalisadorSintatico sintatico =
                new AnalisadorSintatico(gramatica, tabela);

        boolean resultado = sintatico.analisar(tokens);


        // ==========================================
        // 6. RESULTADO FINAL
        // ==========================================

        System.out.println();
        System.out.println("==============================================================");
        System.out.println("                    RESULTADO FINAL");
        System.out.println("==============================================================");

        if (resultado) {
            System.out.println("Programa sintaticamente valido.");
        } else {
            System.out.println("Programa sintaticamente invalido.");
        }

        System.out.println("==============================================================");
    }


    private static void imprimirTabelaTokens(List<Token> tokens) {

        System.out.println();
        System.out.println("========================= TABELA DE TOKENS =========================");

        System.out.printf(
                "%-15s %-30s %-25s%n",
                "TIPO",
                "LEXEMA",
                "ATRIBUTO"
        );

        System.out.println(
                "-------------------------------------------------------------------"
        );

        for (Token token : tokens) {

            String lexema = token.getLexema();
            String atributo = token.getAtributo();

            if (atributo == null) {
                atributo = "-";
            }

            System.out.printf(
                    "%-15s %-30s %-25s%n",
                    token.getTipo(),
                    lexema,
                    atributo
            );
        }
    }


    private static void imprimirResumo(List<Token> tokens) {

        int erros = 0;
        int identificadores = 0;
        int palavrasChave = 0;
        int inteiros = 0;
        int floats = 0;
        int operadores = 0;
        int delimitadores = 0;

        for (Token token : tokens) {

            TokenType tipo = token.getTipo();

            switch (tipo) {

                case ERROR:
                    erros++;
                    break;

                case IDENTIFIER:
                    identificadores++;
                    break;

                case KEYWORD:
                    palavrasChave++;
                    break;

                case INTEGER:
                    inteiros++;
                    break;

                case FLOAT:
                    floats++;
                    break;

                case OPERATOR:
                    operadores++;
                    break;

                case DELIMITER:
                    delimitadores++;
                    break;
            }
        }

        System.out.println();
        System.out.println("=========================== RESUMO ===========================");

        System.out.println("Identificadores : " + identificadores);
        System.out.println("Palavras-chave  : " + palavrasChave);
        System.out.println("Inteiros        : " + inteiros);
        System.out.println("Floats          : " + floats);
        System.out.println("Operadores      : " + operadores);
        System.out.println("Delimitadores   : " + delimitadores);
        System.out.println("Erros lexicos   : " + erros);

        System.out.println("==============================================================");
    }
}
