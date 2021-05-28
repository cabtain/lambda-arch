package com.sds.iot.processor;

import static com.datastax.spark.connector.japi.CassandraStreamingJavaUtil.javaFunctions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.State;
import org.apache.spark.streaming.StateSpec;
import org.apache.spark.streaming.api.java.JavaDStream;

import com.sds.iot.dto.AggregateKey;
import com.sds.iot.dto.AggregateValue;
import com.sds.iot.entity.WindowTrafficData;
import com.datastax.spark.connector.japi.CassandraJavaUtil;
import com.sds.iot.entity.TotalTrafficData;
import com.sds.iot.dto.IoTData;

import scala.Tuple2;

/**
 * Class to process IoT data stream and to produce traffic data details.
 *
 * @author abaghel
 */
public class RealtimeTrafficDataProcessor {

    private static final Logger logger = Logger.getLogger(RealtimeTrafficDataProcessor.class);

    /**
     * Method to get window traffic counts of different type of vehicles for each route. Window duration = 30 seconds
     * and Slide interval = 10 seconds
     *
     * @param filteredIotDataStream IoT data stream
     */
    public static void processWindowTrafficData(JavaDStream<IoTData> filteredIotDataStream) {
        // reduce by key and window (30 sec window and 10 sec slide).
        JavaDStream<WindowTrafficData> trafficDStream = filteredIotDataStream
                .mapToPair(iot -> new Tuple2<>(new AggregateKey(iot.getRouteId(), iot.getVehicleType()), 
                    new AggregateValue(1L, new Double(iot.getSpeed()).longValue())))
                .reduceByKeyAndWindow(((Function2<AggregateValue, AggregateValue, AggregateValue>) (a, b) -> 
                    new AggregateValue(a.getCount() + b.getCount(), a.getSum() + b.getSum())), 
                    Durations.seconds(30), Durations.seconds(10))
                .map(RealtimeTrafficDataProcessor::mapToWindowTrafficData);

        saveWindTrafficData(trafficDStream);
    }

    /**
     * Method to get total traffic counts of different type of vehicles for each route.
     *
     * @param filteredIotDataStream IoT data stream
     */
    public static void processTotalTrafficData(JavaDStream<IoTData> filteredIotDataStream) {
        // Need to keep state for total count
        StateSpec<AggregateKey, AggregateValue, AggregateValue, Tuple2<AggregateKey, AggregateValue>> stateSpec = StateSpec
                .function(RealtimeTrafficDataProcessor::updateState)
                .timeout(Durations.seconds(3600));

        // We need to get count of vehicle group by routeId and vehicleType
        JavaDStream<TotalTrafficData> trafficDStream = filteredIotDataStream
                //.mapToPair(iot -> new Tuple2<>(new AggregateKey(iot.getRouteId(), iot.getVehicleType()), 1L))
                .mapToPair(iot -> new Tuple2<>(new AggregateKey(iot.getRouteId(), iot.getVehicleType()), 
                    new AggregateValue(1L, new Double(iot.getSpeed()).longValue())))
                .reduceByKey((Function2<AggregateValue, AggregateValue, AggregateValue>) (a, b) -> 
                    new AggregateValue(a.getCount() + b.getCount(), a.getSum() + b.getSum()))
                .mapWithState(stateSpec)
                .map(tuple2 -> tuple2)
                .map(RealtimeTrafficDataProcessor::mapToTrafficData);

        saveTotalTrafficData(trafficDStream);
    }

    private static void saveTotalTrafficData(final JavaDStream<TotalTrafficData> trafficDStream) {
        // Map Cassandra table column
        HashMap<String, String> columnNameMappings = new HashMap<>();
        columnNameMappings.put("routeId", "routeid");
        columnNameMappings.put("vehicleType", "vehicletype");
        columnNameMappings.put("totalCount", "totalcount");
        columnNameMappings.put("totalSum", "totalsum");
        columnNameMappings.put("timeStamp", "timestamp");
        columnNameMappings.put("recordDate", "recorddate");

        // call CassandraStreamingJavaUtil function to save in DB
        javaFunctions(trafficDStream).writerBuilder(
                "traffickeyspace",
                "total_traffic",
                CassandraJavaUtil.mapToRow(TotalTrafficData.class, columnNameMappings)
        ).saveToCassandra();
    }


    private static void saveWindTrafficData(final JavaDStream<WindowTrafficData> trafficDStream) {
        // Map Cassandra table column
        HashMap<String, String> columnNameMappings = new HashMap<>();
        columnNameMappings.put("routeId", "routeid");
        columnNameMappings.put("vehicleType", "vehicletype");
        columnNameMappings.put("totalCount", "totalcount");
        columnNameMappings.put("totalSum", "totalsum");
        columnNameMappings.put("timeStamp", "timestamp");
        columnNameMappings.put("recordDate", "recorddate");

        // call CassandraStreamingJavaUtil function to save in DB
        javaFunctions(trafficDStream).writerBuilder(
                "traffickeyspace",
                "window_traffic",
                CassandraJavaUtil.mapToRow(WindowTrafficData.class, columnNameMappings)
        ).saveToCassandra();
    }

    /**
     * Function to create WindowTrafficData object from IoT data
     *
     * @param tuple
     * @return
     */
    private static WindowTrafficData mapToWindowTrafficData(Tuple2<AggregateKey, AggregateValue> tuple) {
        logger.info("Window Count : " +
                "key " + tuple._1().getRouteId() + "-" + tuple._1().getVehicleType() +
                " value " + tuple._2().getCount() + "," + tuple._2().getSum());

        WindowTrafficData trafficData = new WindowTrafficData();
        trafficData.setRouteId(tuple._1().getRouteId());
        trafficData.setVehicleType(tuple._1().getVehicleType());
        trafficData.setTotalCount(tuple._2().getCount());
        trafficData.setTotalSum(tuple._2().getSum());
        trafficData.setTimeStamp(new Date());
        trafficData.setRecordDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        return trafficData;
    }

    private static TotalTrafficData mapToTrafficData(Tuple2<AggregateKey, AggregateValue> tuple) {
        logger.info(
                "Total Count : " + "key " + tuple._1().getRouteId() + "-" + tuple._1().getVehicleType() + 
                " value " + tuple._2().getCount() + "," + tuple._2().getSum());
        TotalTrafficData trafficData = new TotalTrafficData();
        trafficData.setRouteId(tuple._1().getRouteId());
        trafficData.setVehicleType(tuple._1().getVehicleType());
        trafficData.setTotalCount(tuple._2().getCount());//Count
        trafficData.setTotalSum(tuple._2().getSum());
        trafficData.setTimeStamp(new Date());
        trafficData.setRecordDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        return trafficData;
    }


    /**
     * Function to get running sum by maintaining the state
     *
     * @param key
     * @param currentSum
     * @param state
     * @return
     */
    private static Tuple2<AggregateKey, AggregateValue> updateState(
            AggregateKey key,
            org.apache.spark.api.java.Optional<AggregateValue> currentValue,
            State<AggregateValue> state
    ) {
        AggregateValue objectOption = currentValue.get();
        objectOption = objectOption == null ? new AggregateValue(0L, 0L) : objectOption;

        long totalCount = objectOption.getCount() + (state.exists() ? state.get().getCount() : 0);
        long totalSum = objectOption.getSum() + (state.exists() ? state.get().getSum() : 0);
        AggregateValue value = new AggregateValue(totalCount, totalSum);
        Tuple2<AggregateKey, AggregateValue> total = new Tuple2<>(key, value);
        state.update(value);
        return total;
    }

}
