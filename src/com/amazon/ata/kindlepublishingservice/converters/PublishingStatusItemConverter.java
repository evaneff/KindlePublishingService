package com.amazon.ata.kindlepublishingservice.converters;

import com.amazon.ata.kindlepublishingservice.dynamodb.models.PublishingStatusItem;
import com.amazon.ata.kindlepublishingservice.models.PublishingStatusRecord;

import java.util.ArrayList;
import java.util.List;

public class PublishingStatusItemConverter {

    private PublishingStatusItemConverter() {}

    public static List<PublishingStatusRecord> toPublishStatusRecord(List<PublishingStatusItem> publishingStatusItems) {
        List<PublishingStatusRecord> publishingStatusRecords = new ArrayList<>();
        for (PublishingStatusItem item : publishingStatusItems) {
            PublishingStatusRecord publishingStatusRecord = new PublishingStatusRecord(item.getStatus().toString(), item.getStatusMessage(), item.getBookId());
            publishingStatusRecords.add(publishingStatusRecord);
        }
    return publishingStatusRecords;
    }
}
