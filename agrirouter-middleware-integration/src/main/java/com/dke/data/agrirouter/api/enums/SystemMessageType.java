package com.dke.data.agrirouter.api.enums;

import agrirouter.cloud.registration.CloudVirtualizedAppRegistration;
import agrirouter.feed.request.FeedRequests;
import agrirouter.request.payload.account.Endpoints;
import agrirouter.request.payload.endpoint.Capabilities;
import agrirouter.request.payload.endpoint.SubscriptionOuterClass;

/**
 * Enum containing all the content message types the AR is supporting.
 */
public enum SystemMessageType implements TechnicalMessageType {

    EMPTY("", ""),
    DKE_CLOUD_ONBOARD_ENDPOINTS(
            "dke:cloud_onboard_endpoints",
            CloudVirtualizedAppRegistration.OnboardingRequest.getDescriptor().getFullName()
    ),
    DKE_CLOUD_OFFBOARD_ENDPOINTS(
            "dke:cloud_offboard_endpoints",
            CloudVirtualizedAppRegistration.OffboardingRequest.getDescriptor().getFullName()
    ),
    DKE_CAPABILITIES("dke:capabilities", Capabilities.CapabilitySpecification.getDescriptor().getFullName()),
    DKE_SUBSCRIPTION("dke:subscription", SubscriptionOuterClass.Subscription.getDescriptor().getFullName()),
    DKE_LIST_ENDPOINTS("dke:list_endpoints", Endpoints.ListEndpointsQuery.getDescriptor().getFullName()),
    DKE_LIST_ENDPOINTS_UNFILTERED(
            "dke:list_endpoints_unfiltered",
            Endpoints.ListEndpointsQuery.getDescriptor().getFullName()
    ),
    DKE_FEED_CONFIRM("dke:feed_confirm", FeedRequests.MessageConfirm.getDescriptor().getFullName()),
    DKE_FEED_DELETE("dke:feed_delete", FeedRequests.MessageDelete.getDescriptor().getFullName()),
    DKE_FEED_MESSAGE_QUERY("dke:feed_message_query", FeedRequests.MessageQuery.getDescriptor().getFullName()),
    DKE_FEED_HEADER_QUERY("dke:feed_header_query", FeedRequests.MessageQuery.getDescriptor().getFullName()),
    DKE_PING("dke:ping", "");

    private final String key;
    private final String typeUrl;

    SystemMessageType(String key, String typeUrl) {
        this.key = key;
        this.typeUrl = typeUrl;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getTypeUrl() {
        return this.typeUrl;
    }

    @Override
    public boolean needsBase64EncodingAndHasToBeChunkedIfNecessary() {
        return false;
    }
}