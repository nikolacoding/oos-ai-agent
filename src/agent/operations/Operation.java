package agent.operations;

import java.util.*;

public final class Operation {
    private final String type;
    private final List<String> args = new ArrayList<>();

    public Operation(String operationString) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < operationString.length(); i++) {
            char c = operationString.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
                continue;
            }

            if (Character.isWhitespace(c) && !inQuotes) {
                if (!current.isEmpty()) {
                    parts.add(current.toString());
                    current.setLength(0);
                }
                continue;
            }

            current.append(c);
        }

        if (!current.isEmpty()) {
            parts.add(current.toString());
        }

        this.type = parts.getFirst();
        for (int i = 1; i < parts.size(); i++) {
            this.args.add(parts.get(i));
        }
    }

    public String getType() { return type; }
    public List<String> getArgs() { return args; }
}
