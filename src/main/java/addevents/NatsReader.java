package addevents;

import io.nats.client.*;
import io.nats.client.api.ConsumerConfiguration;
import io.nats.client.api.StorageType;
import io.nats.client.api.StreamConfiguration;
import io.nats.client.api.StreamInfo;
import io.nats.client.impl.NatsMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

/**
 * NatsReader reads messages from NATS server. It reads a batch of messages
 * to prevent it bloating the service as there are  millions of Event
 * messages stored in NATS.
 *
 */
public final class NatsReader implements Supplier<List<EventMessage>> , NatsConsumer {
    private static final Logger logger = LogManager.getLogger(NatsReader.class);
    Random random = new Random();

    public static final String TRANSFERS = "Transfers";
    @CommandLine.Option(names = { "-s"}, defaultValue = "nats://192.168.49.2:30409", paramLabel = "NatsServer",
            description = "the nats server endpoint")
    public String endpoint;
    @CommandLine.Option(names = { "-streams" }, defaultValue = "1", paramLabel = "NumStreams",
            description = "The Number of Streams to create in NATS 1-N (Events0-N), subject will be \"Events0.>\". " +
                    "by default 1 stream is created")
    public Integer numberOfStreams;

    @CommandLine.Option(names = { "-mc" }, required = true, paramLabel = "NumMsgs",
            description = "number of message to send to each partition")
    public Integer msgCount;

    @CommandLine.Option(names = { "-timeId" }, required = true, paramLabel = "TimeId",
            description = "TimeId for the Event")
    public Integer timeId;

    @CommandLine.Option(names = { "-useId" }, required = true, paramLabel = "UseId",
            description = "useId for the Event")
    public Integer useId;

    @CommandLine.Option(names = { "-group" }, required = true, paramLabel = "Group",
            description = "Group for the Event")
    public Integer group;

    @CommandLine.Option(names = { "-custPartId", "--custPartId" }, paramLabel = "custPartId",
            description = "custPartId for the Event")
    public Integer custPartId;

    @CommandLine.Option(names = { "-partitions" }, defaultValue = "60000", paramLabel = "NumPartitions",
            description = "The maximum number of partitions with any stream. By default its 60,000" )
    public Integer numberOfPartitions;

    @CommandLine.Option(names = { "-rcIdHash" }, required = true, paramLabel = "CustIdHash",
            description = "Root CustomerId Hash for the Event")
    public Integer rcIdHash;


    enum TestCases { publishAllStreams  }
    @CommandLine.Option(names = { "-tst", "--testName" }, required = true, paramLabel = "TestNameToExecute",
            description = "Test to execute must be one of these values: ${COMPLETION-CANDIDATES}")
    TestCases tstName = null;


    private static final int MAX_ALLOWED_BATCH_SIZE = 256;
    public static final int MAX_ACK_PENDING = -1;
    private static final Integer connectByteBufferSize = 20*1024*1024;
    // 1 second
    private static final Integer initialMaxWaitTimeMs = 1000;
    private static final Integer maxWaitTimeMs = 100;
    private final NatsConfiguration configuration;
    private int totalRead;
    private boolean noMoreMsgs;
    private EventSerialization event;

    public NatsReader(NatsConfiguration configuration) {
        this.configuration = configuration;
        totalRead = 0;
        noMoreMsgs =  true;
        event = NatsEventSerializer.getInstance();

    }

    /**
     * Consumes up to fetchBatchSize messages from NATS.
     * Populates the Messages extracted into a container, see getMessages API
     * call to return these Messages.
     *
     * @return list of the message consumed from NATS
     */
    public List<EventMessage> get() {
        Options options = new Options.Builder().
                connectionTimeout(Duration.ofSeconds(60)).
                server(configuration.getUrl()).
                connectionListener(new NatsConnectionListener(this)).
                reconnectBufferSize(configuration.getConnectionByteBufferSize()).  // Set buffer in bytes
                        build();
        List<EventMessage> messages = new ArrayList<>();
        try (Connection nc = getConnection(options)) {
            JetStreamManagement jsm = nc.jetStreamManagement();
            JetStream js = getJetStream(nc, jsm);
            consumeMessages(js, messages);
        } catch (IOException e) {
            logger.error("I/O error communicating to the NATS server.", e);
        } catch (InterruptedException | JetStreamApiException | TimeoutException e) {
            logger.error("Processing JetStream messages error.", e);
        }
        return messages;
    }

