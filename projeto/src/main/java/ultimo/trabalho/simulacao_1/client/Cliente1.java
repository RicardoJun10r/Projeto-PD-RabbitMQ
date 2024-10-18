package ultimo.trabalho.simulacao_1.client;

import java.nio.charset.StandardCharsets;

import com.rabbitmq.client.*;

public class Cliente1 {
    private static final String EXCHANGE_NAME = "topic_logs";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory fabrica = new ConnectionFactory();
        fabrica.setHost("localhost");
        Connection conexao = fabrica.newConnection();
        Channel canal = conexao.createChannel();

        canal.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
        String nomeFila = canal.queueDeclare().getQueue();

        // Cliente1 deseja consumir apenas dados relativos à pressão atmosférica
        String routingKey = "pressao.*";
        canal.queueBind(nomeFila, EXCHANGE_NAME, routingKey);

        System.out.println("Cliente1 aguardando mensagens de pressão atmosférica...");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String mensagem = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println("Cliente1 recebeu: '" + delivery.getEnvelope().getRoutingKey() + "':'" + mensagem + "'");
        };
        canal.basicConsume(nomeFila, true, deliverCallback, consumerTag -> {
        });
    }
}
