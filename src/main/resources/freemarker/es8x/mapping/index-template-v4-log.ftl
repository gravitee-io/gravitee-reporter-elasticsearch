<#ftl output_format="JSON">
{
    "index_patterns": ["${indexName}*"],
    "template": {
        "settings": {
            <#if indexLifecyclePolicyLog??>"${indexLifecyclePolicyPropertyName}": "${indexLifecyclePolicyLog}",</#if>
            <#if indexLifecyclePolicyLog??>"index.lifecycle.rollover_alias": "${indexName}",</#if>
            "index.number_of_shards":${numberOfShards},
            "index.number_of_replicas":${numberOfReplicas},
            "index.refresh_interval": "${refreshInterval}"
            <#if !(extendedSettingsTemplate.analysis)??>,
            "analysis": {
                "analyzer": {
                    "gravitee_body_analyzer": {
                        "type": "custom",
                        "tokenizer": "whitespace",
                        "filter": [
                            "lowercase"
                        ]
                    }
                }
            }
            </#if>
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
                "api-name": {
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
                            "type": "text",
                            "analyzer": "gravitee_body_analyzer"
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
                            "type": "text",
                            "analyzer": "gravitee_body_analyzer"
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
                            "type": "text",
                            "analyzer": "gravitee_body_analyzer"
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
                            "type": "text",
                            "analyzer": "gravitee_body_analyzer"
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
}