    public void publish(ByteArrayOutputStream output, int numEvents, String subject) {
        Options options = new Options.Builder().
                server(configuration.getUrl()).
                connectionListener(new NatsConnectionListener(this)).
                reconnectBufferSize(configuration.getConnectionByteBufferSize()).  // Set buffer in bytes
                        build();
        try (Connection nc = getConnection(options)) {
            JetStreamManagement jsm = nc.jetStreamManagement();
            JetStream js = nc.jetStream();
            for (int x = 0; x < numEvents; x++) {
                Message msg = NatsMessage.builder()
                        .subject(subject)
                        .data(output.toByteArray())
                        .build();
                js.publish(msg);
            }
        } catch (IOException e) {
            System.err.println("Error I/O ["+e.getLocalizedMessage());
        } catch (InterruptedException | JetStreamApiException  e) {
            System.err.println("Error  ["+e.getLocalizedMessage());
        } catch (Exception e) {
            System.err.println("Error  ["+e.getLocalizedMessage());

        }
    }

    /**
     * Has Nats indicated that no more messages to read
     *
     * @return true if no more messages to read otherwise false
     */
    public boolean noMoreMsgs() {
      return noMoreMsgs;
    }

    /**
     * Get the total message this consumer has gotten from the NATS server
     *
     * @return total number of messages consumer by  this subscriber.
     */
    public int getTotalRead() {
        return totalRead;
    }

    /**
     * Create a Pull subscriber using the durable name.
     *
     * @param js
     * @return handle to the pull consumer.
     * @throws JetStreamApiException
     * @throws IOException
     */
    private JetStreamSubscription getPullSubscribeOptions(JetStream js) throws JetStreamApiException, IOException {
        ConsumerConfiguration cc = ConsumerConfiguration.builder()
                .durable(configuration.getDurableName())
                .maxAckPending(MAX_ACK_PENDING) // as we are pull we should ste to -1 to allow us to scale out
                .build();
        PullSubscribeOptions pullOptions = PullSubscribeOptions.builder().configuration(cc).build();
        /*
         * Consume all the events in the stream
         */
        System.out.println("Creating subscriber for subject: "+configuration.getSubjectName()+ " with durablename: "+configuration.getDurableName());
        return js.subscribe(configuration.getSubjectName(), pullOptions);
    }

    /**
     * Create a pull subscriber with a durableName and the filter subject set.
     * @param js
     * @return handle to the filtered pull consumer.
     * @throws JetStreamApiException
     * @throws IOException
     */
    private JetStreamSubscription getFilteredPullSubscribeOptions(JetStream js) throws JetStreamApiException, IOException {
        ConsumerConfiguration cc = ConsumerConfiguration.builder()
                .filterSubject(configuration.getFilter())
                .durable(configuration.getDurableName())
                .maxAckPending(MAX_ACK_PENDING) // as we are pull we should ste to -1 to allow us to scale out
                .build();
        PullSubscribeOptions pullOptions = PullSubscribeOptions.builder().configuration(cc).build();
        logger.info("filter-consumer filtered on {} ", configuration.getFilter());
        /*
         * Consume all the events in the stream
         */
        return js.subscribe(configuration.getSubjectName(), pullOptions);
    }

