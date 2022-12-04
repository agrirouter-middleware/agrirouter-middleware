package de.agrirouter.middleware.isoxml;

import de.agrirouter.middleware.api.errorhandling.BusinessException;
import de.agrirouter.middleware.api.errorhandling.error.ErrorMessageFactory;
import de.agrirouter.middleware.isoxml.domain.Constants;
import de.agrirouter.middleware.isoxml.domain.TimeStart;
import de.agrirouter.middleware.isoxml.reader.ByteValueReader;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.zip.ZipInputStream;

/**
 * Service to handle time log values from the task data files.
 */
@Slf4j
@Service
public class TaskDataTimeLogService {

    public static final String XML_FILE_EXTENSION = "xml";
    public static final String TIME_LOG_PREFIX = "tlg";
    public static final String BINARY_FILE_EXTENSION = "bin";

    /**
     * Parse the message content (represented as base64 encoded zip).
     *
     * @param base64EncodedZipFile The base 64 encoded zip file.
     */
    public List<Document> parseMessageContent(byte[] base64EncodedZipFile) {
        final var documents = new ArrayList<Document>();
        final var decodedMessageContent = Base64.getDecoder().decode(base64EncodedZipFile);
        log.debug("Message content successfully decoded.");
        log.trace("Decoded message content >>> {}", decodedMessageContent);
        try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(decodedMessageContent))) {
            final var files = readFilesFromZipFile(zipInputStream);
            files.forEach((s, bytes) -> {
                log.debug("Processing file from ZIP.");
                final var fileNameAndExtension = s.split("\\.");
                if (BINARY_FILE_EXTENSION.equals(fileNameAndExtension[1].toLowerCase(Locale.ROOT))) {
                    log.debug("Handling binary file from the ZIP.");
                    if (fileNameAndExtension[0].toLowerCase(Locale.ROOT).contains(TIME_LOG_PREFIX)) {
                        final var xmlDescriptor = findMatchingXmlDescriptorFileForTimeLog(fileNameAndExtension[0], files);
                        final var document = handleTimeLogValue(xmlDescriptor, bytes);
                        documents.add(document);
                    } else {
                        log.debug("Will not handle the following file '{}'. The file will be handled separately.", s);
                    }
                } else {
                    log.debug("Will not handle the following file '{}'. The file will be handled separately.", s);
                }
            });
        } catch (IOException e) {
            log.error("Could not open zipped file. Looks like the file is broken?", e);
            throw new BusinessException(ErrorMessageFactory.couldNotParseTaskData(), e);
        }
        return documents;
    }

    private byte[] findMatchingXmlDescriptorFileForTimeLog(String fileNameOfTheTimeLog, Map<String, byte[]> files) {
        for (Map.Entry<String, byte[]> entry : files.entrySet()) {
            String s = entry.getKey();
            byte[] bytes = entry.getValue();
            log.debug("Processing file from ZIP to find the matching XML descriptor.");
            final var fileNameAndExtension = s.split("\\.");
            if (fileNameAndExtension[0].equalsIgnoreCase(fileNameOfTheTimeLog) && fileNameAndExtension[1].equalsIgnoreCase(XML_FILE_EXTENSION)) {
                return bytes;
            } else {
                log.debug("Skipping file '{}', since this is not the descriptor for '{}'.", s, fileNameOfTheTimeLog);
            }
        }
        throw new BusinessException(ErrorMessageFactory.couldNotFindDescriptorForTheTimeLog());
    }

    private Map<String, byte[]> readFilesFromZipFile(ZipInputStream zipInputStream) throws IOException {
        Map<String, byte[]> files = new HashMap<>();
        var zipEntry = zipInputStream.getNextEntry();
        while (null != zipEntry) {
            if (zipEntry.isDirectory()) {
                log.debug("Skipping the directory '{}'.", zipEntry.getName());
            } else {
                byte[] buffer = new byte[2048];
                log.debug("Processing next ZIP entry >>> {}", zipEntry.getName());
                try (final var byteArrayOutputStream = new ByteArrayOutputStream()) {
                    int len;
                    while ((len = zipInputStream.read(buffer)) > 0) {
                        byteArrayOutputStream.write(buffer, 0, len);
                    }
                    log.debug("Saving the file content to the temporary map.");
                    files.put(zipEntry.getName(), byteArrayOutputStream.toByteArray());
                }
            }
            zipEntry = zipInputStream.getNextEntry();
        }
        return files;
    }

    private Document handleTimeLogValue(byte[] xmlDescriptorBytes, byte[] timeLogBytes) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(TIM.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            final var xmlDescriptor = (TIM) jaxbUnmarshaller.unmarshal(new ByteArrayInputStream(xmlDescriptorBytes));
            final var timeLog = new Document();
            ByteBuffer timeLogByteBuffer = ByteBuffer.wrap(timeLogBytes).order(ByteOrder.LITTLE_ENDIAN);
            while (timeLogByteBuffer.hasRemaining()) {
                readTimeInformation(xmlDescriptor, timeLog, timeLogByteBuffer);
                final var optionalPtn = xmlDescriptor.getPTNOrDLV().stream().filter(o -> o instanceof PTN).findAny();
                if (optionalPtn.isPresent()) {
                    log.debug("Found PTN element.");
                    final var ptn = (PTN) optionalPtn.get();
                    readPositionInformation(ptn, timeLog, timeLogByteBuffer);
                }
                final var numberOfDataLogValuesToFollow = readNumberOfDataLogValuesToFollow(timeLog, timeLogByteBuffer);
                log.debug("There should be {} DLV elements to read.", numberOfDataLogValuesToFollow);
                final var dataLogValues = new Document();
                for (int i = 0; i < numberOfDataLogValuesToFollow; i++) {
                    final var dataLogValueOrderingNumber = ByteValueReader.readByte(timeLogByteBuffer);
                    final var processDataValue = ByteValueReader.readInteger(timeLogByteBuffer);
                    final var dataLogValue = new Document();
                    dataLogValue.append(Constants.DATA_LOG_VALUE_ORDERING_NUMBER, dataLogValueOrderingNumber);
                    dataLogValue.append(Constants.PROCESS_DATA_VALUE, processDataValue);
                    dataLogValues.append(String.format("%s_%s", Constants.DATA_LOG_VALUE, dataLogValueOrderingNumber), dataLogValue);
                }
                append(timeLog, Constants.DATA_LOG_VALUES, dataLogValues);
                logFinalResult(timeLog);
            }
            return timeLog;
        } catch (Exception e) {
            log.error("There was an exception while parsing the task data.", e);
            throw new BusinessException(ErrorMessageFactory.couldNotParseTaskData(), e);
        }
    }

    private byte readNumberOfDataLogValuesToFollow(Document timeLog, ByteBuffer timeLogByteBuffer) {
        final var value = ByteValueReader.readByte(timeLogByteBuffer);
        append(timeLog, Constants.NUMBER_OF_DATA_LOG_VALUES, value);
        return value;
    }

    private void readPositionInformation(PTN ptn, Document timeLog, ByteBuffer timeLogByteBuffer) {
        if (null != ptn.getA()) {
            final var value = ByteValueReader.readDouble(timeLogByteBuffer);
            append(timeLog, Constants.POSITION_NORTH, value);
        }
        if (null != ptn.getB()) {
            final var value = ByteValueReader.readDouble(timeLogByteBuffer);
            append(timeLog, Constants.POSITION_EAST, value);
        }
        if (null != ptn.getC()) {
            final var value = ByteValueReader.readInteger(timeLogByteBuffer);
            append(timeLog, Constants.POSITION_UP, value);
        }
        if (null != ptn.getD()) {
            final var value = ByteValueReader.readByte(timeLogByteBuffer);
            append(timeLog, Constants.POSITION_STATUS, value);
        }
        if (null != ptn.getE()) {
            final var value = ByteValueReader.readUnsignedShort(timeLogByteBuffer);
            append(timeLog, Constants.PDOP, value);
        }
        if (null != ptn.getF()) {
            final var value = ByteValueReader.readUnsignedShort(timeLogByteBuffer);
            append(timeLog, Constants.HDOP, value);
        }
        if (null != ptn.getG()) {
            final var value = ByteValueReader.readByte(timeLogByteBuffer);
            append(timeLog, Constants.NUMBER_OF_SATELLITES, value);
        }
        if (null != ptn.getH()) {
            final var value = ByteValueReader.readUnsignedLong(timeLogByteBuffer);
            append(timeLog, Constants.GPS_UTC_TIME, value);
        }
        if (null != ptn.getI()) {
            final var value = ByteValueReader.readUnsignedShort(timeLogByteBuffer);
            append(timeLog, Constants.GPS_UTC_DATE, value);
        }
    }

    private void readTimeInformation(TIM tim, Document timeLog, ByteBuffer timeLogByteBuffer) {
        if (null != tim.getA()) {
            final var milliSeconds = ByteValueReader.readInteger(timeLogByteBuffer);
            final var days = ByteValueReader.readUnsignedShort(timeLogByteBuffer);
            final var timeStart = new TimeStart();
            timeStart.setDays(days);
            timeStart.setMilliseconds(milliSeconds);
            final var timeStartDocument = new Document();
            timeStartDocument.append(Constants.TIME_START_TIME_OF_DAY, timeStart.getMilliseconds());
            timeStartDocument.append(Constants.TIME_START_DATE, timeStart.getDays());
            timeStartDocument.append(Constants.TIME_START_AS_LOCAL_INSTANT, timeStart.toLocalInstant().toEpochMilli());
            append(timeLog, Constants.TIME_START, timeStartDocument);
        }
        if (null != tim.getD()) {
            append(timeLog, Constants.DURATION, tim.getD());
        }
    }

    private void append(Document timeLog, String key, Object value) {
        log.debug("Appending the following key-value-pair to the time log document. | {} >>> {}", key, value);
        timeLog.append(key, value);
    }

    private void logFinalResult(Document timeLog) {
        log.debug("These are the final values");
        log.debug("###########################################################################################");
        log.debug("");
        log.debug("{}", timeLog.toJson());
        log.debug("");
        log.debug("###########################################################################################");
    }

}
