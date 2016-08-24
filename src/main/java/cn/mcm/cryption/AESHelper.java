package cn.mcm.cryption;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;  
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;  
import java.io.RandomAccessFile;  
import java.nio.ByteBuffer;  
import java.nio.channels.FileChannel;  
import java.security.InvalidAlgorithmParameterException;  
import java.security.InvalidKeyException;  
import java.security.Key;
import java.security.NoSuchAlgorithmException;  
import java.security.NoSuchProviderException;
import java.security.SecureRandom;  
import java.security.spec.AlgorithmParameterSpec;
import java.util.Random;

import javax.crypto.BadPaddingException;  
import javax.crypto.Cipher;  
import javax.crypto.IllegalBlockSizeException;  
import javax.crypto.KeyGenerator;  
import javax.crypto.NoSuchPaddingException;  
import javax.crypto.SecretKey;  
import javax.crypto.spec.IvParameterSpec;  
import javax.crypto.spec.SecretKeySpec;  
import android.util.Log;  
public class AESHelper {  
    public static final String TAG = AESHelper.class.getSimpleName();  
    public static int block = 1024 * 1024;
  
    Runtime mRuntime = Runtime.getRuntime();  
  
    @SuppressWarnings("resource")  
    public boolean AESCipher(int cipherMode, String sourceFilePath,  
            String targetFilePath, String seed) throws Exception {  
        boolean result = false;  
        RandomAccessFile sourceF = null;  
        RandomAccessFile targetF = null;
        FileChannel sFC=null;
        FileChannel tFC=null;
        try {  
        	Cipher mCipher = Cipher.getInstance("AES/CFB/NoPadding");  
            
            byte[] rawkey = getRawKey(seed.getBytes());  
            File sourceFile = new File(sourceFilePath);  
            File targetFile = new File(targetFilePath); 
            if (cipherMode != Cipher.ENCRYPT_MODE  
                    && cipherMode != Cipher.DECRYPT_MODE) {  
                Log.d(TAG,  
                        "Operation mode error, should be encrypt or decrypt!");  
                return false;  
            }  
            sourceF = new RandomAccessFile(sourceFile, "r"); 
            sFC=sourceF.getChannel();
            targetF = new RandomAccessFile(targetFile, "rw");  
            tFC=targetF.getChannel();
  
            SecretKeySpec secretKey = new SecretKeySpec(rawkey, "AES");  
  
            mCipher.init(cipherMode, secretKey, new IvParameterSpec(  
                    new byte[mCipher.getBlockSize()]));  
  
            ByteBuffer byteData = ByteBuffer.allocate(1024);  
            while (sFC.read(byteData) != -1) {
                byteData.flip();  
  
                byte[] byteList = new byte[byteData.remaining()];  
                byteData.get(byteList, 0, byteList.length);
                byte[] bytes = mCipher.doFinal(byteList);  
                tFC.write(ByteBuffer.wrap(bytes));  
                byteData.clear();  
            }  
  
            result = true;  
        } catch (IOException | InvalidKeyException  
                | InvalidAlgorithmParameterException  
                | IllegalBlockSizeException | BadPaddingException e) {  
            Log.d(TAG, e.getMessage());  
  
        } finally {  
            try {  
                if (sFC != null) {  
                	sFC.close();  
                }  
                if (tFC != null) {  
                	tFC.close();  
                }  
            } catch (IOException e) {  
                Log.d(TAG, e.getMessage());  
            } 
        }
        return result;  
    } 

