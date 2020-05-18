package cn.com.quick.p;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/**
 * Date :2020/4/17 14:41
 * Description:
 * History
 */
public abstract class MyBinderImpl extends Binder implements IMyBinder {
    private static final String DESCRIPTOR = "cn.com.quick.p.IMyBinder";
    private static final int TRANSACTION_getBookList = IBinder.FIRST_CALL_TRANSACTION;
    private static final int TRANSACTION_addBook = IBinder.FIRST_CALL_TRANSACTION + 1;

    public MyBinderImpl() {
        attachInterface(this, DESCRIPTOR);
    }

    public static IMyBinder asInterface(IBinder obj) {
        if (obj == null) {
            return null;
        }
        IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
        if (iin instanceof IMyBinder) {
            return (IMyBinder) iin;
        }
        return new MyBinderImpl.Proxy(obj);
    }


    @Override
    public IBinder asBinder() {
        return this;
    }

    @Override
    protected boolean onTransact(int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
        if (reply == null) {
            return super.onTransact(code, data, null, flags);
        }
        switch (code) {
            case INTERFACE_TRANSACTION:
                reply.writeString(DESCRIPTOR);
                break;
            case TRANSACTION_getBookList:
                data.enforceInterface(DESCRIPTOR);
                List<Book> _bookList = this.getBookList();
                reply.writeNoException();
                reply.writeTypedList(_bookList);
                return true;
            case TRANSACTION_addBook:
                data.enforceInterface(DESCRIPTOR);
                Book book = (data.readInt() != 0) ? Book.CREATOR.createFromParcel(data) : null;
                this.addBook(book);
                reply.writeNoException();
                return true;
        }

        return super.onTransact(code, data, reply, flags);
    }

    private static class Proxy implements IMyBinder {

        private IBinder mRemote;

        private Proxy(IBinder obj) {
            this.mRemote = obj;
        }

        @Override
        public List<Book> getBookList() throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();
            List<Book> _result;
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                mRemote.transact(TRANSACTION_getBookList, _data, _reply, 0);
                _reply.readException();
                _result = _reply.createTypedArrayList(Book.CREATOR);
            } finally {
                _reply.recycle();
                _data.recycle();
            }
            return _result;
        }

        @Override
        public void addBook(Book book) throws RemoteException {
            Parcel _data = Parcel.obtain();
            Parcel _reply = Parcel.obtain();
            try {
                _data.writeInterfaceToken(DESCRIPTOR);
                if (book != null) {
                    _data.writeInt(1);
                    book.writeToParcel(_data, 0);
                } else {
                    _data.writeInt(0);
                }
                mRemote.transact(TRANSACTION_addBook, _data, _reply, 0);
                _reply.readException();
            } finally {
                _reply.recycle();
                _data.recycle();
            }
        }

        @Override
        public IBinder asBinder() {
            return mRemote;
        }
    }
}
