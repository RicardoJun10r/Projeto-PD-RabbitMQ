package ultimo.trabalho.simulacao_2.servidores;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Servidor1 extends Servidor {

    public Servidor1() {
        super("Servidor 1");
    }

    public static void main(String[] args) {
        try {
            new Servidor1().iniciar();
        } catch (IOException | TimeoutException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    
}
