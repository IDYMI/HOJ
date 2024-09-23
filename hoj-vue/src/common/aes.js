import CryptoJS from 'crypto-js';

export default {
  methods: {
    // 加密函数
    encrypt(text, secretKey) {
      const iv = CryptoJS.lib.WordArray.random(16); // 生成16字节的随机IV
      const key = CryptoJS.enc.Utf8.parse(secretKey); // 将密钥转换为WordArray

      // 执行AES加密
      const encrypted = CryptoJS.AES.encrypt(text, key, {
        iv: iv,
        mode: CryptoJS.mode.CBC,
        padding: CryptoJS.pad.Pkcs7,
      });

      // 返回格式为: IV(16字节):加密后的数据
      return iv.toString(CryptoJS.enc.Hex) + ':' + encrypted.ciphertext.toString(CryptoJS.enc.Hex);
    },

    // 解密函数
    decrypt(encryptedText, secretKey) {
      const parts = encryptedText.split(':'); // 分离IV和加密后的数据

      if (parts.length !== 2) {
        throw new Error('Invalid encrypted text format. Expected format: IV:Ciphertext');
      }

      const iv = CryptoJS.enc.Hex.parse(parts[0]); // 解析IV
      const encrypted = parts[1]; // 获取加密的数据
      const key = CryptoJS.enc.Utf8.parse(secretKey); // 将密钥转换为WordArray

      // 执行AES解密
      const decrypted = CryptoJS.AES.decrypt(
        { ciphertext: CryptoJS.enc.Hex.parse(encrypted) }, // 将密文解析为WordArray
        key,
        {
          iv: iv,
          mode: CryptoJS.mode.CBC,
          padding: CryptoJS.pad.Pkcs7,
        }
      );

      // 返回解密后的原始字符串
      return decrypted.toString(CryptoJS.enc.Utf8);
    },
  },
};
