<@compress single_line=true>
{
  "policy": {
    "policy_id": "${indexesPrefix}",
    "description": "Policy to manage rollover for ${indexesPrefix}-* indices",
    "default_state": "rollover_state",
    "states": [
      {
        "name": "rollover_state",
        "actions": [
          {
            "rollover": {
              <#if indexLifecycleMinIndexAge??>"min_index_age": "${indexLifecycleMinIndexAge}"</#if>
              "min_size": "${indexLifecycleMinSize}"
            }
          }
        ],
        "transitions": []
      }
    ],
    "ism_template": {
      "index_patterns": ["${indexesPrefix}-*"]
    }
  }
}
</@compress>