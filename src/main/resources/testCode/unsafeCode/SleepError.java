/**
 * @author: serein
 * @date: 2024/9/10 14:41
 * @description: 无限睡眠阻塞程序
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {
        long ONE_HOUR = 60 * 60 * 1000L;
        Thread.sleep(ONE_HOUR);
        System.out.println("sleep执行结束");
    }
}
