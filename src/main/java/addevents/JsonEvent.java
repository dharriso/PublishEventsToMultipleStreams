package addevents;

import com.dslplatform.json.CompiledJson;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE, objectFormatPolicy = CompiledJson.ObjectFormatPolicy.MINIMAL)
public final class JsonEvent extends EventCommon implements EventMessage {

    public JsonEvent() {
    }

    public JsonEvent withTimeId(int timeId) {
        this.timeId = timeId;
        return this;
    }

    public JsonEvent withUseId(int useId) {
        this.useId = useId;
        return this;
    }

    public JsonEvent withEventId(long eventId) {
        this.eventId = eventId;
        return this;
    }

    public JsonEvent withAccessKey(String accessKey) {
        this.accessKey = accessKey;
        return this;
    }

    public JsonEvent withAccessKeyType(int accessKeyType) {
        this.accessKeyType = accessKeyType;
        return this;
    }

    public JsonEvent withOwningCustomerID(String owningCustomerID) {
        this.owningCustomerID = owningCustomerID;
        return this;
    }

    public JsonEvent withRootCustomerID(String rootCustomerID) {
        this.rootCustomerID = rootCustomerID;
        return this;
    }

    public JsonEvent withComposedCustomerID(String composedCustomerID) {
        this.composedCustomerID = composedCustomerID;
        return this;
    }

    public JsonEvent withEventType(long eventType) {
        this.eventType = eventType;
        return this;
    }

    public JsonEvent withOriginalEventTime(Date originalEventTime) {
        this.originalEventTime = originalEventTime;
        return this;
    }

    public JsonEvent withCreationEventTime(Date creationEventTime) {
        this.creationEventTime = creationEventTime;
        // '2009-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'
        Calendar cal = Calendar.getInstance();
        cal.setTime(creationEventTime);
        dayId = cal.get(Calendar.DAY_OF_MONTH);
        return this;
    }

    public JsonEvent withEffectiveEventTime(Date effectiveEventTime) {
        this.effectiveEventTime = effectiveEventTime;
        return this;

    }

    public JsonEvent withBillCycleID(int billCycleID) {
        this.billCycleID = billCycleID;
        return this;
    }

    public JsonEvent withBillPeriodID(int billPeriodID) {
        this.billPeriodID = billPeriodID;
        return this;
    }

    public JsonEvent withErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public JsonEvent withRateEventType(String rateEventType) {
        this.rateEventType = rateEventType;
        return this;
    }

    public JsonEvent withExternalCorrelationID(long externalCorrelationID) {
        this.externalCorrelationID = externalCorrelationID;
        return this;
    }

    public JsonEvent withRootCustomerIDHash(int rootCustomerIDHash) {
        this.rootCustomerIDHash = rootCustomerIDHash;
        return this;
    }

    public JsonEvent withProjectAddOn(byte[] projectAddOn) {
        this.projectAddOn = projectAddOn;
        return this;
    }

    public JsonEvent withRefEventId(int refEventId) {
        this.refEventId = refEventId;
        return this;
    }

    public JsonEvent withRefUseId(int refUseId) {
        this.refUseId = refUseId;
        return this;
    }

    public JsonEvent withInternalRatingRelevant0(byte[] internalRatingRelevant0) {
        this.internalRatingRelevant0 = internalRatingRelevant0;
        return this;
    }

    public JsonEvent withInternalRatingRelevant1(byte[] internalRatingRelevant1) {
        this.internalRatingRelevant1 = internalRatingRelevant1;
        return this;
    }

    public JsonEvent withInternalRatingRelevant2(byte[] internalRatingRelevant2) {
        this.internalRatingRelevant2 = internalRatingRelevant2;
        return this;
    }

    public JsonEvent withExternalRatingIrrelevant0(byte[] externalRatingIrrelevant0) {
        this.externalRatingIrrelevant0 = externalRatingIrrelevant0;
        return this;
    }

    public JsonEvent withExternalRatingIrrelevant1(byte[] externalRatingIrrelevant1) {
        this.externalRatingIrrelevant1 = externalRatingIrrelevant1;
        return this;
    }

    public JsonEvent withExternalRatingIrrelevant2(byte[] externalRatingIrrelevant2) {
        this.externalRatingIrrelevant2 = externalRatingIrrelevant2;
        return this;
    }

