package cn.iocoder.boot.framework.common.util;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class CodeGenerator {

    public static class GenParams {
        public static final String URL = "jdbc:mysql://localhost:3306/hmdp?useInformationSchema=true";
        public static final String USER = "root";
        public static final String PWD = "123456";

        public static final String AUTHOR = "czl";
        public static final String[] TABLES = {"tb_follow"};
        public static final String SERVICE_NAME = "follow";
        public static final String TABLE_PREFIX = "tb_";

        // 【修改点 1】: 指定模板在 /templates/ 目录下，并且去掉 .ftl 后缀（生成器会自动拼接）
        public static final String ENTITY_TEMPLATE = "/templates/entity.java";
        public static final String MAPPER_TEMPLATE = "/templates/mapper.java";
        public static final String SERVICE_TEMPLATE = "/templates/service.java";
        public static final String SERVICE_IMPL_TEMPLATE = "/templates/serviceImpl.java";
        public static final String CONTROLLER_TEMPLATE = "/templates/controller.java";

        // 类名与继承
        public static final String DO_SUFFIX = "DO";
        public static final String DO_SUPER = "cn.iocoder.boot.hmdianping.dal.dataobject.BaseDO";
        public static final String MAPPER_SUPER = "com.baomidou.mybatisplus.core.mapper.BaseMapper";

        // 物理路径配置
        public static final String MODULE_NAME = "/hmdianping";
        public static final String PARENT_PKG = "cn.iocoder.boot.hmdianping";
        public static final String DO_PATH = MODULE_NAME + "/src/main/java/cn/iocoder/boot/hmdianping/dal/dataobject/" + SERVICE_NAME;
        public static final String MAPPER_PATH = MODULE_NAME + "/src/main/java/cn/iocoder/boot/hmdianping/dal/mysql/" + SERVICE_NAME;
        public static final String XML_PATH = MODULE_NAME + "/src/main/resources/cn/iocoder/boot/hmdianping/dal/mysql/" + SERVICE_NAME;
        public static final String SERVICE_PATH = MODULE_NAME + "/src/main/java/cn/iocoder/boot/hmdianping/service/" + SERVICE_NAME;
        public static final String SERVICE_IMPL_PATH = MODULE_NAME + "/src/main/java/cn/iocoder/boot/hmdianping/service/" + SERVICE_NAME + "/Impl/";
        public static final String CONTROLLER_PATH = MODULE_NAME + "/src/main/java/cn/iocoder/boot/hmdianping/controller/" + SERVICE_NAME;

        // 开关
        public static final boolean USE_VALIDATION = true;
        public static final boolean AUTO_RESULT_MAP = true;
        public static final boolean USE_BUILDER = true;
    }

    public static void main(String[] args) {
        // 1. 获取并修正工作目录
        String projectPath = Paths.get("").toAbsolutePath().toString();
        if (projectPath.endsWith("framework" + File.separator + "common") || projectPath.endsWith("framework/common")) {
            projectPath = projectPath.replace(File.separator + "framework" + File.separator + "common", "");
            projectPath = projectPath.replace("/framework/common", "");
        }
        System.out.println("[INFO] 当前项目根目录 (projectPath): " + projectPath);

        // 2. 组装自定义配置 Map
        Map<String, Object> customMap = new HashMap<>();
        customMap.put("useVal", GenParams.USE_VALIDATION);
        customMap.put("autoMap", GenParams.AUTO_RESULT_MAP);
        customMap.put("useBuilder", GenParams.USE_BUILDER);

        String finalProjectPath = projectPath;

        // 3. 执行生成
        FastAutoGenerator.create(GenParams.URL, GenParams.USER, GenParams.PWD)
                .globalConfig(builder -> builder
                        .author(GenParams.AUTHOR)
                        .outputDir(finalProjectPath + GenParams.MODULE_NAME + "/src/main/java")
                        .disableOpenDir())
                .packageConfig(builder -> {
                    builder.parent(GenParams.PARENT_PKG)
                            .entity("dal.dataobject." + GenParams.SERVICE_NAME)
                            .mapper("dal.mysql." + GenParams.SERVICE_NAME)
                            .service("service." + GenParams.SERVICE_NAME)
                            .serviceImpl("service." + GenParams.SERVICE_NAME + ".Impl")
                            .controller("controller." + GenParams.SERVICE_NAME);

                    Map<OutputFile, String> pathInfo = new HashMap<>();
                    pathInfo.put(OutputFile.entity, finalProjectPath + GenParams.DO_PATH);
                    pathInfo.put(OutputFile.mapper, finalProjectPath + GenParams.MAPPER_PATH);
                    pathInfo.put(OutputFile.xml, finalProjectPath + GenParams.XML_PATH);
                    pathInfo.put(OutputFile.service, finalProjectPath + GenParams.SERVICE_PATH);
                    pathInfo.put(OutputFile.serviceImpl, finalProjectPath + GenParams.SERVICE_IMPL_PATH);
                    pathInfo.put(OutputFile.controller, finalProjectPath + GenParams.CONTROLLER_PATH);
                    builder.pathInfo(pathInfo);
                })
                .strategyConfig(builder -> {
                    builder.addInclude(GenParams.TABLES)
                            .addTablePrefix(GenParams.TABLE_PREFIX)

                            // 【修改点 2】: 强力加入 enableFileOverride()，确保修改模板后能成功覆盖旧文件
                            .entityBuilder()
                            .enableFileOverride()
                            .superClass(GenParams.DO_SUPER)
                            .formatFileName("%s" + GenParams.DO_SUFFIX)
                            .enableLombok()

                            .mapperBuilder()
                            .enableFileOverride()
                            .superClass(GenParams.MAPPER_SUPER)
                            .formatMapperFileName("%sMapper")
                            .enableMapperAnnotation()

                            .serviceBuilder()
                            .enableFileOverride()
                            .formatServiceFileName("%sService")
                            .formatServiceImplFileName("%sServiceImpl")

                            .controllerBuilder()
                            .enableFileOverride()
                            .formatFileName("%sController")
                            .enableRestStyle();
                })
                .injectionConfig(builder -> builder.customMap(customMap))
                .templateEngine(new FreemarkerTemplateEngine())
                .templateConfig(builder -> builder
                        // 显式指定读取 /templates/ 下的自定义模板
                        .entity(GenParams.ENTITY_TEMPLATE)
                        .mapper(GenParams.MAPPER_TEMPLATE)
                        .service(GenParams.SERVICE_TEMPLATE)
                        .serviceImpl(GenParams.SERVICE_IMPL_TEMPLATE)
                        .controller(GenParams.CONTROLLER_TEMPLATE))
                .execute();

        System.out.println("[SUCCESS] 代码生成完成！请刷新对应的输出目录查看最新文件。");
    }
}