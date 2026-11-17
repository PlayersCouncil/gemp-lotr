package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_072_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("assassin", "17_72");
					put("denizen", "11_115");   // Denizen of Khazad-dûm: [orc] Orc, strength 9; +2 only at an underground site
					put("halftroll", "14_12");  // Half-troll of Far Harad: an [orc] minion who is no Orc, strength 8
					put("brute", "10_90");      // Mordor Brute: a [sauron] Orc, strength 9; a wound bar for Orcs bearing possessions
					put("army", "18_47");       // Elendil's Army: [gondor] follower, strength +2; Aid: exert a [gondor] companion
					put("worker", "15_135");    // Rohan Worker: [rohan] follower with no printed bonus; its texts answer a transfer and a win
					put("saruman", "17_38");    // Saruman, Servant of Sauron: a Shadow follower, borne by a minion, strength +2
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion; a maneuver ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrkishAssassinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Orkish Assassin
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: While you can spot a follower attached to a companion, each [orc] minion is strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("assassin");

		assertEquals("Orkish Assassin", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void OnceAFollowerIsTransferredToACompanionEachOrcMinionIsStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var assassin = scn.GetShadowCard("assassin");
		var denizen = scn.GetShadowCard("denizen");
		var halftroll = scn.GetShadowCard("halftroll");
		var brute = scn.GetShadowCard("brute");
		var army = scn.GetFreepsCard("army");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(army);
		assertInZone(Zone.SUPPORT, army);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin, denizen, halftroll, brute);

		// The follower lies in the support area, attached to nobody: nothing changes.
		assertEquals(8, scn.GetStrength(assassin));
		assertEquals(9, scn.GetStrength(denizen));
		assertEquals(8, scn.GetStrength(halftroll));
		assertEquals(9, scn.GetStrength(brute));

		scn.SkipToPhase(Phase.MANEUVER);
		// The Aid: Aragorn, the one [gondor] companion, exerts, and the Army goes to Frodo.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(army));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(frodo);
		assertAttachedTo(army, frodo);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		assertEquals(10, scn.GetStrength(assassin));
		assertEquals(11, scn.GetStrength(denizen));
		// An [orc] minion who is no Orc.
		assertEquals(10, scn.GetStrength(halftroll));
		// A [sauron] Orc.
		assertEquals(9, scn.GetStrength(brute));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AShadowFollowerOnAMinionIsNoFollowerOnACompanionButARohanFollowerOnFrodoIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var assassin = scn.GetShadowCard("assassin");
		var denizen = scn.GetShadowCard("denizen");
		var halftroll = scn.GetShadowCard("halftroll");
		var brute = scn.GetShadowCard("brute");
		var saruman = scn.GetShadowCard("saruman");
		var worker = scn.GetFreepsCard("worker");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(worker);
		assertInZone(Zone.SUPPORT, worker);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin, denizen, halftroll, brute);
		// Saruman borne by the [sauron] Brute: 9 + 2.
		scn.AttachCardsTo(brute, saruman);
		assertAttachedTo(saruman, brute);

		assertEquals(8, scn.GetStrength(assassin));
		assertEquals(9, scn.GetStrength(denizen));
		assertEquals(8, scn.GetStrength(halftroll));
		assertEquals(11, scn.GetStrength(brute));

		// A follower of another culture, borne by the Ring-bearer.
		scn.AttachCardsTo(frodo, worker);
		assertAttachedTo(worker, frodo);

		assertEquals(10, scn.GetStrength(assassin));
		assertEquals(11, scn.GetStrength(denizen));
		assertEquals(10, scn.GetStrength(halftroll));
		assertEquals(11, scn.GetStrength(brute));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
