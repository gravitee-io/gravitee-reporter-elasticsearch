<#ftl output_format="JSON">
{
    "index_patterns": ["${indexName}*"],
    "data_stream": {},
    <#if indexLifecyclePolicyEventMetrics?has_content>
    "template": {
        "settings": {
            "${indexLifecyclePolicyPropertyName?json_string}": "${indexLifecyclePolicyEventMetrics?json_string}"
        }
    },
    </#if>
    "priority": 9344593,
    "_meta": {
        "description": "Template for event metrics time series data stream"
    }
}
