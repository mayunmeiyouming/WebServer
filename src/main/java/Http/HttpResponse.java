package Http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

public class HttpResponse extends HttpResponsePackage {

    private SelectionKey key;
    private SocketChannel channel;

    //http 响应字段
    private static String NEWLINE = "\r\n";
    private String head = "HTTP/1.1 200 OK" + NEWLINE;
    private boolean isSetHead = false;

    private CharBuffer headerBuffer = CharBuffer.allocate(1024);
    private static CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();

    public HttpResponse(SelectionKey key) {
        this.key = key;
        this.channel = (SocketChannel) key.channel();
    }

    public void write(String filename) throws IOException {
        try {
            File file = new File(filename);
            if (file.exists() && !file.isDirectory()) {
                if (!isSetHead) {
                    head = "HTTP/1.1 200 OK" + NEWLINE;
                    statusCode = 200;
                    isSetHead = true;
                }
                setContentLength(file.length());
            } else {
                head = "HTTP/1.1 404 Not Found" + NEWLINE;
                statusCode = 404;
            }
            commitResponseHeader();
            if (statusCode == 200 || statusCode == 302) {
                sendData(file);
            } else if (statusCode == 404) {
                file = new File("WebContent/404/404.html");
                sendData(file);
            }
        } finally {
            isSetHead = false;
        }
    }

    private void sendData(File file) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);
        FileChannel fileChannel = inputStream.getChannel();
        ByteBuffer data = ByteBuffer.allocate(1024);
        try {
            while (fileChannel.read(data) > 0) {
                data.flip();
                while (data.hasRemaining())
                    channel.write(data);
                data.clear();
            }
        } finally {
            fileChannel.close();
            inputStream.close();
        }
    }

    public void commitResponseHeader() throws IOException {
        try {
            headerBuffer.clear();
            headerBuffer.put(head);
            Set<Map.Entry<String, String>> entries = getEntries();
            for (Map.Entry<String, String> entry : entries) {
                appendHeaderValue(entry.getKey(), entry.getValue());
            }
            headerBuffer.put(NEWLINE);
            headerBuffer.flip();
            channel.write(encoder.encode(headerBuffer));
        } finally {
            isSetHead = false;
        }
    }

    private void appendHeaderValue(String name, String value) {
        headerBuffer.put(name);
        headerBuffer.put(": ");
        headerBuffer.put(value);
        headerBuffer.put(NEWLINE);
    }

    public void sendRedirect(String url) throws IOException {
        head = "HTTP/1.1 302" + NEWLINE;
        statusCode = 302;
        isSetHead = true;
        setLocation(url);
        commitResponseHeader();
    }

}