    /**
     * Consume the message from NATS.
     *
     * May require more than one iteration if the size is greater than the max size (256) of
     * a NATS pull batch.
     *
     * @param js
     * @param messages
     * @throws JetStreamApiException
     * @throws IOException
     */
    private void consumeMessages(JetStream js, List<EventMessage> messages) throws JetStreamApiException, IOException {
        JetStreamSubscription pullSub;
        if (configuration.getFilter() != null &&
                configuration.getFilter().length() > 0) {
            pullSub = getFilteredPullSubscribeOptions(js);
        } else {
            pullSub = getPullSubscribeOptions(js);
        }
        /*
         * dont block waiting on the complete batch size of messages
         * if we receive less than batch size then we should return
         *
         * no point polling nats
         */
        try {
            boolean noMoreToRead = false;
            do {
                /*
                 * NATS mandates batch size of 256 messages max in a batch if the fetch size is >
                 * then we need to chunk the response from NATS and grab it in chunks of 256
                 */
                int batchSize = calculateBatchSize();
                pullSub.pullNoWait(batchSize);
                /*
                 * if noMoreToRead false indicates that we have exhausted this batch and there might be more
                 * to retrieve from NATS
                 * true means that NATS has sent a 404 indicating no more messages at this time
                 * lets exit, regardless
                 */
                noMoreToRead = fetchBatch(messages, pullSub);
            } while (noMoreToRead == false && totalRead < configuration.getBatchSize());
        } catch (InterruptedException e) {
            logger.error("Error reading message of the NATS server.",e);
        }
    }

    /**
     * fetchBatch
     * fetches a batch of messages from NATS
     *
     * @return true iff NATS has no more messages available.
     *
     * @param messages
     * @param pullSub */
    private  boolean fetchBatch(List<EventMessage> messages, Subscription pullSub) throws InterruptedException {
        /*
         * wait a period for the first one
         */
        noMoreMsgs =  true;
        Message msg = pullSub.nextMessage(Duration.ofMillis(configuration.getInitialMaxWaitTimeMs()));
        if (msg !=null) {
            noMoreMsgs = false;
        }
        while (msg != null) {
            if (msg.isJetStream()) {
                EventMessage pojo = null;
                try {
                    pojo = event.deserialize(new ByteArrayInputStream(msg.getData()));
                    messages.add(pojo);
                    totalRead++;
                    msg.ack();
                    /*
                     * message should be here.
                     */
                    msg = pullSub.nextMessage(Duration.ofMillis(configuration.getMaxWaitTimeMs()));
                } catch (IOException e) {
                    logger.error("Problem serializing the Nats message to JSON, continuing", e);
                }

            } else if (msg.isStatusMessage()) {
                /*
                 * This indicates batch has nothing more to send
                 *
                 * m.getStatus().getCode should == 404
                 * m.getStatus().getMessage should be "No Messages"
                 */
                msg = null;
                //
                noMoreMsgs = true;
            }
        }
        return noMoreMsgs;
    }

    /**
     * Determines the size of a batch request sent to NATS.
     *
     * @return size of the batch to use in request to  NATS
     */
    private int calculateBatchSize() {
        int outStanding = configuration.getBatchSize() - totalRead;

        return outStanding > MAX_ALLOWED_BATCH_SIZE ? MAX_ALLOWED_BATCH_SIZE : outStanding;
    }

    /**
     * Get the JetStream handle from NATS
     *
     * @param nc
     * @param jsm
     * @return
     * @throws JetStreamApiException
     * @throws IOException
     * @throws InterruptedException
     * @throws TimeoutException
     */
    public JetStream getJetStream(Connection nc, JetStreamManagement jsm) throws
            JetStreamApiException, IOException, InterruptedException, TimeoutException {
        /*
         * Perhaps we should drive this from the command line to build the
         * stream up front using nats.cli.
         */
        StreamConfiguration streamConfig = StreamConfiguration.builder()
                .name(configuration.getStreamName())
                .subjects(configuration.getSubjectName())
                .storageType(StorageType.File)
                .replicas(configuration.getNumberOfReplicas())
                .maxMessagesPerSubject(Long.MAX_VALUE)
                .build();
        // Create the stream
        StreamInfo streamInfo = getStreamInfo(jsm, configuration.getStreamName(), false);
        if (streamInfo == null) {
            jsm.addStream(streamConfig);
        }
        JetStream js = nc.jetStream();
        nc.flush(Duration.ofSeconds(1));
        return js;
    }

