package com.example.asset_management.config;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.consumer.ConsumerGroupMetadata;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Configuration class for Docker profile that provides alternative beans
 * when Kafka is disabled for the minimal POC setup.
 */
@Configuration
@Profile("docker")
public class DockerProfileConfig {

    /**
     * Provides a mock ProducerFactory for Docker profile when Kafka is disabled.
     */
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        return new ProducerFactory<String, String>() {
            @Override
            public Producer<String, String> createProducer() {
                return new Producer<String, String>() {
                    @Override
                    public Future<RecordMetadata> send(ProducerRecord<String, String> record) {
                        // Return a completed future with mock metadata
                        SettableListenableFuture<RecordMetadata> future = new SettableListenableFuture<>();
                        future.set(null); // Mock metadata
                        return future;
                    }

                    @Override
                    public Future<RecordMetadata> send(ProducerRecord<String, String> record, org.apache.kafka.clients.producer.Callback callback) {
                        // Return a completed future with mock metadata
                        SettableListenableFuture<RecordMetadata> future = new SettableListenableFuture<>();
                        future.set(null); // Mock metadata
                        if (callback != null) {
                            callback.onCompletion(null, null);
                        }
                        return future;
                    }

                    @Override
                    public void flush() {
                        // No-op for Docker POC
                    }

                    @Override
                    public void close() {
                        // No-op for Docker POC
                    }

                    @Override
                    public void close(Duration timeout) {
                        // No-op for Docker POC
                    }

                    @Override
                    public Map<MetricName, ? extends Metric> metrics() {
                        // Return empty metrics map for Docker POC
                        return Map.of();
                    }

                    @Override
                    public List<PartitionInfo> partitionsFor(String topic) {
                        // Return empty list for Docker POC
                        return List.of();
                    }

                    @Override
                    public void initTransactions() {
                        // No-op for Docker POC
                    }

                    @Override
                    public void beginTransaction() {
                        // No-op for Docker POC
                    }

                    @Override
                    public void sendOffsetsToTransaction(Map<TopicPartition, OffsetAndMetadata> offsets, ConsumerGroupMetadata groupMetadata) {
                        // No-op for Docker POC
                    }

                    @Override
                    public void sendOffsetsToTransaction(Map<TopicPartition, OffsetAndMetadata> offsets, String consumerGroupId) {
                        // No-op for Docker POC
                    }

                    @Override
                    public void commitTransaction() {
                        // No-op for Docker POC
                    }

                    @Override
                    public void abortTransaction() {
                        // No-op for Docker POC
                    }
                };
            }
        };
    }

    /**
     * Provides a mock KafkaTemplate for Docker profile when Kafka is disabled.
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
