package ultimo.trabalho.simulacao_1.drone;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Drone {
    public static void main(String[] args) {
        try {
            Drone drone = new Drone();
            drone.init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private final InetAddress multicastIP;
    private final int porta;
    private final MulticastSocket socket;
    private final Random random;

    public Drone() throws IOException {
        this.porta = 56789;
        this.multicastIP = InetAddress.getByName("225.7.8.9");
        this.socket = new MulticastSocket();
        this.random = new Random();
    }

    public void init() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        Runnable enviarMensagem = () -> {
            try {
                String[] mensagens = gerarDadosClimaticos();
                for (String mensagem : mensagens) {
                    byte[] bufferEnvio = mensagem.getBytes();

                    DatagramPacket pacoteEnvio = new DatagramPacket(
                            bufferEnvio, bufferEnvio.length, multicastIP, porta);

                    System.out.println("Drone enviando dados climáticos: " + mensagem);
                    socket.send(pacoteEnvio);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        scheduler.scheduleAtFixedRate(enviarMensagem, 0, 5, TimeUnit.SECONDS);

        scheduler.schedule(() -> {
            scheduler.shutdown();
            System.out.println("Encerrando o serviço de monitoramento de elementos climáticos.");
        }, 30, TimeUnit.SECONDS);
    }

    private String[] gerarDadosClimaticos() {
        double pressaoAtmosferica = 950 + (1050 - 950) * random.nextDouble();
        double radiacaoSolar = 100 + (1000 - 100) * random.nextDouble();
        double temperatura = -10 + (40 + 10) * random.nextDouble();
        double umidade = 0 + (100 - 0) * random.nextDouble();

        String mensagemPressao = String.format("pressao.info:Pressão atmosférica é %.2f hPa", pressaoAtmosferica);
        String mensagemRadiacao = String.format("radiacao.info:Radiação solar é %.2f W/m²", radiacaoSolar);
        String mensagemTemperatura = String.format("temperatura.info:Temperatura é %.2f ºC", temperatura);
        String mensagemUmidade = String.format("umidade.info:Umidade relativa do ar é %.2f%%", umidade);

        return new String[] { mensagemPressao, mensagemRadiacao, mensagemTemperatura, mensagemUmidade };
    }
}
