import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class PuppeteerTest {
    public static void main(String[] args) {
        try {
            System.out.println("启动浏览器访问腾讯视频...");

            // 设置ChromeDriver
            WebDriverManager.chromedriver().setup();

            // 配置Chrome选项
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless"); // 无头模式
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");

            // 创建WebDriver
            WebDriver driver = new ChromeDriver(options);

            try {
                // 访问腾讯视频
                driver.get("https://v.qq.com");

                // 等待页面加载
                Thread.sleep(3000);

                // 获取页面标题
                String title = driver.getTitle();
                System.out.println("腾讯视频网站标题: " + title);

                // 获取当前URL
                String currentUrl = driver.getCurrentUrl();
                System.out.println("当前URL: " + currentUrl);

            } finally {
                // 关闭浏览器
                driver.quit();
                System.out.println("浏览器已关闭");
            }

        } catch (Exception e) {
            System.err.println("发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}