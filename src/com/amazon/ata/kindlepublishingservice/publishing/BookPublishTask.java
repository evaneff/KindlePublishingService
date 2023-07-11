package com.amazon.ata.kindlepublishingservice.publishing;

import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;
import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.enums.PublishingRecordStatus;

import javax.inject.Inject;

public class BookPublishTask implements Runnable {

    private BookPublishRequestManager bookPublishRequestManager;
    private PublishingStatusDao publishingStatusDao;
    private KindleFormatConverter kindleFormatConverter;
    private CatalogDao catalogDao;

    //processes a publish request from the BookPublishRequestManager. If the BookPublishRequestManager
    // has no publishing requests the BookPublishTask should return immediately without taking action.
    // You will also need to update CatalogDao with new methods for the BookPublishTask to publish new
    // books to our Kindle catalog.


    public BookPublishTask() {}

    @Inject
    public BookPublishTask(BookPublishRequestManager bookPublishRequestManager, PublishingStatusDao publishingStatusDao, KindleFormatConverter kindleFormatConverter, CatalogDao catalogDao) {
        this.bookPublishRequestManager = bookPublishRequestManager;
        this.publishingStatusDao = publishingStatusDao;
        this.kindleFormatConverter = kindleFormatConverter;
        this.catalogDao = catalogDao;
    }

    @Override
    public void run() {
        // getBookPublishRRequestToProcess() -> BookPublishRequestManager
        // return BookPublishRequest
        BookPublishRequest request = bookPublishRequestManager.getBookPublishRequestToProcess();

        // if publishing request is null return??
        // if publishing request is NOT null

        if (request != null) {
            // setPublishingStatus(publishingRecordId, IN_PROGRESS, bookId) ->PublishingStatusDao
            publishingStatusDao.setPublishingStatus(request.getPublishingRecordId(), PublishingRecordStatus.IN_PROGRESS, request.getBookId());
                    // add publishing status -> DynamoDB
        }

        // format -> KindleFormatConverter
        // return KindleFormattedBook
        KindleFormattedBook formattedBook = kindleFormatConverter.format(request);

        //createOrUpdateBook(KindleFormattedBook) -> CatalogDao
        //return CatalogItemVersion
        CatalogItemVersion version = catalogDao.createOrUpdateBook(formattedBook);




        //any exception caught while processing
        // setPublishingStatus(publishingRecordId, FAILED, bookId, message) -> PublishingStatusDao
                // add publishing status - DynamoDB
          //else success
           //setPublishingStatus(publishingRecordId, SUCCESSFUL, bookId) -> PublishingStatusDao
                // add publishing status -> DynamoDB
    }
}
