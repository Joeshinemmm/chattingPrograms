package chatMessage3;

import java.io.*;
import java.net.Socket;

public class ChatClient {

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("사용법: java chatMessage3.ChatClient 닉네임");
            return;
        }
        String name = args[0];
        Socket socket = new Socket("127.0.0.1", 8888);

        try (
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                BufferedReader input = new BufferedReader(new InputStreamReader(System.in))
        ) {
            pw.println(name); // 닉네임 전송
            pw.flush();

            InputThread inputThread = new InputThread(br);
            inputThread.start(); // 백그라운드로 서버가 보내준 메시지를 읽어 화면에 출력

            String line = null;
            while ((line = input.readLine()) != null) {
                if ("/quit".equals(line)) {
                    pw.println("/quit");
                    pw.flush();
                    inputThread.interrupt();
                    break;
                }
                pw.println(line);
                pw.flush();
            }
            System.out.println("접속 종료.");
        } catch (Exception e) {
            System.out.println("접속 종료.");
        }
    }
}

class InputThread extends Thread {
    private BufferedReader br;

    public InputThread(BufferedReader br) {
        this.br = br;
    }

    @Override
    public void run() {
        try {
            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("접속이 종료되었습니다");
        }
    }
}