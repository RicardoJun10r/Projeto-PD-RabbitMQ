package ultimo.trabalho.simulacao_2.servidores;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Servidor3 extends Servidor {

    public Servidor3() {
        super("Servidor 3");
    }

    public static void main(String[] args) {
        try {
            new Servidor3().iniciar();
        } catch (IOException | TimeoutException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    
}
