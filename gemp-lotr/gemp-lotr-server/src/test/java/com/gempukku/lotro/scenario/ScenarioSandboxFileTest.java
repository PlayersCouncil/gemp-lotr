package com.gempukku.lotro.scenario;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroFormat;
import com.gempukku.lotro.game.LotroGameMediator;
import com.gempukku.lotro.game.LotroServer;
import com.gempukku.lotro.game.Player;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * A sandbox in the file format: kind: sandbox with no steps, options.tools, and the default sandbox as it ships.  Each
 * is read, written back as it was read, and set through the authoring tool.
 */
public class ScenarioSandboxFileTest {
    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private static final String AUTHOR = "writer";
    private final Player player = new Player(1, AUTHOR, "", "u", null, null, null, null, false);
    private File canon;
    private ScenarioLibrary library;
    private ScenarioAuthoring authoring;

    @Before
    public void setUp() throws Exception {
        File users = temp.newFolder("scenarios-user");
        canon = new File(users, "public");
        canon.mkdirs();
        library = new ScenarioLibrary(temp.newFolder("shipped"), users, ScenarioTesting.library(), ScenarioSandboxFileTest::format, null, 0, System::currentTimeMillis);
        authoring = new ScenarioAuthoring(library, ScenarioTesting.library(), ScenarioSandboxFileTest::format, null, (who, live) -> {
            LotroGameMediator mediator = new LotroGameMediator("1", live, LotroServer.AUTHORING_TIMER, false);
            mediator.startGame();
            return mediator;
        });
    }

    private static LotroFormat format(String code) {
        try {
            return ScenarioTesting.format(code);
        } catch (CardNotFoundException exp) {
            return null;
        }
    }

    private static String sandbox(String options) {
        return ScenarioTesting.scenario("\tkind: sandbox\n" + options);
    }

    @Test
    public void ToolsAreReadAsAListInTheirOwnOrder() throws Exception {
        ScenarioDefinition def = ScenarioParser.parse(sandbox("\toptions: {\n\t\ttools: counters, add, Move, add\n\t}"));
        assertEquals(List.of("add", "move", "counters"), def.tools);
        assertEquals(List.of("add", "move", "counters"), def.toolsInForce());
        assertEquals("sandbox", def.level());
        assertEquals(ScenarioDefinition.Coverage.GUIDED, def.coverageInForce());
        assertEquals(ScenarioDefinition.Fallback.PASS, ScenarioParser.parse(sandbox("")).fallbackInForce());

        assertEquals(List.of("add", "piles"), ScenarioParser.parse(sandbox("\toptions: {\n\t\ttools: [ \"piles\", \"add\" ]\n\t}")).tools);
        assertEquals(List.of(), ScenarioParser.parse(sandbox("\toptions: {\n\t\ttools: false\n\t}")).tools);
        //Not said: every tool
        ScenarioDefinition all = ScenarioParser.parse(sandbox(""));
        assertNull(all.tools);
        assertEquals(ScenarioSandbox.TOOLS, all.toolsInForce());
        //A tutorial is none of it
        ScenarioDefinition tutorial = ScenarioTesting.parse("");
        assertEquals("none", tutorial.level());
        assertEquals(ScenarioDefinition.Coverage.STRICT, tutorial.coverageInForce());

        try {
            ScenarioParser.parse(sandbox("\toptions: {\n\t\ttools: add, paint\n\t}"));
            fail("A tool that is not one was taken");
        } catch (ScenarioFormatException exp) {
            assertTrue(exp.getMessage(), exp.getMessage().startsWith("options.tools: 'paint' is not a tool"));
            assertTrue(exp.getMessage(), exp.getMessage().contains("add, copy, delete"));
        }
    }

    @Test
    public void ToolsAreWrittenBackAsTheyWereRead() throws Exception {
        for (String said : List.of("tools: add, move, counters", "tools: false")) {
            ScenarioDefinition def = ScenarioParser.parse(sandbox("\toptions: {\n\t\t" + said + "\n\t}"));
            String written = ScenarioWriter.toHjson(def, ScenarioTesting.library());
            assertTrue(written, written.contains(said));
            assertTrue(written, written.contains("kind: sandbox"));
            ScenarioDefinition again = ScenarioParser.parse(written);
            assertEquals(def.tools, again.tools);
            assertEquals(written, ScenarioWriter.toHjson(again, ScenarioTesting.library()));
        }
        //Not said, not written
        assertFalse(ScenarioWriter.toHjson(ScenarioParser.parse(sandbox("")), ScenarioTesting.library()).contains("tools"));
    }

    @Test
    public void TheDefaultSandboxIsInTheWritersForm() throws Exception {
        String shipped = Files.readString(new File("../gemp-lotr-cards/src/main/resources/scenarios/sandboxes/sandbox.hjson").toPath(), StandardCharsets.UTF_8);
        ScenarioDefinition def = ScenarioParser.parse(shipped);
        assertEquals(shipped.strip(), ScenarioWriter.toHjson(def, ScenarioTesting.library()).strip());
    }

    @Test
    public void AnAuthorMakesASandboxAndChoosesItsTools() throws Exception {
        Files.createDirectories(new File(canon, "tutorials").toPath());
        Files.writeString(new File(canon, "tutorials/blank.hjson").toPath(), ScenarioTesting.scenario("").replace("id: test", "id: blank"),
                StandardCharsets.UTF_8);
        authoring.open(player, "blank", true);

        JSONObject state = JSON.parseObject(JSON.toJSONString(authoring.state(player)));
        assertEquals(List.of("tutorial", "puzzle", "sandbox"), state.getList("kinds", String.class));
        JSONObject tools = state.getJSONObject("sandboxTools");
        assertEquals(ScenarioSandbox.TOOLS, tools.getList("names", String.class));
        assertEquals(List.of(), tools.getList("notBuilt", String.class));
        assertFalse(tools.getBooleanValue("said"));

        Map<String, Object> result = authoring.edit(player, "{top: {kind: \"sandbox\", options: {tools: \"add, counters\"}}}", null);
        state = JSON.parseObject(JSON.toJSONString(result)).getJSONObject("state");
        assertEquals("sandbox", state.getJSONObject("draft").getString("kind"));
        assertEquals("add, counters", state.getJSONObject("draft").getJSONObject("options").getString("tools"));
        assertTrue(state.getJSONObject("sandboxTools").getBooleanValue("said"));
        assertEquals(List.of("add", "counters"), state.getJSONObject("sandboxTools").getList("allowed", String.class));

        //Saved (under sandboxes/, its kind's folder) and opened again, it is the same
        authoring.save(player, null, false, true);
        assertTrue(new File(canon, "sandboxes/blank.hjson").isFile());
        authoring.close(player);
        authoring.open(player, "blank", true);
        ScenarioDefinition reopened = library.get("blank").def();
        assertEquals(ScenarioDefinition.Kind.SANDBOX, reopened.kind);
        assertEquals(List.of("add", "counters"), reopened.tools);
    }

    @Test
    public void AMistakenToolListIsLeftOutToOpenTheFile() throws Exception {
        ScenarioSalvage.Result read = ScenarioSalvage.read(sandbox("\toptions: {\n\t\ttools: add, paint\n\t\tautoAccept: false\n\t}"), def -> List.of());
        assertTrue(read.def() != null);
        assertNull(read.def().tools);
        assertFalse(read.def().autoAccept);
        assertEquals(1, read.leftOut().size());
        assertEquals("options.tools", read.leftOut().getFirst().where());
    }
}
