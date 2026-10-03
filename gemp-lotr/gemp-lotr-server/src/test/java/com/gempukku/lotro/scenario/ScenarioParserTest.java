package com.gempukku.lotro.scenario;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.logic.decisions.AwaitingDecisionType;
import com.gempukku.lotro.scenario.ScenarioDefinition.*;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ScenarioParserTest {
    private static void assertRejected(String rest, String messageFragment) {
        try {
            ScenarioTesting.parse(rest);
            fail("Expected the scenario to be rejected with a message containing: " + messageFragment);
        } catch (ScenarioFormatException exp) {
            assertTrue("Expected a message containing '" + messageFragment + "' but got: " + exp.getMessage(),
                    exp.getMessage().contains(messageFragment));
        }
    }

    @Test
    public void headerDefaults() throws Exception {
        ScenarioDefinition def = ScenarioTesting.parse("");
        assertEquals("test", def.id);
        assertEquals(Kind.TUTORIAL, def.kind);
        assertEquals(Seat.FREEPS, def.human);
        assertEquals(Coverage.STRICT, def.coverage);
        assertEquals(Fallback.FAIL, def.fallback);
        assertEquals(RngDefault.IDENTITY, def.rngDefault);
        assertTrue(def.autoPass.isEmpty());
        assertEquals("Tutorial", def.opponentDisplayName());
        assertFalse(def.oneSided);
    }

    @Test
    public void aMessageCanPointAtSeveralThingsAndSayHowFarOffItSits() throws Exception {
        String steps = """
                	options: {
                		bubbleGap: 3
                	}
                	steps: [
                		{
                			when: freeps, action
                			messages: [
                				{
                					text: These two fight side by side.
                					anchor: card:aragorn, card:boromir
                					side: left
                					gap: 2.5
                				}
                				{
                					text: Close by.
                					anchor: ui:twilightPool
                					gap: 24PX
                				}
                				Nothing in particular.
                			]
                		}
                	]
                """;
        ScenarioDefinition def = ScenarioTesting.parse(steps);
        assertEquals("3", def.bubbleGap);
        List<Message> messages = def.steps.getFirst().messages;
        assertEquals(List.of("card:aragorn", "card:boromir"), messages.get(0).anchor);
        assertEquals("2.5", messages.get(0).gap);
        assertEquals("24px", messages.get(1).gap);
        assertNull(messages.get(2).gap);

        //Written out and read back, it says the same
        ScenarioDefinition again = ScenarioParser.parse(ScenarioWriter.toHjson(def, null));
        assertEquals(List.of("card:aragorn", "card:boromir"), again.steps.getFirst().messages.get(0).anchor);
        assertEquals("2.5", again.steps.getFirst().messages.get(0).gap);
        assertEquals("24px", again.steps.getFirst().messages.get(1).gap);
        assertEquals("3", again.bubbleGap);

        assertRejected(steps.replace("gap: 2.5", "gap: wide"), "is not a distance");
        assertRejected(steps.replace("gap: 2.5", "gap: 400"), "further than any screen");
        assertRejected(steps.replace("card:aragorn, card:boromir", "card:aragorn, card:legolas"), "there is no card called 'legolas'");
        assertRejected(steps.replace("bubbleGap: 3", "bubbleGap: lots"), "options.bubbleGap");
    }

    @Test
    public void cardsKeepTheOrderTheyWereDeclaredIn() throws Exception {
        ScenarioDefinition def = ScenarioTesting.parse("");
        assertEquals(List.of("frodo", "aragorn", "gandalf", "servant1", "servant2", "swordarm", "boromir",
                "oppFrodo", "runner1", "runner2", "bowman", "marksman"), List.copyOf(def.cards.keySet()));
        assertEquals(Seat.SHADOW, def.cards.get("bowman").owner());
        assertEquals("1_83", def.cards.get("servant2").blueprintId());
    }

    @Test
    public void whenCanBeWrittenAsTermsOrAsAnObject() throws Exception {
        ScenarioDefinition def = ScenarioTesting.parse("""
                	steps: [
                		{
                			id: terms
                			when: human, action, phase(fellowship), turn(2), skirmish(1), offers(aragorn), text(or Pass)
                		}
                		{
                			id: object
                			when: {
                				seat: opponent
                				decision: assign
                				phase: assignment
                			}
                		}
                	]
                """);
        When terms = def.steps.get(0).when;
        assertEquals(Seat.FREEPS, terms.seat);
        assertEquals(AwaitingDecisionType.CARD_ACTION_CHOICE, terms.decision);
        assertEquals(Phase.FELLOWSHIP, terms.phase);
        assertEquals(Integer.valueOf(2), terms.turn);
        assertEquals(Integer.valueOf(1), terms.skirmish);
        assertEquals("aragorn", terms.offers);
        assertEquals("or Pass", terms.text);

        When object = def.steps.get(1).when;
        assertEquals(Seat.SHADOW, object.seat);
        assertEquals(AwaitingDecisionType.ASSIGN_MINIONS, object.decision);
        assertEquals(Phase.ASSIGNMENT, object.phase);
    }

    @Test
    public void stepsAreNumberedAndOnlyNamedIfTheirAuthorWantsTo() throws Exception {
        ScenarioDefinition def = ScenarioTesting.parse("""
                	steps: [
                		{
                			when: freeps, action
                		}
                		{
                			id: the-big-moment
                			when: freeps, assign
                		}
                		{
                			allow: pass
                		}
                	]
                """);
        assertEquals(1, def.steps.get(0).number);
        assertNull(def.steps.get(0).id);
        assertEquals("1", def.steps.get(0).label());
        assertEquals("step 1 [when: freeps, action]", def.steps.get(0).describe());
        assertEquals("2 (the-big-moment)", def.steps.get(1).label());
        assertEquals("step 3", def.steps.get(2).describe());
        assertSame(def.steps.get(1), def.findStep("2 (the-big-moment)"));
        assertSame(def.steps.get(2), def.findStep("3"));
    }

    @Test
    public void answersAndAllowsShareOneVocabulary() throws Exception {
        ScenarioDefinition def = ScenarioTesting.parse("""
                	steps: [
                		{
                			id: many
                			until: phase(shadow)
                			allow: play(aragorn), play(gandalf), pass
                			answer: play(gandalf), use(aragorn, Exert), choose(servant1, servant2), option(No), option(#2), number(3), assign(frodo: runner1 + runner2, aragorn: bowman), pass
                		}
                	]
                """);
        Step step = def.steps.getFirst();
        assertEquals(3, step.allow.answers.size());
        assertEquals(AnswerKind.PASS, step.allow.answers.get(2).kind());

        List<Answer> answers = step.answers;
        assertEquals(AnswerKind.PLAY, answers.get(0).kind());
        assertEquals(List.of("gandalf"), answers.get(0).cards());
        assertEquals(AnswerKind.USE, answers.get(1).kind());
        assertEquals("Exert", answers.get(1).text());
        assertEquals(List.of("servant1", "servant2"), answers.get(2).cards());
        assertEquals("No", answers.get(3).text());
        assertEquals("#2", answers.get(4).text());
        assertEquals(3, answers.get(5).number());
        assertEquals(List.of("runner1", "runner2"), answers.get(6).assignments().get("frodo"));
        assertEquals(List.of("bowman"), answers.get(6).assignments().get("aragorn"));
        assertEquals(AnswerKind.PASS, answers.get(7).kind());
    }

    @Test
    public void setupAndDirectChanges() throws Exception {
        ScenarioDefinition def = ScenarioTesting.parse("""
                	setup: {
                		freeps: {
                			characters: aragorn
                			hand: gandalf, servant1
                			deck: boromir, swordarm
                		}
                		shadow: {
                			minions: runner1
                		}
                		wounds: {
                			aragorn: 2
                		}
                		burdens: 3
                	}
                	steps: [
                		{
                			id: change
                			do: move(servant2, freeps.deck.bottom), wound(frodo), twilight(+2), burdens(0)
                		}
                	]
                """);
        assertEquals(List.of("gandalf", "servant1"), def.setup.zones.get(Seat.FREEPS).get(SetupZone.HAND));
        assertEquals(List.of("boromir", "swordarm"), def.setup.zones.get(Seat.FREEPS).get(SetupZone.DECK));
        assertEquals(List.of("runner1"), def.setup.zones.get(Seat.SHADOW).get(SetupZone.MINIONS));
        assertEquals(Integer.valueOf(2), def.setup.wounds.get("aragorn"));
        assertEquals(3, def.setup.burdens);

        List<DoOp> ops = def.steps.getFirst().doOps;
        assertEquals(DoKind.MOVE, ops.get(0).kind());
        assertEquals("freeps.deck.bottom", ops.get(0).target());
        assertEquals(1, ops.get(1).amount());
        assertTrue(ops.get(2).relative());
        assertEquals(2, ops.get(2).amount());
        assertFalse(ops.get(3).relative());
    }

    @Test
    public void messagesCanBePlainTextOrDetailed() throws Exception {
        ScenarioDefinition def = ScenarioTesting.parse("""
                	steps: [
                		{
                			id: talk
                			messages: [
                				Just some text about {twilight}.
                				{
                					header: Look here
                					text: Aragorn has {strength:aragorn}.
                					anchor: card:aragorn
                					highlight: card:aragorn, ui:twilightPool
                					avoid: zone:self.hand
                					side: below
                					advance: next
                				}
                			]
                		}
                	]
                """);
        List<Message> messages = def.steps.getFirst().messages;
        assertEquals("Just some text about {twilight}.", messages.get(0).text);
        assertEquals(List.of(), messages.get(0).anchor);
        assertNull("A message has no heading unless it asks for one", messages.get(0).header);
        assertEquals("Look here", messages.get(1).header);
        assertEquals(List.of("card:aragorn"), messages.get(1).anchor);
        assertEquals(List.of("card:aragorn", "ui:twilightPool"), messages.get(1).highlight);
        assertEquals("below", messages.get(1).side);
        assertEquals(Advance.NEXT, messages.get(1).advance);
    }

    @Test
    public void aMessageCanShowACardLarge() throws Exception {
        ScenarioDefinition def = ScenarioTesting.parse("""
                	steps: [
                		{
                			when: freeps, action
                			messages: [
                				{
                					text: This is his strength, and under it his vitality.
                					zoom: card:aragorn
                					focus: strength, vitality
                				}
                				{
                					text: Read the second line.
                					zoom: site:2
                					focus: rect(12, 80, 60, 6)
                					magnify: 2.5
                				}
                				{
                					text: Any card at all.
                					zoom: blueprint:1_83
                				}
                				Now pass.
                			]
                		}
                	]
                """);
        List<Message> messages = def.steps.getFirst().messages;
        Zoom first = messages.get(0).zoom;
        assertEquals("aragorn", first.card());
        assertEquals(List.of("strength", "vitality"), first.focus().stream().map(Focus::name).toList());
        assertEquals(1.0, first.magnify(), 0);
        assertEquals("A card shown large is read and then put away", Advance.NEXT, messages.get(0).advance);

        Zoom second = messages.get(1).zoom;
        assertEquals(Integer.valueOf(2), second.site());
        assertArrayEquals(new double[] {12, 80, 60, 6}, second.focus().getFirst().rect(), 0);
        assertEquals(2.5, second.magnify(), 0);

        assertEquals("1_83", messages.get(2).zoom.blueprint());
        assertNull(messages.get(3).zoom);

        //What a region is depends on the kind of card, which only the card library knows
        assertTrue(ScenarioValidator.validate(def, ScenarioTesting.library()).isEmpty());
        ScenarioDefinition wrong = ScenarioTesting.parse("""
                	steps: [
                		{
                			messages: [
                				{
                					text: An event has no strength.
                					zoom: card:servant1
                					focus: strength, text
                				}
                				{
                					text: There are only four sites.
                					zoom: site:7
                				}
                			]
                		}
                	]
                """);
        List<String> problems = ScenarioValidator.validate(wrong, ScenarioTesting.library());
        assertEquals(problems.toString(), 2, problems.size());
        assertTrue(problems.get(0), problems.get(0).contains("'strength' is not something printed on Servant of the Secret Fire"));
        assertTrue(problems.get(1), problems.get(1).contains("the scenario's path has no site 7"));

        //Where each named region is comes from the kind of card
        var described = CardFaces.describe(def, first, ScenarioTesting.library());
        assertEquals("4_109", described.get("blueprint"));
        assertEquals(2, ((List<?>) described.get("focus")).size());
    }

    @Test
    public void aMessageCanWaitForAMomentInsideTheDecision() throws Exception {
        ScenarioDefinition def = ScenarioTesting.parse("""
                	steps: [
                		{
                			when: freeps, assign
                			messages: [
                				Click the Bowman.
                				{
                					text: Now click Frodo.
                					anchor: card:frodo
                					on: picked(bowman)
                				}
                				{
                					text: That pairs them up.
                					on: assigned(frodo)
                				}
                				{
                					text: Click Done.
                					anchor: ui:button:done
                					on: ready
                				}
                				{
                					text: This one says what the first one leaves out.
                					on: start
                				}
                			]
                			allow: assign(frodo: bowman)
                		}
                	]
                """);
        List<Message> messages = def.steps.getFirst().messages;
        assertNull("With no moment given, a message is shown as soon as the decision is", messages.get(0).on);
        assertEquals(new Moment(MomentKind.PICKED, "bowman"), messages.get(1).on);
        assertEquals(new Moment(MomentKind.ASSIGNED, "frodo"), messages.get(2).on);
        assertEquals(new Moment(MomentKind.READY, null), messages.get(3).on);
        assertNull("'start' is the same as no moment", messages.get(4).on);
    }

    @Test
    public void rulesAndRandomOutcomes() throws Exception {
        ScenarioDefinition def = ScenarioTesting.parse("""
                	human: shadow
                	rules: {
                		oneSided: true
                		human: [
                			{
                				type: modifier
                				modifier: {
                					type: modifyHandSize
                					amount: -4
                				}
                			}
                		]
                	}
                	rng: [
                		shuffle(freeps.deck: gandalf, aragorn)
                		pick(opponent.hand: servant1)
                	]
                """);
        assertTrue(def.oneSided);
        assertTrue("'human' is the shadow seat here", def.rules.containsKey(Seat.SHADOW));
        assertTrue(def.rules.get(Seat.SHADOW).contains("modifyHandSize"));
        assertEquals(RngKind.SHUFFLE, def.rng.get(0).kind());
        assertEquals(List.of("gandalf", "aragorn"), def.rng.get(0).result());
        assertEquals(RngKind.PICK, def.rng.get(1).kind());
        assertEquals("the opponent of a shadow-seat human sits in the freeps seat", Seat.FREEPS, def.rng.get(1).seat());
    }

    @Test
    public void mistakesAreReportedWhereTheyAre() {
        assertRejected("\tcolour: blue", "unrecognized field 'colour'");
        assertRejected("\tsteps: [\n\t\t{\n\t\t\tid: a\n\t\t\tanswer: play(legolas)\n\t\t}\n\t]", "there is no card called 'legolas'");
        assertRejected("\tsteps: [\n\t\t{\n\t\t\tid: a\n\t\t\twhen: freeps, sometime\n\t\t}\n\t]", "don't know what 'sometime' means");
        assertRejected("\tsteps: [\n\t\t{\n\t\t\tid: a\n\t\t\tanswer: jump(aragorn)\n\t\t}\n\t]", "don't know what 'jump(aragorn)' means");
        assertRejected("\tsteps: [\n\t\t{\n\t\t\tid: a\n\t\t}\n\t\t{\n\t\t\tid: a\n\t\t}\n\t]", "two steps called 'a'");
        assertRejected("\tsteps: [\n\t\t{\n\t\t\tanswer: pass, pass\n\t\t}\n\t]", "step 1: has several answers but answers only one decision");
        assertRejected("\tsteps: [\n\t\t{\n\t\t\tid: 7th\n\t\t}\n\t]", "is not usable as a step's name");
        assertRejected("\tsteps: [\n\t\t{\n\t\t\tid: a\n\t\t\tmessages: [\n\t\t\t\tStrength is {might:aragorn}.\n\t\t\t]\n\t\t}\n\t]", "don't know how to fill in {might:aragorn}");
        assertRejected("\tsteps: [\n\t\t{\n\t\t\tid: a\n\t\t\tmessages: [\n\t\t\t\t{\n\t\t\t\t\ttext: Hello\n\t\t\t\t\tanchor: card:legolas\n\t\t\t\t}\n\t\t\t]\n\t\t}\n\t]", "there is no card called 'legolas'");
        assertRejected("\tsteps: [\n\t\t{\n\t\t\tid: a\n\t\t\tmessages: [\n\t\t\t\t{\n\t\t\t\t\ttext: Hello\n\t\t\t\t\tanchor: ui:minimap\n\t\t\t\t}\n\t\t\t]\n\t\t}\n\t]", "is not an anchor");
        assertRejected("\tsteps: [\n\t\t{\n\t\t\tmessages: [\n\t\t\t\t{\n\t\t\t\t\ttext: Hello\n\t\t\t\t\ton: clicked(bowman)\n\t\t\t\t}\n\t\t\t]\n\t\t}\n\t]", "don't know what 'clicked(bowman)' means");
        assertRejected("\tsteps: [\n\t\t{\n\t\t\tmessages: [\n\t\t\t\t{\n\t\t\t\t\ttext: Hello\n\t\t\t\t\ton: picked(legolas)\n\t\t\t\t}\n\t\t\t]\n\t\t}\n\t]", "there is no card called 'legolas'");
        assertRejected("\tsteps: [\n\t\t{\n\t\t\tmessages: [\n\t\t\t\t{\n\t\t\t\t\ttext: Hello\n\t\t\t\t\thighlightStyle: purple\n\t\t\t\t}\n\t\t\t]\n\t\t}\n\t]", "highlightStyle: has to be one of gold, red, green, blue");
        assertRejected("\tsteps: [\n\t\t{\n\t\t\tmessages: [\n\t\t\t\t{\n\t\t\t\t\ttitle: Hello\n\t\t\t\t\ttext: Hello\n\t\t\t\t}\n\t\t\t]\n\t\t}\n\t]", "a message's heading is called 'header'");
        assertRejected("\tsteps: [\n\t\t{\n\t\t\tmessages: [\n\t\t\t\t{\n\t\t\t\t\ttext: Hello\n\t\t\t\t\tfocus: strength\n\t\t\t\t}\n\t\t\t]\n\t\t}\n\t]", "'focus' and 'magnify' belong to a 'zoom'");
        assertRejected("\tsteps: [\n\t\t{\n\t\t\tmessages: [\n\t\t\t\t{\n\t\t\t\t\ttext: Hello\n\t\t\t\t\tzoom: card:legolas\n\t\t\t\t}\n\t\t\t]\n\t\t}\n\t]", "there is no card called 'legolas'");
        assertRejected("\tsteps: [\n\t\t{\n\t\t\tmessages: [\n\t\t\t\t{\n\t\t\t\t\ttext: Hello\n\t\t\t\t\tzoom: aragorn\n\t\t\t\t}\n\t\t\t]\n\t\t}\n\t]", "has to be card:<alias>, site:<number> or blueprint:<id>");
        assertRejected("\tsteps: [\n\t\t{\n\t\t\tmessages: [\n\t\t\t\t{\n\t\t\t\t\ttext: Hello\n\t\t\t\t\tzoom: card:aragorn\n\t\t\t\t\tfocus: nose\n\t\t\t\t}\n\t\t\t]\n\t\t}\n\t]", "don't know what 'nose' means");
        assertRejected("\tsteps: [\n\t\t{\n\t\t\tmessages: [\n\t\t\t\t{\n\t\t\t\t\ttext: Hello\n\t\t\t\t\tzoom: card:aragorn\n\t\t\t\t\tfocus: rect(90, 10, 20, 5)\n\t\t\t\t}\n\t\t\t]\n\t\t}\n\t]", "does not lie on the card");
        assertRejected("\tsteps: [\n\t\t{\n\t\t\tmessages: [\n\t\t\t\t{\n\t\t\t\t\ttext: Hello\n\t\t\t\t\tzoom: card:aragorn\n\t\t\t\t\tmagnify: 3\n\t\t\t\t}\n\t\t\t]\n\t\t}\n\t]", "needs a 'focus' to be centred on");
        assertRejected("\tsteps: [\n\t\t{\n\t\t\tmessages: [\n\t\t\t\t{\n\t\t\t\t\ttext: Hello\n\t\t\t\t\tzoom: card:aragorn\n\t\t\t\t\ton: ready\n\t\t\t\t}\n\t\t\t]\n\t\t}\n\t]", "cannot also wait for a moment");
        assertRejected("\topponent: {\n\t\tname: \"<b>Sauron</b>\"\n\t}", "opponent.name: has to be 1 to 30 letters");
        assertRejected("\tsetup: {\n\t\tfreeps: {\n\t\t\thand: runner1\n\t\t}\n\t}", "belongs to the other seat");
        assertRejected("\tsetup: {\n\t\tfreeps: {\n\t\t\thand: aragorn\n\t\t\tdeck: aragorn\n\t\t}\n\t}", "placed in more than one zone");
        assertRejected("\tring: {\n\t\tfreeps: runner1\n\t}", "belongs to the other seat");
    }

    @Test
    public void theUsualHjsonTrapsGetAHint() {
        //An unquoted value runs to the end of its line, so an object cannot be closed on the line it was opened on
        try {
            ScenarioParser.parse("{\n\tid: x\n\topponent: { name: Sauron, fallback: pass }\n}");
            fail("Expected the scenario to be rejected");
        } catch (ScenarioFormatException exp) {
            assertTrue(exp.getMessage(), exp.getMessage().contains("opened and closed on one line"));
        }
    }

    @Test
    public void everySeatNeedsARingBearerAndASite() {
        try {
            ScenarioParser.parse("""
                    {
                    	id: x
                    	title: X
                    	format: scenario
                    	cards: {
                    		freeps: {
                    			frodo: 10_121
                    		}
                    	}
                    	ringBearer: {
                    		freeps: frodo
                    	}
                    	sites: {
                    		freeps: 1_323
                    	}
                    }
                    """);
            fail("Expected the scenario to be rejected");
        } catch (ScenarioFormatException exp) {
            assertTrue(exp.getMessage(), exp.getMessage().contains("the shadow seat needs a Ring-bearer"));
        }
    }

    @Test
    public void aStepCanBeAPauseAndAPauseCanEndInAChoice() throws Exception {
        ScenarioDefinition def = ScenarioTesting.parse("""
                	steps: [
                		{
                			pause: true
                			when: action, phase(fellowship)
                			messages: [
                				Think about it.
                				{
                					text: Well?
                					choices: {
                						plan: I have a plan
                						help: Help me
                					}
                				}
                			]
                		}
                		{
                			if: help
                			when: freeps, action
                			allow: pass
                		}
                		{
                			unless: help
                			when: freeps, action
                		}
                	]
                """);
        Step pause = def.steps.get(0);
        assertTrue(pause.pause);
        assertEquals("A pause is over one of the player's own decisions", Seat.FREEPS, pause.when.seat);
        assertEquals(List.of("plan", "help"), List.copyOf(pause.messages.get(1).choices.keySet()));
        assertEquals("Help me", pause.messages.get(1).choices.get("help"));
        assertEquals(List.of("plan", "help"), def.flags);
        assertEquals(List.of("help"), def.steps.get(1).onlyIf);
        assertEquals(List.of("help"), def.steps.get(2).unless);

        rejects("a pause is read, not answered", """
                	steps: [
                		{
                			pause: true
                			messages: Read me.
                			allow: pass
                		}
                	]
                """);
        rejects("only a step with 'pause: true' can offer a choice", """
                	steps: [
                		{
                			messages: [
                				{
                					text: Well?
                					choices: {
                						a: A
                						b: B
                					}
                				}
                			]
                		}
                	]
                """);
        rejects("no earlier step offers a choice called 'help'", """
                	steps: [
                		{
                			if: help
                			when: freeps, action
                		}
                	]
                """);
        rejects("an opponent's step already waits", """
                	steps: [
                		{
                			pause: true
                			when: opponent, action
                			messages: Read me.
                		}
                	]
                """);
    }

    @Test
    public void aMessageCanWaitForACardToBeLookedAtAndPointAtPartOfOne() throws Exception {
        ScenarioDefinition def = ScenarioTesting.parse("""
                	steps: [
                		{
                			when: freeps, action
                			messages: [
                				{
                					text: Right-click on Aragorn.
                					anchor: card:aragorn
                					advance: inspect(aragorn)
                				}
                				{
                					text: Now close it.
                					anchor: ui:cardInfo
                					advance: close
                				}
                				{
                					text: This is his strength, and these are his wounds.
                					anchor: card:aragorn
                					highlight: card:aragorn:strength, card:aragorn:wounds
                				}
                			]
                			allow: play(servant1)?, pass
                		}
                	]
                """);
        List<Message> messages = def.steps.getFirst().messages;
        assertEquals(Advance.INSPECT, messages.get(0).advance);
        assertEquals("aragorn", messages.get(0).advanceCard);
        assertEquals(Advance.CLOSE, messages.get(1).advance);
        assertNull(messages.get(2).advance);
        assertEquals("aragorn", Anchors.cardAlias("card:aragorn:strength"));
        assertEquals("strength", Anchors.printedPart("card:aragorn:strength"));
        assertNull("Wounds lie on a card; they are not printed on it", Anchors.printedPart("card:aragorn:wounds"));
        assertTrue(def.steps.getFirst().allow.answers.get(0).ifOffered());
        assertFalse(def.steps.getFirst().allow.answers.get(1).ifOffered());
        assertTrue(ScenarioValidator.validate(def, ScenarioTesting.library()).isEmpty());
        assertArrayEquals(CardFaces.region(CardFaces.Frame.CHARACTER, "strength"),
                CardFaces.region(def, "aragorn", "strength", ScenarioTesting.library()), 0);

        rejects("'elbow' is not a part of a card", """
                	steps: [
                		{
                			messages: [
                				{
                					text: Look.
                					highlight: card:aragorn:elbow
                				}
                			]
                		}
                	]
                """);
        //What is printed on a card depends on the kind of card, which only the card library knows
        ScenarioDefinition wrong = ScenarioTesting.parse("""
                	steps: [
                		{
                			messages: [
                				{
                					text: An event has no vitality.
                					highlight: card:servant1:vitality
                				}
                			]
                		}
                	]
                """);
        List<String> problems = ScenarioValidator.validate(wrong, ScenarioTesting.library());
        assertEquals(problems.toString(), 1, problems.size());
        assertTrue(problems.getFirst(), problems.getFirst().contains("'vitality' is not something printed on Servant of the Secret Fire"));
    }

    /** The same as assertRejected, with the reason first: easier to read when the scenario runs to several lines. */
    private static void rejects(String messageFragment, String rest) {
        assertRejected(rest, messageFragment);
    }
}
