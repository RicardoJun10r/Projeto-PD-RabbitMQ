package ultimo.trabalho.simulacao_2.tecnico;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.*;
import org.json.JSONObject;

public class Tecnico {
    private static final String ORDEM_SERVICOS_QUEUE = "ordem_servicos";

    public static void main(String[] argv) throws IOException, TimeoutException {
        ConnectionFactory fabrica = new ConnectionFactory();
        fabrica.setHost("localhost");
        Connection conexao = fabrica.newConnection();
        Channel canal = conexao.createChannel();

        canal.queueDeclare(ORDEM_SERVICOS_QUEUE, true, false, false, null);

        System.out.println("Tecnico aguardando ordens de serviço...");

        DeliverCallback callbackEntrega = (consumerTag, entrega) -> {
            String mensagem = new String(entrega.getBody(), StandardCharsets.UTF_8);
            System.out.println("Tecnico recebeu ordem de serviço: " + mensagem);
            System.out.println();

            try {
                System.out.println("\n{");
                JSONObject ordemServico = new JSONObject(mensagem);
                System.out.println("Detalhes da Ordem de Serviço:");
                System.out.println("Timestamp: " + ordemServico.getString("timestamp"));
                System.out.println("Servidor: " + ordemServico.getString("server"));
                System.out.println("Serviço: " + ordemServico.getString("service"));
                System.out.println("Status: " + ordemServico.getString("status"));
                System.out.println("Problema: " + ordemServico.getString("problem"));
                System.out.println("Ação Necessária: " + ordemServico.getString("action_required"));
                System.out.println("}\n");
            } catch (Exception e) {
                System.err.println("Erro ao processar a ordem de serviço: " + e.getMessage());
            }
        };

        canal.basicConsume(ORDEM_SERVICOS_QUEUE, true, callbackEntrega, consumerTag -> { });
    }
}
