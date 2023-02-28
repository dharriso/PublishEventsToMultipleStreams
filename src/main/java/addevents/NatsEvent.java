package addevents;

import java.util.Calendar;
import java.util.Date;

public final class NatsEvent implements EventMessage {

    private final Builder builder;
    private final int dayId;

    /**
     * Must use the builder to create the NatsEvent
     *
     * @param builder
     */
    private NatsEvent(Builder builder) {
        this.builder = builder;
        // '2009-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'
        Calendar cal = Calendar.getInstance();
        cal.setTime(builder.getCreationEventTime());
        dayId = cal.get(Calendar.DAY_OF_MONTH);
    }

    public int getTimeId() {
        return builder.getTimeId();
    }

    public int getUseId() {
        return builder.getUseId();
    }

    public long getEventId() {
        return builder.getEventId();
    }

    public String getAccessKey() {
        return builder.getAccessKey();
    }

    public Integer getAccessKeyType() {
        return builder.getAccessKeyType();
    }

    public boolean hasAccessKeyType() {
        return builder.hasAccessKeyType();
    }

    public String getOwningCustomerID() {
        return builder.getOwningCustomerID();
    }

    public String getRootCustomerID() {
        return builder.getRootCustomerID();
    }

    public String getComposedCustomerID() {
        return builder.getComposedCustomerID();
    }

    public long getEventType() {
        return builder.getEventType();
    }

    public Date getOriginalEventTime() {
        return builder.getOriginalEventTime();
    }

    public Date getCreationEventTime() {
        return builder.getCreationEventTime();
    }

    public Date getEffectiveEventTime() {
        return builder.getEffectiveEventTime();
    }

    public int getBillCycleID() {
        return builder.getBillCycleID();
    }

    public int getBillPeriodID() {
        return builder.getBillPeriodID();
    }

    public Integer getErrorCode() {
        return builder.getErrorCode();
    }

    public boolean hasErrorCode() {
        return builder.hasErrorCode();
    }

    public String getRateEventType() {
        return builder.getRateEventType();
    }

    public Long getExternalCorrelationID() {
        return builder.getExternalCorrelationID();
    }

    public boolean hasCorrelationId() {
        return builder.hasCorrelationId();
    }

    public int getRootCustomerIDHash() {
        return builder.getRootCustomerIDHash();
    }

    public byte[] getProjectAddOn() {
        return builder.getProjectAddOn();
    }

    public int getRefEventId() {
        return builder.getRefEventId();
    }

    public int getRefUseId() {
        return builder.getRefUseId();
    }

    public int getDayId() {
        return dayId;
    }

    public byte[] getInternalRatingRelevant0() {
        return builder.getInternalRatingRelevant0();
    }

    public byte[] getInternalRatingRelevant1() {
        return builder.getInternalRatingRelevant1();
    }

    public byte[] getInternalRatingRelevant2() {
        return builder.getInternalRatingRelevant2();
    }

    public byte[] getExternalRatingIrrelevant0() {
        return builder.getExternalRatingIrrelevant0();
    }

    public byte[] getExternalRatingIrrelevant1() {
        return builder.getExternalRatingIrrelevant1();
    }

    public byte[] getExternalRatingIrrelevant2() {
        return builder.getExternalRatingIrrelevant2();
    }

    public byte[] getExternalRatingResult0() {
        return builder.getExternalRatingResult0();
    }

    public byte[] getExternalRatingResult1() {
        return builder.getExternalRatingResult1();
    }

    public byte[] getExternalRatingResult2() {
        return builder.getExternalRatingResult2();
    }

    public byte[] getExternalDataTransp0() {
        return builder.getExternalDataTransp0();
    }

    public byte[] getExternalDataTransp1() {
        return builder.getExternalDataTransp1();
    }

    public byte[] getExternalDataTransp2() {
        return builder.getExternalDataTransp2();
    }

    public byte[] getUniversalAttribute0() {
        return builder.getUniversalAttribute0();
    }

    public byte[] getUniversalAttribute1() {
        return builder.getUniversalAttribute1();
    }

    public byte[] getUniversalAttribute2() {
        return builder.getUniversalAttribute2();
    }

    public byte[] getUniversalAttribute3() {
        return builder.getUniversalAttribute3();
    }

    public byte[] getUniversalAttribute4() {
        return builder.getUniversalAttribute4();
    }

    /**
     * Builder provides only to build this wee yoke
     */
    public static final class Builder extends EventCommon {

