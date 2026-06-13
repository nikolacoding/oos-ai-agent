package agent.operations;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.*;

class OperationsParserTest {

    @Test
    void parseOperations() {
        var ops = OperationsParser.parseOperations(new File("test/vfs/test_operations/ops.txt"));
        assertEquals(2, ops.size());

        var op1 = ops.poll();
        assertNotNull(op1);
        assertEquals("think", op1.getType().toLowerCase());
        assertEquals(1, op1.getArgs().size());
        assertEquals("5",  op1.getArgs().getFirst());

        var op2 = ops.poll();
        var op2args = op2.getArgs();
        assertNotNull(op2);
        assertEquals("open",  op2.getType().toLowerCase());
        assertEquals("/macka/pas.txt", op2args.getFirst().toLowerCase());
        assertEquals("append", op2args.get(1).toLowerCase());
        assertEquals("as", op2args.get(2).toLowerCase());
        assertEquals("mp", op2args.get(3).toLowerCase());
    }

    @Test
    void generateNRandomOperations() {
        var ops = OperationsParser.generateNRandomOperations(10);
        assertEquals(10, ops.size());
    }
}