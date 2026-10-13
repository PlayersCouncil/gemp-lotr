package com.gempukku.lotro.game.state;

import com.gempukku.lotro.framework.TestConstants;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.timing.GameStats;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Tests of what EventSerializer writes for the client: the server's protocol, and no behaviour of any card.
 *
 * The two here are about the "handRevealed" attribute on a player's playerZones element of a game stats event,
 * which is how every viewer's client knows to show that player's hand (#1095).  They were written in the test
 * file of Race Text 92_7 ("You must play with your hand revealed."), the one card that reveals a hand, and were
 * moved here as they stood when that file was brought onto the test rig (G-0037).  What the card does is tested
 * there, through the rig; these read the XML, which the rig does not.
 */
public class EventSerializerTests implements TestConstants
{
	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");       // Gimli, Son of Gloin
					put("legolas", "1_50");     // Legolas, Greenleaf
					put("runner", "1_178");     // Goblin Runner
					put("enquea", "1_231");     // Ulaire Enquea, Lieutenant of Morgul
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_7"
		);
	}

	// The same table with no modifier: the control.
	protected VirtualTableScenario GetNoModScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");       // Gimli, Son of Gloin
					put("legolas", "1_50");     // Legolas, Greenleaf
					put("runner", "1_178");     // Goblin Runner
					put("enquea", "1_231");     // Ulaire Enquea, Lieutenant of Morgul
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RevealedHandFlagIsSerializedOnlyOnTheOwnersPlayerZones() throws Exception {
		var scn = GetShadowScenario();
		scn.StartGame();

		var stats = new GameStats();
		stats.updateGameStats(scn.game());

		Element ge = serialize(new GameEvent(GameEvent.Type.GAME_STATS).gameStats(stats));
		NodeList zones = ge.getElementsByTagName("playerZones");
		assertEquals(2, zones.getLength());
		for (int i = 0; i < zones.getLength(); i++) {
			var zone = (Element) zones.item(i);
			if (zone.getAttribute("name").equals(P2))
				assertEquals("true", zone.getAttribute("handRevealed"));
			else
				assertFalse(zone.hasAttribute("handRevealed"));
		}
	}

	@Test
	public void NoHandRevealedFlagIsSerializedWithout92_7() throws Exception {
		var scn = GetNoModScenario();
		scn.StartGame();

		var stats = new GameStats();
		stats.updateGameStats(scn.game());
		assertTrue(stats.getRevealedHands().isEmpty());

		Element ge = serialize(new GameEvent(GameEvent.Type.GAME_STATS).gameStats(stats));
		NodeList zones = ge.getElementsByTagName("playerZones");
		for (int i = 0; i < zones.getLength(); i++)
			assertFalse(((Element) zones.item(i)).hasAttribute("handRevealed"));
	}

	private static Element serialize(GameEvent event) throws Exception {
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		return (Element) new EventSerializer().serializeEvent(doc, event);
	}
}