	public String encrypt(String seed, String source) {
		byte[] result = null;
		try {
			byte[] rawkey = getRawKey(seed.getBytes());
			result = encrypt(rawkey, source.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
		String content = toHex(result);
		return content;

	}

	public String decrypt(String seed, String encrypted) {
		byte[] rawKey;
		try {
			rawKey = getRawKey(seed.getBytes());
			byte[] enc = toByte(encrypted);
			//byte[] enc = encrypted.getBytes();
			byte[] result = decrypt(rawKey, enc);
			//String string = new String(result);
			//return string;
			return new String(result);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static byte[] getRawKey(byte[] seed) throws NoSuchAlgorithmException, NoSuchProviderException{     
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
         SecureRandom sr = null;  
       if (android.os.Build.VERSION.SDK_INT >=  17) {  
         sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");  
       } else {  
         sr = SecureRandom.getInstance("SHA1PRNG");  
       }   
        sr.setSeed(seed);     
        kgen.init(128, sr); //256 bits or 128 bits,192bits  
        SecretKey skey = kgen.generateKey();     
        byte[] raw = skey.getEncoded();     
        return raw;     
    } 
	

	private byte[] encrypt(byte[] raw, byte[] input) throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		// Cipher cipher = Cipher.getInstance("AES");
		Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(
				new byte[cipher.getBlockSize()]));
		byte[] encrypted = cipher.doFinal(input);
		return encrypted;
	}

	private byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(
				new byte[cipher.getBlockSize()]));
		byte[] decrypted = cipher.doFinal(encrypted);
		return decrypted;
	}

	public String toHex(String txt) {
		return toHex(txt.getBytes());
	}

	public String fromHex(String hex) {
		return new String(toByte(hex));
	}

