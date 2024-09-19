import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author: serein
 * @date: 2024/9/10 16:00
 * @description: 非法读文件，导致信息泄露
 */
public class Main {

    public static void main(String[] args) throws IOException {
        String userDir = System.getProperty("user.dir");
        String privateFilePath = userDir + File.separator + "src/main/resources/application.yml";
        List<String> allLines = Files.readAllLines(Paths.get(privateFilePath));
        System.out.println(String.join("\n", allLines));
    }
}
