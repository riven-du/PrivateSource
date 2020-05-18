package cn.com.quick.p;

import androidx.annotation.Nullable;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Date :2020/4/17 14:20
 * Description:
 * History
 */
public class MySerializable implements Serializable {

    public static void write() {
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(""));
            objectOutputStream.writeObject(new MySerializable());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void read() {

    }
}
