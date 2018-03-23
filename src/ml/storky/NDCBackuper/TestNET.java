package ml.storky.NDCBackuper;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.*;

/**
 * Created by storky on 6.6.16.
 */
public class TestNET {
    public static void main(String[] args) {
        JSch jSch = new JSch();
        try {
            Session session = jSch.getSession("paty", "12.0.0.2", 22);
            //session.getHostKeyRepository().add(new HostKey("localhost", HostKey.SSHRSA, ));

            session.setConfig("StrictHostKeyChecking", "no");
            //
            session.setPassword("cisco");
            session.connect();
            System.out.println("connected");
            ChannelShell shell = (ChannelShell) session.openChannel("shell");
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(shell.getOutputStream()));
            BufferedReader br = new BufferedReader(new InputStreamReader(shell.getInputStream()));
            shell.connect();
            //System.out.println(br.readLine());
            bw.write("terminal length 0");
            bw.newLine();
            System.out.println("term len odesl");
            bw.write("show running-config");
            bw.newLine();
            System.out.println("show run odesl");
            bw.flush();
            System.out.println("flush");
            br.readLine(); // empty line
            br.readLine(); // term len
            br.readLine(); // show run
            br.readLine(); // build conf
            br.readLine(); // build space
            br.readLine(); // build config length

            String line = "";
            StringBuilder builder = new StringBuilder();
            boolean rdy = true;

            while(!(line = br.readLine()).equals("end")) {
                System.out.println(line);
                builder.append(line);
                builder.append("\n");
            }

            session.disconnect();
            //return builder.toString();


        } catch (JSchException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
