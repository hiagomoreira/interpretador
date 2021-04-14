package interpretador;

import java.io.FileReader;
import java.io.IOException;

@SuppressWarnings("unused")
public class Main {
    public static void main(String[] args) throws IOException {
        Parser parser = new Parser("..\\interpretador\\src\\interpretador\\main.cpp");

        //Dispara a análise sintática!
        parser.programa();
        System.out.println("Análise sintática concluída!");
    }
}

