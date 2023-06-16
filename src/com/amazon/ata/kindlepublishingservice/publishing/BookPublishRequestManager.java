package com.amazon.ata.kindlepublishingservice.publishing;

import java.util.LinkedList;
import java.util.Queue;

public class BookPublishRequestManager {

    private Queue<BookPublishRequest> requests;

    // accept dependencies into constructor???
    public BookPublishRequestManager() {
        requests = new LinkedList<>();
    }

    public void addBookPublishRequest(BookPublishRequest bookPublishRequest) {
        requests.add(bookPublishRequest);
    }

    public BookPublishRequest getBookPublishRequestToProcess() {
        if (requests.isEmpty()) {
            return null;
        }
        return requests.remove();
    }
}
