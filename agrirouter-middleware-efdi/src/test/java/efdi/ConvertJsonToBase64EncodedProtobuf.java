package efdi;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import de.saschadoemer.iso11783.clientname.ClientName;
import de.saschadoemer.iso11783.clientname.ClientNameDecoder;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Base64;

class ConvertJsonToBase64EncodedProtobuf {

    @Test
    void givenValidDeviceDescriptionWhenConvertingTheJsonThereShouldBeBase64Outcome1() throws InvalidProtocolBufferException {
        final var json = "{\n" +
                "  \"versionMajor\": \"VERSION_MAJOR_E2_DIS\",\n" +
                "  \"versionMinor\": 1,\n" +
                "  \"taskControllerManufacturer\": \"HOLMER EasyHelp 4.0\",\n" +
                "  \"taskControllerVersion\": \"0.0.1\",\n" +
                "  \"device\": [\n" +
                "    {\n" +
                "      \"deviceId\": {\n" +
                "        \"number\": \"-1\"\n" +
                "      },\n" +
                "      \"deviceDesignator\": \"harvester\",\n" +
                "      \"clientName\": \"oBCEAD3hBNI=\",\n" +
                "      \"deviceSerialNumber\": \"T4_4095\",\n" +
                "      \"deviceElement\": [\n" +
                "        {\n" +
                "          \"deviceElementId\": {\n" +
                "            \"number\": \"-1\"\n" +
                "          },\n" +
                "          \"deviceElementObjectId\": 100,\n" +
                "          \"deviceElementType\": \"C_DEVICE\",\n" +
                "          \"deviceElementDesignator\": \"Maschine\",\n" +
                "          \"deviceObjectReference\": [\n" +
                "            {\n" +
                "              \"deviceObjectId\": 10000\n" +
                "            },\n" +
                "            {\n" +
                "              \"deviceObjectId\": 10001\n" +
                "            },\n" +
                "            {\n" +
                "              \"deviceObjectId\": 10002\n" +
                "            },\n" +
                "            {\n" +
                "              \"deviceObjectId\": 10003\n" +
                "            },\n" +
                "            {\n" +
                "              \"deviceObjectId\": 10004\n" +
                "            }\n" +
                "          ]\n" +
                "        }\n" +
                "      ],\n" +
                "      \"deviceProcessData\": [\n" +
                "        {\n" +
                "          \"deviceProcessDataObjectId\": 10000,\n" +
                "          \"deviceProcessDataDdi\": 271,\n" +
                "          \"deviceValuePresentationObjectId\": 10000\n" +
                "        },\n" +
                "        {\n" +
                "          \"deviceProcessDataObjectId\": 10001,\n" +
                "          \"deviceProcessDataDdi\": 394,\n" +
                "          \"deviceValuePresentationObjectId\": 10001\n" +
                "        },\n" +
                "        {\n" +
                "          \"deviceProcessDataObjectId\": 10002,\n" +
                "          \"deviceProcessDataDdi\": 395,\n" +
                "          \"deviceValuePresentationObjectId\": 10002\n" +
                "        },\n" +
                "        {\n" +
                "          \"deviceProcessDataObjectId\": 10003,\n" +
                "          \"deviceProcessDataDdi\": 397,\n" +
                "          \"deviceValuePresentationObjectId\": 10003\n" +
                "        },\n" +
                "        {\n" +
                "          \"deviceProcessDataObjectId\": 10004,\n" +
                "          \"deviceProcessDataDdi\": 493,\n" +
                "          \"deviceValuePresentationObjectId\": 10004\n" +
                "        }\n" +
                "      ],\n" +
                "      \"deviceValuePresentation\": [\n" +
                "        {\n" +
                "          \"deviceValuePresentationObjectId\": 10000,\n" +
                "          \"scale\": 1.0\n" +
                "        },\n" +
                "        {\n" +
                "          \"deviceValuePresentationObjectId\": 10001,\n" +
                "          \"scale\": 1.0\n" +
                "        },\n" +
                "        {\n" +
                "          \"deviceValuePresentationObjectId\": 10002,\n" +
                "          \"scale\": 1.0\n" +
                "        },\n" +
                "        {\n" +
                "          \"deviceValuePresentationObjectId\": 10003,\n" +
                "          \"scale\": 1.0\n" +
                "        },\n" +
                "        {\n" +
                "          \"deviceValuePresentationObjectId\": 10004,\n" +
                "          \"scale\": 1.0\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        final var deviceDescription = GrpcEfdi.ISO11783_TaskData.newBuilder();
        JsonFormat.parser().merge(json, deviceDescription);
        final var hexEncodedDeviceName = new String(Hex.encodeHex(deviceDescription.build().getDevice(0).getClientName().toByteArray()));
        final var clientName = ClientNameDecoder.decode(hexEncodedDeviceName);
        final var base64EncodedProtobuf = Base64.getEncoder().encodeToString(deviceDescription.build().toByteString().toByteArray());
        Assertions.assertNotNull(base64EncodedProtobuf);
        System.out.println(base64EncodedProtobuf);
    }

