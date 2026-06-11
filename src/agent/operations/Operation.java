package agent.operations;

import java.util.*;

public final class Operation {
    private final String type;
    private final List<String> args = new ArrayList<>();

    public Operation(String operationString) {
        String[] parts = operationString.split(" ");

        this.type = parts[0];
        for (int i = 1; i < parts.length; i++)
            this.args.add(parts[i]);
    }

    public String getType() { return type; }
    public List<String> getArgs() { return args; }
}
