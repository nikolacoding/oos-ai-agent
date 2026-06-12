import agent.Agent;
import agent.Slots;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import utility.Utility;
import vfs.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Simulation extends Thread {

    private final String preset;
    private final String settingsPath;

    private Slots slots;
    private int maxRunningAgents;
    private final List<VFS> mounts = new ArrayList<>();
    private final List<Agent> agents = new ArrayList<>();

    public Simulation(String settingsPath, String preset) {
        this.preset = preset;
        this.settingsPath = settingsPath;
    }

    @Override
    public void run() {
        this.parseSettings();
        System.out.println("== Simulacija je pocela ==");
        for (int i = 0; i < maxRunningAgents; i++) {
            Agent agent = new Agent(Utility.random.nextInt(2) * i, Utility.random.nextInt(5), this.slots, this.preset);
            agents.add(agent);
        }

        agents.forEach(Agent::start);
    }

    private void parseSettings() {
        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode root = mapper.readTree(new File(this.settingsPath));
            if (root == null) {
                throw new IllegalStateException(String.format("Navedeni settings fajl (%s) je prazan.", this.settingsPath));
            }

            JsonNode settingsNode = root.path("settings");
            JsonNode maxRunningAgentsNode = settingsNode.path("max_running_agents");
            if (settingsNode.isMissingNode() || !settingsNode.isObject() || maxRunningAgentsNode.isMissingNode() || !maxRunningAgentsNode.canConvertToInt()) {
                throw new IllegalStateException(String.format("Navedeni settings fajl (%s) nema validan settings.max_running_agents zapis.", this.settingsPath));
            }

            this.maxRunningAgents = maxRunningAgentsNode.asInt();
            this.slots = new Slots(this.maxRunningAgents);

            JsonNode vfsNode = root.path("vfs");
            JsonNode mountsNode = vfsNode.path("mounts");
            if (vfsNode.isMissingNode() || !vfsNode.isObject() || mountsNode.isMissingNode() || !mountsNode.isArray()) {
                throw new IllegalStateException(String.format("Navedeni settings fajl (%s) nema validan vfs.mounts zapis.", this.settingsPath));
            }

            this.mounts.clear();
            for (JsonNode mountNode : mountsNode) {
                JsonNode pathNode = mountNode.path("path");
                JsonNode modeNode = mountNode.path("mode");

                if (pathNode.isMissingNode() || modeNode.isMissingNode() || !pathNode.isTextual() || !modeNode.isTextual()) {
                    throw new IllegalStateException(String.format("Navedeni settings fajl (%s) sadrzi neispravan mount zapis.", this.settingsPath));
                }

                var newMount = new VFS(pathNode.asText(), modeNode.asText());
                this.mounts.add(newMount);
            }
        } catch (IOException e) {
            throw new IllegalStateException(String.format("Navedeni settings fajl (%s) se ne moze procitati.", this.settingsPath), e);
        }
    }
}
