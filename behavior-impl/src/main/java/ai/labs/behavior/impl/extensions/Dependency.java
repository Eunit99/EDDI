package ai.labs.behavior.impl.extensions;

import ai.labs.behavior.impl.BehaviorGroup;
import ai.labs.behavior.impl.BehaviorRule;
import ai.labs.behavior.impl.BehaviorSet;
import ai.labs.memory.IConversationMemory;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author ginccc
 */
@Slf4j
@NoArgsConstructor
public class Dependency implements IBehaviorExtension {
    private static final String ID = "dependency";

    private String reference;

    private ExecutionState state = ExecutionState.NOT_EXECUTED;
    private final String referenceQualifier = "reference";
    private BehaviorSet behaviorSet;

    private Dependency(String referencedRuleName) {
        this.reference = referencedRuleName;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public Map<String, String> getValues() {
        HashMap<String, String> result = new HashMap<>();
        result.put(referenceQualifier, reference);
        return result;
    }

    @Override
    public void setValues(Map<String, String> values) {
        if (values != null && !values.isEmpty()) {
            if (values.containsKey(referenceQualifier)) {
                reference = values.get(referenceQualifier);
            }
        }
    }

    @Override
    public ExecutionState execute(IConversationMemory memory, List<BehaviorRule> trace)
            throws BehaviorRule.InfiniteLoopException {

        //before we execute the behavior rules we make deep copies, so that we don't change the rules in conversation memory!
        List<BehaviorRule> filteredBehaviorRules = new LinkedList<>();
        try {
            List<BehaviorGroup> behaviorGroups = behaviorSet.getBehaviorGroups();
            List<BehaviorRule> behaviorRules = new LinkedList<>();
            for (BehaviorGroup behaviorGroup : behaviorGroups) {
                behaviorRules.addAll(behaviorGroup.getBehaviorRules());
            }
            filteredBehaviorRules.addAll(cloneBehaviorRules(behaviorRules, reference));
        } catch (CloneNotSupportedException e) {
            log.error(e.getLocalizedMessage(), e);
        }

        for (BehaviorRule behaviorRule : filteredBehaviorRules) {
            ExecutionState state = behaviorRule.execute(memory, trace);
            if (state == ExecutionState.ERROR) {
                this.state = ExecutionState.ERROR;
                break;
            } else if (state == ExecutionState.SUCCESS) {
                this.state = ExecutionState.SUCCESS;
                break;
            } else {
                this.state = ExecutionState.FAIL;
            }
        }

        if (this.state == ExecutionState.NOT_EXECUTED) {
            this.state = ExecutionState.FAIL;
        }

        return this.state;
    }

    @Override
    public ExecutionState getExecutionState() {
        return state;
    }

    @Override
    public IBehaviorExtension clone() {
        Dependency clone = new Dependency(reference);
        clone.setValues(getValues());
        clone.setContainingBehaviorRuleSet(behaviorSet);
        return clone;
    }

    private List<BehaviorRule> cloneBehaviorRules(List<BehaviorRule> behaviorRules, String filter) throws CloneNotSupportedException {
        List<BehaviorRule> clone = new LinkedList<>();
        for (BehaviorRule behaviorRule : behaviorRules) {
            if (behaviorRule.getName().equals(filter)) {
                clone.add(behaviorRule.clone());
            }
        }

        return clone;
    }

    @Override
    public void setContainingBehaviorRuleSet(BehaviorSet behaviorSet) {
        this.behaviorSet = behaviorSet;
    }
}