    @Test
    void givenValidDeviceDescriptionWhenConvertingTheJsonThereShouldBeBase64Outcome2() throws InvalidProtocolBufferException {
        final var json = "{\n" +
                "  \"versionMajor\": \"VERSION_MAJOR_E2_DIS\",\n" +
                "  \"versionMinor\": 1,\n" +
                "  \"taskControllerManufacturer\": \"HOLMER EasyHelp 4.0\",\n" +
                "  \"taskControllerVersion\": \"0.0.1\",\n" +
                "  \"device\": [\n" +
                "    {\n" +
                "      \"deviceId\": {\n" +
                "        \"number\": \"-1\"\n" +
                "      },\n" +
                "      \"deviceDesignator\": \"harvester\",\n" +
                "      \"deviceSerialNumber\": \"T4_4095\",\n" +
                "      \"deviceElement\": [\n" +
                "        {\n" +
                "          \"deviceObjectReference\": [\n" +
                "            {\n" +
                "              \"deviceObjectId\": 10000\n" +
                "            },\n" +
                "            {\n" +
                "              \"deviceObjectId\": 10001\n" +
                "            },\n" +
                "            {\n" +
                "              \"deviceObjectId\": 10002\n" +
                "            },\n" +
                "            {\n" +
                "              \"deviceObjectId\": 10003\n" +
                "            },\n" +
                "            {\n" +
                "              \"deviceObjectId\": 10004\n" +
                "            },\n" +
                "            {\n" +
                "              \"deviceObjectId\": 10005\n" +
                "            },\n" +
                "            {\n" +
                "              \"deviceObjectId\": 10006\n" +
                "            }\n" +
                "          ]\n" +
                "        }\n" +
                "      ],\n" +
                "      \"deviceProcessData\": [\n" +
                "        {\n" +
                "          \"deviceProcessDataObjectId\": 10000,\n" +
                "          \"deviceProcessDataDdi\": 271,\n" +
                "          \"deviceValuePresentationObjectId\": 10000\n" +
                "        },\n" +
                "        {\n" +
                "          \"deviceProcessDataObjectId\": 10001,\n" +
                "          \"deviceProcessDataDdi\": 394,\n" +
                "          \"deviceValuePresentationObjectId\": 10001\n" +
                "        },\n" +
                "        {\n" +
                "          \"deviceProcessDataObjectId\": 10002,\n" +
                "          \"deviceProcessDataDdi\": 395,\n" +
                "          \"deviceValuePresentationObjectId\": 10002\n" +
                "        },\n" +
                "        {\n" +
                "          \"deviceProcessDataObjectId\": 10003,\n" +
                "          \"deviceProcessDataDdi\": 397,\n" +
                "          \"deviceValuePresentationObjectId\": 10003\n" +
                "        },\n" +
                "        {\n" +
                "          \"deviceProcessDataObjectId\": 10004,\n" +
                "          \"deviceProcessDataDdi\": 493,\n" +
                "          \"deviceValuePresentationObjectId\": 10004\n" +
                "        },\n" +
                "        {\n" +
                "          \"deviceProcessDataObjectId\": 10005,\n" +
                "          \"deviceProcessDataDdi\": 66001,\n" +
                "          \"deviceProcessDataDesignator\": \"Verbrauch Anfahrt\",\n" +
                "          \"deviceValuePresentationObjectId\": 10005\n" +
                "        },\n" +
                "        {\n" +
                "          \"deviceProcessDataObjectId\": 10006,\n" +
                "          \"deviceProcessDataDdi\": 66002,\n" +
                "          \"deviceProcessDataDesignator\": \"Verbrauch Feld\",\n" +
                "          \"deviceValuePresentationObjectId\": 10006\n" +
                "        }\n" +
                "      ],\n" +
                "      \"deviceValuePresentation\": [\n" +
                "        {\n" +
                "          \"deviceValuePresentationObjectId\": 10000,\n" +
                "          \"scale\": 1.0\n" +
                "        },\n" +
                "        {\n" +
                "          \"deviceValuePresentationObjectId\": 10001,\n" +
                "          \"scale\": 1.0\n" +
                "        },\n" +
                "        {\n" +
                "          \"deviceValuePresentationObjectId\": 10002,\n" +
                "          \"scale\": 1.0\n" +
                "        },\n" +
                "        {\n" +
                "          \"deviceValuePresentationObjectId\": 10003,\n" +
                "          \"scale\": 1.0\n" +
                "        },\n" +
                "        {\n" +
                "          \"deviceValuePresentationObjectId\": 10004,\n" +
                "          \"scale\": 1.0\n" +
                "        },\n" +
                "        {\n" +
                "          \"deviceValuePresentationObjectId\": 10005,\n" +
                "          \"scale\": 1.0,\n" +
                "          \"unitDesignator\": \"l\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"deviceValuePresentationObjectId\": 10006,\n" +
                "          \"scale\": 1.0,\n" +
                "          \"unitDesignator\": \"l\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        final var deviceDescription = GrpcEfdi.ISO11783_TaskData.newBuilder();
        JsonFormat.parser().merge(json, deviceDescription);
        final var base64EncodedProtobuf = Base64.getEncoder().encodeToString(deviceDescription.build().toByteString().toByteArray());
        Assertions.assertNotNull(base64EncodedProtobuf);
        System.out.println(base64EncodedProtobuf);
    }

}
