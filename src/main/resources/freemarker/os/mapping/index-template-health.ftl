<#ftl output_format="JSON">
{
    "index_patterns": ["${indexName}*"],
    "settings": {
        "index.plugins.index_state_management.policy_id": "${indexesPrefix}",
        "index.plugins.index_state_management.rollover_alias": "${indexName}",
        "index.number_of_shards":${numberOfShards},
        "index.number_of_replicas":${numberOfReplicas},
        "index.refresh_interval": "${refreshInterval}"
        <#if extendedSettingsTemplate??>,<#include "/${extendedSettingsTemplate}"></#if>
    },
    "mappings": {
            "properties": {
                "api": {
                    "type": "keyword"
                },
                "available": {
                    "type": "boolean",
                    "index": false
                },
                "endpoint": {
                    "type": "keyword"
                },
                "gateway": {
                    "type": "keyword"
                },
                "response-time": {
                    "type": "integer"
                },
                "state": {
                    "type": "integer",
                    "index": false
                },
                "transition": {
                    "type": "boolean"
                },
                "steps": {
                    "type": "object",
                    "enabled": false
                },
                "success": {
                    "type": "boolean",
                    "index": true
                }
            }
    }
}