    public JsonEvent withExternalRatingResult0(byte[] externalRatingResult0) {
        this.externalRatingResult0 = externalRatingResult0;
        return this;
    }

    public JsonEvent withExternalRatingResult1(byte[] externalRatingResult1) {
        this.externalRatingResult1 = externalRatingResult1;
        return this;
    }

    public JsonEvent withExternalRatingResult2(byte[] externalRatingResult2) {
        this.externalRatingResult2 = externalRatingResult2;
        return this;
    }

    public JsonEvent withExternalDataTransp0(byte[] externalDataTransp0) {
        this.externalDataTransp0 = externalDataTransp0;
        return this;
    }

    public JsonEvent withExternalDataTransp1(byte[] externalDataTransp1) {
        this.externalDataTransp1 = externalDataTransp1;
        return this;
    }

    public JsonEvent withExternalDataTransp2(byte[] externalDataTransp2) {
        this.externalDataTransp2 = externalDataTransp2;
        return this;
    }

    public JsonEvent withUniversalAttribute0(byte[] universalAttribute0) {
        this.universalAttribute0 = universalAttribute0;
        return this;
    }

    public JsonEvent withUniversalAttribute1(byte[] universalAttribute1) {
        this.universalAttribute1 = universalAttribute1;
        return this;
    }

    public JsonEvent withUniversalAttribute2(byte[] universalAttribute2) {
        this.universalAttribute2 = universalAttribute2;
        return this;
    }

    public JsonEvent withUniversalAttribute3(byte[] universalAttribute3) {
        this.universalAttribute3 = universalAttribute3;
        return this;
    }

    public JsonEvent withUniversalAttribute4(byte[] universalAttribute4) {
        this.universalAttribute4 = universalAttribute4;
        return this;
    }

    @Override
    public String toString() {
        return "JsonEvent{" + "timeId=" + timeId + ", useId=" + useId + ", eventId=" + eventId + ", accessKey='" + accessKey + '\'' + ", accessKeyType=" + accessKeyType + ", owningCustomerID='" + owningCustomerID + '\'' + ", rootCustomerID='" + rootCustomerID + '\'' + ", composedCustomerID='" + composedCustomerID + '\'' + ", eventType=" + eventType + ", originalEventTime=" + originalEventTime + ", creationEventTime=" + creationEventTime + ", effectiveEventTime=" + effectiveEventTime + ", billCycleID=" + billCycleID + ", billPeriodID=" + billPeriodID + ", errorCode=" + errorCode + ", rateEventType='" + rateEventType + '\'' + ", externalCorrelationID=" + externalCorrelationID + ", rootCustomerIDHash=" + rootCustomerIDHash + ", projectAddOn='" + projectAddOn + '\'' + ", refEventId=" + refEventId + ", refUseId=" + refUseId + ", internalRatingRelevant0=" + Arrays.toString(
                internalRatingRelevant0) + ", internalRatingRelevant1=" + Arrays.toString(internalRatingRelevant1) + ", internalRatingRelevant2=" + Arrays.toString(internalRatingRelevant2) + ", externalRatingIrrelevant0=" + Arrays.toString(
                externalRatingIrrelevant0) + ", externalRatingIrrelevant1=" + Arrays.toString(externalRatingIrrelevant1) + ", externalRatingIrrelevant2=" + Arrays.toString(externalRatingIrrelevant2) + ", externalRatingResult0=" + Arrays.toString(
                externalRatingResult0) + ", externalRatingResult1=" + Arrays.toString(externalRatingResult1) + ", externalRatingResult2=" + Arrays.toString(externalRatingResult2) + ", externalDataTransp0=" + Arrays.toString(
                externalDataTransp0) + ", externalDataTransp1=" + Arrays.toString(externalDataTransp1) + ", externalDataTransp2=" + Arrays.toString(externalDataTransp2) + ", universalAttribute0=" + Arrays.toString(
                universalAttribute0) + ", universalAttribute1=" + Arrays.toString(universalAttribute1) + ", universalAttribute2=" + Arrays.toString(universalAttribute2) + ", universalAttribute3=" + Arrays.toString(
                universalAttribute3) + ", universalAttribute4=" + Arrays.toString(universalAttribute4) + ", dayId=" + dayId + '}';
    }
}