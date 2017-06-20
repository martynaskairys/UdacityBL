package com.martynaskairys.udacitybl;


public class Book {

    private String mBookTitle;
    private String mBookDescription;
    private String mBookInfoLink;
    private String[] mBookAuthors;
    private String mImageUrl;

    public Book(String bookTitle, String bookDescription,
                String bookInfoLink, String[] bookAuthors, String imageUrl) {

        mBookTitle = bookTitle;
        mBookDescription = bookDescription;
        mBookInfoLink = bookInfoLink;
        mBookAuthors = bookAuthors;
        mImageUrl = imageUrl;

    }

    public String getBookTitle() {
        return mBookTitle;
    }

    public String getBookDescription() {
        return mBookDescription;
    }

    public String getBookInfoLink() {
        return mBookInfoLink;
    }

    public String[] getBookAuthors() {return mBookAuthors;}

    public String getImageUrl(){return mImageUrl;}

    public String generateStringOfAuthors() {

        String string = "";
        for (int i = 0; i < mBookAuthors.length; i++) {
            if (i == mBookAuthors.length - 1)
                string += mBookAuthors[i];
            else
                string += mBookAuthors[i] + ", ";
        }
        return string;
    }


}
