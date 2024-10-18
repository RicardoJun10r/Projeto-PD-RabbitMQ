package ultimo.trabalho.simulacao_1.client;

import com.rabbitmq.client.*;

import java.nio.charset.StandardCharsets;

public class Cliente2 {
    private static final String EXCHANGE_NAME = "topic_logs";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory fabrica = new ConnectionFactory();
        fabrica.setHost("localhost");
        Connection conexao = fabrica.newConnection();
        Channel canal = conexao.createChannel();

        canal.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
        String nomeFila = canal.queueDeclare().getQueue();

        // Cliente2 deseja consumir dados de temperatura e umidade relativa do ar
        String[] routingKeys = {"temperatura.*", "umidade.*"};
        for (String routingKey : routingKeys) {
            canal.queueBind(nomeFila, EXCHANGE_NAME, routingKey);
        }

        System.out.println("Cliente2 aguardando mensagens de temperatura e umidade...");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String mensagem = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println("Cliente2 recebeu: '" + delivery.getEnvelope().getRoutingKey() + "':'" + mensagem + "'");
        };
        canal.basicConsume(nomeFila, true, deliverCallback, consumerTag -> { });
    }
}
