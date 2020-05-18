package cn.com.quick.p;

import android.os.IInterface;
import android.os.RemoteException;

import java.util.List;

/**
 * Date :2020/4/17 14:32
 * Description:
 * History
 */
public interface IMyBinder extends IInterface {

    List<Book> getBookList() throws RemoteException;
    void addBook(Book book) throws RemoteException;
}
