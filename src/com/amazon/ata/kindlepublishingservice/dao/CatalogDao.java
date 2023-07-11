package com.amazon.ata.kindlepublishingservice.dao;

import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.enums.PublishingRecordStatus;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;
import com.amazon.ata.kindlepublishingservice.publishing.KindleFormattedBook;
import com.amazon.ata.kindlepublishingservice.utils.KindlePublishingUtils;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import javax.inject.Inject;

public class CatalogDao {

    private final DynamoDBMapper dynamoDbMapper;
    private KindlePublishingUtils kindlePublishingUtils;
    private PublishingStatusDao publishingStatusDao;

    /**
     * Instantiates a new CatalogDao object.
     *
     * @param dynamoDbMapper The {@link DynamoDBMapper} used to interact with the catalog table.
     */
    @Inject
    public CatalogDao(DynamoDBMapper dynamoDbMapper, KindlePublishingUtils kindlePublishingUtils, PublishingStatusDao publishingStatusDao) {

        this.dynamoDbMapper = dynamoDbMapper;
        this.kindlePublishingUtils = kindlePublishingUtils;
        this.publishingStatusDao = publishingStatusDao;
    }

    /**
     * Returns the latest version of the book from the catalog corresponding to the specified book id.
     * Throws a BookNotFoundException if the latest version is not active or no version is found.
     * @param bookId Id associated with the book.
     * @return The corresponding CatalogItem from the catalog table.
     */
    public CatalogItemVersion getBookFromCatalog(String bookId) {
        CatalogItemVersion book = getLatestVersionOfBook(bookId);

        if (book == null || book.isInactive()) {
            throw new BookNotFoundException(String.format("No book found for id: %s", bookId));
        }

        return book;
    }

    // Returns null if no version exists for the provided bookId
    private CatalogItemVersion getLatestVersionOfBook(String bookId) {
        CatalogItemVersion book = new CatalogItemVersion();
        book.setBookId(bookId);

        DynamoDBQueryExpression<CatalogItemVersion> queryExpression = new DynamoDBQueryExpression()
            .withHashKeyValues(book)
            .withScanIndexForward(false)
            .withLimit(1);

        List<CatalogItemVersion> results = dynamoDbMapper.query(CatalogItemVersion.class, queryExpression);
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }
    public CatalogItemVersion saveInactiveStatus(CatalogItemVersion book) {
        dynamoDbMapper.save(book);
        return book;
    }
    public void validateBookExists(String bookId) {
        CatalogItemVersion book = getLatestVersionOfBook(bookId);
        if (book == null) {
            throw new BookNotFoundException(String.format("No book found for id: %s", bookId));

        }
    }
//    public CatalogItemVersion createBook(KindleFormattedBook formattedBook) {
//
//    }
//
//    public CatalogItemVersion updateBook(KindleFormattedBook formattedBook) {
//
//    }


    public CatalogItemVersion createOrUpdateBook(KindleFormattedBook formattedBook) {
        // if adding new book
        if (formattedBook.getBookId() == null) {
            // generate book id -> KindlePublishingUtils
            // return bookId
            String bookId = kindlePublishingUtils.generateBookId();
            // add CatalogItemVersion -> DynamoDB
            CatalogItemVersion book = new CatalogItemVersion();
            book.setBookId(bookId);
            book.setVersion(1);
            book.setInactive(false);
            book.setTitle(formattedBook.getTitle());
            book.setAuthor(formattedBook.getAuthor());
            book.setText(formattedBook.getText());
            book.setGenre(formattedBook.getGenre());
            dynamoDbMapper.save(book);
            return book;
        } else {
            try {
                // else updating existing book
                // look up book ID
                // if book ID not found in catalog
                //throw BookNotFoundException
                validateBookExists(formattedBook.getBookId());
            } catch (BookNotFoundException e) {
                // setPublishingStatus(publishingRecordId, FAILED, bookId, message) -> PublishingStatusDao
                //add publishing status -> DynamoDB
                publishingStatusDao.setPublishingStatus(kindlePublishingUtils.generatePublishingRecordId(), PublishingRecordStatus.FAILED, formattedBook.getBookId());
            }
            // else continue
            // add CatalogItemVersion -> DynamoDB
            CatalogItemVersion previousVersion = getLatestVersionOfBook(formattedBook.getBookId());
            CatalogItemVersion newVersion = new CatalogItemVersion();
            newVersion.setBookId(formattedBook.getBookId());
            newVersion.setVersion(previousVersion.getVersion() + 1);
            newVersion.setInactive(false);
            newVersion.setTitle(formattedBook.getTitle());
            newVersion.setAuthor(formattedBook.getAuthor());
            newVersion.setText(formattedBook.getText());
            newVersion.setGenre(formattedBook.getGenre());
            dynamoDbMapper.save(newVersion);
            // mark previous version as inactive
            previousVersion.setInactive(true);
            dynamoDbMapper.save(previousVersion);
            // return CatalogItemVersion
            return newVersion;


        }





    }

}
