package ultimo.trabalho.simulacao_1;

import com.rabbitmq.client.*;

public class Receptor1 {
    private static final String EXCHANGE = "topic_logs";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory fabrica = new ConnectionFactory();
        fabrica.setHost("localhost");
        Connection conexao = fabrica.newConnection();
        Channel canal = conexao.createChannel();

        canal.exchangeDeclare(EXCHANGE, BuiltinExchangeType.TOPIC);
        String nomeFila = canal.queueDeclare().getQueue();

        String routingKey = "#";
        canal.queueBind(nomeFila, EXCHANGE, routingKey);

        System.out.println(" [*] Esperando mensagens...");

        DeliverCallback callbackEntrega = (tagConsumidor, entrega) -> {
            String mensagem = new String(entrega.getBody(), "UTF-8");
            System.out.println(" Recebida <-- '" + entrega.getEnvelope().getRoutingKey() + "':'" + mensagem + "'");
        };
        canal.basicConsume(nomeFila, true, callbackEntrega, tagConsumidor -> { });
    }
}
