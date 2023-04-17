package TcpSocket;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.quarkus.kafka.client.serialization.JsonbSerializer;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.json.JSONObject;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static java.lang.Runtime.getRuntime;

@QuarkusMain
public class SocketClient {
    public static void main(String[] args) throws Exception {

        String HOST = "localhost";
        Integer PORT = 9999;
        String KEY = "";
        String newKEY = "";

        String processApp = "";
        String processScript = "";

        Properties prop = new Properties();
        try {
            FileInputStream fis = new FileInputStream("application.properties");
            prop.load(fis);
            HOST = prop.getProperty("aisServer");
            PORT = Integer.parseInt(prop.getProperty("aisServerPort"));
            KEY = prop.getProperty("aisKey");
            newKEY = prop.getProperty("aisKey_new");

            processApp = prop.getProperty("processApp");
            processScript = prop.getProperty("processScript");
        } catch (IOException e) {
            HOST = System.getenv("aisServer");
            PORT = Integer.parseInt(System.getenv("aisServerPort"));
            newKEY = System.getenv("aisKey_new");

            processApp = System.getenv("processApp");
            processScript = System.getenv("processScript");

            e.printStackTrace();
        }

        char[] keyCode_new = newKEY.toCharArray();
        byte[] aisKey = new byte[keyCode_new.length/2];
        int cnt = 0;
        int cntByte = 0;
        String hexValue ="";

        for(char i: keyCode_new) {
            hexValue += keyCode_new[cnt];

            if(cnt % 2 != 0){
                int hexDecimal = Integer.decode("0x" + hexValue);
                aisKey[cntByte] = (byte) hexDecimal;
                hexValue = "";
                cntByte += 1;
            }

            cnt += 1;
        }

        // Create a Kafka producer
//        Properties props = new Properties();
//        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
//        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//        KafkaProducer<String, Object> producer = new KafkaProducer<>(props);


        while (true) {
            System.out.println("Attempt to Connect to the server......\n");

            try (
                    Socket socket = new Socket(HOST, PORT);
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));
            ) {
                out.println(new String(aisKey, StandardCharsets.UTF_8));
                System.out.println("Connected to the server......\n");

                String msgTyp5_1 = "";

                do {
                    var recv = in.readLine();
                    //System.out.println(recv);

                    if(recv.startsWith("!NMVDM")) {
                        System.out.println(recv);

                        String[] nmea = recv.split(",");
                        System.out.println(nmea[1]);

                        if(Integer.parseInt(nmea[1]) == 2){
                            if(Integer.parseInt(nmea[2]) == 1){
                                msgTyp5_1 = recv;
                            }
                            else {
                                String[] cmd = {processApp, processScript, msgTyp5_1, recv};
                                Process p = Runtime.getRuntime().exec(cmd);
                                p.waitFor();

                                String line = "", output = "";
                                StringBuilder sb = new StringBuilder();

                                BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                                while ((line = br.readLine())!= null) {
                                    sb = sb.append(line).append("\n");
                                }

                                output = sb.toString();

                                if(!output.equals("")){
                                    JSONObject jsonObject = new JSONObject(output);
                                    System.out.println(jsonObject);
                                }
                            }
                        }
                        else {
                            String[] cmd = {processApp, processScript, recv};
                            Process p = Runtime.getRuntime().exec(cmd);
                            p.waitFor();

                            String line = "", output = "";
                            StringBuilder sb = new StringBuilder();

                            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                            while ((line = br.readLine())!= null) {
                                sb = sb.append(line).append("\n");
                            }

                            output = sb.toString();

                            if(!output.equals("")){
                                JSONObject jsonObject = new JSONObject(output);
                                System.out.println(jsonObject);
                            }
                        }
                    }

                    if(recv == null) {
                        //producer.flush();
                        break;
                    }

                    // Create a producer record
                    //ProducerRecord<String, Object> record = new ProducerRecord<>("ais-msg", recv);

                    // Send the record
                    //producer.send(record);

                } while (true);
            } catch (IOException e) {
                //producer.flush();
                System.err.println("Couldn't get I/O for the connection to " +
                        HOST);
                //System.exit(1);
            }

            Thread.sleep(5000);
        }

        //System.out.println("Exiting......\n");
        //Quarkus.waitForExit();
    }
}
