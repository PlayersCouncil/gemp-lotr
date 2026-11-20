package com.gempukku.lotro.scenario;

import org.junit.Test;

import static org.junit.Assert.*;

/** What message text can quote from the game (see Templates). */
public class ScenarioTemplatesTest {
    @Test
    public void handsResistanceTokensAndWhatACardCostsToPlay() throws Exception {
        ScenarioTable table = ScenarioTesting.table("""
                	setup: {
                		freeps: {
                			characters: aragorn
                			hand: gandalf, servant1, swordarm
                		}
                		shadow: {
                			minions: runner1
                			hand: bowman, marksman
                		}
                		tokens: {
                			runner1: 2
                		}
                	}
                """);
        assertEquals("3 in hand, the other 2", Templates.render(table, "{hand} in hand, the other {hand:opponent}"));
        assertEquals("3 2 3 2", Templates.render(table, "{hand:freeps} {hand:shadow} {hand:human} {hand:opponent}"));
        assertEquals(String.valueOf(table.GetResistance(table.Card("frodo"))), Templates.render(table, "{resistance:frodo}"));
        assertEquals("2 0", Templates.render(table, "{tokens:runner1} {tokens:aragorn}"));
        assertEquals("{threats} was there already", "0", Templates.render(table, "{threats}"));

        //The Goblin Bowman (twilight 2, site 4) roams at site 1: playing him costs the roaming penalty as well
        assertEquals("2", Templates.render(table, "{cost:bowman}"));
        assertEquals("4", Templates.render(table, "{playCost:bowman}"));
        //...which only a roaming minion has
        assertEquals(Templates.render(table, "{cost:gandalf}"), Templates.render(table, "{playCost:gandalf}"));
    }

    @Test
    public void whatCannotBeFilledInIsAMistakeInTheFile() {
        for (String[] wrong : new String[][] {
                {"{hand:nobody}", "a hand is a seat's"},
                {"{tokens:nobody}", "there is no card called 'nobody'"},
                {"{handsize}", "don't know how to fill in {handsize}"},
        }) {
            try {
                ScenarioTesting.parse("""
                        	steps: [
                        		{
                        			when: freeps, action
                        			messages: [
                        				"Say TEXT"
                        			]
                        			allow: pass
                        		}
                        	]
                        """.replace("TEXT", wrong[0]));
                fail(wrong[0]);
            } catch (ScenarioFormatException exp) {
                assertTrue(exp.getMessage(), exp.getMessage().startsWith("step 1.messages[1].text: ") && exp.getMessage().contains(wrong[1]));
            }
        }
    }

    @Test
    public void everyNameOfferedCanBeFilledIn() throws Exception {
        ScenarioDefinition def = ScenarioTesting.parse("");
        for (String total : Templates.names().get("totals"))
            Templates.check(def, "{" + total + "}", "test");
        for (String part : Templates.names().get("perCard"))
            Templates.check(def, "{" + part + ":frodo}", "test");
        for (String part : Templates.names().get("perSeat"))
            Templates.check(def, "{" + part + ":shadow}", "test");
    }
}
