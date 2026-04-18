const puppeteer = require('puppeteer');

async function getTencentVideoTitle() {
    console.log('启动浏览器访问腾讯视频...');

    let browser;
    try {
        // 启动浏览器
        browser = await puppeteer.launch({
            headless: 'new', // 使用新的无头模式
            args: [
                '--no-sandbox',
                '--disable-setuid-sandbox',
                '--disable-dev-shm-usage',
                '--disable-gpu',
                '--window-size=1920,1080'
            ]
        });

        // 创建新页面
        const page = await browser.newPage();

        // 设置用户代理
        await page.setUserAgent('Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36');

        // 访问腾讯视频
        console.log('正在访问 https://v.qq.com...');
        await page.goto('https://v.qq.com', {
            waitUntil: 'networkidle2', // 等待网络空闲
            timeout: 30000 // 30秒超时
        });

        // 等待页面加载
        await page.waitForTimeout(3000);

        // 获取页面标题
        const title = await page.title();
        console.log('腾讯视频网站标题:', title);

        // 获取当前URL
        const currentUrl = page.url();
        console.log('当前URL:', currentUrl);

        // 可选：截图保存
        await page.screenshot({ path: 'tencent-video.png', fullPage: true });
        console.log('页面截图已保存为 tencent-video.png');

        return title;

    } catch (error) {
        console.error('发生错误:', error.message);
        throw error;
    } finally {
        // 关闭浏览器
        if (browser) {
            await browser.close();
            console.log('浏览器已关闭');
        }
    }
}

// 执行函数
getTencentVideoTitle()
    .then(title => {
        console.log('成功获取标题:', title);
        process.exit(0);
    })
    .catch(error => {
        console.error('执行失败:', error);
        process.exit(1);
    });