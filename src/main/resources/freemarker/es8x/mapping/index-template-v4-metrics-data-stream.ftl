<#ftl output_format="JSON">
{
    "index_patterns": ["${indexName}*"],
    "template": {
        "settings": {
            "index.lifecycle.name": ${indexLifecyclePolicyPropertyName},
            "index.mode": "time_series",
            "index.look_ahead_time": ${indexLookAheadTime},
            <#if extendedSettingsTemplate??>,<#include "/${extendedSettingsTemplate}"></#if>
        },
        "mappings": {
            "properties": {
                "topic-name": {
                    "type": "keyword",
                    "time_series_dimension": true
                },
                "plan-id": {
                    "type": "keyword",
                    "time_series_dimension": true
                },
                "api-id": {
                    "type": "keyword",
                    "time_series_dimension": true
                },
                "application-id": {
                    "type": "keyword",
                    "time_series_dimension": true
                },
                "gateway": {
                    "type": "keyword",
                    "time_series_dimension": true
                },
                "org-id": {
                    "type": "keyword",
                    "time_series_dimension": true
                },
                "env-id": {
                    "type": "keyword",
                    "time_series_dimension": true
                },
                "client_throughput_produce_bytes_total": {
                    "type": "long",
                    "time_series_metric": "counter"
                },
                "client_throughput_consume_bytes_total": {
                    "type": "long",
                    "time_series_metric": "counter"
                },
                "gateway_throughput_produce_bytes_total": {
                    "type": "long",
                    "time_series_metric": "counter"
                },
                "gateway_throughput_consume_bytes_total": {
                    "type": "long",
                    "time_series_metric": "counter"
                },
                "client_messages_produce_total": {
                    "type": "integer",
                    "time_series_metric": "counter"
                },
                "client_messages_consume_total": {
                    "type": "integer",
                    "time_series_metric": "counter"
                },
                "gateway_messages_produce_total": {
                    "type": "integer",
                    "time_series_metric": "counter"
                },
                "gateway_messages_consume_total": {
                    "type": "integer",
                    "time_series_metric": "counter"
                },
                "successful_connection_count": {
                    "type": "integer",
                    "time_series_metric": "gauge"
                },
                "failed_connection_count": {
                    "type": "integer",
                    "time_series_metric": "gauge"
                },
                "produce_throttle_time_per_topic": {
                    "type": "integer",
                    "time_series_metric": "counter"
                },
                "consume_throttle_time_per_topic": {
                    "type": "integer",
                    "time_series_metric": "counter"
                },
                "consume_lag_per_topic": {
                    "type": "integer",
                    "time_series_metric": "gauge"
                }
                <#if extendedRequestMappingTemplate??>,<#include "/${extendedRequestMappingTemplate}"></#if>
            }
        }
    }
}