        public Builder(int timeId,
                       int useId,
                       long eventId,
                       String accessKey,
                       String owningCustomerID,
                       String rootCustomerID,
                       long eventType,
                       Date originalEventTime,
                       Date creationEventTime,
                       Date effectiveEventTime,
                       int billCycleID,
                       int billPeriodID,
                       String rateEventType,
                       int rootCustomerIDHash) {
            this.timeId = timeId;
            this.useId = useId;
            this.eventId = eventId;
            this.accessKey = accessKey;
            this.owningCustomerID = owningCustomerID;
            this.rootCustomerID = rootCustomerID;
            this.eventType = eventType;
            this.originalEventTime = originalEventTime;
            this.creationEventTime = creationEventTime;
            this.effectiveEventTime = effectiveEventTime;
            this.billCycleID = billCycleID;
            this.billPeriodID = billPeriodID;
            this.rateEventType = rateEventType;
            this.rootCustomerIDHash = rootCustomerIDHash;
        }

        public Builder withAccessKeyType(int accessKeyType) {
            this.accessKeyType = accessKeyType;
            return this;
        }

        public Builder withComposedCustomerID(String composedCustomerID) {
            this.composedCustomerID = composedCustomerID;
            return this;
        }

        public Builder withErrorCode(int errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public Builder withExternalCorrelationID(long externalCorrelationID) {
            this.externalCorrelationID = externalCorrelationID;
            return this;
        }

        public Builder withProjectAddOn(byte[] projectAddOn) {
            this.projectAddOn = projectAddOn;
            return this;
        }

        public Builder withRefEventId(int refEventId) {
            this.refEventId = refEventId;
            return this;
        }

        public Builder withRefUseId(int refUseId) {
            this.refUseId = refUseId;
            return this;
        }

        public Builder withInternalRatingRelevant0(byte[] internalRatingRelevant0) {
            this.internalRatingRelevant0 = internalRatingRelevant0;
            return this;
        }

        public Builder withInternalRatingRelevant1(byte[] internalRatingRelevant1) {
            this.internalRatingRelevant1 = internalRatingRelevant1;
            return this;
        }

        public Builder withInternalRatingRelevant2(byte[] internalRatingRelevant2) {
            this.internalRatingRelevant2 = internalRatingRelevant2;
            return this;
        }

        public Builder withExternalRatingIrrelevant0(byte[] externalRatingIrrelevant0) {
            this.externalRatingIrrelevant0 = externalRatingIrrelevant0;
            return this;
        }

        public Builder withExternalRatingIrrelevant1(byte[] externalRatingIrrelevant1) {
            this.externalRatingIrrelevant1 = externalRatingIrrelevant1;
            return this;
        }

        public Builder withExternalRatingIrrelevant2(byte[] externalRatingIrrelevant2) {
            this.externalRatingIrrelevant2 = externalRatingIrrelevant2;
            return this;
        }

        public Builder withExternalRatingResult0(byte[] externalRatingResult0) {
            this.externalRatingResult0 = externalRatingResult0;
            return this;
        }

        public Builder withExternalRatingResult1(byte[] externalRatingResult1) {
            this.externalRatingResult1 = externalRatingResult1;
            return this;
        }

        public Builder withExternalRatingResult2(byte[] externalRatingResult2) {
            this.externalRatingResult2 = externalRatingResult2;
            return this;
        }

        public Builder withExternalDataTransp0(byte[] externalDataTransp0) {
            this.externalDataTransp0 = externalDataTransp0;
            return this;
        }

        public Builder withExternalDataTransp1(byte[] externalDataTransp1) {
            this.externalDataTransp1 = externalDataTransp1;
            return this;
        }

        public Builder withExternalDataTransp2(byte[] externalDataTransp2) {
            this.externalDataTransp2 = externalDataTransp2;
            return this;
        }

        public Builder withUniversalAttribute0(byte[] universalAttribute0) {
            this.universalAttribute0 = universalAttribute0;
            return this;
        }

        public Builder withUniversalAttribute1(byte[] universalAttribute1) {
            this.universalAttribute1 = universalAttribute1;
            return this;
        }

        public Builder withUniversalAttribute2(byte[] universalAttribute2) {
            this.universalAttribute2 = universalAttribute2;
            return this;
        }

        public Builder withUniversalAttribute3(byte[] universalAttribute3) {
            this.universalAttribute3 = universalAttribute3;
            return this;
        }

        public Builder withUniversalAttribute4(byte[] universalAttribute4) {
            this.universalAttribute4 = universalAttribute4;
            return this;
        }

        public NatsEvent build() {
            return new NatsEvent(this);
        }
    }
}
