package ultimo.trabalho.simulacao_1;

import java.nio.charset.StandardCharsets;
import java.util.Random;

import com.rabbitmq.client.*;

public class Emissor {
    private static final String EXCHANGE = "topic_logs";

    public static void main(String[] argv) throws Exception {

        String routingKey, mensagem;
        ConnectionFactory fabrica = new ConnectionFactory();
        fabrica.setHost("localhost");
        try (Connection conexao = fabrica.newConnection();
             Channel canal = conexao.createChannel()) {

            canal.exchangeDeclare(EXCHANGE, BuiltinExchangeType.TOPIC);

            for (int i = 0; i < 30; i++) {
                routingKey = getTipoLog(i % 30);
                mensagem = "log " + i + " - " + routingKey + " - " + new Random().nextInt(500);
                canal.basicPublish(
                        EXCHANGE,
                        routingKey,
                        null,
                        mensagem.getBytes(StandardCharsets.UTF_8));
                System.out.println(" Enviada --> '" + routingKey + "':'" + mensagem + "'");
                Thread.sleep(1000);
            }
        }
    }

    private static String getTipoLog(int chave) {

        if (chave >= 0 && chave < 12){

            if (chave < 4) {
                return "kern.info";
            } else if (chave < 8) {
                return "kern.warning";
            } else return "kern.error";
        }
        else if(chave >= 12 && chave < 21){

            if (chave < 15) {
                return "syslog.info";
            } else if (chave < 18) {
                return "syslog.warning";
            } else return "syslog.error";

        }
        else {
            if (chave < 23) {
                return "user.info";
            } else if (chave < 27) {
                return "user.warning";
            } else return "user.error";
        }
    }
}
