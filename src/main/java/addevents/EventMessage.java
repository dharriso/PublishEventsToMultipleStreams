package addevents;

import java.util.Date;

public interface EventMessage {
    int getTimeId();

    int getUseId();

    long getEventId();

    String getAccessKey();

    Integer getAccessKeyType();

    boolean hasAccessKeyType();

    String getOwningCustomerID();

    String getRootCustomerID();

    String getComposedCustomerID();

    long getEventType();

    Date getOriginalEventTime();

    Date getCreationEventTime();

    Date getEffectiveEventTime();

    int getBillCycleID();

    int getBillPeriodID();

    Integer getErrorCode();

    boolean hasErrorCode();

    String getRateEventType();

    Long getExternalCorrelationID();

    boolean hasCorrelationId();

    int getRootCustomerIDHash();

    byte[] getProjectAddOn();

    int getRefEventId();

    int getRefUseId();

    int getDayId();

    byte[] getInternalRatingRelevant0() ;

    byte[] getInternalRatingRelevant1();

    byte[] getInternalRatingRelevant2();

    byte[] getExternalRatingIrrelevant0();

    byte[] getExternalRatingIrrelevant1();

    byte[] getExternalRatingIrrelevant2();

    byte[] getExternalRatingResult0();

    byte[] getExternalRatingResult1() ;

    byte[] getExternalRatingResult2();

    byte[] getExternalDataTransp0();

    byte[] getExternalDataTransp1();

    byte[] getExternalDataTransp2();

    byte[] getUniversalAttribute0();

    byte[] getUniversalAttribute1();

    byte[] getUniversalAttribute2();

    byte[] getUniversalAttribute3();

    byte[] getUniversalAttribute4();
}