    /**
     * Get the stream information from NATS JetStream
     *
     * @param jsm
     * @param streamName
     * @param deleteStr
     * @return
     */
    private StreamInfo getStreamInfo(JetStreamManagement jsm, String streamName, boolean deleteStr) {
        StreamInfo strDetails = null;
        try {
            strDetails = jsm.getStreamInfo(streamName);
            if (strDetails != null && deleteStr) {
                jsm.deleteStream(streamName);
                strDetails = null;
            }
        }
        catch (JetStreamApiException | IOException jsae) {
            logger.info("Error NATS Management API stream", jsae);
            return null;
        }
        return strDetails;
    }

    /**
     * Create a Stream for each parition.
     *
     * @param numberOfStreams
     */
    public void createPartitionedEventStreams(int numberOfStreams ) {
        Options options = new Options.Builder().
                server(configuration.getUrl()).
                connectionListener(new NatsConnectionListener(this)).
                reconnectBufferSize(configuration.getConnectionByteBufferSize()).  // Set buffer in bytes
                        build();
        try (Connection nc = getConnection(options)) {
            JetStreamManagement jsm = nc.jetStreamManagement();
            /*
             * Perhaps we should drive this from the command line to build the
             * stream up front using nats.cli.
             */
            for (int stream = 0; stream < numberOfStreams; ++stream) {
                String strStream = Integer.toString(stream);
                String streamName = configuration.getStreamName()+strStream;
                String subjectName = streamName+".>";
                StreamConfiguration streamConfig = StreamConfiguration.builder()
                        .name(streamName)
                        .subjects(subjectName)
                        .storageType(StorageType.File)
                        .replicas(configuration.getNumberOfReplicas())
                        .maxMessagesPerSubject(Long.MAX_VALUE)
                        .build();
                // Create the stream
                // StreamInfo streamInfo = getStreamInfo(jsm, streamName, false);
                jsm.addStream(streamConfig);
                logger.error("Created stream: {} subject : {} ", streamName, subjectName);

/**
 *

                if (streamInfo == null) {
                    jsm.addStream(streamConfig);
                    logger.error("Created stream: {} subject : {} ", streamName, subjectName);
                } else {
                    logger.error("Stream already present. Not Created: Stream :{} subject : {} ", streamName, subjectName);
                }
 */
                Thread.sleep(10);;
            }
        } catch (IOException e) {
            logger.error("I/O error",e);
        } catch (InterruptedException | JetStreamApiException  e) {
            logger.error("NATS Error ", e);
        } catch (Exception e) {
            logger.error("Error  ",e);
        }
    }
    public void deletePartitionedEventStreams(int numberPartitions ) {
        Options options = new Options.Builder().
                server(configuration.getUrl()).
                reconnectBufferSize(configuration.getConnectionByteBufferSize()).  // Set buffer in bytes
                        build();
        try (Connection nc = getConnection(options)) {
            JetStreamManagement jsm = nc.jetStreamManagement();

            for (int part = 0; part < numberPartitions; ++part) {
                String partition = Integer.toString(part);
                String streamName = configuration.getStreamName() + partition;
                StreamInfo strDetails = jsm.getStreamInfo(streamName);
                if (strDetails != null ) {
                    jsm.deleteStream(streamName);
                    System.out.println("Deleted Stream: "+streamName);
                } else{
                    System.out.println("Stream: "+streamName+" doest not exist. Cannot delete");
                }
            }
        } catch (IOException e) {
            logger.error("Error  ",e);
        } catch (InterruptedException jsa) {
            logger.error("Error  ",jsa);
        } catch (JetStreamApiException e) {
            logger.error("Error stream not found ",e);
        }
    }
    private void createTransferStream() {
        Options options = new Options.Builder().
                server(configuration.getUrl()).
                connectionListener(new NatsConnectionListener(this)).
                reconnectBufferSize(configuration.getConnectionByteBufferSize()).  // Set buffer in bytes
                        build();
        try (Connection nc = getConnection(options)) {
            JetStreamManagement jsm = nc.jetStreamManagement();
            /*
             * Perhaps we should drive this from the command line to build the
             * stream up front using nats.cli.
             */

            String streamName = TRANSFERS;
            String subjectName = streamName+".>";
            StreamConfiguration streamConfig = StreamConfiguration.builder()
                    .name(streamName)
                    .subjects(subjectName)
                    .storageType(StorageType.File)
                    .replicas(configuration.getNumberOfReplicas())
                    .maxMessagesPerSubject(Long.MAX_VALUE)
                    .build();
            // Create the stream
            StreamInfo streamInfo = getStreamInfo(jsm, streamName, false);
            if (streamInfo == null) {
                jsm.addStream(streamConfig);
                logger.error("Created stream: {} subject : {} ", streamName, subjectName);
            } else {
                logger.error("Stream already present. Not Created: Stream :{} subject : {} ", streamName, subjectName);
            }
        } catch (IOException e) {
            logger.error("I/O error",e);
        } catch (InterruptedException | JetStreamApiException  e) {
            logger.error("NATS Error ", e);
        } catch (Exception e) {
            logger.error("Error  ",e);
        }

    }

