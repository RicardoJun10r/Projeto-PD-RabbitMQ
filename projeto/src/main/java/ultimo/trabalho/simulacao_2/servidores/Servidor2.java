package ultimo.trabalho.simulacao_2.servidores;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Servidor2 extends Servidor {

    public Servidor2() {
        super("Servidor 2");
    }
    
    public static void main(String[] args) {
        try {
            new Servidor2().iniciar();
        } catch (IOException | TimeoutException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    
}
