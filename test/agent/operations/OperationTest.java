package agent.operations;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OperationTest {

    @Test
    void getType() {
        var op = new Operation("OPEN /dir/file.txt read as f");
        assertEquals("open", op.getType().toLowerCase());
    }

    @Test
    void getArgs() {
        var op = new Operation("OPEN /dir/file.txt read as f");
        List<String> expectedArgs = Arrays.asList("/dir/file.txt", "read", "as", "f");

        for (int i = 0; i < expectedArgs.size(); i++)
            assertEquals(expectedArgs.get(i), op.getArgs().get(i));
    }
}