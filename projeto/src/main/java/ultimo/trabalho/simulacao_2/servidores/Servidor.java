package ultimo.trabalho.simulacao_2.servidores;

import java.io.IOException;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.TimeoutException;

import org.json.JSONObject;

import com.rabbitmq.client.*;

public class Servidor {

    private static final String EXCHANGE_NAME = "logs_fanout";
    protected String serverName;

    public Servidor(String serverName) {
        this.serverName = serverName;
    }

    public void iniciar() throws IOException, TimeoutException, InterruptedException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.exchangeDeclare(EXCHANGE_NAME, "fanout");

            Random random = new Random();

            while (true) {
                JSONObject mensagemJson = gerarMensagem(random);

                String mensagem = mensagemJson.toString();

                channel.basicPublish(EXCHANGE_NAME, "", null, mensagem.getBytes("UTF-8"));
                System.out.println(serverName + " enviou: " + mensagem);
                System.out.println();

                Thread.sleep(5000);
            }
        }
    }

    private JSONObject gerarMensagem(Random random) {
        JSONObject mensagem = new JSONObject();
        mensagem.put("timestamp", Instant.now().toString());
        mensagem.put("service", "Serviço " + serverName.charAt(serverName.length() - 1));
        mensagem.put("status", gerarStatus(random));
        mensagem.put("server", serverName);

        JSONObject metrics = new JSONObject();
        metrics.put("cpu_usage", random.nextInt(101));
        metrics.put("memory_usage", random.nextInt(101));
        metrics.put("response_time", random.nextInt(1001));

        mensagem.put("metrics", metrics);

        return mensagem;
    }

    private String gerarStatus(Random random) {
        String[] statuses = {"azul", "amarelo", "vermelho"};
        return statuses[random.nextInt(statuses.length)];
    }

}
