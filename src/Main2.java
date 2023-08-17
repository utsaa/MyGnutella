import java.io.File;
import java.util.List;

public class Main2 {
    public static void main(String[] args) {
        File file=new File("src/Peer1");
        System.out.println(List.of(file.listFiles()));
    }
}
