package top.hcode.hoj.pojo.bo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "hoj")
public class File_ {

    /**
     * 列出指定目录中的所有文件名
     *
     * @param directoryPath 要列出文件名的目录路径
     * @return 文件名列表
     * @throws IOException 如果读取目录失败
     */
    public static List<String> listFileNames(String directoryPath) {
        try {
            Path path = Paths.get(directoryPath);

            // 确保目录存在且是一个目录
            if (Files.exists(path) && Files.isDirectory(path)) {
                try {
                    // 使用 Files.list 方法列出目录中的所有文件和子目录
                    return Files.list(path)
                            .filter(Files::isRegularFile) // 仅保留文件
                            .map(p -> p.getFileName().toString()) // 获取文件名
                            .collect(Collectors.toList());
                } catch (IOException e) {
                    throw new IOException("Failed to list files in directory: " + directoryPath, e);
                }
            } else {
                throw new IOException("Path does not exist or is not a directory: " + directoryPath);
            }
        } catch (IOException e) {
            log.error("读取目录 {" + directoryPath + "} 失败-------------->{}", e.getMessage());
        }
        return null;
    }

    public static void zip(File sourceFile, File outputFile) {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputFile))) {
            compress(sourceFile, sourceFile.getName(), zos);
        } catch (IOException e) {
            log.error("压缩 {" + sourceFile + "} 到 {" + outputFile + "} 失败-------------->{}", e.getMessage());
        }
    }

    private static void compress(File file, String name, ZipOutputStream zos) throws IOException {
        if (file.isDirectory()) {
            // 空目录
            if (name.endsWith("/")) {
                zos.putNextEntry(new ZipEntry(name));
                zos.closeEntry();
            } else {
                name += "/";
                zos.putNextEntry(new ZipEntry(name));
                zos.closeEntry();
            }
            // 压缩目录下的文件
            File[] files = file.listFiles();
            if (files != null) {
                for (File childFile : files) {
                    compress(childFile, name + childFile.getName(), zos);
                }
            }
        } else {
            // 压缩文件
            zos.putNextEntry(new ZipEntry(name));
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }
            }
            zos.closeEntry();
        }
    }

}
