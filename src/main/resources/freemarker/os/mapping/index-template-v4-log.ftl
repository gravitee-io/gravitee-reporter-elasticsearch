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
            "@timestamp": {
                "type": "date"
            },
            "api-id": {
                "type": "keyword"
            },
            "request-id": {
                "type": "keyword"
            },
            "client-identifier": {
                "type": "keyword"
            },
            "request-ended": {
                "type": "boolean"
            },
            "entrypoint-request": {
                "type": "object",
                "properties": {
                    "body":{
                        "type": "text"
                    },
                    "headers":  {
                       "enabled":  false,
                       "type": "object"
                   }
                }
            },
            "entrypoint-response": {
                "type": "object",
                "properties": {
                    "body":{
                        "type": "text"
                    },
                    "headers":  {
                       "enabled":  false,
                       "type": "object"
                   }
                }
            },
            "endpoint-request": {
                "type": "object",
                "properties": {
                    "body":{
                        "type": "text"
                    },
                    "headers":  {
                       "enabled":  false,
                       "type": "object"
                   }
                }
            },
            "endpoint-response": {
                "type": "object",
                "properties": {
                    "body":{
                        "type": "text"
                    },
                    "headers": {
                        "enabled":  false,
                        "type": "object"
                    }
                }
            }
        }
    }
}
