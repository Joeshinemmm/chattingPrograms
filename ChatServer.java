package chatMessage3;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatServer {

    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(8888);
        List<ChatThread> list = new CopyOnWriteArrayList<>();

        while (true) {
            Socket socket = serverSocket.accept();
            ChatThread chatThread = new ChatThread(socket, list);
            chatThread.start();
        }
    }
}

class ChatThread extends Thread {
    private String name;
    private BufferedReader br;
    private PrintWriter pw;
    private Socket socket;
    private List<ChatThread> list;

    public ChatThread(Socket socket, List<ChatThread> list) throws IOException {
        this.socket = socket;
        this.list = list;
        this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.name = br.readLine();
        this.list.add(this);
    }

    public void sendMessage(String msg) {
        pw.println(msg);
        pw.flush();
    }

    @Override
    public void run() {
        try {
            broadCast(name + "님이 연결되었습니다", false);

            String line = null;

            while ((line = br.readLine()) != null) {
                if ("/quit".equals(line)) {
                    broadCast(name + "님이 퇴장하셨습니다.", true);
                    throw new RuntimeException("접속 종료");
                }
                broadCast(name + ": " + line, true);
            }
        } catch (IOException e) {
            // ChatThread의 연결이 끊어짐
        } finally {
            broadCast(name + "님의 연결이 끊어졌습니다", false);
            this.list.remove(this);
            try {
                br.close();
                pw.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void broadCast(String msg, boolean includeMe) {
        List<ChatThread> chatThreads = new CopyOnWriteArrayList<>(this.list);

        for (ChatThread ct : chatThreads) {
            if (!includeMe && ct == this) {
                continue;
            }
            ct.sendMessage(msg);
        }
    }
}
