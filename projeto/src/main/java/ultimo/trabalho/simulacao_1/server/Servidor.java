package ultimo.trabalho.simulacao_1.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Servidor {
    private int id;
    private static final String EXCHANGE_NAME = "topic_logs";
    private Connection connection;
    private Channel channel;

    public Servidor(int id) {
        this.id = id;
    }

    public void init() throws Exception {
        if (id == 1) {
            System.out.println("Iniciando Servidor " + id + " (Recepção do Drone e Reenvio para Grupo 2)");
            receberERepassarParaGrupo2();
        } else if (id == 2 || id == 3) {
            System.out.println("Iniciando Servidor " + id + " (Recepção do Grupo 2 e Envio para RabbitMQ)");
            // Inicializa a conexão com RabbitMQ
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);

            receberDoGrupo2EMandarParaRabbitMQ();
        }
    }

    @SuppressWarnings("resource")
    private void receberERepassarParaGrupo2() throws IOException {
        int portaGrupo1 = 56789;
        int portaGrupo2 = 56790; // Nova porta para o Grupo 2
        MulticastSocket socketGrupo1 = new MulticastSocket(portaGrupo1);
        InetAddress multicastIPGrupo1 = InetAddress.getByName("225.7.8.9");
        socketGrupo1.joinGroup(new InetSocketAddress(multicastIPGrupo1, portaGrupo1), null);

        MulticastSocket socketGrupo2 = new MulticastSocket();
        InetAddress multicastIPGrupo2 = InetAddress.getByName("225.7.8.10");

        byte[] bufferRecepcao = new byte[1024];
        DatagramPacket pacoteRecepcao = new DatagramPacket(bufferRecepcao, bufferRecepcao.length);

        while (true) {
            System.out.println("Servidor " + id + " aguardando mensagem do Drone no Grupo 1...");
            socketGrupo1.receive(pacoteRecepcao);

            String mensagemRecebida = new String(pacoteRecepcao.getData(), 0, pacoteRecepcao.getLength());
            System.out.println("Servidor " + id + " recebeu: " + mensagemRecebida);

            // Repassa a mensagem para o Grupo 2
            byte[] bufferEnvio = mensagemRecebida.getBytes();
            DatagramPacket pacoteEnvio = new DatagramPacket(
                    bufferEnvio, bufferEnvio.length, multicastIPGrupo2, portaGrupo2);

            socketGrupo2.send(pacoteEnvio);
            System.out.println("Servidor " + id + " repassou mensagem para o Grupo 2.");
        }
    }

    @SuppressWarnings("resource")
    private void receberDoGrupo2EMandarParaRabbitMQ() throws IOException {
        int portaGrupo2 = 56790; // Porta para o Grupo 2
        MulticastSocket socketGrupo2 = new MulticastSocket(portaGrupo2);
        InetAddress multicastIPGrupo2 = InetAddress.getByName("225.7.8.10");
        socketGrupo2.joinGroup(new InetSocketAddress(multicastIPGrupo2, portaGrupo2), null);

        byte[] bufferRecepcao = new byte[1024];
        DatagramPacket pacoteRecepcao = new DatagramPacket(bufferRecepcao, bufferRecepcao.length);

        while (true) {
            System.out.println("Servidor " + id + " aguardando mensagem no Grupo 2...");
            socketGrupo2.receive(pacoteRecepcao);

            String mensagemRecebida = new String(pacoteRecepcao.getData(), 0, pacoteRecepcao.getLength());
            System.out.println("Servidor " + id + " recebeu: " + mensagemRecebida);

            // Extrai a routing key e a mensagem
            String[] partes = mensagemRecebida.split(":", 2);
            String routingKey;
            String mensagem;
            if (partes.length == 2) {
                routingKey = partes[0];
                mensagem = partes[1];
            } else {
                routingKey = "unknown";
                mensagem = mensagemRecebida;
            }

            // Publica a mensagem no RabbitMQ
            try {
                channel.basicPublish(EXCHANGE_NAME, routingKey, null, mensagem.getBytes(StandardCharsets.UTF_8));
                System.out.println("Servidor " + id + " publicou no RabbitMQ: '" + routingKey + "':'" + mensagem + "'");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
