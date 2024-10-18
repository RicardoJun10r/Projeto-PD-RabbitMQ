package ultimo.trabalho.simulacao_1.server;

public class ServidorMain {
    public static void main(String[] args) {
        try {
            Servidor servidor = new Servidor(3);
            servidor.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
