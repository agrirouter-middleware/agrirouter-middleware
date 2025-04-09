package efdi;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import de.saschadoemer.iso11783.clientname.ClientNameDecoder;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Base64;

class ConvertJsonToBase64EncodedProtobuf {

    @Test
    void givenValidDeviceDescriptionWhenConvertingTheJsonThereShouldBeBase64Outcome1() throws InvalidProtocolBufferException {
        final var json = """
                {
                  "versionMajor": "VERSION_MAJOR_E2_DIS",
                  "versionMinor": 1,
                  "taskControllerManufacturer": "HOLMER EasyHelp 4.0",
                  "taskControllerVersion": "0.0.1",
                  "device": [
                    {
                      "deviceId": {
                        "number": "-1"
                      },
                      "deviceDesignator": "harvester",
                      "clientName": "oBCEAD3hBNI=",
                      "deviceSerialNumber": "T4_4095",
                      "deviceElement": [
                        {
                          "deviceElementId": {
                            "number": "-1"
                          },
                          "deviceElementObjectId": 100,
                          "deviceElementType": "C_DEVICE",
                          "deviceElementDesignator": "Maschine",
                          "deviceObjectReference": [
                            {
                              "deviceObjectId": 10000
                            },
                            {
                              "deviceObjectId": 10001
                            },
                            {
                              "deviceObjectId": 10002
                            },
                            {
                              "deviceObjectId": 10003
                            },
                            {
                              "deviceObjectId": 10004
                            }
                          ]
                        }
                      ],
                      "deviceProcessData": [
                        {
                          "deviceProcessDataObjectId": 10000,
                          "deviceProcessDataDdi": 271,
                          "deviceValuePresentationObjectId": 10000
                        },
                        {
                          "deviceProcessDataObjectId": 10001,
                          "deviceProcessDataDdi": 394,
                          "deviceValuePresentationObjectId": 10001
                        },
                        {
                          "deviceProcessDataObjectId": 10002,
                          "deviceProcessDataDdi": 395,
                          "deviceValuePresentationObjectId": 10002
                        },
                        {
                          "deviceProcessDataObjectId": 10003,
                          "deviceProcessDataDdi": 397,
                          "deviceValuePresentationObjectId": 10003
                        },
                        {
                          "deviceProcessDataObjectId": 10004,
                          "deviceProcessDataDdi": 493,
                          "deviceValuePresentationObjectId": 10004
                        }
                      ],
                      "deviceValuePresentation": [
                        {
                          "deviceValuePresentationObjectId": 10000,
                          "scale": 1.0
                        },
                        {
                          "deviceValuePresentationObjectId": 10001,
                          "scale": 1.0
                        },
                        {
                          "deviceValuePresentationObjectId": 10002,
                          "scale": 1.0
                        },
                        {
                          "deviceValuePresentationObjectId": 10003,
                          "scale": 1.0
                        },
                        {
                          "deviceValuePresentationObjectId": 10004,
                          "scale": 1.0
                        }
                      ]
                    }
                  ]
                }""";
        final var deviceDescription = GrpcEfdi.ISO11783_TaskData.newBuilder();
        JsonFormat.parser().merge(json, deviceDescription);
        final var hexEncodedDeviceName = new String(Hex.encodeHex(deviceDescription.build().getDevice(0).getClientName().toByteArray()));
        final var clientName = ClientNameDecoder.decode(hexEncodedDeviceName);
        final var base64EncodedProtobuf = Base64.getEncoder().encodeToString(deviceDescription.build().toByteString().toByteArray());
        Assertions.assertNotNull(base64EncodedProtobuf);
    }

    @Test
    void givenValidDeviceDescriptionWhenConvertingTheJsonThereShouldBeBase64Outcome2() throws InvalidProtocolBufferException {
        final var json = """
                {
                  "versionMajor": "VERSION_MAJOR_E2_DIS",
                  "versionMinor": 1,
                  "taskControllerManufacturer": "HOLMER EasyHelp 4.0",
                  "taskControllerVersion": "0.0.1",
                  "device": [
                    {
                      "deviceId": {
                        "number": "-1"
                      },
                      "deviceDesignator": "harvester",
                      "deviceSerialNumber": "T4_4095",
                      "deviceElement": [
                        {
                          "deviceObjectReference": [
                            {
                              "deviceObjectId": 10000
                            },
                            {
                              "deviceObjectId": 10001
                            },
                            {
                              "deviceObjectId": 10002
                            },
                            {
                              "deviceObjectId": 10003
                            },
                            {
                              "deviceObjectId": 10004
                            },
                            {
                              "deviceObjectId": 10005
                            },
                            {
                              "deviceObjectId": 10006
                            }
                          ]
                        }
                      ],
                      "deviceProcessData": [
                        {
                          "deviceProcessDataObjectId": 10000,
                          "deviceProcessDataDdi": 271,
                          "deviceValuePresentationObjectId": 10000
                        },
                        {
                          "deviceProcessDataObjectId": 10001,
                          "deviceProcessDataDdi": 394,
                          "deviceValuePresentationObjectId": 10001
                        },
                        {
                          "deviceProcessDataObjectId": 10002,
                          "deviceProcessDataDdi": 395,
                          "deviceValuePresentationObjectId": 10002
                        },
                        {
                          "deviceProcessDataObjectId": 10003,
                          "deviceProcessDataDdi": 397,
                          "deviceValuePresentationObjectId": 10003
                        },
                        {
                          "deviceProcessDataObjectId": 10004,
                          "deviceProcessDataDdi": 493,
                          "deviceValuePresentationObjectId": 10004
                        },
                        {
                          "deviceProcessDataObjectId": 10005,
                          "deviceProcessDataDdi": 66001,
                          "deviceProcessDataDesignator": "Verbrauch Anfahrt",
                          "deviceValuePresentationObjectId": 10005
                        },
                        {
                          "deviceProcessDataObjectId": 10006,
                          "deviceProcessDataDdi": 66002,
                          "deviceProcessDataDesignator": "Verbrauch Feld",
                          "deviceValuePresentationObjectId": 10006
                        }
                      ],
                      "deviceValuePresentation": [
                        {
                          "deviceValuePresentationObjectId": 10000,
                          "scale": 1.0
                        },
                        {
                          "deviceValuePresentationObjectId": 10001,
                          "scale": 1.0
                        },
                        {
                          "deviceValuePresentationObjectId": 10002,
                          "scale": 1.0
                        },
                        {
                          "deviceValuePresentationObjectId": 10003,
                          "scale": 1.0
                        },
                        {
                          "deviceValuePresentationObjectId": 10004,
                          "scale": 1.0
                        },
                        {
                          "deviceValuePresentationObjectId": 10005,
                          "scale": 1.0,
                          "unitDesignator": "l"
                        },
                        {
                          "deviceValuePresentationObjectId": 10006,
                          "scale": 1.0,
                          "unitDesignator": "l"
                        }
                      ]
                    }
                  ]
                }""";
        final var deviceDescription = GrpcEfdi.ISO11783_TaskData.newBuilder();
        JsonFormat.parser().merge(json, deviceDescription);
        final var base64EncodedProtobuf = Base64.getEncoder().encodeToString(deviceDescription.build().toByteString().toByteArray());
        Assertions.assertNotNull(base64EncodedProtobuf);
        System.out.println(base64EncodedProtobuf);
    }

}
