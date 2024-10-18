package ultimo.trabalho.simulacao_2.monitoramento;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.*;
import org.json.JSONObject;

public class ServicoMonitoramento {
    private static final String EXCHANGE_NAME = "logs_fanout";
    private static final String ORDEM_SERVICOS_QUEUE = "ordem_servicos";

    public static void main(String[] argv) throws IOException, TimeoutException {
        ConnectionFactory fabrica = new ConnectionFactory();
        fabrica.setHost("localhost");
        Connection conexao = fabrica.newConnection();
        Channel canal = conexao.createChannel();

        canal.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);

        String nomeFila = canal.queueDeclare().getQueue();

        canal.queueBind(nomeFila, EXCHANGE_NAME, "");

        canal.queueDeclare(ORDEM_SERVICOS_QUEUE, true, false, false, null);

        System.out.println("ServicoMonitoramento aguardando mensagens...");

        DeliverCallback callbackEntrega = (consumerTag, receptor) -> {
            String mensagem = new String(receptor.getBody(), StandardCharsets.UTF_8);
            System.out.println("ServicoMonitoramento recebeu: " + mensagem);
            System.out.println();

            try {
                JSONObject jsonMensagem = new JSONObject(mensagem);

                String status = jsonMensagem.getString("status");

                if (status.equalsIgnoreCase("amarelo") || status.equalsIgnoreCase("vermelho")) {
                    JSONObject ordemServico = new JSONObject();
                    ordemServico.put("timestamp", jsonMensagem.getString("timestamp"));
                    ordemServico.put("server", jsonMensagem.getString("server"));
                    ordemServico.put("service", jsonMensagem.getString("service"));
                    ordemServico.put("status", status);

                    String problema = "";
                    String acaoNecessaria = "";

                    JSONObject metrics = jsonMensagem.getJSONObject("metrics");
                    int cpuUsage = metrics.getInt("cpu_usage");
                    int memoryUsage = metrics.getInt("memory_usage");
                    int responseTime = metrics.getInt("response_time");

                    if (cpuUsage > 90) {
                        problema = "Uso de CPU em " + cpuUsage + "%, serviço não responde";
                        acaoNecessaria = "Verificar e reiniciar o serviço";
                    } else if (memoryUsage > 90) {
                        problema = "Uso de memória em " + memoryUsage + "%, possível lentidão";
                        acaoNecessaria = "Liberar memória ou reiniciar serviço";
                    } else if (responseTime > 500) {
                        problema = "Tempo de resposta alto: " + responseTime + "ms";
                        acaoNecessaria = "Analisar conexão do serviço";
                    } else {
                        problema = "Desconhecido";
                        acaoNecessaria = "Investigar problema";
                    }

                    ordemServico.put("problem", problema);
                    ordemServico.put("action_required", acaoNecessaria);

                    canal.basicPublish("", ORDEM_SERVICOS_QUEUE, null, ordemServico.toString().getBytes(StandardCharsets.UTF_8));
                    System.out.println("Ordem de serviço enviada: " + ordemServico.toString());
                    System.out.println();
                }
            } catch (Exception e) {
                System.err.println("Erro ao processar a mensagem: " + e.getMessage());
            }
        };

        canal.basicConsume(nomeFila, true, callbackEntrega, consumerTag -> { });
    }
}