	public byte[] toByte(String hexString) {
		int len = hexString.length() / 2;
		byte[] result = new byte[len];
		for (int i = 0; i < len; i++)
			result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2),
					16).byteValue();
		return result;
	}

	//ת��16����
	public String toHex(byte[] buf) {
		if (buf == null || buf.length <= 0)
			return "";
		StringBuffer result = new StringBuffer(2 * buf.length);
		for (int i = 0; i < buf.length; i++) {
			appendHex(result, buf[i]);
		}
		return result.toString();
	} 
	
	public byte[] hex2byte(String inputString) {
        if (inputString == null || inputString.length() < 2) {
            return new byte[0];
        }
        int l = inputString.length() / 2;
        byte[] result = new byte[l];
        for (int i = 0; i < l; ++i) {
            String tmp = inputString.substring(2 * i, 2 * i + 2);
            result[i] = (byte) (Integer.parseInt(tmp, 16) & 0xFF);
        }
        return result;
    }

	private void appendHex(StringBuffer sb, byte b) {
		final String HEX = "0123456789ABCDEF";
		sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
	}
	
	//���ļ��ֿ�
	public static void blockEncCode(String inputFileName, String outputFileName , String seed) {
		  FileInputStream fileInputStream = null;
		  BufferedInputStream bufferedInputStream = null;
		  RandomAccessFile randomAccessFile = null;
		  try {
		   randomAccessFile = new RandomAccessFile(outputFileName, "rw");
		  } catch (Exception e) {
		   System.out.println(e.toString());
		  }
		  byte[] intBytes = null;
		  try {
		   fileInputStream = new FileInputStream(inputFileName);
		   bufferedInputStream = new BufferedInputStream(fileInputStream);
		   byte[] buf = new byte[block];
		   byte[] tempByte = null;
		   int length = 0;
		   while ((length = bufferedInputStream.read(buf)) != -1) {
		    tempByte = new byte[length];
		    System.arraycopy(buf, 0, tempByte, 0, length);
		    tempByte = AESHelper.getEncCode(tempByte, seed);
		    intBytes = intToByte(tempByte.length);
		    randomAccessFile.write(intBytes);
		    randomAccessFile.write(tempByte);
		   }
		  } catch (Exception e) {
		   System.out.println(e.toString());
		  } finally {
		   if (randomAccessFile != null) {
		    try {
		     randomAccessFile.close();
		     randomAccessFile = null;
		    } catch (IOException e) {
		     e.printStackTrace();
		    }
		   }
		   if (bufferedInputStream != null) {
		    try {
		     bufferedInputStream.close();
		     bufferedInputStream = null;
		    } catch (IOException e) {
		    }
		   }
		  }
		 }
		 
	public static void blockDecCode(String inputFileName, String outputFileName ,String seed) {
		  RandomAccessFile randomAccessFile = null;
		  BufferedOutputStream bufferedOutputStream = null;
		  int blockLength = -1;
		  byte[] tempByte = null;
		  long readLength = -1;
		  long fileLength = -1;
		  try {
		   randomAccessFile = new RandomAccessFile(inputFileName, "r");
		   bufferedOutputStream = new BufferedOutputStream(
		     new FileOutputStream(outputFileName));
		   fileLength = randomAccessFile.length();
		   
		   byte[] intBytes = new byte[4];
		   
		   if (fileLength > intBytes.length) {
		    randomAccessFile.read(intBytes, 0, intBytes.length);
		    blockLength = bytesToInt(intBytes);
		    readLength = intBytes.length;
		   }
		   while (blockLength != -1) {
		    tempByte = new byte[blockLength];
		    blockLength = randomAccessFile.read(tempByte);
		    readLength += blockLength;
		    tempByte = AESHelper.getDecCode(tempByte, seed);
		    bufferedOutputStream.write(tempByte);
		    if (readLength < fileLength) {
		     randomAccessFile.read(intBytes, 0, intBytes.length);
		     blockLength = bytesToInt(intBytes);
		     
		     if (blockLength != -1) {
		      readLength += intBytes.length;
		      continue;
		     }
		     break;
		    }
		    break;
		   }
		  } catch (Exception e) {
		   System.out.println(readLength);
		   e.printStackTrace();
		  } finally {
		   if (randomAccessFile != null) {
		    try {
		     randomAccessFile.close();
		     randomAccessFile = null;
		    } catch (IOException e) {
		    }
		   }
		   if (bufferedOutputStream != null) {
		    try {
		     bufferedOutputStream.close();
		     bufferedOutputStream = null;
		    } catch (IOException e) {
		    }
		   }
		  }
		 }
	public static byte[] intToByte(int i) {
		  byte[] bt = new byte[4];
		  bt[0] = (byte) (0xff & i);
		  bt[1] = (byte) ((0xff00 & i) >> 8);
		  bt[2] = (byte) ((0xff0000 & i) >> 16);
		  bt[3] = (byte) ((0xff000000 & i) >> 24);
		  return bt;
		 }
	public static int bytesToInt(byte[] bytes) {
		  int num = bytes[0] & 0xFF;
		  num |= ((bytes[1] << 8) & 0xFF00);
		  num |= ((bytes[2] << 16) & 0xFF0000);
		  num |= ((bytes[3] << 24) & 0xFF000000);
		  return num;
		 }
	public static byte[] getEncCode(byte[] byteE, String seed) {
		  byte[] byteFina = null;
		  Cipher cipher = null;
		  
		  try {
			  SecretKeySpec skeySpec = new SecretKeySpec(getRawKey(seed.getBytes()), "AES");
			  
			  cipher = Cipher.getInstance("AES/CFB/NoPadding");
			  cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(new byte[cipher.getBlockSize()]));
			  //cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(new byte[cipher.getBlockSize()]));
		   byteFina = cipher.doFinal(byteE);
		  }
		  catch (Exception e) {
		   e.printStackTrace();
		  }
		  finally {
		   cipher = null;
		  }
		  
		  return byteFina;
		 }
		 
	public static byte[] getDecCode(byte[] byteD, String seed) {
		  byte[] byteFina = null;
		  Cipher cipher = null;
		  
		  try {
			  SecretKeySpec skeySpec = new SecretKeySpec(getRawKey(seed.getBytes()), "AES");
			  cipher = Cipher.getInstance("AES/CFB/NoPadding");
			  cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(
						new byte[cipher.getBlockSize()]));
		      byteFina = cipher.doFinal(byteD);
		  }
		  catch (Exception e) {
		   e.printStackTrace();
		  }
		  finally {
		   cipher = null;
		  }
		  
		  return byteFina;
		 }
}