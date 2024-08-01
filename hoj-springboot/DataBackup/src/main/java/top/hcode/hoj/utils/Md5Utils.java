package top.hcode.hoj.utils;

import java.security.MessageDigest;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.sql.Connection;
import org.apache.commons.codec.binary.Hex;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import cn.hutool.crypto.SecureUtil;

@Component
@Slf4j(topic = "hoj")
public class Md5Utils {

    /**
     * 生成盐和加盐后的MD5码，并将盐混入到MD5码中,对MD5密码进行加强
     *
     * @param password 原始密码
     * @return 加盐的MD5字符串
     *
     */
    public static String generateSaltPassword(String password) {

        Random random = new Random();

        // 生成一个16位的随机数，也就是所谓的盐
        StringBuilder stringBuilder = new StringBuilder(16);
        stringBuilder.append(random.nextInt(99999999)).append(random.nextInt(99999999));
        int len = stringBuilder.length();
        if (len < 16) {
            for (int i = 0; i < 16 - len; i++) {
                stringBuilder.append("0");
            }
        }
        String salt = stringBuilder.toString();

        // 将盐加到明文中，并生成新的MD5码
        password = md5Hex(password + salt);

        // 将盐混到新生成的MD5码中，之所以这样做是为了后期更方便的校验明文和秘文
        char[] cs = new char[48];
        for (int i = 0; i < 48; i += 3) {
            cs[i] = password.charAt(i / 3 * 2);
            char c = salt.charAt(i / 3);
            cs[i + 1] = c;
            cs[i + 2] = password.charAt(i / 3 * 2 + 1);
        }
        return new String(cs);
    }

    /**
     * 生成盐和加盐后的MD5码，并将盐混入到MD5码中,对MD5密码进行加强
     *
     * @param password 原始密码
     * @return 加盐的MD5字符串
     *
     */
    public String generateSaltMD5Password(String password) {
        // md5 加密
        password = SecureUtil.md5(password);

        Random random = new Random();

        // 生成一个16位的随机数，也就是所谓的盐
        StringBuilder stringBuilder = new StringBuilder(16);
        stringBuilder.append(random.nextInt(99999999)).append(random.nextInt(99999999));
        int len = stringBuilder.length();
        if (len < 16) {
            for (int i = 0; i < 16 - len; i++) {
                stringBuilder.append("0");
            }
        }
        String salt = stringBuilder.toString();

        // 将盐加到明文中，并生成新的MD5码
        password = md5Hex(password + salt);

        // 将盐混到新生成的MD5码中，之所以这样做是为了后期更方便的校验明文和秘文
        char[] cs = new char[48];
        for (int i = 0; i < 48; i += 3) {
            cs[i] = password.charAt(i / 3 * 2);
            char c = salt.charAt(i / 3);
            cs[i + 1] = c;
            cs[i + 2] = password.charAt(i / 3 * 2 + 1);
        }
        return new String(cs);
    }

    /**
     * 验证明文和加盐后的MD5码是否匹配
     *
     * @param password 原始密码
     * @param md5      加盐md5密码
     * @return 加盐的MD5字符串
     *
     */
    public boolean verifySaltPassword(String password, String md5) {
        // md5 加密
        password = SecureUtil.md5(password);

        // 先从MD5码中取出之前加的盐和加盐后生成的MD5码
        char[] cs1 = new char[32];
        char[] cs2 = new char[16];
        for (int i = 0; i < 48; i += 3) {
            cs1[i / 3 * 2] = md5.charAt(i);
            cs1[i / 3 * 2 + 1] = md5.charAt(i + 2);
            cs2[i / 3] = md5.charAt(i + 1);
        }
        String salt = new String(cs2);
        // 比较二者是否相同
        return md5Hex(password + salt).equals(new String(cs1));
    }

    /**
     * 生成MD5密码
     *
     * @param password 原始密码
     * @return MD5字符串
     *
     */
    private static String md5Hex(String src) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] bs = md5.digest(src.getBytes());
            return new String(new Hex().encode(bs));
        } catch (Exception e) {
            return null;
        }
    }

    // 数据库 URL，用户名和密码
    private static final String DB_URL = "jdbc:mysql://localhost:3306/hoj";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "hoj123456";
    private static final int THREAD_COUNT = 10; // 线程数
    private static final int BATCH_SIZE = 1000; // 批量大小

    public static void updateAllPasswords() throws SQLException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

        try {
            connection.setAutoCommit(false);

            String selectSQL = "SELECT username, password FROM user_info";
            PreparedStatement selectStatement = connection.prepareStatement(selectSQL);
            ResultSet resultSet = selectStatement.executeQuery();

            List<UserInfo> userList = new ArrayList<>();
            while (resultSet.next()) {
                String username = resultSet.getString("username");
                String originalPassword = resultSet.getString("password");
                userList.add(new UserInfo(username, originalPassword));
            }

            int totalUsers = userList.size();
            int chunkSize = totalUsers / THREAD_COUNT;

            List<Future<Void>> futures = new ArrayList<>();
            for (int i = 0; i < THREAD_COUNT; i++) {
                int start = i * chunkSize;
                int end = (i == THREAD_COUNT - 1) ? totalUsers : start + chunkSize;
                List<UserInfo> sublist = userList.subList(start, end);
                futures.add(executor.submit(new PasswordUpdateTask(sublist)));
            }

            for (Future<Void> future : futures) {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }

            connection.commit();
            System.out.println("All passwords updated successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
        } finally {
            if (connection != null) {
                connection.close();
            }
            executor.shutdown();
        }
    }

    private static class UserInfo {
        String username;
        String password;

        UserInfo(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    private static class PasswordUpdateTask implements Callable<Void> {
        private List<UserInfo> userList;

        PasswordUpdateTask(List<UserInfo> userList) {
            this.userList = userList;
        }

        @Override
        public Void call() throws Exception {
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            connection.setAutoCommit(false);

            String updateSQL = "UPDATE user_info SET password = ? WHERE username = ?";
            PreparedStatement updateStatement = connection.prepareStatement(updateSQL);

            try {
                int count = 0;
                for (UserInfo user : userList) {
                    String processedPassword = generateSaltPassword(user.password);
                    updateStatement.setString(1, processedPassword);
                    updateStatement.setString(2, user.username);
                    updateStatement.addBatch();

                    if (++count % BATCH_SIZE == 0) {
                        updateStatement.executeBatch();
                    }
                }
                updateStatement.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                e.printStackTrace();
                connection.rollback();
            } finally {
                if (updateStatement != null) {
                    updateStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            }

            return null;
        }
    }

    // public static void main(String[] args) throws SQLException {
    // updateAllPasswords();
    // }
}