    public int getRandomInt(int min, int max) {
        return random.nextInt(max - min) + min;
    }

    public void publishToAllStreams(int numberOfStreams, int numMsgsPerStream,
                                    int useId, int timeId,int group, int numberOfBuckets) {
        ExecutorService executorService =
                Executors.newFixedThreadPool(numberOfStreams);
        long startTime = System.nanoTime();
        Date now = java.util.Calendar.getInstance().getTime();
        Options options = new Options.Builder().
                server(configuration.getUrl()).
                connectionListener(new NatsConnectionListener(this)).
                reconnectBufferSize(configuration.getConnectionByteBufferSize()).  // Set buffer in bytes
                        build();
        for (int i = 0; i < numberOfStreams; i++) {
            final int finalI = i;
            executorService.execute(() -> {
                Connection nc = null;
                try {
                     nc = this.getConnection(options);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (int j = 0; j < numMsgsPerStream; j++) {
                    try (ByteArrayOutputStream output = new ByteArrayOutputStream();) {
                        JetStream js = nc.jetStream();
                        int rcIdHash = getRandomInt(0, 255);
                        JsonEvent event = new JsonEvent().withTimeId(timeId)
                                .withUseId(useId)
                                .withEventId(22)
                                .withAccessKey("4474080310311")
                                .withOwningCustomerID("447408031031")
                                .withRootCustomerID("447408031031")
                                .withEventType(10022)
                                .withOriginalEventTime(now)
                                .withCreationEventTime(now)
                                .withEffectiveEventTime(now)
                                .withBillCycleID(1)
                                .withBillPeriodID(98)
                                .withRateEventType("MOC")
                                .withRootCustomerIDHash(rcIdHash);
                        // subject is streamName + partitionId(0-60k) e.g. Events0.12345
                        String subject = "Events" + finalI + "." + getRandomInt(0, numberOfBuckets);
                        /**
                         * publish numMsgsPerPartition for this partition
                         */

                        //event.serialize(output);
                        NatsEventSerializer.getInstance().serialize(event, output);
                        Message msg = NatsMessage.builder()
                                .subject(subject)
                                .data(output.toByteArray())
                                .build();
                        try {
                            js.publish(msg);
                        } catch (Exception e) {
                            System.err.println("Error Publishing message to nats : "+e.getLocalizedMessage());
                            nc = getConnection(nc, options);
                        }
                    } catch (Exception e) {
                        System.err.println("Error Publishing message to nats : "+e.getLocalizedMessage());
                    }
                }
            });
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void publishAllStreams(int numberOfStreams, int numMsgsPerStream,
                                     int useId, int timeId,int group, int numberOfBuckets) {
        Date now = java.util.Calendar.getInstance().getTime();
        Options options = new Options.Builder().
                server(configuration.getUrl()).
                connectionListener(new NatsConnectionListener(this)).
                reconnectBufferSize(configuration.getConnectionByteBufferSize()).  // Set buffer in bytes
                        build();
        Connection nc = null;
        try {
            nc = getConnection(options);

            JetStream js = nc.jetStream();

            for (int stream = 0; stream < numberOfStreams; stream++) {
                for (int x = 0; x < numMsgsPerStream; x++) {
                    // number 0-255
                    try (ByteArrayOutputStream output = new ByteArrayOutputStream();) {
                        int rcIdHash = getRandomInt(0, 255);
                        JsonEvent event = new JsonEvent().withTimeId(timeId)
                                .withUseId(useId)
                                .withEventId(22)
                                .withAccessKey("4474080310311")
                                .withOwningCustomerID("447408031031")
                                .withRootCustomerID("447408031031")
                                .withEventType(10022)
                                .withOriginalEventTime(now)
                                .withCreationEventTime(now)
                                .withEffectiveEventTime(now)
                                .withBillCycleID(1)
                                .withBillPeriodID(98)
                                .withRateEventType("MOC")
                                .withRootCustomerIDHash(rcIdHash);
                        // subject is streamName + partitionId(0-60k) e.g. Events0.12345
                        String subject = "Events" + stream + "." + getRandomInt(0, numberOfBuckets);
                        /**
                         * publish numMsgsPerPartition for this partition
                         */

                        //event.serialize(output);
                        NatsEventSerializer.getInstance().serialize(event, output);
                        Message msg = NatsMessage.builder()
                                .subject(subject)
                                .data(output.toByteArray())
                                .build();
                        try {
                            js.publish(msg);
                        } catch (Exception e) {
                            System.err.println("Error Publishing message to nats ["+e.getLocalizedMessage());
                            nc = getConnection(nc, options);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error I/O ["+e.getLocalizedMessage());
        } catch (InterruptedException e) {
            System.err.println("Error  ["+e.getLocalizedMessage());
        } catch (Exception e) {
            System.err.println("Error  ["+e.getLocalizedMessage());
        } finally {
            close(nc);
        }

    }

    private Connection getConnection(final Options options) throws IOException, InterruptedException {
        return Nats.connect(options);
    }
    private Connection getConnection(Connection connection,final Options options) throws IOException, InterruptedException {
        if (connection != null) {
            try {
                connection.close();
            } catch (InterruptedException e) {
                System.err.println("Error closing connection ignoring : "+e.getLocalizedMessage());
            }
        }
        return Nats.connect(options);
    }
    private void close(Connection connection)  {
        if (connection != null) {
            try {
                connection.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public static void main(String[] args){
        NatsReader reader = new NatsReader(null);
        CommandLine commandLine = null;
        try {
            commandLine = new CommandLine(reader);
            commandLine.parseArgs(args);
            if (commandLine.isUsageHelpRequested()) {
                commandLine.usage(System.out);
                return;
            }
        } catch (CommandLine.ParameterException ex) {
            logger.error(ex.getMessage());
            commandLine.usage(System.out);
            return;
        }
        logger.error("server :{}", reader.endpoint);
        logger.error("number of streams :{} ", reader.numberOfStreams);
        logger.error("number of buckets :{} ", reader.numberOfPartitions);
        logger.error("number of message to send to each stream :{} ", reader.msgCount);
        logger.error("timeId :{} useId :{} ", reader.timeId, reader.useId);

        logger.error("Test to execute : {}", reader.tstName);

        NatsReaderConfiguration config = new NatsReaderConfiguration(true, "Events.>",
                null,
                "PULL", true ,
                reader.endpoint, "queueName",
                "Events", "durableName",
                1000, 3,  40, connectByteBufferSize,
                initialMaxWaitTimeMs, maxWaitTimeMs);
        NatsReader natsReader = new NatsReader(config);

        switch (reader.tstName) {

            case publishAllStreams:
                natsReader.publishAllStreams(reader.numberOfStreams, reader.msgCount, reader.useId, reader.timeId,
                        reader.group, reader.numberOfPartitions);
                break;
        }
    }

    @Override
    public NatsConfiguration getConfiguration() {
        return configuration;
    }
}
