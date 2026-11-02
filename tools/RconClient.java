import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Sends commands to the local dev server over RCON, reading port and password from run/server.properties.
 * Usage (from the repo root, JDK 17): java tools/RconClient.java "worldborder get" "test runall"
 */
public class RconClient {
    private static final int TYPE_LOGIN = 3;
    private static final int TYPE_COMMAND = 2;

    private record Packet(int id, String body) {
    }

    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        try (Reader reader = Files.newBufferedReader(Path.of("run/server.properties"))) {
            props.load(reader);
        }
        String host = props.getProperty("server-ip", "").isBlank() ? "127.0.0.1" : props.getProperty("server-ip");
        int port = Integer.parseInt(props.getProperty("rcon.port", "25575"));

        try (Socket socket = new Socket(host, port)) {
            socket.setSoTimeout(30_000);
            DataInputStream in = new DataInputStream(socket.getInputStream());
            OutputStream out = socket.getOutputStream();

            send(out, 1, TYPE_LOGIN, props.getProperty("rcon.password", ""));
            if (read(in).id() != 1) {
                throw new IOException("RCON login failed");
            }
            int id = 2;
            for (String command : args) {
                send(out, id++, TYPE_COMMAND, command);
                System.out.println("> " + command);
                System.out.println(read(in).body());
            }
        }
    }

    private static void send(OutputStream out, int id, int type, String body) throws IOException {
        byte[] payload = body.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(14 + payload.length).order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(10 + payload.length).putInt(id).putInt(type).put(payload).put((byte) 0).put((byte) 0);
        out.write(buffer.array());
        out.flush();
    }

    private static Packet read(DataInputStream in) throws IOException {
        byte[] lengthBytes = new byte[4];
        in.readFully(lengthBytes);
        int length = ByteBuffer.wrap(lengthBytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
        byte[] data = new byte[length];
        in.readFully(data);
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        int id = buffer.getInt();
        buffer.getInt();
        String body = new String(data, 8, length - 10, StandardCharsets.UTF_8);
        return new Packet(id, body);
    }
}
