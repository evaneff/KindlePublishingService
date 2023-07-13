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
    private CatalogDao catalogDao;

    //processes a publish request from the BookPublishRequestManager. If the BookPublishRequestManager
    // has no publishing requests the BookPublishTask should return immediately without taking action.
    // You will also need to update CatalogDao with new methods for the BookPublishTask to publish new
    // books to our Kindle catalog.

    @Inject
    public BookPublishTask(BookPublishRequestManager bookPublishRequestManager, PublishingStatusDao publishingStatusDao, CatalogDao catalogDao) {
        this.bookPublishRequestManager = bookPublishRequestManager;
        this.publishingStatusDao = publishingStatusDao;
        this.catalogDao = catalogDao;
    }
    @Override
    public void run() {
        BookPublishRequest request = bookPublishRequestManager.getBookPublishRequestToProcess();
        if (request == null) return;

        publishingStatusDao.setPublishingStatus(request.getPublishingRecordId(), PublishingRecordStatus.IN_PROGRESS, request.getBookId());

        try {
            CatalogItemVersion item = catalogDao.createOrUpdateBook(KindleFormatConverter.format(request));
            publishingStatusDao.setPublishingStatus(request.getPublishingRecordId(), PublishingRecordStatus.SUCCESSFUL, item.getBookId());
        } catch (BookNotFoundException e) {
            publishingStatusDao.setPublishingStatus(request.getPublishingRecordId(), PublishingRecordStatus.FAILED, request.getBookId());
        }
    }

//    @Override
//    public void run() {
//        // getBookPublishRequestToProcess() -> BookPublishRequestManager
//        // return BookPublishRequest
//        BookPublishRequest request = bookPublishRequestManager.getBookPublishRequestToProcess();
//
//        // if publishing request is null return??
//        if (request == null) {
//            return;
//        }
//        // if publishing request is NOT null
//            // setPublishingStatus(publishingRecordId, IN_PROGRESS, bookId) ->PublishingStatusDao
//        publishingStatusDao.setPublishingStatus(request.getPublishingRecordId(), PublishingRecordStatus.IN_PROGRESS, request.getBookId());
//                    // add publishing status -> DynamoDB
//
//        // format -> KindleFormatConverter
//        // return KindleFormattedBook
//        KindleFormattedBook formattedBook = kindleFormatConverter.format(request);
//        //createOrUpdateBook(KindleFormattedBook) -> CatalogDao
//        //return CatalogItemVersion
//        if (formattedBook.getBookId() == null) {
//            CatalogItemVersion newBook = catalogDao.createBook(formattedBook);
//        } else {
//            try {
//                catalogDao.validateBookExists(formattedBook.getBookId());
//            } catch (BookNotFoundException e) {
//                // setPublishingStatus(publishingRecordId, FAILED, bookId, message) -> PublishingStatusDao
//                //add publishing status -> DynamoDB
//                publishingStatusDao.setPublishingStatus(request.getPublishingRecordId(), PublishingRecordStatus.FAILED, request.getBookId());
//            }
//            CatalogItemVersion existingBook = catalogDao.updateBook(formattedBook);
//            //else success
//            //setPublishingStatus(publishingRecordId, SUCCESSFUL, bookId) -> PublishingStatusDao
//            // add publishing status -> DynamoDB
//            publishingStatusDao.setPublishingStatus(request.getPublishingRecordId(), PublishingRecordStatus.SUCCESSFUL, request.getBookId());
//        }
//    }
}
