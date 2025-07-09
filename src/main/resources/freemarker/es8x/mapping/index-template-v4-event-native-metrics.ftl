<#ftl output_format="JSON">
{
    "index_patterns": ["gravitee-v4-metrics-data-stream-*"],
    "data_stream": {},
    "template": {
        "settings": {
            "index.mode": "time_series",
            "index.lifecycle.name": "gravitee-v4-metrics-data-stream-policy"
        },
        "mappings": {
            "properties": {
                "gw-id": {
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
                "api-id": {
                    "type": "keyword",
                    "time_series_dimension": true
                },
                "plan-id": {
                    "type": "keyword",
                    "time_series_dimension": true
                },
                "app-id": {
                    "type": "keyword",
                    "time_series_dimension": true
                },
                "topic": {
                    "type": "keyword",
                    "time_series_dimension": true
                },
                "downstream-publish-messages-total": {
                    "type": "integer",
                    "time_series_metric": "counter"
                },
                "downstream-publish-message-bytes": {
                    "type": "long",
                    "time_series_metric": "counter"
                },
                "upstream-publish-messages-total": {
                    "type": "integer",
                    "time_series_metric": "counter"
                },
                "upstream-publish-message-bytes": {
                    "type": "long",
                    "time_series_metric": "counter"
                },
                "downstream-subscribe-messages-total": {
                    "type": "integer",
                    "time_series_metric": "counter"
                },
                "downstream-subscribe-message-bytes": {
                    "type": "long",
                    "time_series_metric": "counter"
                },
                "upstream-subscribe-messages-total": {
                    "type": "integer",
                    "time_series_metric": "counter"
                },
                "upstream-subscribe-message-bytes": {
                    "type": "long",
                    "time_series_metric": "counter"
                },
                "downstream-active-connections": {
                    "type": "integer",
                    "time_series_metric": "gauge"
                },
                "upstream-active-connections": {
                    "type": "integer",
                    "time_series_metric": "gauge"
                },
                "@timestamp": {
                    "type": "date"
                }
            }
        }
    },
    "priority": 9344593,
    "_meta": {
        "description": "Template for Gravitee v4 metrics data stream"
    }
}