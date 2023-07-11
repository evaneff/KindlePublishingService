package com.amazon.ata.kindlepublishingservice.publishing;

import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;
import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.enums.PublishingRecordStatus;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;
import com.amazon.ata.kindlepublishingservice.utils.KindlePublishingUtils;

import javax.inject.Inject;

public class BookPublishTask implements Runnable {

    private BookPublishRequestManager bookPublishRequestManager;
    private PublishingStatusDao publishingStatusDao;
    private KindleFormatConverter kindleFormatConverter;
    private CatalogDao catalogDao;
    private KindlePublishingUtils kindlePublishingUtils;

    //processes a publish request from the BookPublishRequestManager. If the BookPublishRequestManager
    // has no publishing requests the BookPublishTask should return immediately without taking action.
    // You will also need to update CatalogDao with new methods for the BookPublishTask to publish new
    // books to our Kindle catalog.


    public BookPublishTask() {}

    @Inject
    public BookPublishTask(BookPublishRequestManager bookPublishRequestManager, PublishingStatusDao publishingStatusDao, KindleFormatConverter kindleFormatConverter, CatalogDao catalogDao, KindlePublishingUtils kindlePublishingUtils) {
        this.bookPublishRequestManager = bookPublishRequestManager;
        this.publishingStatusDao = publishingStatusDao;
        this.kindleFormatConverter = kindleFormatConverter;
        this.catalogDao = catalogDao;
        this.kindlePublishingUtils = kindlePublishingUtils;
    }

    @Override
    public void run() {
        // getBookPublishRequestToProcess() -> BookPublishRequestManager
        // return BookPublishRequest
        BookPublishRequest request = bookPublishRequestManager.getBookPublishRequestToProcess();

        // if publishing request is null return??
        if (request == null) {
        // return??
        }
        // if publishing request is NOT null
            // setPublishingStatus(publishingRecordId, IN_PROGRESS, bookId) ->PublishingStatusDao
        publishingStatusDao.setPublishingStatus(request.getPublishingRecordId(), PublishingRecordStatus.IN_PROGRESS, request.getBookId());
                    // add publishing status -> DynamoDB

        // format -> KindleFormatConverter
        // return KindleFormattedBook
        KindleFormattedBook formattedBook = kindleFormatConverter.format(request);
        //createOrUpdateBook(KindleFormattedBook) -> CatalogDao
        //return CatalogItemVersion
        if (formattedBook.getBookId() == null) {
            CatalogItemVersion newBook = catalogDao.createBook(formattedBook);
        } else {
            try {
                catalogDao.validateBookExists(formattedBook.getBookId());
            } catch (BookNotFoundException e) {
                // setPublishingStatus(publishingRecordId, FAILED, bookId, message) -> PublishingStatusDao
                //add publishing status -> DynamoDB
                publishingStatusDao.setPublishingStatus(request.getPublishingRecordId(), PublishingRecordStatus.FAILED, request.getBookId());
            }
            CatalogItemVersion existingBook = catalogDao.updateBook(formattedBook);
            //else success
            //setPublishingStatus(publishingRecordId, SUCCESSFUL, bookId) -> PublishingStatusDao
            // add publishing status -> DynamoDB
            publishingStatusDao.setPublishingStatus(request.getPublishingRecordId(), PublishingRecordStatus.SUCCESSFUL, request.getBookId());
        }
    }
}
