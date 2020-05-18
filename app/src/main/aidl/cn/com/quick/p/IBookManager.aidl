// IBookManager.aidl
package cn.com.quick.p;

import cn.com.quick.p.Book;

interface IBookManager {

    List<Book> getBookList();

    void addBook(in Book book);
    
}
